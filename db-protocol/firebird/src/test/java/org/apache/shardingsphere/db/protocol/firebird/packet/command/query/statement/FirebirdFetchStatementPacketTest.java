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

package org.apache.shardingsphere.db.protocol.firebird.packet.command.query.statement;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.db.protocol.firebird.payload.FirebirdPacketPayload;
import org.firebirdsql.gds.BlrConstants;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class FirebirdFetchStatementPacketTest {
    
    @Test
    void assertParse() {
        FirebirdPacketPayload payload = new FirebirdPacketPayload(Unpooled.buffer(), StandardCharsets.UTF_8);
        payload.writeInt4(0);
        payload.writeInt4(3);
        ByteBuf blr = Unpooled.buffer();
        blr.writeZero(4);
        blr.writeByte(9);
        blr.writeByte(0);
        blr.writeByte(BlrConstants.blr_long);
        blr.writeZero(3);
        blr.writeByte(BlrConstants.blr_short);
        blr.writeZero(3);
        blr.writeByte(BlrConstants.blr_end);
        payload.writeBuffer(blr);
        payload.writeInt4(7);
        payload.writeInt4(10);
        payload.getByteBuf().readerIndex(0);
        FirebirdFetchStatementPacket packet = new FirebirdFetchStatementPacket(new FirebirdPacketPayload(payload.getByteBuf(), StandardCharsets.UTF_8));
        assertThat(packet.getStatementId(), is(3));
        assertThat(packet.getMessage(), is(7));
        assertThat(packet.getFetchSize(), is(10));
        assertThat(packet.getParameterTypes().size(), is(2));
        assertThat(packet.getParameterTypes().get(0), is(FirebirdBinaryColumnType.LONG));
        assertThat(packet.getParameterTypes().get(1), is(FirebirdBinaryColumnType.SHORT));
    }
    
    @Test
    void assertLength() {
        FirebirdPacketPayload payload = new FirebirdPacketPayload(Unpooled.buffer(), StandardCharsets.UTF_8);
        payload.writeInt4(0);
        payload.writeInt4(1);
        ByteBuf blr = Unpooled.buffer();
        blr.writeZero(4);
        blr.writeByte(9);
        blr.writeByte(0);
        blr.writeByte(BlrConstants.blr_long);
        blr.writeZero(3);
        blr.writeByte(BlrConstants.blr_short);
        blr.writeZero(3);
        blr.writeByte(BlrConstants.blr_end);
        payload.writeBuffer(blr);
        payload.writeInt4(0);
        payload.writeInt4(0);
        payload.getByteBuf().readerIndex(0);
        assertThat(FirebirdFetchStatementPacket.getLength(payload), is(36));
    }
}
