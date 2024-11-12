package com.moesif.sdk.okhttp3client;

import com.moesif.api.models.EventModel;
import com.moesif.api.models.EventRequestModel;
import com.moesif.api.models.EventResponseModel;
import com.moesif.helpers.EncodeUtils;
import com.moesif.sdk.okhttp3client.models.OkHttp3EventMapper;
import com.moesif.sdk.okhttp3client.models.filter.IMoesifEventFilter;
import com.moesif.sdk.okhttp3client.util.JsonSerialize;
import com.moesif.external.facebook.stetho.inspector.network.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;


public class MoesifResponseHandler implements ResponseHandler {
    private static final Logger logger = LoggerFactory.getLogger(
                                            MoesifResponseHandler.class);
    private final EventRequestModel loggedRequest;
    private final EventResponseModel loggedResponse;
    private final ByteArrayOutputStream outputStream;
    private final Boolean jsonHeader;
    private final String moesifApplicationId;
    private final Long maxAllowedBodySize;
    private final EventModelBuffer buffer;
    private final String userId;
    private final String companyId;
    private final String sessionToken;
    private final Object metadata;
    private final IMoesifEventFilter moesifEventFilter;

    public MoesifResponseHandler(EventRequestModel loggedRequest,
                                 EventResponseModel loggedResponse,
                                 ByteArrayOutputStream outputStream,
                                 Boolean jsonHeader,
                                 String moesifApplicationId,
                                 Long maxAllowedBodyBytes,
                                 Integer maxSendBufferSize,
                                 String userId,
                                 String companyId,
                                 String sessionToken,
                                 Object metadata,
                                 IMoesifEventFilter moesifEventFilter) {
        this.loggedRequest = loggedRequest;
        this.loggedResponse = loggedResponse;
        this.outputStream = outputStream;
        this.jsonHeader = jsonHeader;
        this.moesifApplicationId = moesifApplicationId;
        this.maxAllowedBodySize = maxAllowedBodyBytes;
        this.buffer = new EventModelBuffer(maxSendBufferSize);
        this.userId = userId;
        this.companyId = companyId;
        this.sessionToken = sessionToken;
        this.metadata = metadata;
        this.moesifEventFilter = moesifEventFilter;
    }

    @Override
    public void onRead(int numBytes) {

    }

    @Override
    public void onReadDecoded(int numBytes) {

    }

    @Override
    public void onEOF() {
        sendEvent();

    }

    @Override
    public void onError(IOException e) {
        logger.warn("Error Decompressing stream: " + e.getMessage());
    }

    private void sendEvent() {
        try {
            setBodyAndTransferEncoding(jsonHeader,
                    loggedResponse,
                    outputStream,
                    maxAllowedBodySize);
            EventModel loggedEvent =
                    OkHttp3EventMapper.createOkHttp3Event(
                            loggedRequest,
                            loggedResponse,
                            userId,
                            companyId,
                            sessionToken,
                            metadata
                    );
            if( null != moesifEventFilter) {
                loggedEvent = moesifEventFilter.maskContent(loggedEvent);
            }
            buffer.add(loggedEvent);
            if (buffer.isFull()){
                List<EventModel> loggedEvents = buffer.empty();
                MoesifApiLogEvent.sendEventsAsync(
                        moesifApplicationId,
                        loggedEvents);
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Is Moesif Application ID configured? {}", e.getMessage());
        } catch (Exception e) {
            logger.warn("Error creating or submitting event {}", e.getMessage());
        } catch (Throwable throwable) {
            logger.warn("Error with throwable", throwable);
        }
    }

    private static void setBodyAndTransferEncoding(
            boolean isJsonHeader,
            EventResponseModel loggedResponse,
            ByteArrayOutputStream bodyStream,
            Long maxAllowedBodySize) {
        if ((null != bodyStream) && (bodyStream.size() <= maxAllowedBodySize)){
            if (isJsonHeader) {
                try {
                    loggedResponse.setBody(
                        JsonSerialize.jsonBAOutStreamToObj(bodyStream));
                } catch (Exception e) {
                    loggedResponse.setBody(
                        EncodeUtils.BaosToB64Str(bodyStream));
                    loggedResponse.setTransferEncoding("base64");    
                }
            } else {
                loggedResponse.setBody(
                        EncodeUtils.BaosToB64Str(bodyStream));
                loggedResponse.setTransferEncoding("base64");
            }
        }
    }
}
