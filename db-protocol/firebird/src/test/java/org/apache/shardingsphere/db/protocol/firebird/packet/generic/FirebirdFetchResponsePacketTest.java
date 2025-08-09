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

package org.apache.shardingsphere.db.protocol.firebird.packet.generic;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.db.protocol.binary.BinaryCell;
import org.apache.shardingsphere.db.protocol.binary.BinaryRow;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.db.protocol.firebird.payload.FirebirdPacketPayload;
import org.firebirdsql.gds.ISCConstants;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class FirebirdFetchResponsePacketTest {
    
    @Test
    void assertWriteWithRow() {
        ByteBuf byteBuf = Unpooled.buffer();
        FirebirdPacketPayload payload = new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8);
        BinaryRow row = new BinaryRow(Collections.singleton(new BinaryCell(FirebirdBinaryColumnType.LONG, 123)));
        FirebirdFetchResponsePacket packet = new FirebirdFetchResponsePacket(row);
        packet.write(payload);
        assertThat(byteBuf.readInt(), is(FirebirdCommandPacketType.FETCH_RESPONSE.getValue()));
        assertThat(byteBuf.readInt(), is(ISCConstants.FETCH_OK));
        assertThat(byteBuf.readInt(), is(1));
        byte[] nullBits = new byte[4];
        byteBuf.readBytes(nullBits);
        assertThat(nullBits, is(new byte[4]));
        assertThat(byteBuf.readInt(), is(123));
    }
    
    @Test
    void assertWriteWithoutRow() {
        ByteBuf byteBuf = Unpooled.buffer();
        FirebirdPacketPayload payload = new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8);
        FirebirdFetchResponsePacket packet = new FirebirdFetchResponsePacket();
        packet.write(payload);
        assertThat(byteBuf.readInt(), is(FirebirdCommandPacketType.FETCH_RESPONSE.getValue()));
        assertThat(byteBuf.readInt(), is(ISCConstants.FETCH_NO_MORE_ROWS));
        assertThat(byteBuf.readInt(), is(0));
        assertThat(byteBuf.readableBytes(), is(0));
    }
}
