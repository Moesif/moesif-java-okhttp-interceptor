package com.moesif.test.unit.sdk.okhttpclient;

import com.moesif.api.models.EventRequestModel;
import com.moesif.api.models.EventResponseModel;
import com.moesif.sdk.okhttp3client.models.util.EventConfig;
import com.moesif.sdk.okhttp3client.models.util.IEventValueProvider;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EventConfigTest {

    @Test
    void testDefaultTags(){
        IEventValueProvider<String> c = EventConfig.getTagsProvider();
        assertEquals(null, c.getValue(null, null));
        assertEquals(null,  c.getValue(new EventRequestModel(), new EventResponseModel()));

    }

    @Test
    void testCustomTags(){
        testTag("1.1.1.1", "2.2.2.2", "REQ-IP,RESP-IP");
        testTag("", "2.2.2.2", "RESP-IP");
        testTag("1.1.1.1", null, "REQ-IP");
        testTag(null, null, "");
    }

    void testTag(String ip1, String ip2, String expectedTag){
        EventConfig.setTagsProvider(new SetIpEventValueProvider());
        EventRequestModel req = new EventRequestModel();
        req.setIpAddress(ip1);
        EventResponseModel resp = new EventResponseModel();
        resp.setIpAddress(ip2);
        IEventValueProvider<String> c = EventConfig.getTagsProvider();
        assertEquals(expectedTag,  c.getValue(req, resp));
    }

    public static class SetIpEventValueProvider implements IEventValueProvider<String> {

        @Override
        public String getValue(EventRequestModel request, EventResponseModel response) {
            List<String> tags = new ArrayList<>();
            hasStrAddTagToColl(request.getIpAddress(), "REQ-IP", tags);
            hasStrAddTagToColl(response.getIpAddress(), "RESP-IP", tags);
            return String.join(",", tags);
        }

        private static void hasStrAddTagToColl(String s, String tag, List<String> coll){
            if(StringUtils.isNotBlank(s)) coll.add(tag);
        }
    }
}
