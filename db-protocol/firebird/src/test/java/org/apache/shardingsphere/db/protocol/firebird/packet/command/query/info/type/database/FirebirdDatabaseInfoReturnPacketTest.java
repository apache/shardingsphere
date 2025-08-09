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

package org.apache.shardingsphere.db.protocol.firebird.packet.command.query.info.type.database;

import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.db.protocol.firebird.constant.FirebirdArchType;
import org.apache.shardingsphere.db.protocol.firebird.exception.FirebirdProtocolException;
import org.apache.shardingsphere.db.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FirebirdDatabaseInfoReturnPacketTest {
    
    @Test
    void assertWriteDialect() {
        FirebirdDatabaseInfoReturnPacket packet = new FirebirdDatabaseInfoReturnPacket(Collections.singletonList(FirebirdDatabaseInfoPacketType.DB_SQL_DIALECT));
        FirebirdPacketPayload payload = new FirebirdPacketPayload(Unpooled.buffer(), StandardCharsets.UTF_8);
        packet.write(payload);
        payload.getByteBuf().readerIndex(0);
        FirebirdPacketPayload result = new FirebirdPacketPayload(payload.getByteBuf(), StandardCharsets.UTF_8);
        assertThat(result.readInt1(), is(FirebirdDatabaseInfoPacketType.DB_SQL_DIALECT.getCode()));
        assertThat(result.readInt2LE(), is(1));
        assertThat(result.readInt1(), is(3));
    }
    
    @Test
    void assertWriteOdsVersion() {
        FirebirdDatabaseInfoReturnPacket packet = new FirebirdDatabaseInfoReturnPacket(Collections.singletonList(FirebirdDatabaseInfoPacketType.ODS_VERSION));
        FirebirdPacketPayload payload = new FirebirdPacketPayload(Unpooled.buffer(), StandardCharsets.UTF_8);
        packet.write(payload);
        payload.getByteBuf().readerIndex(0);
        FirebirdPacketPayload result = new FirebirdPacketPayload(payload.getByteBuf(), StandardCharsets.UTF_8);
        assertThat(result.readInt1(), is(FirebirdDatabaseInfoPacketType.ODS_VERSION.getCode()));
        assertThat(result.readInt2LE(), is(4));
    }
    
    @Test
    void assertWriteOdsMinorVersion() {
        FirebirdDatabaseInfoReturnPacket packet = new FirebirdDatabaseInfoReturnPacket(Collections.singletonList(FirebirdDatabaseInfoPacketType.ODS_MINOR_VERSION));
        FirebirdPacketPayload payload = new FirebirdPacketPayload(Unpooled.buffer(), StandardCharsets.UTF_8);
        packet.write(payload);
        payload.getByteBuf().readerIndex(0);
        FirebirdPacketPayload result = new FirebirdPacketPayload(payload.getByteBuf(), StandardCharsets.UTF_8);
        assertThat(result.readInt1(), is(FirebirdDatabaseInfoPacketType.ODS_MINOR_VERSION.getCode()));
        assertThat(result.readInt2LE(), is(4));
    }
    
    @Test
    void assertWriteFirebirdVersion() {
        FirebirdDatabaseInfoReturnPacket packet = new FirebirdDatabaseInfoReturnPacket(Collections.singletonList(FirebirdDatabaseInfoPacketType.FIREBIRD_VERSION));
        FirebirdPacketPayload payload = new FirebirdPacketPayload(Unpooled.buffer(), StandardCharsets.UTF_8);
        packet.write(payload);
        String serverName = String.format("Firebird %d.%d (ShardingSphere-Proxy)", 5, 0);
        String fbVersion = String.format("%s-%s%d.%d.%d.%d %s", FirebirdArchType.ARCHITECTURE.getIdentifier(), "V", 5, 0, 0, 0, serverName);
        payload.getByteBuf().readerIndex(0);
        FirebirdPacketPayload result = new FirebirdPacketPayload(payload.getByteBuf(), StandardCharsets.UTF_8);
        assertThat(result.readInt1(), is(FirebirdDatabaseInfoPacketType.FIREBIRD_VERSION.getCode()));
        assertThat(result.readInt2LE(), is(fbVersion.length() + 2));
        assertThat(result.readInt1(), is(1));
        assertThat(result.readInt1(), is(fbVersion.length()));
        assertThat(result.readBytes(fbVersion.length()).toString(StandardCharsets.UTF_8), is(fbVersion));
    }
    
    @Test
    void assertParseDatabaseInfoWithUnknownType() {
        FirebirdDatabaseInfoReturnPacket packet = new FirebirdDatabaseInfoReturnPacket(Collections.singletonList(FirebirdDatabaseInfoPacketType.DB_ID));
        assertThrows(FirebirdProtocolException.class, () -> packet.write(new FirebirdPacketPayload(Unpooled.buffer(), StandardCharsets.UTF_8)));
    }
}
