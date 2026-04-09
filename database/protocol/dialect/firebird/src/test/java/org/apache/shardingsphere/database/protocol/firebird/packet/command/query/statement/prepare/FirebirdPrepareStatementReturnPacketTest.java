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

import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.common.FirebirdCommonInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoReturnValue;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.database.protocol.payload.PacketPayload;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdPrepareStatementReturnPacketTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Test
    void assertWriteWithDescribeColumns() {
        FirebirdPrepareStatementReturnPacket packet = new FirebirdPrepareStatementReturnPacket();
        packet.setType(FirebirdSQLInfoReturnValue.SELECT);
        FirebirdReturnColumnPacket columnPacket = createColumnPacket(Collections.singleton(FirebirdSQLInfoPacketType.ALIAS), "tbl_alias", "col_alias", "owner");
        packet.getDescribeSelect().add(columnPacket);
        packet.getDescribeBind().add(columnPacket);
        PacketPayload actualPayload = payload;
        when(payload.getCharset()).thenReturn(StandardCharsets.UTF_8);
        packet.write(actualPayload);
        verify(payload).writeInt1(FirebirdSQLInfoPacketType.STMT_TYPE.getCode());
        verify(payload, times(2)).writeInt1(FirebirdSQLInfoPacketType.ALIAS.getCode());
        verify(payload, times(2)).getCharset();
        verify(payload, times(2)).writeBytes(argThat(each -> Arrays.equals("col_alias".getBytes(StandardCharsets.UTF_8), each)));
        verify(payload).writeInt1(FirebirdCommonInfoPacketType.END.getCode());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertWriteWithNullStringMetadataArguments")
    void assertWriteWithNullStringMetadata(final String name, final FirebirdSQLInfoPacketType requestedItem,
                                           final String tableAlias, final String columnAlias, final String owner) {
        FirebirdPrepareStatementReturnPacket packet = new FirebirdPrepareStatementReturnPacket();
        packet.setType(FirebirdSQLInfoReturnValue.SELECT);
        packet.getDescribeSelect().add(createColumnPacket(Collections.singleton(requestedItem), tableAlias, columnAlias, owner));
        PacketPayload actualPayload = payload;
        packet.write(actualPayload);
        verify(payload).writeInt1(requestedItem.getCode());
        verify(payload).writeInt2LE(0);
        verify(payload).writeBytes(argThat(each -> 0 == each.length));
        verify(payload, never()).getCharset();
    }
    
    private FirebirdReturnColumnPacket createColumnPacket(final Collection<FirebirdSQLInfoPacketType> requestedItems,
                                                          final String tableAlias, final String columnAlias, final String owner) {
        ShardingSphereColumn column = new ShardingSphereColumn("col", Types.INTEGER, false, false, false, true, false, true);
        ShardingSphereTable table = new ShardingSphereTable("tbl", Collections.singleton(column), Collections.emptyList(), Collections.emptyList());
        return new FirebirdReturnColumnPacket(requestedItems, 1, table, column, tableAlias, columnAlias, owner, null, false, null);
    }
    
    private static Stream<Arguments> assertWriteWithNullStringMetadataArguments() {
        return Stream.of(
                Arguments.of("null_alias", FirebirdSQLInfoPacketType.ALIAS, "tbl_alias", null, "owner"),
                Arguments.of("null_relation_alias", FirebirdSQLInfoPacketType.RELATION_ALIAS, null, "col_alias", "owner"),
                Arguments.of("null_owner", FirebirdSQLInfoPacketType.OWNER, "tbl_alias", "col_alias", null));
    }
}
