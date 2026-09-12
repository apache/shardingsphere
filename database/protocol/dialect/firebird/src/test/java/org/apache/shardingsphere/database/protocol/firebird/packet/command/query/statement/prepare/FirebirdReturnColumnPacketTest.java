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

import org.apache.shardingsphere.database.exception.core.exception.protocol.DatabaseProtocolException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdReturnColumnPacketTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Test
    void assertWrite() {
        FirebirdReturnColumnPacket packet = createPacket(Arrays.asList(
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
                FirebirdSQLInfoPacketType.DESCRIBE_END), FirebirdBinaryColumnType.VARYING, 99, null);
        when(payload.getCharset()).thenReturn(StandardCharsets.UTF_8);
        packet.write(payload);
        verify(payload).writeInt1(FirebirdSQLInfoPacketType.SQLDA_SEQ.getCode());
        verify(payload).writeInt1(FirebirdSQLInfoPacketType.DESCRIBE_END.getCode());
        verify(payload).writeInt4LE(99);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertWriteLengthArguments")
    void assertWriteLength(final String name, final FirebirdBinaryColumnType columnType, final int expectedLength) {
        FirebirdReturnColumnPacket packet = createPacket(Collections.singletonList(FirebirdSQLInfoPacketType.LENGTH), columnType, 99, null);
        packet.write(payload);
        verify(payload).writeInt4LE(expectedLength);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertWriteSubTypeArguments")
    void assertWriteSubType(final String name, final Integer blobSubType, final int expectedSubType) {
        createPacket(Collections.singletonList(FirebirdSQLInfoPacketType.SUB_TYPE), FirebirdBinaryColumnType.BLOB, null, blobSubType).write(payload);
        verify(payload).writeInt4LE(expectedSubType);
    }
    
    @Test
    void assertWriteWithUnsupportedRequestedItem() {
        FirebirdReturnColumnPacket packet = createPacket(Collections.singletonList(FirebirdSQLInfoPacketType.SELECT), FirebirdBinaryColumnType.LONG, null, null);
        try (MockedConstruction<DatabaseProtocolException> ignored = mockConstruction(DatabaseProtocolException.class)) {
            assertThrows(DatabaseProtocolException.class, () -> packet.write(payload));
        }
    }
    
    private FirebirdReturnColumnPacket createPacket(final Collection<FirebirdSQLInfoPacketType> requestedItems, final FirebirdBinaryColumnType dataType,
                                                    final Integer columnLength, final Integer blobSubType) {
        return new FirebirdReturnColumnPacket(requestedItems, 1, "tbl", "col", dataType, "t", "c", "o", columnLength, blobSubType);
    }
    
    private static Stream<Arguments> assertWriteLengthArguments() {
        return Stream.of(
                Arguments.of("length_varying", FirebirdBinaryColumnType.VARYING, 99),
                Arguments.of("length_legacy_varying", FirebirdBinaryColumnType.LEGACY_VARYING, 99),
                Arguments.of("length_text", FirebirdBinaryColumnType.TEXT, 99),
                Arguments.of("length_legacy_text", FirebirdBinaryColumnType.LEGACY_TEXT, 99),
                Arguments.of("length_long", FirebirdBinaryColumnType.LONG, FirebirdBinaryColumnType.LONG.getLength()));
    }
    
    private static Stream<Arguments> assertWriteSubTypeArguments() {
        return Stream.of(
                Arguments.of("blob_with_subtype", 7, 7),
                Arguments.of("blob_without_subtype", null, FirebirdBinaryColumnType.BLOB.getSubtype()),
                Arguments.of("long_default_subtype", null, FirebirdBinaryColumnType.LONG.getSubtype()));
    }
}
