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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute;

import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.firebird.constant.protocol.FirebirdProtocolVersion;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketFactory;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;

class FirebirdExecuteImmediatePacketTest {
    
    @ParameterizedTest(name = "{0}")
    @CsvSource({"7, 7", "0, 0", "32767, 32767", "32768, 32768", "-32768, 32768", "-536, 65000", "-1, 65535"})
    void assertParse(final int wireHandle, final int expectedHandle) {
        FirebirdPacketPayload payload = createPayload(wireHandle, "SET TRANSACTION");
        try {
            FirebirdExecuteImmediatePacket actual = new FirebirdExecuteImmediatePacket(payload);
            assertThat(actual.getTransactionId(), is(expectedHandle));
            assertThat(actual.getSQL(), is("SET TRANSACTION"));
            assertThat(payload.getByteBuf().readableBytes(), is(0));
        } finally {
            payload.getByteBuf().release();
        }
    }
    
    private FirebirdPacketPayload createPayload(final int transactionId, final String sql) {
        FirebirdPacketPayload result = new FirebirdPacketPayload(Unpooled.buffer(), StandardCharsets.UTF_8);
        result.writeInt4(FirebirdCommandPacketType.EXEC_IMMEDIATE.getValue());
        result.writeInt4(transactionId);
        result.writeInt4(1);
        result.writeInt4(3);
        result.writeString(sql);
        result.writeInt4(0);
        result.writeInt4(0);
        return result;
    }
    
    @Test
    void assertFramingWithFollowingPacket() {
        FirebirdPacketPayload payload = createPayload(0, "SET TRANSACTION");
        int expectedLength = payload.getByteBuf().readableBytes();
        payload.getByteBuf().writeInt(FirebirdCommandPacketType.TRANSACTION.getValue()).writeInt(1).writeInt(0);
        int actual = FirebirdCommandPacketFactory.getExpectedLength(FirebirdCommandPacketType.EXEC_IMMEDIATE, payload, FirebirdProtocolVersion.PROTOCOL_VERSION19, 1);
        assertThat(actual, is(expectedLength));
        assertThat(payload.getByteBuf().readerIndex(), is(0));
        assertThat(FirebirdCommandPacketFactory.newInstance(FirebirdCommandPacketType.EXEC_IMMEDIATE, payload, FirebirdProtocolVersion.PROTOCOL_VERSION19, 1),
                isA(FirebirdExecuteImmediatePacket.class));
        assertThat(payload.readInt4(), is(FirebirdCommandPacketType.TRANSACTION.getValue()));
    }
}
