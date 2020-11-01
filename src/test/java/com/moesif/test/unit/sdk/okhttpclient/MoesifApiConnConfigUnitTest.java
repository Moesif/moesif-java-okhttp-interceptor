package com.moesif.test.unit.sdk.okhttpclient;

import com.moesif.sdk.okhttp3client.config.EnvironmentVars;
import com.moesif.sdk.okhttp3client.config.MoesifApiConnConfig;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoesifApiConnConfigUnitTest {

    @Test
    void testEnsureEnvVarAppIdNotSet() {
        assertTrue(
                StringUtils.isBlank(EnvironmentVars.loadVar(
                        EnvironmentVars.MOESIF_APPLICATION_ID)));
    }

    @Test
    void testDoesntExistLoadMoesifApplicationId() {
        assertTrue(
                StringUtils.isBlank(
                        EnvironmentVars.loadMoesifApplicationId()));
        assertTrue(
                StringUtils.isBlank(
                        new MoesifApiConnConfig().getApplicationId()));
        return;
    }

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
        assertTrue("aaa".equals(MoesifApiConnConfig.cleanAppId("'aaa\"")));
        assertTrue("aaa".equals(MoesifApiConnConfig.cleanAppId("\"aaa'")));
        assertTrue("aaa".equals(MoesifApiConnConfig.cleanAppId(" 'aaa \" '")));
    }
}
