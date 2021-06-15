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

package org.apache.shardingsphere.db.protocol.postgresql.packet.command.admin;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.db.protocol.postgresql.packet.identifier.PostgreSQLMessagePacketType;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class PostgreSQLUnsupportedCommandPacketTest {
    
    @Mock
    private ByteBuf byteBuf;
    
    @Test
    public void assertWrite() {
        PostgreSQLUnsupportedCommandPacket rowPacket = new PostgreSQLUnsupportedCommandPacket(PostgreSQLMessagePacketType.AUTHENTICATION_REQUEST);
        rowPacket.write(new PostgreSQLPacketPayload(byteBuf));
        assertThat(byteBuf.writerIndex(), is(0));
    }
    
    @Test
    public void assertGetMessageType() {
        PostgreSQLUnsupportedCommandPacket rowPacket = new PostgreSQLUnsupportedCommandPacket(PostgreSQLMessagePacketType.AUTHENTICATION_REQUEST);
        assertThat(rowPacket.getIdentifier(), is(PostgreSQLMessagePacketType.AUTHENTICATION_REQUEST));
    }
}
