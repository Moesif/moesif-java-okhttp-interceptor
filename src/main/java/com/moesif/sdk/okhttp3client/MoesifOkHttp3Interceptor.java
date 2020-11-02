package com.moesif.sdk.okhttp3client;

import com.moesif.api.models.EventRequestModel;
import com.moesif.api.models.EventResponseModel;
import com.moesif.sdk.okhttp3client.config.MoesifApiConnConfig;
import com.moesif.sdk.okhttp3client.models.OkHttp3RequestMapper;
import com.moesif.sdk.okhttp3client.models.OkHttp3ResponseMapper;
import com.moesif.sdk.okhttp3client.util.ResponseWrap;
import com.moesif.external.facebook.stetho.inspector.network.NetworkEventReporterMoesifImpl;
import com.moesif.external.facebook.stetho.inspector.network.NetworkEventReporterMoesif;
import okhttp3.*;
import okio.BufferedSource;
import okio.Okio;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MoesifOkHttp3Interceptor
 * This intrceptor can be used both as Application and Network interceptor
 *
 * OkHttpClient client = new OkHttpClient.Builder()
 *         .addInterceptor(new MoesifOkHttp3Interceptor())
 *         .build();
 *
 * https://square.github.io/okhttp/interceptors/
 * Credit: adopted from Facebook/Stetho StethoInterceptor.java
 */
public class MoesifOkHttp3Interceptor implements Interceptor {
    private static final Logger logger = LoggerFactory.getLogger(
            MoesifOkHttp3Interceptor.class);

    private final NetworkEventReporterMoesif mEventReporter =
            NetworkEventReporterMoesifImpl.get();
    private final AtomicInteger mNextRequestId = new AtomicInteger(0);
    private static MoesifApiConnConfig connConfig;

    /**
     * Initialize the Interceptor
     * Without parameters uses default: Look for Moesif App Id environment vars
     */
    public MoesifOkHttp3Interceptor() {
        init(null);
    }

    /**
     * Initialize the Interceptor
     * @param moesifApplicationId String Moesif application Id.
     *                            Supercedes any environment variables
     */
    public MoesifOkHttp3Interceptor(String moesifApplicationId) {
        init(new MoesifApiConnConfig(moesifApplicationId));
    }

    /**
     * Initialize the Interceptor
     * @param moesifApplicationId String Moesif Application Id
     * @param eventsBufferSize How many events to queue in buffer prior to send
     *                        to collector
     */
    public MoesifOkHttp3Interceptor(String moesifApplicationId, Integer eventsBufferSize) {
        MoesifApiConnConfig  c = new MoesifApiConnConfig(moesifApplicationId);
        c.setEventsBufferSize(eventsBufferSize);
        init(c);
    }


    /**
     * Initialize the Interceptor
     * @param eventsBufferSize How many events to queue in buffer prior to send
     *                        to collector
     */
    public MoesifOkHttp3Interceptor(Integer eventsBufferSize) {
        MoesifApiConnConfig  c = new MoesifApiConnConfig(null);
        c.setEventsBufferSize(eventsBufferSize);
        init(c);
    }
    /**
     * Initialize the Interceptor
     * @param connConfig
     */
    public MoesifOkHttp3Interceptor(MoesifApiConnConfig connConfig) {
        init(connConfig);
    }

    public void init(MoesifApiConnConfig connConfig) {
        this.connConfig = (null == connConfig)
                ? new MoesifApiConnConfig() : connConfig;
    }

    /**
     * This is the main entrypoint for Interceptor
     * For Application Interceptors - called once only
     * For Network Interceptors - maybe called more than once
     * such as first for HTTP 301 and then for the redirect call.
     * Or multiple times for authentication scenarios
     * @param chain
     * @return Response object
     * @throws IOException
     */
    @Override
    public Response intercept(Chain chain) throws IOException {
        final String requestId =
                String.valueOf(mNextRequestId.getAndIncrement());
        final Request request = chain.request();

        final EventRequestModel loggedRequest =
                OkHttp3RequestMapper.createOkHttp3Request(
                        request,
                         null, //TODO how to match play sdk MoesifApiFilter.scala
                        connConfig.getBaseUri(),
                        connConfig.getMaxAllowedBodyBytesRequest());
        Response response;
        try {
            response = chain.proceed(request);
        } catch (IOException e) {
            logger.warn("IOException in chain cannot proceed", e.getMessage());
            throw e;
        }

        final Connection connection = chain.connection();
        ResponseWrap respw = new ResponseWrap(response);
        final EventResponseModel loggedResponse =
                OkHttp3ResponseMapper.createOkHttp3Response(
                        response,
                        connection);
        if (!respw.hasNullBody()) {
            try {
                int outStreamSize = (int) respw.getBodyContentLength();
                final ByteArrayOutputStream outputStream = outStreamSize > 0
                        ? new ByteArrayOutputStream(outStreamSize)
                        : new ByteArrayOutputStream();
                MoesifResponseHandler moeRespHandler = new MoesifResponseHandler(
                                                loggedRequest,
                                                loggedResponse,
                                                outputStream,
                                                respw.isJsonHeader(),
                                                connConfig.getApplicationId(),
                                                connConfig.getMaxAllowedBodyBytesResponse(),
                                                connConfig.getEventsBufferSize());
                InputStream bodyByIS = isAllowedContentType(respw.getBodyContentType(),
                                                connConfig.getBodyContentTypesBlackList())
                                    ? respw.getBodyByteInputStream()
                                    : null;
                InputStream responseStream = mEventReporter
                        .interpretResponseStream(
                                requestId,
                                respw.getBodyContentType(),
                                respw.getContentEncoding(),
                                bodyByIS,
                                moeRespHandler,
                                outputStream
                        );
                if (responseStream != null) {
                    response = response.newBuilder()
                            .body(new ForwardingResponseBody(
                                    response.body(),
                                    responseStream)
                            )
                            .build();
                }
            } catch (Exception e) {
                logger.warn("Error parsing response body", e);
            }
        }
        else {
            logger.warn("Body is null");
        }
        return response;
    }

    /**
     * Checks whether Content-Type is contained in blockedContentTypes
     * @param cTypeToCheck
     * @param blockedContentTypes
     * @return
     */
    public static boolean isAllowedContentType(String cTypeToCheck,
                                       Collection<String> blockedContentTypes){
        return StringUtils.isBlank(cTypeToCheck)
                || CollectionUtils.isEmpty(blockedContentTypes)
                || !blockedContentTypes.contains(cTypeToCheck.toLowerCase());
    }

    private static class ForwardingResponseBody extends ResponseBody {
        private final ResponseBody mBody;
        private final BufferedSource mInterceptedSource;

        public ForwardingResponseBody(ResponseBody body,
                                      InputStream interceptedStream) {
            mBody = body;
            mInterceptedSource = Okio.buffer(Okio.source(interceptedStream));
        }

        @Override
        public MediaType contentType() {
            return mBody.contentType();
        }

        @Override
        public long contentLength() {
            return mBody.contentLength();
        }

        @Override
        public BufferedSource source() {
            /* close on the delegating body will actually close this
            intercepted source, but it was derived from mBody.byteStream()
            therefore the close will be forwarded all the way to the original.
            */
            return mInterceptedSource;
        }
    }
}