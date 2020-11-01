package com.moesif.sdk.okhttp3client.models.util;

import com.moesif.api.models.EventRequestModel;
import com.moesif.api.models.EventResponseModel;

public interface IEventValueProvider<T> {

    T getValue(EventRequestModel request, EventResponseModel response);
}
