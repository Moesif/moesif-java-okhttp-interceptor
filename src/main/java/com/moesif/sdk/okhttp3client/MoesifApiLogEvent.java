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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class MoesifApiLogEvent {

    private static final Logger logger = LoggerFactory.getLogger(MoesifApiLogEvent.class);

    public static void sendEventAsync(String moesifKey,
                                      EventModel loggedEvent)
            throws IOException, InterruptedException {
        MoesifAPIClient client = new MoesifAPIClient(moesifKey);
        final APIController apiController = client.getAPI();
        final CountDownLatch cdLatch = new CountDownLatch(1);
        MoesifApiCallBack callBack = new MoesifApiCallBack(cdLatch);
        apiController.createEventAsync(loggedEvent, callBack);
        if (!cdLatch.await(10000, TimeUnit.MILLISECONDS))
            logger.warn("Lock await failed");
    }

    public static class MoesifApiCallBack implements APICallBack<HttpResponse>{
        public static CountDownLatch cdLatch;
        public MoesifApiCallBack(CountDownLatch cdLatch){
            this.cdLatch = cdLatch;
        }

        public void onSuccess(HttpContext context, HttpResponse response) {
            inspectStatusCode(context.getResponse().getStatusCode());
            cdLatch.countDown();
        }

        private static void inspectStatusCode(int respStatusCode){
            if (201 != respStatusCode)
                logger.debug("Received status code " + respStatusCode);
        }

        public void onFailure(HttpContext context, Throwable error) {
            logger.debug("onFailure " + context.getResponse()
                    + " " + error.getMessage());
        }
    }
}
