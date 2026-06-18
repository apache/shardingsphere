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

package org.apache.shardingsphere.database.protocol.opengauss.packet.authentication;

import org.apache.shardingsphere.database.protocol.opengauss.constant.OpenGaussProtocolVersion;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.identifier.PostgreSQLMessagePacketType;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class OpenGaussAuthenticationSCRAMSha256PacketTest {
    
    private final OpenGaussAuthenticationHexData authHexData = new OpenGaussAuthenticationHexData();
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("writeCases")
    void assertWrite(final String name, final int version, final int serverIteration, final int expectedWriteBytesCount, final int expectedServerIterationWriteCount) {
        PostgreSQLPacketPayload payload = mock(PostgreSQLPacketPayload.class);
        DatabasePacket packet = new OpenGaussAuthenticationSCRAMSha256Packet(version, serverIteration, authHexData, "");
        packet.write(payload);
        verify(payload).writeInt4(10);
        verify(payload).writeInt4(2);
        verify(payload).writeBytes(authHexData.getSalt().getBytes());
        verify(payload).writeBytes(authHexData.getNonce().getBytes());
        verify(payload, times(expectedWriteBytesCount)).writeBytes(any());
        verify(payload, times(expectedServerIterationWriteCount)).writeInt4(serverIteration);
    }
    
    @Test
    void assertGetIdentifier() {
        OpenGaussAuthenticationSCRAMSha256Packet packet = new OpenGaussAuthenticationSCRAMSha256Packet(OpenGaussProtocolVersion.PROTOCOL_350.getVersion(), 2048, authHexData, "");
        assertThat(packet.getIdentifier(), CoreMatchers.is(PostgreSQLMessagePacketType.AUTHENTICATION_REQUEST));
    }
    
    private static Stream<Arguments> writeCases() {
        return Stream.of(
                Arguments.of("versionBelow350WritesServerSignature", OpenGaussProtocolVersion.PROTOCOL_350.getVersion() - 1, 2048, 3, 0),
                Arguments.of("version350SkipsSignatureAndIteration", OpenGaussProtocolVersion.PROTOCOL_350.getVersion(), 2048, 2, 0),
                Arguments.of("version351WritesIterationOnly", OpenGaussProtocolVersion.PROTOCOL_351.getVersion(), 10000, 2, 1));
    }
}
