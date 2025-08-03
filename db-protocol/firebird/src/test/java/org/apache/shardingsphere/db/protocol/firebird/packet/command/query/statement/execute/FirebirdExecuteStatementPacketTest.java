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

package org.apache.shardingsphere.db.protocol.firebird.packet.command.query.statement.execute;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.db.protocol.firebird.constant.protocol.FirebirdProtocolVersion;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.db.protocol.firebird.payload.FirebirdPacketPayload;
import org.firebirdsql.gds.BlrConstants;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class FirebirdExecuteStatementPacketTest {

    @Test
    void assertParse() {
        FirebirdPacketPayload payload = new FirebirdPacketPayload(Unpooled.buffer(), StandardCharsets.UTF_8);
        payload.writeInt4(FirebirdCommandPacketType.EXECUTE.getValue());
        payload.writeInt4(1);
        payload.writeInt4(2);
        ByteBuf blr = Unpooled.buffer();
        blr.writeZero(4);
        blr.writeByte(5);
        blr.writeByte(0);
        blr.writeByte(BlrConstants.blr_long);
        blr.writeZero(3);
        blr.writeByte(BlrConstants.blr_end);
        payload.writeBuffer(blr);
        payload.writeInt4(0);
        payload.writeInt4(1);
        payload.writeInt1(0);
        payload.writeBytes(new byte[3]);
        payload.writeInt4(123);
        payload.getByteBuf().readerIndex(0);
        FirebirdExecuteStatementPacket packet = new FirebirdExecuteStatementPacket(new FirebirdPacketPayload(payload.getByteBuf(), StandardCharsets.UTF_8),
                FirebirdProtocolVersion.PROTOCOL_VERSION13);
        assertThat(packet.getType(), is(FirebirdCommandPacketType.EXECUTE));
        assertThat(packet.getStatementId(), is(1));
        assertThat(packet.getTransactionId(), is(2));
        assertThat(packet.getParameterTypes(), is(Collections.singletonList(FirebirdBinaryColumnType.LONG)));
        assertThat(packet.getParameterValues(), is(Collections.singletonList(123)));
    }

    @Test
    void assertParseStoredProcedure() {
        FirebirdPacketPayload payload = new FirebirdPacketPayload(Unpooled.buffer(), StandardCharsets.UTF_8);
        payload.writeInt4(FirebirdCommandPacketType.EXECUTE2.getValue());
        payload.writeInt4(1);
        payload.writeInt4(2);
        ByteBuf blr = Unpooled.buffer();
        blr.writeZero(4);
        blr.writeByte(5);
        blr.writeByte(0);
        blr.writeByte(BlrConstants.blr_long);
        blr.writeZero(3);
        blr.writeByte(BlrConstants.blr_end);
        payload.writeBuffer(blr);
        payload.writeInt4(0);
        payload.writeInt4(1);
        payload.writeInt1(0);
        payload.writeBytes(new byte[3]);
        payload.writeInt4(123);
        ByteBuf returnBlr = Unpooled.buffer();
        returnBlr.writeZero(4);
        returnBlr.writeByte(5);
        returnBlr.writeByte(0);
        returnBlr.writeByte(BlrConstants.blr_long);
        returnBlr.writeZero(3);
        returnBlr.writeByte(BlrConstants.blr_end);
        payload.writeBuffer(returnBlr);
        payload.writeInt4(9);
        payload.writeInt4(30);
        payload.writeInt4(1);
        payload.writeInt4(1024);
        payload.getByteBuf().readerIndex(0);
        FirebirdExecuteStatementPacket packet = new FirebirdExecuteStatementPacket(new FirebirdPacketPayload(payload.getByteBuf(), StandardCharsets.UTF_8),
                FirebirdProtocolVersion.PROTOCOL_VERSION19);
        assertThat(packet.isStoredProcedure(), is(true));
        assertThat(packet.getReturnColumns(), is(Collections.singletonList(FirebirdBinaryColumnType.LONG)));
        assertThat(packet.getOutputMessageNumber(), is(9));
        assertThat(packet.getParameterValues(), is(Collections.singletonList(123)));
        assertThat(packet.getStatementTimeout(), is(30L));
        assertThat(packet.getCursorFlags(), is(1L));
        assertThat(packet.getMaxBlobSize(), is(1024L));
    }

    @Test
    void assertLength() {
        FirebirdPacketPayload payload = new FirebirdPacketPayload(Unpooled.buffer(), StandardCharsets.UTF_8);
        payload.writeInt4(FirebirdCommandPacketType.EXECUTE.getValue());
        payload.writeInt4(1);
        payload.writeInt4(2);
        ByteBuf blr = Unpooled.buffer();
        blr.writeZero(4);
        blr.writeByte(5);
        blr.writeByte(0);
        blr.writeByte(BlrConstants.blr_long);
        blr.writeZero(3);
        blr.writeByte(BlrConstants.blr_end);
        payload.writeBuffer(blr);
        payload.writeInt4(0);
        payload.writeInt4(1);
        payload.writeInt1(0);
        payload.writeBytes(new byte[3]);
        payload.writeInt4(123);
        int expectedLength = payload.getByteBuf().writerIndex();
        payload.getByteBuf().readerIndex(0);
        assertThat(FirebirdExecuteStatementPacket.getLength(payload, FirebirdProtocolVersion.PROTOCOL_VERSION13), is(expectedLength));
    }
}