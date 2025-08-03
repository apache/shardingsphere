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

import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.db.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class FirebirdFreeStatementPacketTest {
    
    @Test
    void assertParse() {
        FirebirdPacketPayload payload = new FirebirdPacketPayload(Unpooled.buffer(), StandardCharsets.UTF_8);
        payload.writeInt4(0);
        payload.writeInt4(5);
        payload.writeInt4(FirebirdFreeStatementPacket.DROP);
        payload.getByteBuf().readerIndex(0);
        FirebirdFreeStatementPacket packet = new FirebirdFreeStatementPacket(new FirebirdPacketPayload(payload.getByteBuf(), StandardCharsets.UTF_8));
        assertThat(packet.getStatementId(), is(5));
        assertThat(packet.getOption(), is(FirebirdFreeStatementPacket.DROP));
    }
    
    @Test
    void assertLength() {
        assertThat(FirebirdFreeStatementPacket.getLength(), is(12));
    }
}
