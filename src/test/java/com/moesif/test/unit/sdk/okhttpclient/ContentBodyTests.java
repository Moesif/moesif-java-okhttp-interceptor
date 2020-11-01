package com.moesif.test.unit.sdk.okhttpclient;

import com.moesif.sdk.okhttp3client.MoesifOkHttp3Interceptor;
import com.moesif.sdk.okhttp3client.config.MoesifApiConnConfig;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContentBodyTests {

    @ParameterizedTest
    @ValueSource(strings = {
            "text/plain",
            "text/Plain",
            "never/before-seen",
    })
    void isAllowedContentType(String contentType) {
        assertTrue(
                MoesifOkHttp3Interceptor.isAllowedContentType(
                        contentType,
                        new MoesifApiConnConfig()
                                .getBodyContentTypesBlackList())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "application/pdf",
            "image/Png",
    })
    void isNotAllowedContentType(String contentType) {
        assertFalse(
                MoesifOkHttp3Interceptor.isAllowedContentType(
                        contentType,
                        new MoesifApiConnConfig()
                                .getBodyContentTypesBlackList())
        );
    }

    @Test
    void allLowerCase() {
        for (String s : new MoesifApiConnConfig()
                .getBodyContentTypesBlackList()) {
            assertTrue(StringUtils.isNotBlank(s));
            assertTrue(s.equals(s.toLowerCase()));
        }
    }
}
