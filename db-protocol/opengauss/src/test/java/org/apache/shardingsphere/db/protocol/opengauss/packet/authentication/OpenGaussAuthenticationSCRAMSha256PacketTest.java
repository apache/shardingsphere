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

package org.apache.shardingsphere.db.protocol.opengauss.packet.authentication;

import org.apache.shardingsphere.db.protocol.opengauss.constant.OpenGaussProtocolVersion;
import org.apache.shardingsphere.db.protocol.postgresql.packet.identifier.PostgreSQLMessagePacketType;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class OpenGaussAuthenticationSCRAMSha256PacketTest {
    
    private static final byte[] RANDOM_64_CODE = new byte[64];
    
    private static final byte[] TOKEN = new byte[8];
    
    private static final byte[] SERVER_SIGNATURE = new byte[64];
    
    @Test
    public void assertWriteProtocol300Packet() {
        PostgreSQLPacketPayload payload = mock(PostgreSQLPacketPayload.class);
        OpenGaussAuthenticationSCRAMSha256Packet packet = new OpenGaussAuthenticationSCRAMSha256Packet(
                OpenGaussProtocolVersion.PROTOCOL_350.getVersion(), RANDOM_64_CODE, TOKEN, SERVER_SIGNATURE, 2048);
        packet.write(payload);
        verify(payload).writeInt4(10);
        verify(payload).writeInt4(2);
        verify(payload).writeBytes(RANDOM_64_CODE);
        verify(payload).writeBytes(TOKEN);
        verify(payload).writeBytes(SERVER_SIGNATURE);
    }
    
    @Test
    public void assertWriteProtocol350Packet() {
        PostgreSQLPacketPayload payload = mock(PostgreSQLPacketPayload.class);
        OpenGaussAuthenticationSCRAMSha256Packet packet = new OpenGaussAuthenticationSCRAMSha256Packet(
                OpenGaussProtocolVersion.PROTOCOL_350.getVersion(), RANDOM_64_CODE, TOKEN, SERVER_SIGNATURE, 2048);
        packet.write(payload);
        verify(payload).writeInt4(10);
        verify(payload).writeInt4(2);
        verify(payload).writeBytes(RANDOM_64_CODE);
        verify(payload).writeBytes(TOKEN);
    }
    
    @Test
    public void assertWriteProtocol351Packet() {
        PostgreSQLPacketPayload payload = mock(PostgreSQLPacketPayload.class);
        OpenGaussAuthenticationSCRAMSha256Packet packet = new OpenGaussAuthenticationSCRAMSha256Packet(
                OpenGaussProtocolVersion.PROTOCOL_351.getVersion(), RANDOM_64_CODE, TOKEN, SERVER_SIGNATURE, 10000);
        packet.write(payload);
        verify(payload).writeInt4(10);
        verify(payload).writeInt4(2);
        verify(payload).writeBytes(RANDOM_64_CODE);
        verify(payload).writeBytes(TOKEN);
        verify(payload).writeInt4(10000);
    }
    
    @Test
    public void assertIdentifierTag() {
        OpenGaussAuthenticationSCRAMSha256Packet packet = new OpenGaussAuthenticationSCRAMSha256Packet(
                OpenGaussProtocolVersion.PROTOCOL_351.getVersion(), RANDOM_64_CODE, TOKEN, SERVER_SIGNATURE, 10000);
        assertThat(packet.getIdentifier(), is(PostgreSQLMessagePacketType.AUTHENTICATION_REQUEST));
    }
}
