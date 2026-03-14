/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.infra.instance.util;

import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.internal.configuration.plugins.Plugins;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(NetworkInterface.class)
class IpUtilsTest {
    
    @BeforeEach
    @AfterEach
    void clearCachedIpAddress() throws ReflectiveOperationException {
        Plugins.getMemberAccessor().set(IpUtils.class.getDeclaredField("cachedIpAddress"), null, null);
    }
    
    @Test
    void assertGetIpWithCachedIpAddress() throws ReflectiveOperationException {
        Plugins.getMemberAccessor().set(IpUtils.class.getDeclaredField("cachedIpAddress"), null, "cached-ip");
        assertThat(IpUtils.getIp(), is("cached-ip"));
    }
    
    @Test
    void assertGetIpWithSocketException() throws SocketException {
        when(NetworkInterface.getNetworkInterfaces()).thenThrow(SocketException.class);
        assertThat(IpUtils.getIp(), is("UnknownIP"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getIpArguments")
    void assertGetIp(final String name, final Enumeration<NetworkInterface> networkInterfaces, final String expectedIp) throws SocketException {
        when(NetworkInterface.getNetworkInterfaces()).thenReturn(networkInterfaces);
        assertThat(IpUtils.getIp(), is(expectedIp));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getIpWithNullArguments")
    void assertGetIpWithNullResult(final String name, final Enumeration<NetworkInterface> networkInterfaces) throws SocketException {
        when(NetworkInterface.getNetworkInterfaces()).thenReturn(networkInterfaces);
        assertNull(IpUtils.getIp());
    }
    
    private static Stream<Arguments> getIpArguments() {
        return Stream.of(
                Arguments.of("public_ipv4", Collections.enumeration(Collections.singleton(mockNetworkInterface(mockIpAddress("8.8.8.8", false, false)))), "8.8.8.8"),
                Arguments.of("local_ipv4", Collections.enumeration(Collections.singleton(mockNetworkInterface(mockIpAddress("192.168.1.10", true, false)))), "192.168.1.10"),
                Arguments.of("local_then_public_prefers_public",
                        Collections.enumeration(Collections.singleton(mockNetworkInterface(mockIpAddress("192.168.1.10", true, false), mockIpAddress("8.8.8.8", false, false)))), "8.8.8.8"));
    }
    
    private static Stream<Arguments> getIpWithNullArguments() {
        return Stream.of(
                Arguments.of("interface_without_addresses", Collections.enumeration(Collections.singleton(mockNetworkInterface()))),
                Arguments.of("public_like_ipv6", Collections.enumeration(Collections.singleton(mockNetworkInterface(mockIpAddress("2001:db8::1", false, false))))),
                Arguments.of("local_ipv6", Collections.enumeration(Collections.singleton(mockNetworkInterface(mockIpAddress("fe80::1", true, false))))),
                Arguments.of("loopback_ipv4", Collections.enumeration(Collections.singleton(mockNetworkInterface(mockIpAddress("127.0.0.1", false, true))))),
                Arguments.of("site_local_loopback_ipv4", Collections.enumeration(Collections.singleton(mockNetworkInterface(mockIpAddress("127.0.0.2", true, true))))));
    }
    
    private static NetworkInterface mockNetworkInterface(final InetAddress... ipAddresses) {
        NetworkInterface result = mock(NetworkInterface.class);
        when(result.getInetAddresses()).thenReturn(Collections.enumeration(Arrays.asList(ipAddresses)));
        return result;
    }
    
    private static InetAddress mockIpAddress(final String hostAddress, final boolean siteLocalAddress, final boolean loopbackAddress) {
        InetAddress result = mock(InetAddress.class);
        when(result.getHostAddress()).thenReturn(hostAddress);
        when(result.isSiteLocalAddress()).thenReturn(siteLocalAddress);
        when(result.isLoopbackAddress()).thenReturn(loopbackAddress);
        return result;
    }
}
