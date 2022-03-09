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
 * Map okHttp3 Request  to  Moesif EventRequestModel
 */
public class OkHttp3RequestMapper extends EventRequestModel {

    /**
     * Map okHttp3 Request  to  Moesif EventRequestModel
     * set local ipv4 for ip address
     * set apiVersion to null
     *
     * @param request okHttp3 Request
     * @return EventRequestModel
     * @throws IOException IOException
     */
    /*
    public static EventRequestModel createOkHttp3Request(
            Request request) throws IOException {
        return createOkHttp3Request(request, null, null);
    }
    */

    /**
     * Map okHttp3 Request  to  Moesif EventRequestModel
     *
     * @param request    okHttp3 Request
     * @param requestDate request's Date.
     * @param apiVersion allow client to set outbound apiVersion eg:"uber-v1"
     * @param ipAddress  IP addr where event occurred, If null, use local IPV4
     * @param maxAllowedBodyBytesRequest The maximum allowed number of bytes in request body
     * @return EventRequestModel
     * @throws IOException IOException
     */
    public static EventRequestModel createOkHttp3Request(
            Request request,
            Date requestDate,
            String apiVersion,
            String ipAddress,
            Long maxAllowedBodyBytesRequest
    ) throws IOException {
        if (StringUtils.isBlank(ipAddress))
            ipAddress = NetUtils.getIPAddress(true);
        EventRequestBuilder erb = new EventRequestBuilder()
                .time(requestDate)
                .uri(request.url().toString())
                .verb(request.method())
                .apiVersion(apiVersion)
                .ipAddress(ipAddress)
                .headers(CollectionUtils.flattenMultiMap(
                        request.headers().toMultimap())
                );
        if (isBodyContentLenAcceptable(request, maxAllowedBodyBytesRequest))
            erb.body(OkHttp3RequestMapper.bodyAsJson(
                        request));
        return erb.build();
    }

    /**
     * Examine request header and verify content length is either absent
     * or within maxAllowedBodyBytesReq
     * @param request The request
     * @param maxAllowedBodyBytesReq The maximum allowed size of body bytes in request
     * @return whether contentLength is within limits
     * @throws IOException IOException
     */
    private static boolean isBodyContentLenAcceptable(
            Request request,
            Long maxAllowedBodyBytesReq)throws IOException {
        if (null == maxAllowedBodyBytesReq)
            return true;
        if (maxAllowedBodyBytesReq > 0
                && request != null
                && request.body() != null){
            // contentLength returned could be -1 if unknown content length
            return request.body().contentLength() <= maxAllowedBodyBytesReq;
        }
        return false;
    }

    /**
     * Convert RequestBody to Object.
     * Uses Stetho RequestBodyHelper to gunzip + deflate
     *
     * @param request The request
     * @return Json Object or null
     * @throws IOException IOException
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