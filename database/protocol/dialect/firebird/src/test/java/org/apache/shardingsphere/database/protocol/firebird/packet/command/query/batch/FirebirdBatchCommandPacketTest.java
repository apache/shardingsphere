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

import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.firebird.exception.FirebirdProtocolException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FirebirdBatchCommandPacketTest {
    
    @Test
    void assertBatchExecuteCommandPacket() {
        FirebirdPacketPayload payload = new FirebirdPacketPayload(Unpooled.buffer().writeZero(4).writeInt(21).writeInt(31), StandardCharsets.UTF_8);
        FirebirdBatchExecuteCommandPacket packet = new FirebirdBatchExecuteCommandPacket(payload);
        assertThat(packet.getStatementHandle(), is(21));
        assertThat(packet.getTransactionHandle(), is(31));
        assertThat(FirebirdBatchExecuteCommandPacket.getLength(), is(12));
    }
    
    @Test
    void assertBatchCancelCommandPacket() {
        FirebirdPacketPayload payload = new FirebirdPacketPayload(Unpooled.buffer().writeZero(4).writeInt(9), StandardCharsets.UTF_8);
        FirebirdBatchCancelCommandPacket packet = new FirebirdBatchCancelCommandPacket(payload);
        assertThat(packet.getStatementHandle(), is(9));
        assertThat(FirebirdBatchCancelCommandPacket.getLength(), is(8));
    }
    
    @Test
    void assertBatchSendMessageGetLengthWhenHeaderIncomplete() {
        FirebirdPacketPayload payload = new FirebirdPacketPayload(Unpooled.buffer().writeZero(8), StandardCharsets.UTF_8);
        assertThat(FirebirdBatchSendMessageCommandPacket.getLength(payload, 1), is(-1));
    }
    
    @Test
    void assertBatchSendMessageGetLengthWithoutHeaderContext() {
        FirebirdPacketPayload payload = new FirebirdPacketPayload(Unpooled.buffer().writeZero(12), StandardCharsets.UTF_8);
        assertThrows(FirebirdProtocolException.class, () -> FirebirdBatchSendMessageCommandPacket.getLength(payload, 66));
    }
    
    @Test
    void assertBatchSendMessageGetLengthWithoutBatchStatement() {
        FirebirdBatchSendMessageCommandPacket.registerBatchColumnTypes(77, Collections.singletonList(FirebirdBinaryColumnType.VARYING));
        FirebirdPacketPayload payload = new FirebirdPacketPayload(Unpooled.buffer().writeZero(4).writeInt(1).writeInt(1), StandardCharsets.UTF_8);
        assertThrows(FirebirdProtocolException.class, () -> FirebirdBatchSendMessageCommandPacket.getLength(payload, 77));
        FirebirdBatchSendMessageCommandPacket.unregisterConnection(77);
    }
}
