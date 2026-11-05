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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql;

import org.apache.shardingsphere.database.exception.core.exception.protocol.DatabaseProtocolException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.common.FirebirdCommonInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.database.protocol.payload.PacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class FirebirdSQLInfoReturnPacketTest {
    
    private static final int REQ_SELECT_COUNT = 13;
    
    private static final int REQ_INSERT_COUNT = 14;
    
    private static final int REQ_UPDATE_COUNT = 15;
    
    private static final int REQ_DELETE_COUNT = 16;
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Test
    void assertWriteRecords() {
        FirebirdSQLRecordsInfo recordsInfo = new FirebirdSQLRecordsInfo(5L, 7L, 9L);
        FirebirdSQLInfoReturnPacket packet = new FirebirdSQLInfoReturnPacket(Arrays.asList(FirebirdSQLInfoPacketType.RECORDS, FirebirdCommonInfoPacketType.END), recordsInfo, 0);
        packet.write(payload);
        InOrder inOrder = inOrder(payload);
        inOrder.verify(payload).writeInt1(FirebirdSQLInfoPacketType.RECORDS.getCode());
        inOrder.verify(payload).writeInt2LE(28);
        inOrder.verify(payload).writeInt1(REQ_SELECT_COUNT);
        inOrder.verify(payload).writeInt2LE(4);
        inOrder.verify(payload).writeInt4LE(0);
        inOrder.verify(payload).writeInt1(REQ_INSERT_COUNT);
        inOrder.verify(payload).writeInt2LE(4);
        inOrder.verify(payload).writeInt4LE(5);
        inOrder.verify(payload).writeInt1(REQ_UPDATE_COUNT);
        inOrder.verify(payload).writeInt2LE(4);
        inOrder.verify(payload).writeInt4LE(7);
        inOrder.verify(payload).writeInt1(REQ_DELETE_COUNT);
        inOrder.verify(payload).writeInt2LE(4);
        inOrder.verify(payload).writeInt4LE(9);
        inOrder.verify(payload).writeInt1(FirebirdCommonInfoPacketType.END.getCode());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("statementFlags")
    void assertWriteStatementFlags(final String name, final int flags, final List<FirebirdInfoPacketType> infoItems) {
        FirebirdSQLInfoReturnPacket packet = new FirebirdSQLInfoReturnPacket(infoItems, new FirebirdSQLRecordsInfo(0L, 0L, 0L), flags);
        packet.write((PacketPayload) payload);
        InOrder order = inOrder(payload);
        order.verify(payload).writeInt1(27);
        order.verify(payload).writeInt2LE(4);
        order.verify(payload).writeInt4LE(flags);
        order.verify(payload).writeInt1(1);
        verifyNoMoreInteractions(payload);
    }
    
    private static Stream<Arguments> statementFlags() {
        return Stream.of(
                Arguments.of("ddl_without_end", 0, Collections.singletonList(FirebirdSQLInfoPacketType.STMT_FLAGS)),
                Arguments.of("dml_without_end", 2, Collections.singletonList(FirebirdSQLInfoPacketType.STMT_FLAGS)),
                Arguments.of("select_without_end", 3, Collections.singletonList(FirebirdSQLInfoPacketType.STMT_FLAGS)),
                Arguments.of("ddl_with_end", 0, Arrays.asList(FirebirdSQLInfoPacketType.STMT_FLAGS, FirebirdCommonInfoPacketType.END)),
                Arguments.of("dml_with_end", 2, Arrays.asList(FirebirdSQLInfoPacketType.STMT_FLAGS, FirebirdCommonInfoPacketType.END)),
                Arguments.of("select_with_end", 3, Arrays.asList(FirebirdSQLInfoPacketType.STMT_FLAGS, FirebirdCommonInfoPacketType.END)),
                Arguments.of("select_with_repeated_end", 3, Arrays.asList(FirebirdSQLInfoPacketType.STMT_FLAGS, FirebirdCommonInfoPacketType.END, FirebirdCommonInfoPacketType.END)));
    }
    
    @Test
    void assertParseSQLInfoWithUnknownType() {
        FirebirdSQLInfoReturnPacket packet = new FirebirdSQLInfoReturnPacket(Collections.singletonList(FirebirdSQLInfoPacketType.STMT_TYPE), new FirebirdSQLRecordsInfo(0L, 0L, 0L), 0);
        assertThrows(DatabaseProtocolException.class, () -> packet.write(payload));
    }
}
