/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.moesif.external.facebook.stetho.inspector.network;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.InflaterOutputStream;

// @VisibleForTest
public class DecompressionHelper {
    private static final Logger logger = LoggerFactory.getLogger(
            DecompressionHelper.class);
    static final String GZIP_ENCODING = "gzip";
    static final String DEFLATE_ENCODING = "deflate";

    public static InputStream teeInputWithDecompression(
            String requestId,
            InputStream availableInputStream,
            OutputStream decompressedOutput,
            @Nullable String contentEncoding,
            ResponseHandler responseHandler) throws IOException {
        OutputStream output = decompressedOutput;
        CountingOutputStream decompressedCounter = null;

        if (contentEncoding != null) {
            boolean gzipEncoding = GZIP_ENCODING.equals(contentEncoding);
            boolean deflateEncoding = DEFLATE_ENCODING.equals(contentEncoding);

            if (gzipEncoding || deflateEncoding) {
                decompressedCounter = new CountingOutputStream(decompressedOutput);
                if (gzipEncoding) {
                    output = GunzippingOutputStream.create(decompressedCounter);
                } else if (deflateEncoding) {
                    output = new InflaterOutputStream(decompressedCounter);
                }
            } else {
                logger.warn("Unsupported Content-Encoding in response for request #" + requestId +
                        ": " + contentEncoding);
            }
        }

        return new ResponseHandlingInputStream(
                availableInputStream,
                requestId,
                output,
                decompressedCounter,
                responseHandler);
    }
}
