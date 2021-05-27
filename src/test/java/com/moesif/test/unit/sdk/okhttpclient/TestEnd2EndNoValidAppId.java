package com.moesif.test.unit.sdk.okhttpclient;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static com.moesif.test.unit.helpers.UrlsForTest.*;


public class TestEnd2EndNoValidAppId extends End2EndRunner {

    @ParameterizedTest
    @ValueSource(strings = {
            URL_200_HTTP_HTML_NO_PATH,
    })
    public void test_ConnTimeout(String url) {
        for (boolean isNetworkIntercept : APP_AND_NET_INTERCEPT)
            assertThrows(IOException.class, () -> {
                        runInterceptor(url, isNetworkIntercept);
                    },
                    toMsg("Conn exception expected: ",
                            url,
                            isNetworkIntercept));
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
            URL_200_HTTP_TEXT,
    })
    public void test_NoAppIdSet(String url) {
        for (boolean isNetworkIntercept : APP_AND_NET_INTERCEPT)
            assertDoesNotThrow(() -> {
                        runInterceptor(url, isNetworkIntercept);
                    },
                    toMsg("Even without AppId No exception thrown: ",
                            url,
                            isNetworkIntercept));
    }

}
