package com.moesif.helpers;

import java.io.ByteArrayOutputStream;
import java.util.Base64;


public class EncodeUtils {
    /**
     * Encodes ByteArrayOutputStream to base64
     * @param baos
     * @return b64 encoded string of bytearrayoutputstream
     */
    public static String BaosToB64Str(ByteArrayOutputStream baos) {
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }
}
