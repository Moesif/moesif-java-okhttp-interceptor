package com.moesif.sdk.okhttp3client.models.filter;

import okhttp3.Request;
import okhttp3.Response;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.Optional;

public interface IOkHttpModelFilter {
    /**
     * If return value is True, Event is not submitted to Moesif
     * @param request The request object
     * @param response the response object
     * @return 'true' indicates dont submit event to Moesif
     */
    public boolean skip(Request request, Response response);

    /**
     *
     * @param request the request object
     * @param response the response object
     * @return Optional string containing identified user
     */
    public Optional<String> identifyUser(Request request, Response  response);
    public Optional<String> identifyCompany(Request request, Response  response);
    public Optional<String> sessionToken(Request request, Response  response);
    public @Nullable Map<String, Object> getMetadata(Request request, Response response);
    public Optional<String> getApiVersion(Request request, Response response);
}
