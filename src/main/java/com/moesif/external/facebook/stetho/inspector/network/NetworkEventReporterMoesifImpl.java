/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.moesif.external.facebook.stetho.inspector.network;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;


/* see NetworkEventReporterImpl from stetho - modified for Moesif */
/**
 * Implementation of {@link NetworkEventReporterMoesif} which allows callers to inform the Stetho
 * system of network traffic.  Callers can safely eagerly access this class and store a
 * reference if they wish.  When WebKit Inspector clients are connected, the internal
 * implementation will be automatically wired up to them.
 */
public class NetworkEventReporterMoesifImpl implements NetworkEventReporterMoesif {
    private static final Logger logger = LoggerFactory.getLogger(NetworkEventReporterMoesifImpl.class);
    private final AtomicInteger mNextRequestId = new AtomicInteger(0);
    @Nullable
    private ResourceTypeHelper mResourceTypeHelper;

    private static NetworkEventReporterMoesif sInstance;

    private NetworkEventReporterMoesifImpl() {
    }

    /**
     * Static accessor allowing callers to easily hook into the WebKit Inspector system without
     * creating dependencies on the main Stetho initialization code path.
     * @return existing or new instance of NetworkEventReporterMoesifImpl
     */
    public static synchronized NetworkEventReporterMoesif get() {
        if (sInstance == null) {
            sInstance = new NetworkEventReporterMoesifImpl();
        }
        return sInstance;
    }

    /**
     * Not used by Moesif
     * @return isEnabled
     */
    @Override
    public boolean isEnabled() {
        return false;
    }

    /**
     * Not used by Moesif
     * @param request Request descriptor.
     */
    @Override
    public void requestWillBeSent(InspectorRequest request) {
    }

    /**
     * Not used by Moesif
     * @param response Response descriptor.
     */
    @Override
    public void responseHeadersReceived(InspectorResponse response) {
    }

    // MOESIF REQUIRED
    @Override
    public InputStream interpretResponseStream(
            String requestId,
            @Nullable String contentType,
            @Nullable String contentEncoding,
            @Nullable InputStream availableInputStream,
            ResponseHandler responseHandler,
            OutputStream decompressedOutput) {

        if (availableInputStream == null) {
            responseHandler.onEOF();
            return null;
        }
        try {
            return DecompressionHelper.teeInputWithDecompression(
                    requestId,
                    availableInputStream,
                    decompressedOutput,
                    contentEncoding,
                    responseHandler);
        } catch (IOException e) {
            logger.warn("Error writing response body data for request #" + requestId);
        }
        return availableInputStream;
    }

    /**
     * Not used by Moesif
     * @param requestId Unique identifier for the request as per {@link InspectorRequest#id()}
     * @param errorText Text to report for the error; using {@link IOException#toString()} is
     */
    @Override
    public void httpExchangeFailed(String requestId, String errorText) {
        loadingFailed(requestId, errorText);
    }

    /**
     * Not used by Moesif
     * @param requestId Unique identifier for the request as per {@link InspectorRequest#id()}
     */
    @Override
    public void responseReadFinished(String requestId) {
        loadingFinished(requestId);
    }

    private void loadingFinished(String requestId) {
        logger.info("Loading Finished");
    }

    @Override
    public void responseReadFailed(String requestId, String errorText) {
        loadingFailed(requestId, errorText);
    }

    private void loadingFailed(String requestId, String errorText) {
        logger.warn("Loading Failed: " + errorText);
    }

    @Override
    public void dataSent(
            String requestId,
            int dataLength,
            int encodedDataLength) {
        dataReceived(requestId, dataLength, encodedDataLength);
    }

    @Override
    public void dataReceived(
            String requestId,
            int dataLength,
            int encodedDataLength) {
        logger.info("Data received");
    }

    @Override
    public String nextRequestId() {
        return String.valueOf(mNextRequestId.getAndIncrement());
    }

    @Nullable
    private String getContentType(InspectorHeaders headers) {
        // This may need to change in the future depending on how cumbersome header simulation
        // is for the various hooks we expose.
        return headers.firstHeaderValue("Content-Type");
    }

    @Override
    public void webSocketCreated(String requestId, String url) {
        logger.info("Websocket created");
    }

    @Override
    public void webSocketClosed(String requestId) {
        logger.info("Websocket closed");
    }

    @Override
    public void webSocketWillSendHandshakeRequest(InspectorWebSocketRequest request) {
        logger.info("webSocketWillSendHandshakeRequest");
    }

    @Override
    public void webSocketHandshakeResponseReceived(InspectorWebSocketResponse response) {
        logger.info("webSocketHandshakeResponseReceived");
    }

    @Override
    public void webSocketFrameSent(InspectorWebSocketFrame frame) {
        logger.info("webSocketFrameSent");
    }

    @Override
    public void webSocketFrameReceived(InspectorWebSocketFrame frame) {
        logger.info("webSocketFrameReceived");
    }

    @Override
    public void webSocketFrameError(String requestId, String errorMessage) {
        logger.info("webSocketFrameError");
    }

    @NonNull
    private ResourceTypeHelper getResourceTypeHelper() {
        if (mResourceTypeHelper == null) {
            mResourceTypeHelper = new ResourceTypeHelper();
        }
        return mResourceTypeHelper;
    }
}
