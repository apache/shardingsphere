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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class FirebirdBatchSetBpbCommandPacketTest {
    
    @Test
    void assertConstructor() {
        FirebirdBatchSetBpbCommandPacket actual = new FirebirdBatchSetBpbCommandPacket(new FirebirdPacketPayload(createPacket(), StandardCharsets.UTF_8));
        assertThat(actual.getStatementHandle(), is(7));
        assertThat(ByteBufUtil.getBytes(actual.getBpb()), is(new byte[]{1, 3, 1, 0, 0}));
    }
    
    @Test
    void assertGetLength() {
        assertThat(FirebirdBatchSetBpbCommandPacket.getLength(new FirebirdPacketPayload(createPacket(), StandardCharsets.UTF_8)), is(20));
    }
    
    private ByteBuf createPacket() {
        ByteBuf result = Unpooled.buffer();
        result.writeInt(FirebirdCommandPacketType.BATCH_SET_BPB.getValue());
        result.writeInt(7);
        result.writeInt(5);
        result.writeBytes(new byte[]{1, 3, 1, 0, 0, 0, 0, 0});
        return result;
    }
}
