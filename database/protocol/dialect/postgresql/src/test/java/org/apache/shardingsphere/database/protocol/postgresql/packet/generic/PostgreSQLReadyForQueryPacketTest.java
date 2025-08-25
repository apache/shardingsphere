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

package org.apache.shardingsphere.database.protocol.postgresql.packet.generic;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.database.protocol.postgresql.packet.ByteBufTestUtils;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class PostgreSQLReadyForQueryPacketTest {
    
    @Test
    void assertReadWriteWithInTransaction() {
        ByteBuf byteBuf = ByteBufTestUtils.createByteBuf(6);
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        PostgreSQLReadyForQueryPacket packet = PostgreSQLReadyForQueryPacket.IN_TRANSACTION;
        packet.write(payload);
        assertThat(byteBuf.writerIndex(), is(6));
        assertThat(byteBuf.getByte(5), is((byte) 'T'));
    }
    
    @Test
    void assertReadWriteWithNotInTransaction() {
        ByteBuf byteBuf = ByteBufTestUtils.createByteBuf(6);
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        PostgreSQLReadyForQueryPacket packet = PostgreSQLReadyForQueryPacket.NOT_IN_TRANSACTION;
        packet.write(payload);
        assertThat(byteBuf.writerIndex(), is(6));
        assertThat(byteBuf.getByte(5), is((byte) 'I'));
    }
    
    @Test
    void assertReadWriteWithTransactionFailed() {
        ByteBuf byteBuf = ByteBufTestUtils.createByteBuf(6);
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        PostgreSQLReadyForQueryPacket packet = PostgreSQLReadyForQueryPacket.TRANSACTION_FAILED;
        packet.write(payload);
        assertThat(byteBuf.writerIndex(), is(6));
        assertThat(byteBuf.getByte(5), is((byte) 'E'));
    }
}
