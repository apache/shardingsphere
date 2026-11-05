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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.database;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.common.FirebirdCommonInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

class FirebirdDatabaseInfoPacketTypeTest {
    
    @Test
    void assertValueOf() {
        assertThat(FirebirdDatabaseInfoPacketType.valueOf(FirebirdDatabaseInfoPacketType.DB_SQL_DIALECT.getCode()), is(FirebirdDatabaseInfoPacketType.DB_SQL_DIALECT));
    }
    
    @Test
    void assertIsCommon() {
        assertFalse(FirebirdDatabaseInfoPacketType.DB_SIZE_IN_PAGES.isCommon());
    }
    
    @ParameterizedTest(name = "unknown database info code {0}")
    @ValueSource(ints = {65, 128, 255})
    void assertCreatePacketWithUnknownType(final int code) {
        ByteBuf buffer = Unpooled.buffer();
        try {
            FirebirdPacketPayload payload = new FirebirdPacketPayload(buffer, StandardCharsets.UTF_8);
            payload.writeInt4(40);
            payload.writeInt4(0);
            payload.writeInt4(0);
            payload.writeBuffer(new byte[]{62, (byte) code, 101, 1});
            payload.writeInt4(1024);
            FirebirdInfoPacket actual = FirebirdDatabaseInfoPacketType.createPacket(payload);
            assertThat(actual.getInfoItems().get(0), is(FirebirdDatabaseInfoPacketType.DB_SQL_DIALECT));
            assertThat(actual.getInfoItems().get(1).getCode(), is(code));
            assertFalse(actual.getInfoItems().get(1).isCommon());
            assertThat(actual.getInfoItems().get(2), is(FirebirdDatabaseInfoPacketType.ATT_CHARSET));
            assertThat(actual.getInfoItems().get(3), is(FirebirdCommonInfoPacketType.END));
        } finally {
            buffer.release();
        }
    }
    
    @ParameterizedTest(name = "database info code {0} above signed byte range")
    @ValueSource(ints = {146, 147, 148})
    void assertCreatePacketWithUnsignedType(final int code) {
        ByteBuf buffer = Unpooled.buffer();
        try {
            FirebirdPacketPayload payload = new FirebirdPacketPayload(buffer, StandardCharsets.UTF_8);
            payload.writeInt4(40);
            payload.writeInt4(0);
            payload.writeInt4(0);
            payload.writeBuffer(new byte[]{(byte) code, 1});
            payload.writeInt4(1024);
            FirebirdInfoPacket actual = FirebirdDatabaseInfoPacketType.createPacket(payload);
            assertThat(actual.getInfoItems().get(0), is(FirebirdDatabaseInfoPacketType.valueOf(code)));
        } finally {
            buffer.release();
        }
    }
}
