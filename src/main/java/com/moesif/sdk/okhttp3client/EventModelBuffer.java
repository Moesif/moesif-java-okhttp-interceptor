package com.moesif.sdk.okhttp3client;

import com.moesif.api.models.EventModel;

import java.util.ArrayList;
import java.util.List;

public class EventModelBuffer {
    private List<EventModel> buffer;
    private int maxSize;

    public EventModelBuffer(int maxSize) {
        this.maxSize = maxSize;
        init();
    }

    private void init(){
        buffer = new ArrayList<EventModel>();

    }

    public boolean isFull() {
        return buffer.size() >= this.maxSize;
    }

    public void add(EventModel loggedEvent) {
        buffer.add(loggedEvent);
    }

    public List<EventModel> empty(){
        List<EventModel> b = this.buffer;
        init();
        return b;
    }

}
