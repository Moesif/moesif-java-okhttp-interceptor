package com.moesif.sdk.okhttp3client.models.filter;

import com.moesif.api.models.EventModel;
import okhttp3.Request;
import okhttp3.Response;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.Optional;

public class DefaultEventFilterConfig implements IInterceptEventFilter{
    @Override
    public boolean skip(Request request, Response response) {
        return false;
    }

    @Override
    public EventModel maskContent(EventModel eventModel) {
        return eventModel;
    }

    @Override
    public Optional<String> identifyUser(Request request, Response response) {
        return Optional.empty();
    }

    @Override
    public Optional<String> identifyCompany(Request request, Response response) {
        return Optional.empty();
    }

    @Override
    public Optional<String> sessionToken(Request request, Response response) {
        return Optional.empty();
    }

    @Override
    public @Nullable Map<String, Object> getMetadata(Request request, Response response) {
        return null;
    }

    @Override
    public Optional<String> getApiVersion(Request request, Response response) {
        return Optional.empty();
    }
}
