package com.moesif.test.unit.sdk.okhttpclient;

import com.moesif.sdk.okhttp3client.MoesifOkHttp3Interceptor;
import com.moesif.sdk.okhttp3client.config.MoesifApiConnConfig;
import org.junit.jupiter.api.Test;

public class MoesifOkHttp3InterceptorUnitTest {

    @Test
    void testInitNoConfig(){
        new MoesifOkHttp3Interceptor();
    }

    @Test
    void testInitBlankConfig(){
        new MoesifOkHttp3Interceptor(new MoesifApiConnConfig());
    }

    @Test
    void testInitSetConfig(){
        new MoesifOkHttp3Interceptor(
                new MoesifApiConnConfig(
                        "appid",
                        "baseUri"));
    }
}
