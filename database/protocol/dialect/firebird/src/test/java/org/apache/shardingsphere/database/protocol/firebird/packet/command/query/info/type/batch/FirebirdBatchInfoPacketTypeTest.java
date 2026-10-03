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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.batch;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.common.FirebirdCommonInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class FirebirdBatchInfoPacketTypeTest {
    
    @Test
    void assertCreatePacket() {
        ByteBuf packet = Unpooled.buffer();
        packet.writeInt(FirebirdCommandPacketType.INFO_BATCH.getValue());
        packet.writeInt(7);
        packet.writeInt(0);
        packet.writeInt(3);
        packet.writeBytes(new byte[]{13, 10, 14, 0});
        packet.writeInt(64);
        FirebirdInfoPacket actual = FirebirdBatchInfoPacketType.createPacket(new FirebirdPacketPayload(packet, StandardCharsets.UTF_8));
        assertThat(actual.getHandle(), is(7));
        assertThat(actual.getInfoItems(), is(Arrays.asList(FirebirdBatchInfoPacketType.BLOB_ALIGNMENT, FirebirdBatchInfoPacketType.BUFFER_BYTES_SIZE, FirebirdBatchInfoPacketType.BLOB_HEADER)));
        assertThat(actual.getMaxLength(), is(64));
    }
    
    @Test
    void assertCreatePacketWithUnknownItems() {
        ByteBuf packet = Unpooled.buffer();
        packet.writeInt(FirebirdCommandPacketType.INFO_BATCH.getValue());
        packet.writeInt(7);
        packet.writeInt(0);
        packet.writeInt(3);
        packet.writeBytes(new byte[]{20, 126, 1, 0});
        packet.writeInt(64);
        FirebirdInfoPacket actual = FirebirdBatchInfoPacketType.createPacket(new FirebirdPacketPayload(packet, StandardCharsets.UTF_8));
        assertThat(actual.getInfoItems(), is(Arrays.asList(FirebirdCommonInfoPacketType.ERROR, FirebirdCommonInfoPacketType.LENGTH, FirebirdCommonInfoPacketType.END)));
    }
}
