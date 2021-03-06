package com.moesif.test.live;

import com.moesif.test.unit.sdk.okhttpclient.End2EndRunner;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.TimeUnit;

import static com.moesif.test.unit.helpers.UrlsForTest.*;


import static com.moesif.test.unit.sdk.okhttpclient.CustomFilter.getCustomBuild;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class BasicLiveTest extends End2EndRunner {

    @ParameterizedTest
    @ValueSource(strings = {
            //URL_200_PNG,
            //URL_200_FULL_JSON_NO_CONT_LEN_RESP,
            URL_POST_200_JSON,
            //URL_200_HTTP_TEXT,
            //URL_404_EMPTY_JSON,
            //URL_200_IP,
            //URL_404,
    })
    public void test_no_Exceptions(String url) {
        for (boolean isNetworkIntercept : APP_AND_NET_INTERCEPT) {
            assertDoesNotThrow(() -> {
                        runInterceptor(url, isNetworkIntercept);
                        TimeUnit.SECONDS.sleep(5); // ALLOW FOR ASYNC EVENTS TO BE SUBMITTED
                    },
                    toMsg("No exception expected: ", url, isNetworkIntercept));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            //URL_200_PNG,
            //URL_200_FULL_JSON_NO_CONT_LEN_RESP,
            URL_POST_200_JSON,
            //URL_200_HTTP_TEXT,
            //URL_404_EMPTY_JSON,
            //URL_200_IP,
            //URL_404,
    })
    public void testCustomFilter(String url){
        assertDoesNotThrow(() -> {
                    makeGetRequest(getCustomBuild().build(), url);
                    TimeUnit.SECONDS.sleep(5); // ALLOW FOR ASYNC EVENTS TO BE SUBMITTED
                },
                toMsg("No exception expected: ", url, false));
    }
}
