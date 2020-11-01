package com.moesif.helpers;

import org.apache.commons.validator.routines.InetAddressValidator;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;


public class NetUtils {

    private static final InetAddressValidator v = new InetAddressValidator();

    /**
     * Get IP address from first non-localhost interface
     *
     * @param useIPv4 true=return ipv4, false=return ipv6
     * @return address or empty string
     */
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(
                    NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                String ip = parseInterface(intf, useIPv4);
                if (null != ip)
                    return ip;
            }
        } catch (Exception e) {
        }
        return null;
    }

    private static String parseInterface(NetworkInterface netIntf,
                                         boolean useIPv4) {
        for (InetAddress addr : Collections.list(netIntf.getInetAddresses())) {
            if (addr.isLoopbackAddress())
                continue;
            String ip = parseAddr(addr, useIPv4);
            if (null != ip)
                return ip;
        }
        return null;
    }

    private static String parseAddr(InetAddress addr, boolean useIPv4) {
        if (null == addr)
            return null;
        String ip = null;
        String sAddr = addr.getHostAddress();
        boolean isIPv4 = v.isValidInet4Address(sAddr);

        if (useIPv4 && isIPv4) {
            ip = sAddr;
        } else if (!useIPv4 && !isIPv4) {
            ip = ipv6DropZoneSuffixToUppercase(sAddr);
        }
        if (null != ip && !addr.isSiteLocalAddress())
            return ip;
        return null;
    }

    /**
     * Given ip address "fe80::1ff:fe23:4567:890a%eth0"
     * drops anything after '%'
     * makes uppercase
     *
     * @param sAddr ipv6
     * @return uppercase ipv6 minus zone suffix.
     */
    public static String ipv6DropZoneSuffixToUppercase(String sAddr) {
        int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
        return delim < 0
                ? sAddr.toUpperCase()
                : sAddr.substring(0, delim).toUpperCase();
    }
}