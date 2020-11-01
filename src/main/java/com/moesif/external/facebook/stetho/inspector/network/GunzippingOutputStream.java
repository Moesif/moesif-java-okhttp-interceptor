/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.moesif.external.facebook.stetho.inspector.network;

import com.moesif.helpers.ExceptionUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.concurrent.*;
import java.util.zip.GZIPInputStream;

/**
 * An {@link OutputStream} filter which decompresses gzip data before it is written to the
 * specified destination output stream.  This is functionally equivalent to
 * {@link java.util.zip.InflaterOutputStream} but provides gzip header awareness.  The
 * implementation however is very different to avoid actually interpreting the gzip header.
 */
class GunzippingOutputStream extends FilterOutputStream {
    private final Future<Void> mCopyFuture;

    private static final ExecutorService sExecutor = Executors.newCachedThreadPool();

    public static GunzippingOutputStream create(OutputStream finalOut) throws IOException {
        PipedInputStream pipeIn = new PipedInputStream();
        PipedOutputStream pipeOut = new PipedOutputStream(pipeIn);

        Future<Void> copyFuture = sExecutor.submit(
                new GunzippingCallable(pipeIn, finalOut));

        return new GunzippingOutputStream(pipeOut, copyFuture);
    }

    private GunzippingOutputStream(OutputStream out, Future<Void> copyFuture) throws IOException {
        super(out);
        mCopyFuture = copyFuture;
    }

    @Override
    public void close() throws IOException {
        boolean success = false;
        try {
            super.close();
            success = true;
        } finally {
            try {
                getAndRethrow(mCopyFuture);
            } catch (IOException e) {
                if (success) {
                    throw e;
                }
            }
        }
    }

    private static <T> T getAndRethrow(Future<T> future) throws IOException {
        while (true) {
            try {
                return future.get();
            } catch (InterruptedException e) {
                // Continue...
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                ExceptionUtils.propagateIfInstanceOf(cause, IOException.class);
                org.apache.commons.lang3.exception.ExceptionUtils.wrapAndThrow(cause);
            }
        }
    }

    private static class GunzippingCallable implements Callable<Void> {
        private final InputStream mIn;
        private final OutputStream mOut;

        public GunzippingCallable(InputStream in, OutputStream out) {
            mIn = in;
            mOut = out;
        }

        @Override
        public Void call() throws IOException {
            GZIPInputStream in = new GZIPInputStream(mIn);
            try {
                IOUtils.copy(in, mOut);
            } finally {
                in.close();
                mOut.close();
            }
            return null;
        }
    }
}
