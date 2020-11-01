package com.moesif.sdk.okhttp3client.util;

import okhttp3.Response;

import java.io.InputStream;

public class ResponseWrap {
    private Response r;

    public ResponseWrap(Response r) {
        this.r = r;
    }

    public Boolean isJsonHeader() {
        return this.r.header("Content-Type", "")
                .toLowerCase()
                .contains("application/json");
    }

    public Boolean hasNullBody() {
        return (null == r.body());
    }

    public long getBodyContentLength() {
        return r.body().contentLength();
    }

    public String getContentEncoding() {
        return r.header("Content-Encoding");
    }

    public InputStream getBodyByteInputStream() {
        return r.body().byteStream();
    }

    /**
     *
     * @return Body contentType str or null
     */
    public String getBodyContentType(){
        try {
            return r.body().contentType().toString();
        }
        catch (Exception ex){
            return null;
        }
    }
}