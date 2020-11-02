package com.moesif.test.unit.sdk.okhttpclient;

import com.moesif.sdk.okhttp3client.MoesifOkHttp3Interceptor;
import okhttp3.*;
import org.junit.platform.commons.util.StringUtils;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class End2EndRunner {

    private static Integer HTTP_TEST_TIMEOUT_SECONDS = 6;
    public static List<Boolean> APP_AND_NET_INTERCEPT = Arrays.asList(
                                                            true, false);

    public void testUnknownHost(String url) {
        for (boolean isNetworkIntercept : Arrays.asList(false, true))
            assertThrows(UnknownHostException.class, () -> {
                        runInterceptor(url, isNetworkIntercept);
                    },
                    toMsg("unknownhost exception expected: ",
                            url, isNetworkIntercept));
    }


    public static void runInterceptor(String url, boolean isNetworkInterceptor) throws IOException {
        OkHttpClient client = buildClient(isNetworkInterceptor);
        makeGetRequest(client, url);
        return;
    }


    public static void runTestWithBody(String url, String verb, String jsonBody, boolean isNetworkInterceptor) throws IOException {
        OkHttpClient client = buildClient(isNetworkInterceptor);
        makeRequest(client, url, verb, jsonBody);
        return;
    }

    public static OkHttpClient buildClient(boolean isNetworkInterceptor) throws IOException {
        return getBuilder(isNetworkInterceptor).build();
    }

    public static OkHttpClient.Builder getBuilder(boolean isNetworkInterceptor) throws IOException {
        OkHttpClient.Builder bld = new OkHttpClient.Builder();
        if (isNetworkInterceptor)
            bld.addNetworkInterceptor(new MoesifOkHttp3Interceptor(1));
        else
            bld.addInterceptor(new MoesifOkHttp3Interceptor(1));
        addTimeout(bld);
        return bld;
    }

    private static void addTimeout(OkHttpClient.Builder bld) {
        bld.connectTimeout(HTTP_TEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(HTTP_TEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(HTTP_TEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                // callTimeout spans end2end including DNS
                .callTimeout(HTTP_TEST_TIMEOUT_SECONDS * 5, TimeUnit.SECONDS);
    }


    public static void makeGetRequest(OkHttpClient client, String url) throws IOException {

        makeRequest(client, url, "GET", null);
    }

    public static void makeRequest(OkHttpClient client, String url, String verb, String jsonBody) throws IOException {
        Request.Builder rb = new Request.Builder()
                .url(url)
                .header("User-Agent", "OkHttp Example");
        addBodyJsonVerb(rb, verb, jsonBody);
        Request request = rb.build();

        Response response = client.newCall(request).execute();
        ResponseBody b = response.body();
        b.close();
        return;
    }

    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    public static void addBodyJsonVerb(Request.Builder r, String verb, String json) {

        switch (verb) {
            case "POST":
                r.post(RequestBody.create(json, JSON));
                break;
            case "PATCH":
                r.patch(RequestBody.create(json, JSON));
                break;
            case "PUT":
                r.put(RequestBody.create(json, JSON));
                break;
            case "DELETE":
                if (StringUtils.isBlank(json))
                    r.delete();
                else
                    r.delete(RequestBody.create(json, JSON));
                break;
            case "HEAD":
                r.head();
                break;
            default:
                break; // GET
        }
        return;
    }

    public static String toMsg(String msg, String url,
                               boolean isNetIntercept) {
        return "[" + (isNetIntercept ? "net" : "app") + "] "
                + msg + " [" + url + "]";
    }

}
