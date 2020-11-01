package com.moesif.sdk.okhttp3client.models.util;

import com.moesif.api.models.EventRequestModel;
import com.moesif.api.models.EventResponseModel;

public class DefaultNullValueProvider implements IEventValueProvider<String> {

    public String getValue(EventRequestModel request,
                           EventResponseModel response) {
        return null;
    }
}
