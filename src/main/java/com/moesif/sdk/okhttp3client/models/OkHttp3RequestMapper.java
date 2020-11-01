package com.moesif.sdk.okhttp3client.models;

import com.moesif.api.models.EventRequestBuilder;
import com.moesif.api.models.EventRequestModel;
import com.moesif.helpers.CollectionUtils;
import com.moesif.helpers.NetUtils;
import com.moesif.sdk.okhttp3client.util.JsonSerialize;
import com.moesif.external.facebook.stetho.inspector.network.RequestBodyHelperMoesif;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

/**
 * Map okHttp3 Request -> to -> Moesif EventRequestModel
 */
public class OkHttp3RequestMapper extends EventRequestModel {

    /**
     * Map okHttp3 Request -> to -> Moesif EventRequestModel
     * set local ipv4 for ip address
     * set apiVersion to null
     *
     * @param request okHttp3 Request
     * @return EventRequestModel
     * @throws IOException
     */
    public static EventRequestModel createOkHttp3Request(
            Request request) throws IOException {
        return createOkHttp3Request(request, null, null);
    }

    /**
     * Map okHttp3 Request -> to -> Moesif EventRequestModel
     *
     * @param request    okHttp3 Request
     * @param apiVersion allow client to set outbound apiVersion eg:"uber-v1"
     * @param ipAddress  IP addr where event occurred, If null, use local IPV4
     * @return EventRequestModel
     * @throws IOException
     */
    public static EventRequestModel createOkHttp3Request(
            Request request,
            String apiVersion,
            String ipAddress)
            throws IOException {
        if (StringUtils.isBlank(ipAddress))
            ipAddress = NetUtils.getIPAddress(true);
        return new EventRequestBuilder()
                .time(new Date())
                .uri(request.url().toString())
                .verb(request.method())
                .apiVersion(apiVersion)
                .ipAddress(ipAddress)
                .headers(CollectionUtils.flattenMultiMap(
                        request.headers().toMultimap())
                )
                .body(OkHttp3RequestMapper.bodyAsJson(
                        request))
                .build();
    }

    /**
     * Convert RequestBody to Object.
     * Uses Stetho RequestBodyHelper to gunzip + deflate
     *
     * @param request
     * @return
     * @throws IOException
     */
    @Nullable
    private static Object bodyAsJson(Request request)
            throws IOException {
        RequestBody body = request.body();
        if (body == null || body.contentLength() == 0) {
            return null;
        }
        // Gunzip + deflate using Stetho
        RequestBodyHelperMoesif rbh = new RequestBodyHelperMoesif();
        OutputStream out = rbh.createBodySink(
                request.header("Content-Encoding"));
        BufferedSink bufferedSink = Okio.buffer(Okio.sink(out));
        try {
            body.writeTo(bufferedSink);
        } finally {
            bufferedSink.close();
        }
        return JsonSerialize.jsonBAOutStreamToObj(
                rbh.getDisplayBody());
    }
}