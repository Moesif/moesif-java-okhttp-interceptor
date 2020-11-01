package com.moesif.test.unit.helpers;

import com.moesif.helpers.NetUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NetUtilsTest {

    @Test
    void getIPAddress() {
        String ipv4 = NetUtils.getIPAddress(true);
        assertTrue(InetAddressValidator.getInstance()
                .isValidInet4Address(ipv4));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "fe80::1ff:fe23:4567:890a",
            "2001:db8:3333:4444:5555:6666:7777:8888",
            "2001:db8::",
            "2001:0db8:0001:0000:0000:0ab9:C0A8:0102",
            "2001:db8:1::ab9:C0A8:102",
    })
    void ipv6DropZoneSuffix(String ipv6) {
        for (String zoneSuffix : new String[]{
                "%eth2",
                "%2",
                "%",
                "",
        }) {
            String observed = NetUtils.ipv6DropZoneSuffixToUppercase(
                    ipv6 + zoneSuffix);
            assertEquals(observed, ipv6.toUpperCase());
            assertTrue(InetAddressValidator.getInstance()
                    .isValidInet6Address(observed));
        }
        return;
    }
}