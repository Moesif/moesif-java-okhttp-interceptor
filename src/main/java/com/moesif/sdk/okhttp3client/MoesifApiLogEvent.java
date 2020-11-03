package com.moesif.sdk.okhttp3client;

import com.moesif.api.MoesifAPIClient;
import com.moesif.api.controllers.APIController;
import com.moesif.api.http.client.APICallBack;
import com.moesif.api.http.client.HttpContext;
import com.moesif.api.http.response.HttpResponse;
import com.moesif.api.models.EventModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;


public class MoesifApiLogEvent {

    private static final Logger logger = LoggerFactory.getLogger(
            MoesifApiLogEvent.class);

    public static void sendEventsAsync(String moesifApplicationId,
                                       List<EventModel> loggedEvents)
            throws IOException {
            MoesifAPIClient client = new MoesifAPIClient(moesifApplicationId);
            final APIController apiController = client.getAPI();
            MoesifApiCallBack callBack = new MoesifApiCallBack();
            apiController.createEventsBatchAsync(loggedEvents, callBack);
        }

    public static class MoesifApiCallBack implements APICallBack<HttpResponse>{

        public void onSuccess(HttpContext context, HttpResponse response) {
            inspectStatusCode(context.getResponse().getStatusCode());
        }

        private static void inspectStatusCode(int respStatusCode){
            if (201 != respStatusCode)
                logger.debug("Received status code " + respStatusCode);
            else
                logger.debug("Event submitted to Moesif");
        }

        public void onFailure(HttpContext context, Throwable error) {
            logger.debug("onFailure " + context.getResponse()
                    + " " + error.getMessage());
        }
    }
}
