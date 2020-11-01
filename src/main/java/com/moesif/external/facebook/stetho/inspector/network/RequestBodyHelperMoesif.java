/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.moesif.external.facebook.stetho.inspector.network;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.InflaterOutputStream;

/* See Stetho RequestBodyHelper - modified for moesif */
/**
 * Helper which manages provides computed request sizes as well as transparent decompression.
 * Note that request compression is not officially part of the HTTP standard however it is
 * commonly in use and can be conveniently supported here.
 * <p/>
 * To use, invoke {@link #createBodySink} to prepare an output stream where the raw body can be
 * written.  Then invoke {@link #getDisplayBody()} to retrieve the possibly decoded body.
 * Finally, {@link #reportDataSent()} can be called to report to Stetho the raw and decompressed
 * payload sizes.
 */
public class RequestBodyHelperMoesif {

    private ByteArrayOutputStream mDeflatedOutput;
    private CountingOutputStream mDeflatingOutput;

    public RequestBodyHelperMoesif() {
    }

    public OutputStream createBodySink(@Nullable String contentEncoding)
            throws IOException {
        OutputStream deflatingOutput;
        ByteArrayOutputStream deflatedOutput = new ByteArrayOutputStream();
        if (DecompressionHelper.GZIP_ENCODING.equals(contentEncoding)) {
            deflatingOutput = GunzippingOutputStream.create(deflatedOutput);
        } else if (DecompressionHelper.DEFLATE_ENCODING
                .equals(contentEncoding)) {
            deflatingOutput = new InflaterOutputStream(deflatedOutput);
        } else {
            deflatingOutput = deflatedOutput;
        }

        mDeflatingOutput = new CountingOutputStream(deflatingOutput);
        mDeflatedOutput = deflatedOutput;

        return mDeflatingOutput;
    }

    public byte[] getDisplayBody() {
        throwIfNoBody();
        return mDeflatedOutput.toByteArray();
    }

    public boolean hasBody() {
        return mDeflatedOutput != null;
    }

    public void reportDataSent() {
    }

    private void throwIfNoBody() {
        if (!hasBody()) {
            throw new IllegalStateException(
                    "No body found; has createBodySink been called?");
        }
    }
}
