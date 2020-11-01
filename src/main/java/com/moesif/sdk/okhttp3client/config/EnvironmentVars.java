package com.moesif.sdk.okhttp3client.config;

public class EnvironmentVars {
    public static String MOESIF_APPLICATION_ID = "MOESIF_APPLICATION_ID";
    public static String MOESIF_BASE_URI = "MOESIF_BASE_URI";

    public static String loadMoesifApplicationId() {
        return loadVar(MOESIF_APPLICATION_ID);
    }

    public static String loadBaseUri() {
        return loadVar(MOESIF_BASE_URI);
    }

    public static String loadVar(String key) {
        return System.getenv(key);
    }

}
