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

package org.apache.shardingsphere.db.protocol.postgresql.packet.generic;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.db.protocol.postgresql.packet.ByteBufTestUtils;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLSSLNegativePacket;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class PostgreSQLSSLNegativePacketTest {
    
    @Test
    public void assertReadWrite() {
        ByteBuf byteBuf = ByteBufTestUtils.createByteBuf(1);
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(byteBuf);
        PostgreSQLSSLNegativePacket packet = new PostgreSQLSSLNegativePacket();
        assertThat(packet.getMessageType(), is('\0'));
        packet.write(payload);
        assertThat(byteBuf.writerIndex(), is(1));
        assertThat(payload.readInt1(), is((int) 'N'));
    }
    
}
