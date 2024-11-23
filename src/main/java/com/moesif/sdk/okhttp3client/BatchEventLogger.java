package com.moesif.sdk.okhttp3client;

import com.moesif.api.MoesifAPIClient;
import com.moesif.api.controllers.APIController;
import com.moesif.api.http.client.APICallBack;
import com.moesif.api.http.client.HttpContext;
import com.moesif.api.http.response.HttpResponse;
import com.moesif.api.models.EventModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class BatchEventLogger implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(BatchEventLogger.class);

    private final BlockingQueue<EventModel> queue;
    private final int batchSize;
    private final long maxWaitTimeMillis;
    private final APIController apiController;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public BatchEventLogger(BlockingQueue<EventModel> queue, int batchSize, long maxWaitTimeMillis, String applicationId) {
        this.queue = queue;
        this.batchSize = batchSize;
        this.maxWaitTimeMillis = maxWaitTimeMillis;
        MoesifAPIClient client = new MoesifAPIClient(applicationId);
        this.apiController = client.getAPI();
    }

    @Override
    public void run() {
        try {
            List<EventModel> batch = new ArrayList<>();
            long batchStartTime = 0;

            while (running.get() || !queue.isEmpty()) {
                long timeout = maxWaitTimeMillis;

                if (!batch.isEmpty()) {
                    long elapsedTime = System.currentTimeMillis() - batchStartTime;
                    timeout = maxWaitTimeMillis - elapsedTime;
                    if (timeout <= 0) {
                        // Time limit reached, send the batch
                        sendBatch(new ArrayList<>(batch));
                        batch.clear();
                        batchStartTime = 0;
                        timeout = maxWaitTimeMillis;
                    }
                }

                // Poll for the next event with the calculated timeout
                EventModel event = queue.poll(timeout, TimeUnit.MILLISECONDS);

                if (event != null) {
                    if (batch.isEmpty()) {
                        // Start the batch timer
                        batchStartTime = System.currentTimeMillis();
                    }
                    batch.add(event);

                    if (batch.size() >= batchSize) {
                        logger.debug("Seding batch of {} events after reaching batch size limit", batch.size());
                        // Batch size limit reached, send the batch
                        sendBatch(new ArrayList<>(batch));
                        batch.clear();
                        batchStartTime = 0;
                    }
                } else {
                    // No event received within timeout
                    if (!batch.isEmpty()) {
                        logger.debug("Seding batch of {} events after max wait timeout", batch.size());
                        // Send any accumulated events
                        sendBatch(new ArrayList<>(batch));
                        batch.clear();
                        batchStartTime = 0;
                    }
                    if (!running.get()) {
                        // Exit if the running flag is false
                        break;
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.info("EventConsumer interrupted, shutting down");
        } catch (Exception e) {
            logger.error("Error in EventConsumer", e);
        } finally {
            // Process any remaining events before exiting
            processRemainingEvents();
        }
    }

    private void sendBatch(List<EventModel> batch) {
        if (!batch.isEmpty()) {
            try {
                apiController.createEventsBatchAsync(batch, new MoesifApiCallBack());
            } catch (Exception e) {
                // Handle exception during sending
                logger.error("Exception while sending event batch", e);
            }
        }
    }

    private void processRemainingEvents() {
        List<EventModel> remainingEvents = new ArrayList<>();
        queue.drainTo(remainingEvents);
        if (!remainingEvents.isEmpty()) {
            logger.info("Processing remaining events before shutdown");
            sendBatch(remainingEvents);
        }
    }

    public void shutdown() {
        running.set(false);
    }

   public static class MoesifApiCallBack implements APICallBack<HttpResponse> {

        public void onSuccess(HttpContext context, HttpResponse response) {
            int respStatusCode = response.getStatusCode();
            if (201 != respStatusCode)
              logger.debug("Received status code {}", respStatusCode);
            else
                logger.debug("Event submitted to Moesif");
        }

        public void onFailure(HttpContext context, Throwable error) {
          logger.debug("onFailure {} {}", context.getResponse(), error.getMessage());
        }
    }
}
