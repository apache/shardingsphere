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

import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdArchType;
import org.apache.shardingsphere.database.protocol.firebird.exception.FirebirdProtocolException;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
class FirebirdDatabaseInfoReturnPacketTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Test
    void assertWriteDialect() {
        FirebirdDatabaseInfoReturnPacket packet = new FirebirdDatabaseInfoReturnPacket(Collections.singletonList(FirebirdDatabaseInfoPacketType.DB_SQL_DIALECT));
        packet.write(payload);
        InOrder order = inOrder(payload);
        order.verify(payload).writeInt1(FirebirdDatabaseInfoPacketType.DB_SQL_DIALECT.getCode());
        order.verify(payload).writeInt2LE(1);
        order.verify(payload).writeInt1(3);
    }
    
    @Test
    void assertWriteOdsVersion() {
        FirebirdDatabaseInfoReturnPacket packet = new FirebirdDatabaseInfoReturnPacket(Collections.singletonList(FirebirdDatabaseInfoPacketType.ODS_VERSION));
        packet.write(payload);
        InOrder order = inOrder(payload);
        order.verify(payload).writeInt1(FirebirdDatabaseInfoPacketType.ODS_VERSION.getCode());
        order.verify(payload).writeInt2LE(4);
        order.verify(payload).writeInt4LE(5);
    }
    
    @Test
    void assertWriteOdsMinorVersion() {
        FirebirdDatabaseInfoReturnPacket packet = new FirebirdDatabaseInfoReturnPacket(Collections.singletonList(FirebirdDatabaseInfoPacketType.ODS_MINOR_VERSION));
        packet.write(payload);
        InOrder order = inOrder(payload);
        order.verify(payload).writeInt1(FirebirdDatabaseInfoPacketType.ODS_MINOR_VERSION.getCode());
        order.verify(payload).writeInt2LE(4);
        order.verify(payload).writeInt4LE(0);
    }
    
    @Test
    void assertWriteFirebirdVersion() {
        FirebirdDatabaseInfoReturnPacket packet = new FirebirdDatabaseInfoReturnPacket(Collections.singletonList(FirebirdDatabaseInfoPacketType.FIREBIRD_VERSION));
        packet.write(payload);
        String serverName = String.format("Firebird %d.%d (ShardingSphere-Proxy)", 5, 0);
        String fbVersion = String.format("%s-%s%d.%d.%d.%d %s", FirebirdArchType.ARCHITECTURE.getIdentifier(), "V", 5, 0, 0, 0, serverName);
        InOrder order = inOrder(payload);
        order.verify(payload).writeInt1(FirebirdDatabaseInfoPacketType.FIREBIRD_VERSION.getCode());
        order.verify(payload).writeInt2LE(fbVersion.length() + 2);
        order.verify(payload).writeInt1(1);
        order.verify(payload).writeInt1(fbVersion.length());
        order.verify(payload).writeBytes(fbVersion.getBytes(StandardCharsets.UTF_8));
    }
    
    @Test
    void assertParseDatabaseInfoWithUnknownType() {
        FirebirdDatabaseInfoReturnPacket packet = new FirebirdDatabaseInfoReturnPacket(Collections.singletonList(FirebirdDatabaseInfoPacketType.DB_ID));
        assertThrows(FirebirdProtocolException.class, () -> packet.write(payload));
    }
}
