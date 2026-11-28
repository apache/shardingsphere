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

package org.apache.shardingsphere.database.protocol.firebird.packet.generic;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.database.protocol.payload.PacketPayload;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class FirebirdSliceResponsePacketTest {
    
    @Test
    void assertWrite() {
        ByteBuf buffer = Unpooled.buffer();
        try {
            FirebirdSliceResponsePacket packet = new FirebirdSliceResponsePacket(2, new byte[]{-1, -1, -1, -2});
            packet.write((PacketPayload) new FirebirdPacketPayload(buffer, StandardCharsets.UTF_8));
            assertThat(ByteBufUtil.getBytes(buffer), is(new byte[]{0, 0, 0, 60, 0, 0, 0, 2, 0, 0, 0, 2, -1, -1, -1, -2}));
        } finally {
            buffer.release();
        }
    }
}
