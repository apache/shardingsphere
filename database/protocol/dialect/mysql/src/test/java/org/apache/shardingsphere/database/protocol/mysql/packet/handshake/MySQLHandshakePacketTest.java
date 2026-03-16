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

package org.apache.shardingsphere.database.protocol.mysql.packet.handshake;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.protocol.constant.DatabaseProtocolServerInfo;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLAuthenticationMethod;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLCapabilityFlag;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLStatusFlag;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.apache.shardingsphere.database.protocol.payload.PacketPayload;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLHandshakePacketTest {
    
    private static final String EXPECTED_SERVER_VERSION = DatabaseProtocolServerInfo.getDefaultProtocolVersion(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
    
    private static final int SECURE_CONNECTION_CAPABILITY_FLAGS_LOWER = MySQLCapabilityFlag.calculateHandshakeCapabilityFlagsLower();
    
    private static final int PLUGIN_AUTH_CAPABILITY_FLAGS_UPPER = MySQLCapabilityFlag.CLIENT_PLUGIN_AUTH.getValue() >> 16;
    
    private static final byte[] FOO_AUTH_PLUGIN_DATA_PART_1 = {106, 105, 55, 122, 117, 98, 115, 109};
    
    private static final byte[] BAR_AUTH_PLUGIN_DATA_PART_2 = {68, 102, 53, 122, 65, 49, 84, 79, 85, 115, 116, 113};
    
    @Mock
    private MySQLPacketPayload payload;
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("constructWithPayloadArguments")
    void assertConstructWithPayload(final String name, final int capabilityFlagsLower, final int capabilityFlagsUpper,
                                    final byte[] expectedAuthPluginDataPart2, final String expectedAuthPluginName) {
        when(payload.readInt1()).thenReturn(MySQLConstants.PROTOCOL_VERSION, MySQLConstants.DEFAULT_CHARSET.getId(), 0);
        when(payload.readStringNul()).thenReturn(EXPECTED_SERVER_VERSION, MySQLAuthenticationMethod.NATIVE.getMethodName());
        when(payload.readStringNulByBytes()).thenReturn(FOO_AUTH_PLUGIN_DATA_PART_1, BAR_AUTH_PLUGIN_DATA_PART_2);
        when(payload.readInt4()).thenReturn(1000);
        when(payload.readInt2()).thenReturn(capabilityFlagsLower, MySQLStatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), capabilityFlagsUpper);
        MySQLHandshakePacket actual = new MySQLHandshakePacket(payload);
        assertThat(actual.getServerVersion(), is(EXPECTED_SERVER_VERSION));
        assertThat(actual.getCapabilityFlagsLower(), is(capabilityFlagsLower));
        assertThat(actual.getConnectionId(), is(1000));
        assertThat(actual.getCharacterSet(), is(MySQLConstants.DEFAULT_CHARSET.getId()));
        assertThat(actual.getStatusFlag(), is(MySQLStatusFlag.SERVER_STATUS_AUTOCOMMIT));
        assertThat(actual.getCapabilityFlagsUpper(), is(capabilityFlagsUpper));
        assertThat(actual.getAuthPluginData().getAuthenticationPluginDataPart1(), is(FOO_AUTH_PLUGIN_DATA_PART_1));
        assertThat(actual.getAuthPluginData().getAuthenticationPluginDataPart2(), is(expectedAuthPluginDataPart2));
        verify(payload).skipReserved(10);
        if (null == expectedAuthPluginName) {
            assertNull(actual.getAuthPluginName());
        } else {
            assertThat(actual.getAuthPluginName(), is(expectedAuthPluginName));
        }
    }
    
    @Test
    void assertConstructWithInvalidProtocolVersion() {
        when(payload.readInt1()).thenReturn(MySQLConstants.PROTOCOL_VERSION + 1);
        assertThrows(IllegalArgumentException.class, () -> new MySQLHandshakePacket(payload));
    }
    
    @Test
    void assertConstructWithSSLEnabled() {
        MySQLHandshakePacket actual = new MySQLHandshakePacket(1, true, new MySQLAuthenticationPluginData());
        assertThat(actual.getCapabilityFlagsLower() & MySQLCapabilityFlag.CLIENT_SSL.getValue(), is(MySQLCapabilityFlag.CLIENT_SSL.getValue()));
        assertThat(actual.getAuthPluginName(), is(MySQLAuthenticationMethod.CACHING_SHA2_PASSWORD.getMethodName()));
    }
    
    @Test
    void assertConstructWithoutSSLEnabled() {
        MySQLHandshakePacket actual = new MySQLHandshakePacket(1, false, new MySQLAuthenticationPluginData());
        assertThat(actual.getCapabilityFlagsLower() & MySQLCapabilityFlag.CLIENT_SSL.getValue(), is(0));
    }
    
    @Test
    void assertSetAuthPluginName() {
        mockPayloadForConstruct();
        MySQLHandshakePacket actual = new MySQLHandshakePacket(payload);
        actual.setAuthPluginName(MySQLAuthenticationMethod.NATIVE);
        assertThat(actual.getAuthPluginName(), is(MySQLAuthenticationMethod.NATIVE.getMethodName()));
        assertThat(actual.getCapabilityFlagsUpper() & PLUGIN_AUTH_CAPABILITY_FLAGS_UPPER, is(PLUGIN_AUTH_CAPABILITY_FLAGS_UPPER));
    }
    
    @Test
    void assertWriteWithSecureConnectionAndPluginAuth() {
        MySQLAuthenticationPluginData authPluginData = new MySQLAuthenticationPluginData(FOO_AUTH_PLUGIN_DATA_PART_1, BAR_AUTH_PLUGIN_DATA_PART_2);
        MySQLHandshakePacket actual = new MySQLHandshakePacket(1000, false, authPluginData);
        actual.write((PacketPayload) payload);
        verify(payload).writeInt1(MySQLConstants.PROTOCOL_VERSION);
        verify(payload).writeStringNul(EXPECTED_SERVER_VERSION);
        verify(payload).writeInt4(1000);
        verify(payload).writeStringNul(new String(authPluginData.getAuthenticationPluginDataPart1()));
        verify(payload).writeInt2(SECURE_CONNECTION_CAPABILITY_FLAGS_LOWER);
        verify(payload).writeInt1(MySQLConstants.DEFAULT_CHARSET.getId());
        verify(payload).writeInt2(MySQLStatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue());
        verify(payload).writeInt2(MySQLCapabilityFlag.calculateHandshakeCapabilityFlagsUpper());
        verify(payload).writeInt1(authPluginData.getAuthenticationPluginData().length + 1);
        verify(payload).writeReserved(10);
        verify(payload).writeStringNul(new String(authPluginData.getAuthenticationPluginDataPart2()));
        verify(payload).writeStringNul(MySQLAuthenticationMethod.CACHING_SHA2_PASSWORD.getMethodName());
    }
    
    @Test
    void assertWriteWithoutSecureConnectionAndPluginAuth() {
        mockPayloadForConstruct();
        MySQLHandshakePacket actual = new MySQLHandshakePacket(payload);
        actual.write((PacketPayload) payload);
        verify(payload).writeInt1(MySQLConstants.PROTOCOL_VERSION);
        verify(payload).writeStringNul(EXPECTED_SERVER_VERSION);
        verify(payload).writeInt4(1000);
        verify(payload).writeStringNul(new String(FOO_AUTH_PLUGIN_DATA_PART_1));
        verify(payload, times(2)).writeInt2(0);
        verify(payload).writeInt1(MySQLConstants.DEFAULT_CHARSET.getId());
        verify(payload).writeInt2(MySQLStatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue());
        verify(payload).writeInt1(0);
        verify(payload).writeReserved(10);
        verify(payload, never()).writeStringNul(new String(BAR_AUTH_PLUGIN_DATA_PART_2));
        verify(payload, never()).writeStringNul(MySQLAuthenticationMethod.CACHING_SHA2_PASSWORD.getMethodName());
    }
    
    private void mockPayloadForConstruct() {
        when(payload.readInt1()).thenReturn(MySQLConstants.PROTOCOL_VERSION, MySQLConstants.DEFAULT_CHARSET.getId(), 0);
        when(payload.readStringNul()).thenReturn(EXPECTED_SERVER_VERSION, MySQLAuthenticationMethod.NATIVE.getMethodName());
        when(payload.readStringNulByBytes()).thenReturn(FOO_AUTH_PLUGIN_DATA_PART_1, BAR_AUTH_PLUGIN_DATA_PART_2);
        when(payload.readInt4()).thenReturn(1000);
        when(payload.readInt2()).thenReturn(0, MySQLStatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), 0);
    }
    
    private static Stream<Arguments> constructWithPayloadArguments() {
        return Stream.of(
                Arguments.of("secure_connection_and_plugin_auth", SECURE_CONNECTION_CAPABILITY_FLAGS_LOWER, PLUGIN_AUTH_CAPABILITY_FLAGS_UPPER,
                        BAR_AUTH_PLUGIN_DATA_PART_2, MySQLAuthenticationMethod.NATIVE.getMethodName()),
                Arguments.of("secure_connection_without_plugin_auth", SECURE_CONNECTION_CAPABILITY_FLAGS_LOWER, 0, BAR_AUTH_PLUGIN_DATA_PART_2, null),
                Arguments.of("no_secure_connection_and_no_plugin_auth", 0, 0, new byte[0], null));
    }
}
