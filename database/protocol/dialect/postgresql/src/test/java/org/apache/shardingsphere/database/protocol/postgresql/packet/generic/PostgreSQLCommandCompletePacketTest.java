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

package org.apache.shardingsphere.database.protocol.postgresql.packet.generic;

import org.apache.shardingsphere.database.protocol.postgresql.packet.ByteBufTestUtils;
import org.apache.shardingsphere.database.protocol.payload.PacketPayload;
import org.apache.shardingsphere.database.protocol.postgresql.packet.identifier.PostgreSQLMessagePacketType;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class PostgreSQLCommandCompletePacketTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("writeCases")
    void assertWrite(final String name, final String sqlCommand, final long rowCount, final String expectedString) {
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(ByteBufTestUtils.createByteBuf(expectedString.length() + 1), StandardCharsets.ISO_8859_1);
        new PostgreSQLCommandCompletePacket(sqlCommand, rowCount).write((PacketPayload) payload);
        assertThat(payload.readStringNul(), is(expectedString));
    }
    
    @Test
    void assertGetIdentifier() {
        assertThat(new PostgreSQLCommandCompletePacket("SELECT", 1L).getIdentifier(), is(PostgreSQLMessagePacketType.COMMAND_COMPLETE));
    }
    
    private static Stream<Arguments> writeCases() {
        return Stream.of(
                Arguments.of("insert command with oid and row count", "INSERT", 1L, "INSERT 0 1"),
                Arguments.of("select command with row count", "SELECT", 2L, "SELECT 2"),
                Arguments.of("create command without row count", "CREATE", 3L, "CREATE"));
    }
}
