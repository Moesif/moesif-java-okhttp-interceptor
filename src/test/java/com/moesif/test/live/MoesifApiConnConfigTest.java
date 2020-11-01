package com.moesif.test.live;

import com.moesif.sdk.okhttp3client.config.EnvironmentVars;
import com.moesif.sdk.okhttp3client.config.MoesifApiConnConfig;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * These live tests require VALID ENVIRONMENT VARIABLE TO BE SET
 * MOESIF_APPLICATION_ID else they will fail
 */
class MoesifApiConnConfigTest {

    @Test
    void testEnsureEnvVarAppIdSet() {
        assertTrue(
                StringUtils.isNotBlank(EnvironmentVars.loadVar(
                        EnvironmentVars.MOESIF_APPLICATION_ID)));
    }


    @Test
    void testEnsureLoadMoesifApplicationId() {
        assertTrue(
                StringUtils.isNotBlank(
                        EnvironmentVars.loadMoesifApplicationId()));
        assertEquals(
                EnvironmentVars.loadMoesifApplicationId(),
                EnvironmentVars.loadVar(
                        EnvironmentVars.MOESIF_APPLICATION_ID));
        return;
    }

    @Test
    public void testMoesifApiConnConfig() {
        MoesifApiConnConfig connNoParam = new MoesifApiConnConfig();
        MoesifApiConnConfig connWithParam = new MoesifApiConnConfig(
                EnvironmentVars.loadMoesifApplicationId(),
                null);
        assertEquals(connNoParam.getApplicationId(),
                connWithParam.getApplicationId());
    }
}