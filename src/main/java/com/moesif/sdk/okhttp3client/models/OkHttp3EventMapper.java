package com.moesif.sdk.okhttp3client.models;

import com.moesif.api.models.EventBuilder;
import com.moesif.api.models.EventModel;
import com.moesif.api.models.EventRequestModel;
import com.moesif.api.models.EventResponseModel;

public class OkHttp3EventMapper extends EventModel {

    public static EventModel createOkHttp3Event(
            EventRequestModel loggedRequest,
            EventResponseModel loggedResponse) {
        return createOkHttp3Event(
                loggedRequest,
                loggedResponse,
                null,
                null,
                null,
                null);
    }

    public static EventModel createOkHttp3Event(
            EventRequestModel loggedRequest,
            EventResponseModel loggedResponse,
            String userId,
            String compayId,
            String sessionToken,
            Object metadata) {
        return new EventBuilder()
                .request(loggedRequest)
                .response(loggedResponse)
                .userId(userId)
                .companyId(compayId)
                .sessionToken(sessionToken)
                .metadata(metadata)
                .build();
    }

}
