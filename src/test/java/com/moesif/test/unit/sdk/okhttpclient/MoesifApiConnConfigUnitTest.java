package com.moesif.test.unit.sdk.okhttpclient;

import com.moesif.sdk.okhttp3client.config.EnvironmentVars;
import com.moesif.sdk.okhttp3client.config.MoesifApiConnConfig;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoesifApiConnConfigUnitTest {
    @Test
    void testMoesifApiConnConfigUnit() {
        MoesifApiConnConfig connNoParam = new MoesifApiConnConfig();
        MoesifApiConnConfig connWithParam = new MoesifApiConnConfig(
                EnvironmentVars.loadMoesifApplicationId(),
                null);
        assertEquals(connNoParam.getApplicationId(),
                connWithParam.getApplicationId());
    }

    @Test
    public void testEnsureBlacklistContentTypes() {
        assertTrue(CollectionUtils.isNotEmpty(
                new MoesifApiConnConfig().getBodyContentTypesBlackList()));
    }

    @Test
    public void readAppId(){
        assertEquals("aaa", MoesifApiConnConfig.cleanAppId("'aaa\""));
        assertEquals("aaa", MoesifApiConnConfig.cleanAppId("\"aaa'"));
        assertEquals("aaa", MoesifApiConnConfig.cleanAppId(" 'aaa \" '"));
    }
}
