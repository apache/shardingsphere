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

package org.apache.shardingsphere.db.protocol.firebird.packet.command.query.info;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.shardingsphere.db.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoPacketType;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.info.type.common.FirebirdCommonInfoPacketType;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class FirebirdInfoPacketTest {
    
    private FirebirdPacketPayload payload;
    
    @BeforeEach
    void setUp() {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeZero(4);
        byteBuf.writeInt(1);
        byteBuf.writeInt(2);
        byteBuf.writeInt(2);
        byteBuf.writeByte(FirebirdSQLInfoPacketType.RECORDS.getCode());
        byteBuf.writeByte(FirebirdCommonInfoPacketType.END.getCode());
        byteBuf.writeZero(2);
        byteBuf.writeInt(100);
        payload = new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8);
        byteBuf.readerIndex(0);
    }
    
    @Test
    void assertGetLength() {
        assertThat(FirebirdInfoPacket.getLength(payload), is(24));
    }
    
    @Test
    void assertNewInstance() {
        FirebirdInfoPacket actual = new FirebirdInfoPacket(payload, FirebirdSQLInfoPacketType::valueOf);
        assertThat(actual.getHandle(), is(1));
        assertThat(actual.getIncarnation(), is(2));
        assertThat(actual.getMaxLength(), is(100));
        List<FirebirdInfoPacketType> infoItems = actual.getInfoItems();
        assertThat(infoItems.get(0), is(FirebirdSQLInfoPacketType.RECORDS));
        assertThat(infoItems.get(1), is(FirebirdCommonInfoPacketType.END));
    }
}
