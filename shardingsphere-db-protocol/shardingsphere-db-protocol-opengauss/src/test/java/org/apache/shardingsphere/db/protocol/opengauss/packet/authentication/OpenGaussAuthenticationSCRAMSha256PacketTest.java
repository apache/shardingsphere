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

import org.apache.shardingsphere.db.protocol.postgresql.packet.identifier.PostgreSQLMessagePacketType;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class OpenGaussAuthenticationSCRAMSha256PacketTest {
    
    private static final byte[] RANDOM_64_CODE = new byte[64];
    
    private static final byte[] TOKEN = new byte[8];
    
    private static final int SERVER_ITERATION = 2048;
    
    private final OpenGaussAuthenticationSCRAMSha256Packet packet = new OpenGaussAuthenticationSCRAMSha256Packet(RANDOM_64_CODE, TOKEN, SERVER_ITERATION);
    
    @Test
    public void assertWrite() {
        PostgreSQLPacketPayload payload = mock(PostgreSQLPacketPayload.class);
        packet.write(payload);
        verify(payload).writeInt4(10);
        verify(payload).writeInt4(2);
        verify(payload).writeBytes(RANDOM_64_CODE);
        verify(payload).writeBytes(TOKEN);
        verify(payload).writeInt4(SERVER_ITERATION);
    }
    
    @Test
    public void assertIdentifierTag() {
        assertThat(packet.getIdentifier(), is(PostgreSQLMessagePacketType.AUTHENTICATION_REQUEST));
    }
}
