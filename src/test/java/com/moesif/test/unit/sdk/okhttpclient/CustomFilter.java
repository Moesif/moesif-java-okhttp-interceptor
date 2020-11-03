package com.moesif.test.unit.sdk.okhttpclient;

import com.moesif.api.models.EventModel;
import com.moesif.sdk.okhttp3client.MoesifOkHttp3Interceptor;
import com.moesif.sdk.okhttp3client.config.MoesifApiConnConfig;
import com.moesif.sdk.okhttp3client.models.filter.IInterceptEventFilter;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class CustomFilter {

    @Test
    void testFilter() throws IOException {
        getCustomBuild();
    }

    public static OkHttpClient.Builder getCustomBuild() throws IOException {
        MoesifApiConnConfig cfg = new MoesifApiConnConfig();
        cfg.setEventFilterConfig(new TestEventFilterConfig());
        cfg.setEventsBufferSize(1);
        OkHttpClient.Builder bld = new OkHttpClient.Builder();
        bld.addInterceptor(new MoesifOkHttp3Interceptor(cfg));
        return bld;
    }

    public static class TestEventFilterConfig implements IInterceptEventFilter{

        @Override
        public EventModel maskContent(EventModel eventModel) {
            if (eventModel.getRequest().getIpAddress() == "127.0.0.1")
                eventModel.getRequest().setIpAddress("127.0.0.2");
            return eventModel;
        }

        @Override
        public boolean skip(Request request, Response response) {
            return request.method() == "DELETE";
        }

        @Override
        public Optional<String> identifyUser(Request request, Response response) {
            return Optional.of("customUser");
        }

        @Override
        public Optional<String> identifyCompany(Request request, Response response) {
            return Optional.of("customCompany");
        }

        @Override
        public Optional<String> sessionToken(Request request, Response response) {
            return Optional.of("customSessionToken");
        }

        @Override
        public @Nullable Map<String, Object> getMetadata(Request request, Response response) {
            Map<String, Object> customMetadata = new HashMap<String, Object>();
            Map<String, Object> subObject = new HashMap<String, Object>();
            subObject.put("some_bool", true);
            customMetadata.put("some_string", "value_a");
            customMetadata.put("some_int", 77);
            customMetadata.put("some_obj", subObject);
            return customMetadata;
        }

        @Override
        public Optional<String> getApiVersion(Request request, Response response) {
            return Optional.of("v-testing");
        }
    }
}
