/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.moesif.external.facebook.stetho.inspector.network;

import org.checkerframework.checker.lock.qual.GuardedBy;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * {@link InputStream} that caches the data as the data is read, and writes them to the given
 * {@link OutputStream}. This also guarantees that we will attempt to reach EOF on the
 * {@link InputStream} passing all data to the {@link OutputStream}.
 * This is done to allow us to guarantee all responses are represented in the webkit inspector.
 */
// @VisibleForTest
public final class ResponseHandlingInputStream extends FilterInputStream {

    private static final Logger logger = LoggerFactory.getLogger(ResponseHandlingInputStream.class);

    private static final int BUFFER_SIZE = 1024;

    private final String mRequestId;
    private final OutputStream mOutputStream;
    @Nullable
    private final CountingOutputStream mDecompressedCounter;
    private final ResponseHandler mResponseHandler;

    /**
     * This stream will no longer be usable if {@link #close()} has been called on this stream.
     */
    @GuardedBy("this")
    private boolean mClosed;

    @GuardedBy("this")
    private boolean mEofSeen;

    @Nullable
    @GuardedBy("this")
    private byte[] mSkipBuffer;

    private long mLastDecompressedCount = 0;

    /**
     * @param inputStream
     * @param requestId           the requestId to use when we call the { NetworkEventReporter}
     * @param outputStream        stream to write to.
     * @param decompressedCounter Optional decompressing counting output stream which
     *                            can be queried after each write to determine the number of decompressed bytes
     *                            yielded.  Used to implement {@link ResponseHandler#onReadDecoded(int)}.
     *                            //@param networkPeerManager A peer manager which is used to log internal errors to the
     *                            Inspector console.
     * @param responseHandler     Special interface to intercept read events before they are sent
     *                            to peers via { NetworkEventReporter} methods.
     */
    public ResponseHandlingInputStream(
            InputStream inputStream,
            String requestId,
            OutputStream outputStream,
            @Nullable CountingOutputStream decompressedCounter,
            ResponseHandler responseHandler) {
        super(inputStream);
        mRequestId = requestId;
        mOutputStream = outputStream;
        mDecompressedCounter = decompressedCounter;
        mResponseHandler = responseHandler;
        mClosed = false;
    }

    private synchronized int checkEOF(int n) {
        if (n == -1) {
            closeOutputStreamQuietly();
            mResponseHandler.onEOF();
            mEofSeen = true;
        }
        return n;
    }

    @Override
    public int read() throws IOException {
        try {
            int result = checkEOF(in.read());
            if (result != -1) {
                mResponseHandler.onRead(1);
                writeToOutputStream(result);
            }
            return result;
        } catch (IOException ex) {
            throw handleIOException(ex);
        }
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        try {
            int result = checkEOF(in.read(b, off, len));
            if (result != -1) {
                mResponseHandler.onRead(result);
                writeToOutputStream(b, off, result);
            }
            return result;
        } catch (IOException ex) {
            throw handleIOException(ex);
        }
    }

    @Override
    public synchronized long skip(long n) throws IOException {
        byte[] buffer = getSkipBufferLocked();
        long total = 0;
        while (total < n) {
            long bytesDiff = n - total;
            int bytesToRead = (int) Math.min((long) buffer.length, bytesDiff);
            int result = this.read(buffer, 0, bytesToRead);
            if (result == -1) {
                break;
            }
            total += result;
        }
        return total;
    }

    @NonNull
    private byte[] getSkipBufferLocked() {
        if (mSkipBuffer == null) {
            mSkipBuffer = new byte[BUFFER_SIZE];
        }

        return mSkipBuffer;
    }

    @Override
    public boolean markSupported() {
        // this can be implemented, but isn't needed for TeedInputStream's behavior
        return false;
    }

    @Override
    public void mark(int readlimit) {
        // noop -- mark is not supported
    }

    @Override
    public void reset() throws IOException {
        throw new UnsupportedOperationException("Mark not supported");
    }

    @Override
    public void close() throws IOException {
        try { // Probably not needed for moesif
            long bytesRead = 0;
            if (!mEofSeen) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int count;
                while ((count = this.read(buffer)) != -1) {
                    bytesRead += count;
                }
            }
            //if (bytesRead > 0) {
            //    logger.warn("There were " + String.valueOf(bytesRead) + " bytes that were not consumed while "
            //            + "processing request " + mRequestId);
            //}
        } finally {
            super.close();
            closeOutputStreamQuietly();
        }
    }

    /**
     * Attempts to close all the output stream, and swallows any exceptions.
     */
    private synchronized void closeOutputStreamQuietly() {
        if (!mClosed) {
            try {
                mOutputStream.close();
                reportDecodedSizeIfApplicable();
            } catch (IOException e) {
                //logger.warn("Could not close the output stream" + e);
            } finally {
                mClosed = true;
            }
        }
    }

    /**
     * Handles reporting an {@link IOException}. We do this so we can centralize the logic while still
     * maintaining the ability of the catch clause to throw.
     *
     * @param ex
     * @return
     */
    private IOException handleIOException(IOException ex) {
        mResponseHandler.onError(ex);
        return ex;
    }

    private void reportDecodedSizeIfApplicable() {
        if (mDecompressedCounter != null) {
            long currentCount = mDecompressedCounter.getCount();
            int delta = (int) (currentCount - mLastDecompressedCount);
            mResponseHandler.onReadDecoded(delta);
            mLastDecompressedCount = currentCount;
        }
    }

    /**
     * Writes the byte to all the output streams. If we get an exception when writing to any
     * of the streams, we close all the streams, and then propagate the first exception that
     * occurred when writing.
     */
    private synchronized void writeToOutputStream(int oneByte) {
        if (mClosed) {
            return;
        }

        try {
            mOutputStream.write(oneByte);
            reportDecodedSizeIfApplicable();
        } catch (IOException e) {
            handleIOExceptionWritingToStream(e);
        }
    }

    /**
     * Same as {@link #writeToOutputStream(int)}, but we write a buffer instead.
     */
    private synchronized void writeToOutputStream(byte[] b, int offset, int count) {
        if (mClosed) {
            return;
        }

        try {
            mOutputStream.write(b, offset, count);
            reportDecodedSizeIfApplicable();
        } catch (IOException e) {
            handleIOExceptionWritingToStream(e);
        }
    }

    private void handleIOExceptionWritingToStream(IOException e) {
        logger.debug("Could not write response body to the stream " + e);
        closeOutputStreamQuietly();
    }
}
