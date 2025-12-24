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
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class OpenGaussAuthenticationSCRAMSha256PacketTest {
    
    private final OpenGaussAuthenticationHexData authHexData = new OpenGaussAuthenticationHexData();
    
    @Test
    void assertWriteProtocol300Packet() {
        PostgreSQLPacketPayload payload = mock(PostgreSQLPacketPayload.class);
        OpenGaussAuthenticationSCRAMSha256Packet packet = new OpenGaussAuthenticationSCRAMSha256Packet(OpenGaussProtocolVersion.PROTOCOL_350.getVersion() - 1, 2048, authHexData, "");
        packet.write(payload);
        verify(payload).writeInt4(10);
        verify(payload).writeInt4(2);
        verify(payload).writeBytes(authHexData.getSalt().getBytes());
        verify(payload).writeBytes(authHexData.getNonce().getBytes());
        verify(payload, times(3)).writeBytes(any());
    }
    
    @Test
    void assertWriteProtocol350Packet() {
        PostgreSQLPacketPayload payload = mock(PostgreSQLPacketPayload.class);
        OpenGaussAuthenticationSCRAMSha256Packet packet = new OpenGaussAuthenticationSCRAMSha256Packet(OpenGaussProtocolVersion.PROTOCOL_350.getVersion(), 2048, authHexData, "");
        packet.write(payload);
        verify(payload).writeInt4(10);
        verify(payload).writeInt4(2);
        verify(payload).writeBytes(authHexData.getSalt().getBytes());
        verify(payload).writeBytes(authHexData.getNonce().getBytes());
        verify(payload, times(2)).writeBytes(any());
    }
    
    @Test
    void assertWriteProtocol351Packet() {
        PostgreSQLPacketPayload payload = mock(PostgreSQLPacketPayload.class);
        OpenGaussAuthenticationSCRAMSha256Packet packet = new OpenGaussAuthenticationSCRAMSha256Packet(OpenGaussProtocolVersion.PROTOCOL_351.getVersion(), 10000, authHexData, "");
        packet.write(payload);
        verify(payload).writeInt4(10);
        verify(payload).writeInt4(2);
        verify(payload).writeBytes(authHexData.getSalt().getBytes());
        verify(payload).writeBytes(authHexData.getNonce().getBytes());
        verify(payload).writeInt4(10000);
        verify(payload, times(2)).writeBytes(any());
    }
}
