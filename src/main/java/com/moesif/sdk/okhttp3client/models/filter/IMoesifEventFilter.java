package com.moesif.sdk.okhttp3client.models.filter;

import com.moesif.api.models.EventModel;

public interface IMoesifEventFilter {
    /**
     * Return an eventModel that can be submitted to Moesif
     * @param eventModel System generated default event model
     * @return eventModel that is submitted to Moesif
     */
    public EventModel maskContent(EventModel eventModel);

}
