package com.moesif.sdk.okhttp3client.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.Arrays;
import java.util.Collection;


public class MoesifApiConnConfig {
    public static String DEFAULT_BASE_URI = "https://api.moesif.net";
    public String baseUri;


    public Collection<String> bodyContentTypesBlackList =
            DefaultDomainData.bodyContentTypesBlackList;


    private String applicationId;

    public MoesifApiConnConfig() {
        init(null, null);
    }

    public MoesifApiConnConfig(String moesifApplicationId) {
        init(moesifApplicationId, null);
    }
    public MoesifApiConnConfig(String applicationId, String baseUri) {
        init(applicationId, baseUri);
    }

    public void init(String applicationId, String baseUri) {
        setBaseUri(baseUri);
        setApplicationId(applicationId);
    }

    /**
     * THIS DOES NOT WORK AT THE MOMENT
     * as the moesifapi-java has the Url hardcoded
     * @return
     */
    public String getBaseUri() {
        Validate.notBlank(baseUri, "BaseUrl not set");
        return baseUri;
    }

    /**
     * THIS DOES NOT WORK AT THE MOMENT
     * as the moesifapi-java has the Url hardcoded
     * @param baseUri
     */
    public void setBaseUri(String baseUri) {
        this.baseUri = getOrBlank(baseUri,
                EnvironmentVars.loadBaseUri(),
                DEFAULT_BASE_URI);
    }


    public Collection<String> getBodyContentTypesBlackList() {
        return bodyContentTypesBlackList;
    }

    //This token authenticates your API Calls

    /**
     * getValue value for applicationId
     *
     * @return applicationId Your Application Id for authn/authz
     */
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * Set value for applicationId
     *
     * @param applicationId Your Application Id for authn/authz
     */
    public void setApplicationId(String applicationId) {
        this.applicationId = getOrBlank(applicationId,
                EnvironmentVars.loadMoesifApplicationId(),
                "");
    }

    private static String getOrBlank(String valPri1,
                                     String valPri2,
                                     String defVal) {
        return StringUtils.defaultIfBlank(
                cleanAppId(valPri1),
                StringUtils.defaultIfBlank(cleanAppId(valPri2), cleanAppId(defVal)));
    }

    public static String cleanAppId(String s){
        if (StringUtils.isNotBlank((s))) {
            s = StringUtils.deleteWhitespace(s);
            for (char ch : Arrays.asList('"', '\''))
                s = StringUtils.remove(s, ch);
        }
        return s;
    }
    /**
     * Max bytes of body size allowed.
     * All body above this size are dropped.
     *
     * @return
     */
    public Integer getMaxAllowedBodySize() {
        return DefaultDomainData.maxAllowedBytes;
    }
}
