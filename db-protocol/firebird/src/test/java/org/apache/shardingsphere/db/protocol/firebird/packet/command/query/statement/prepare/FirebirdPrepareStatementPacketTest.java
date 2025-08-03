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

package org.apache.shardingsphere.db.protocol.firebird.packet.command.query.statement.prepare;

import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoPacketType;
import org.apache.shardingsphere.db.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirebirdPrepareStatementPacketTest {
    
    @Test
    void assertParse() {
        FirebirdPacketPayload payload = new FirebirdPacketPayload(Unpooled.buffer(), StandardCharsets.UTF_8);
        payload.writeInt4(0);
        payload.writeInt4(1);
        payload.writeInt4(2);
        payload.writeInt4(3);
        payload.writeString("select 1");
        payload.writeBuffer(new byte[]{(byte) FirebirdSQLInfoPacketType.STMT_TYPE.getCode(), (byte) FirebirdSQLInfoPacketType.DESCRIBE_VARS.getCode()});
        payload.writeInt4(10);
        payload.getByteBuf().readerIndex(0);
        FirebirdPrepareStatementPacket packet = new FirebirdPrepareStatementPacket(new FirebirdPacketPayload(payload.getByteBuf(), StandardCharsets.UTF_8));
        assertThat(packet.getTransactionId(), is(1));
        assertThat(packet.getStatementId(), is(2));
        assertThat(packet.getSqlDialect(), is(3));
        assertThat(packet.getSQL(), is("select 1"));
        assertTrue(packet.isValidStatementHandle());
        assertTrue(packet.nextItem());
        assertThat(packet.getCurrentItem(), is(FirebirdSQLInfoPacketType.STMT_TYPE));
        assertTrue(packet.nextItem());
        assertThat(packet.getCurrentItem(), is(FirebirdSQLInfoPacketType.DESCRIBE_VARS));
        assertFalse(packet.nextItem());
    }
    
    @Test
    void assertIsInvalidStatementHandle() {
        FirebirdPacketPayload payload = new FirebirdPacketPayload(Unpooled.buffer(), StandardCharsets.UTF_8);
        payload.writeInt4(0);
        payload.writeInt4(1);
        payload.writeInt4(0xFFFF);
        payload.writeInt4(3);
        payload.writeString("select 1");
        payload.writeBuffer(new byte[0]);
        payload.writeInt4(10);
        payload.getByteBuf().readerIndex(0);
        FirebirdPrepareStatementPacket packet = new FirebirdPrepareStatementPacket(new FirebirdPacketPayload(payload.getByteBuf(), StandardCharsets.UTF_8));
        assertFalse(packet.isValidStatementHandle());
    }
    
    @Test
    void assertLength() {
        FirebirdPacketPayload payload = new FirebirdPacketPayload(Unpooled.buffer(), StandardCharsets.UTF_8);
        payload.writeInt4(0);
        payload.writeInt4(1);
        payload.writeInt4(2);
        payload.writeInt4(3);
        payload.writeString("select 1");
        payload.writeBuffer(new byte[]{(byte) FirebirdSQLInfoPacketType.STMT_TYPE.getCode(), (byte) FirebirdSQLInfoPacketType.DESCRIBE_VARS.getCode()});
        payload.writeInt4(10);
        payload.getByteBuf().readerIndex(0);
        assertThat(FirebirdPrepareStatementPacket.getLength(payload), is(40));
    }
}
