package com.moesif.test.live;

import com.moesif.test.unit.sdk.okhttpclient.End2EndRunner;
import static com.moesif.test.unit.helpers.UrlsForTest.*;

import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * These live tests require VALID ENVIRONMENT VARIABLE TO BE SET
 * MOESIF_APPLICATION_ID else they will fail
 */
public class TestEnd2EndWithValidAppId extends End2EndRunner {

    @ParameterizedTest
    @ValueSource(strings = {
            URL_404_EMPTY_JSON,
            URL_200_FULL_JSON_NO_CONT_LEN_RESP,
            URL_200_IP,
            URL_404,
            URL_200_HTTP_TEXT,
    })
    public void test_no_Exceptions(String url) {
        for (boolean isNetworkIntercept : APP_AND_NET_INTERCEPT)
            assertDoesNotThrow(() -> {
                        runInterceptor(url, isNetworkIntercept);
                        TimeUnit.SECONDS.sleep(5); // ALLOW FOR ASYNC EVENTS TO BE SUBMITTED
                    },
                    toMsg("No except expected: ", url, isNetworkIntercept));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            URL_200_HTTP_HTML_NO_PATH,
    })
    public void test_ConnTimeout(String url) {
        for (boolean isNetworkIntercept : APP_AND_NET_INTERCEPT)
            assertThrows(IOException.class, () -> {
                        runInterceptor(url, isNetworkIntercept);
                        TimeUnit.SECONDS.sleep(5); // ALLOW FOR ASYNC EVENTS TO BE SUBMITTED
                    },
                    toMsg("Conn except expected: ", url, isNetworkIntercept));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            URL_DOMAIN_NOT_EXIST,
    })
    public void test_UnknownHost(String url) {
        testUnknownHost(url);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "PUT", "POST", "PATCH", "DELETE"
    })
    public void testVerbWithBody(String verb) {
        String url = URL_200_FULL_JSON_NO_CONT_LEN_RESP;
        for (boolean isNetworkIntercept : APP_AND_NET_INTERCEPT)
            assertDoesNotThrow(() -> {
                        runTestWithBody(
                                url,
                                verb,
                                SAMPLE_JSON_BODY,
                                isNetworkIntercept);
                        TimeUnit.SECONDS.sleep(5); // ALLOW FOR ASYNC EVENTS TO BE SUBMITTED
                    },
                    toMsg("No exception expected: [" + verb + "]",
                            url,
                            isNetworkIntercept)
            );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "HEAD",
            "GET",
            "DELETE"
    })
    public void testNoBody(String verb) {
        String url = URL_200_FULL_JSON_NO_CONT_LEN_RESP;
        for (boolean isNetworkIntercept : APP_AND_NET_INTERCEPT)
            assertDoesNotThrow(() -> {
                        runTestWithBody(url, verb, null, isNetworkIntercept);
                        TimeUnit.SECONDS.sleep(5); // ALLOW FOR ASYNC EVENTS TO BE SUBMITTED
                    },
                    toMsg("No exception expected: [" + verb + "]",
                            url,
                            isNetworkIntercept));
    }

    /**
     * Test basic auth see:
     * https://square.github.io/okhttp/recipes/#handling-authentication-kt-java
     */
    @Test
    public void testBasicAuth() {
        for (boolean isNetworkIntercept : APP_AND_NET_INTERCEPT) {
            assertDoesNotThrow(() -> {
                OkHttpClient.Builder builder = getBuilder(isNetworkIntercept);
                builder.authenticator(new Authenticator() {
                    @Override
                    public Request authenticate(Route route,
                                                Response response){
                        if (StringUtils.isNotBlank(
                                response.request().header("Authorization"))) {
                            // Give up, we've already attempted to authenticate.
                            return null;
                        }
                        return response.request().newBuilder()
                                .header("Authorization",
                                        Credentials.basic(
                                                BASIC_AUTH_USER,
                                                BASIC_AUTH_PWD))
                                .build();
                    }
                });
                makeGetRequest(builder.build(), URL_BASIC_AUTH);
            });
        }
    }
}
