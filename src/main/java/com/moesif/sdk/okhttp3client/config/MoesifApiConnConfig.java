package com.moesif.sdk.okhttp3client.config;

import com.moesif.sdk.okhttp3client.models.filter.DefaultEventFilterConfig;
import com.moesif.sdk.okhttp3client.models.filter.IInterceptEventFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.Arrays;
import java.util.Collection;


public class MoesifApiConnConfig {
    public static String DEFAULT_BASE_URI = "https://api.moesif.net";

    public String baseUri;
    public Integer eventsBufferSize = 100;
    public Integer maxQueueSize = 100000;
    public Integer eventTimeoutMillis = 2000;
    public Collection<String> bodyContentTypesBlackList = DefaultDomainData.bodyContentTypesBlackList;

    private String applicationId;
    private IInterceptEventFilter eventFilterConfig;
    private boolean debug;

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
        setEventFilterConfig(null);
    }

    /**
     * THIS DOES NOT WORK AT THE MOMENT
     * as the moesifapi-java has the Url hardcoded
     * @return the base URI
     */
    public String getBaseUri() {
        Validate.notBlank(baseUri, "BaseUrl not set");
        return baseUri;
    }

    /**
     * THIS DOES NOT WORK AT THE MOMENT
     * as the moesifapi-java has the Url hardcoded
     * @param baseUri The base URI to set
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
     * Max bytes of body bytes for Request.
     * All body above this size are dropped.
     *
     * @return The max allowed number of bytes in a request body
     */

    public Long getMaxAllowedBodyBytesRequest() {
        return DefaultDomainData.maxAllowedBodyBytesRequest;
    }
    /**
     * Max bytes of body bytes for Response.
     * All body above this size are dropped.
     *
     * @return The max allowed number of bytes in a response body
     */
    public Long getMaxAllowedBodyBytesResponse() {
        return DefaultDomainData.maxAllowedBodyBytesResponse;
    }

    public Integer getEventsBufferSize() {
        return eventsBufferSize;
    }

    public void setEventsBufferSize(Integer eventsBufferSize) {
        this.eventsBufferSize = Math.max(eventsBufferSize, 1);
    }

    public IInterceptEventFilter getEventFilterConfig() {
        return eventFilterConfig;
    }

    public void setEventFilterConfig(IInterceptEventFilter eventFilterConfig) {
        this.eventFilterConfig = null == eventFilterConfig
                ? new DefaultEventFilterConfig()
                : eventFilterConfig;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    @Override
    public String toString() { // produce a log friendly single-line string representation of the config
        return "MoesifApiConnConfig{" +
                "baseUri='" + baseUri + '\'' +
                ", eventsBufferSize=" + eventsBufferSize +
                ", eventFilterConfig=" + eventFilterConfig +
                ", bodyContentTypesBlackList=" + bodyContentTypesBlackList +
                ", applicationId='" + applicationId + '\'' +
                ", debug=" + debug +
                '}';
    }
}
