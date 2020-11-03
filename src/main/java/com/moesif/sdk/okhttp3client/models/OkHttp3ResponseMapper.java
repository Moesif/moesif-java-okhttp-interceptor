package com.moesif.sdk.okhttp3client.models;

import com.moesif.api.models.EventResponseBuilder;
import com.moesif.api.models.EventResponseModel;
import com.moesif.helpers.CollectionUtils;
import okhttp3.Connection;
import okhttp3.Response;

import java.util.Date;


public class OkHttp3ResponseMapper extends EventResponseModel {

    public static EventResponseModel createOkHttp3Response(
            Response response,
            Connection connection) {
        String ipAddress = getIpAddr(connection);
        return createOkHttp3Response(response, ipAddress);
    }

    public static EventResponseModel createOkHttp3Response(
                Response response,
                String ipAddress) {
        return new EventResponseBuilder()
                .time(new Date())
                .status(response.code())
                .headers(CollectionUtils.flattenMultiMap(
                        response.headers().toMultimap())
                )
                .ipAddress(ipAddress)
                .build();
    }


    /**
     * Obtains socket from Okhttp3 Connection object and runs GetHostByAddress
     *
     * @param c Connection
     * @return null or ipAddress
     */
    public static String getIpAddr(Connection c) {
        String ipAddress = null;
        if (c != null) {
            ipAddress = c
                    .route()
                    .socketAddress()
                    .getAddress()
                    .getHostAddress();
        }
        return ipAddress;
    }
}
