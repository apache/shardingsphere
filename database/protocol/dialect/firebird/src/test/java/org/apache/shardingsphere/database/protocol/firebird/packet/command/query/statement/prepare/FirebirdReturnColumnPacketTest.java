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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare;

import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdReturnColumnPacketTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Test
    void assertWrite() {
        ShardingSphereColumn column = new ShardingSphereColumn("col", Types.VARCHAR, false, false, false, true, false, true);
        ShardingSphereTable table = new ShardingSphereTable("tbl", Collections.singleton(column), Collections.emptyList(), Collections.emptyList());
        FirebirdReturnColumnPacket packet = new FirebirdReturnColumnPacket(Arrays.asList(
                FirebirdSQLInfoPacketType.SQLDA_SEQ,
                FirebirdSQLInfoPacketType.TYPE,
                FirebirdSQLInfoPacketType.SUB_TYPE,
                FirebirdSQLInfoPacketType.SCALE,
                FirebirdSQLInfoPacketType.LENGTH,
                FirebirdSQLInfoPacketType.FIELD,
                FirebirdSQLInfoPacketType.ALIAS,
                FirebirdSQLInfoPacketType.RELATION,
                FirebirdSQLInfoPacketType.RELATION_ALIAS,
                FirebirdSQLInfoPacketType.OWNER,
                FirebirdSQLInfoPacketType.DESCRIBE_END), 1, table, column, "t", "c", "o", 99, false, null);
        when(payload.getCharset()).thenReturn(java.nio.charset.StandardCharsets.UTF_8);
        packet.write(payload);
        verify(payload).writeInt1(FirebirdSQLInfoPacketType.SQLDA_SEQ.getCode());
        verify(payload).writeInt1(FirebirdSQLInfoPacketType.DESCRIBE_END.getCode());
        verify(payload).writeInt4LE(99);
    }
    
    @Test
    void assertWriteUsesDefaultColumnLength() {
        ShardingSphereColumn column = new ShardingSphereColumn("col", Types.INTEGER, false, false, false, true, false, true);
        ShardingSphereTable table = new ShardingSphereTable("tbl", Collections.singleton(column), Collections.emptyList(), Collections.emptyList());
        FirebirdReturnColumnPacket packet = new FirebirdReturnColumnPacket(Collections.singletonList(FirebirdSQLInfoPacketType.LENGTH),
                1, table, column, "t", "c", "o", null, false, null);
        packet.write(payload);
        verify(payload).writeInt4LE(4);
    }

    @Test
    void assertWriteUsesBlobSubtype() {
        ShardingSphereColumn column = new ShardingSphereColumn("col", Types.BLOB, false, false, false, true, false, true);
        ShardingSphereTable table = new ShardingSphereTable("tbl", Collections.singleton(column), Collections.emptyList(), Collections.emptyList());
        FirebirdReturnColumnPacket packet = new FirebirdReturnColumnPacket(Collections.singletonList(FirebirdSQLInfoPacketType.SUB_TYPE),
                1, table, column, "t", "c", "o", null, true, 7);
        packet.write(payload);
        verify(payload).writeInt4LE(7);
    }
}
