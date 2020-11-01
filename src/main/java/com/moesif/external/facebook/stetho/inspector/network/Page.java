package com.moesif.external.facebook.stetho.inspector.network;

import com.fasterxml.jackson.annotation.JsonValue;

public class Page {
    public enum ResourceType {
        DOCUMENT("Document"),
        STYLESHEET("Stylesheet"),
        IMAGE("Image"),
        FONT("Font"),
        SCRIPT("Script"),
        XHR("XHR"),
        WEBSOCKET("WebSocket"),
        OTHER("Other");

        private final String mProtocolValue;

        private ResourceType(String protocolValue) {
            mProtocolValue = protocolValue;
        }

        @JsonValue
        public String getProtocolValue() {
            return mProtocolValue;
        }
    }

}
