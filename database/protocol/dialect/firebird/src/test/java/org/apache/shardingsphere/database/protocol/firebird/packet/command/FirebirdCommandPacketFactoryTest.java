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

package org.apache.shardingsphere.database.protocol.firebird.packet.command;

import org.apache.shardingsphere.database.protocol.firebird.constant.protocol.FirebirdProtocolVersion;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.admin.FirebirdUnsupportedCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdCancelBlobCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdCloseBlobCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdCreateBlobCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdGetBlobSegmentCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdOpenBlobCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdPutBlobSegmentCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdSeekBlobCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.FirebirdAllocateStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.FirebirdFetchStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.FirebirdFreeStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.FirebirdExecuteStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.FirebirdPrepareStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.transaction.FirebirdCommitTransactionPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.transaction.FirebirdRollbackTransactionPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.transaction.FirebirdStartTransactionPacket;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class FirebirdCommandPacketFactoryTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private FirebirdPacketPayload payload;
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("newInstanceArguments")
    void assertNewInstance(final String name, final FirebirdCommandPacketType commandPacketType, final Class<? extends FirebirdCommandPacket> expectedPacketType) {
        lenient().when(payload.readString()).thenReturn("");
        assertThat(FirebirdCommandPacketFactory.newInstance(commandPacketType, payload, FirebirdProtocolVersion.PROTOCOL_VERSION13), isA(expectedPacketType));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("expectedLengthArguments")
    void assertGetExpectedLength(final String name, final FirebirdCommandPacketType commandPacketType, final int expectedLength) {
        assertThat(FirebirdCommandPacketFactory.getExpectedLength(commandPacketType, payload, FirebirdProtocolVersion.PROTOCOL_VERSION13), is(expectedLength));
    }
    
    private static Stream<Arguments> newInstanceArguments() {
        return Stream.of(
                Arguments.of("info_database", FirebirdCommandPacketType.INFO_DATABASE, FirebirdInfoPacket.class),
                Arguments.of("info_blob", FirebirdCommandPacketType.INFO_BLOB, FirebirdInfoPacket.class),
                Arguments.of("transaction", FirebirdCommandPacketType.TRANSACTION, FirebirdStartTransactionPacket.class),
                Arguments.of("create_blob", FirebirdCommandPacketType.CREATE_BLOB, FirebirdCreateBlobCommandPacket.class),
                Arguments.of("create_blob2", FirebirdCommandPacketType.CREATE_BLOB2, FirebirdCreateBlobCommandPacket.class),
                Arguments.of("open_blob", FirebirdCommandPacketType.OPEN_BLOB, FirebirdOpenBlobCommandPacket.class),
                Arguments.of("open_blob2", FirebirdCommandPacketType.OPEN_BLOB2, FirebirdOpenBlobCommandPacket.class),
                Arguments.of("get_segment", FirebirdCommandPacketType.GET_SEGMENT, FirebirdGetBlobSegmentCommandPacket.class),
                Arguments.of("put_segment", FirebirdCommandPacketType.PUT_SEGMENT, FirebirdPutBlobSegmentCommandPacket.class),
                Arguments.of("cancel_blob", FirebirdCommandPacketType.CANCEL_BLOB, FirebirdCancelBlobCommandPacket.class),
                Arguments.of("close_blob", FirebirdCommandPacketType.CLOSE_BLOB, FirebirdCloseBlobCommandPacket.class),
                Arguments.of("seek_blob", FirebirdCommandPacketType.SEEK_BLOB, FirebirdSeekBlobCommandPacket.class),
                Arguments.of("allocate_statement", FirebirdCommandPacketType.ALLOCATE_STATEMENT, FirebirdAllocateStatementPacket.class),
                Arguments.of("prepare_statement", FirebirdCommandPacketType.PREPARE_STATEMENT, FirebirdPrepareStatementPacket.class),
                Arguments.of("execute", FirebirdCommandPacketType.EXECUTE, FirebirdExecuteStatementPacket.class),
                Arguments.of("execute2", FirebirdCommandPacketType.EXECUTE2, FirebirdExecuteStatementPacket.class),
                Arguments.of("fetch", FirebirdCommandPacketType.FETCH, FirebirdFetchStatementPacket.class),
                Arguments.of("info_sql", FirebirdCommandPacketType.INFO_SQL, FirebirdInfoPacket.class),
                Arguments.of("commit", FirebirdCommandPacketType.COMMIT, FirebirdCommitTransactionPacket.class),
                Arguments.of("rollback", FirebirdCommandPacketType.ROLLBACK, FirebirdRollbackTransactionPacket.class),
                Arguments.of("free_statement", FirebirdCommandPacketType.FREE_STATEMENT, FirebirdFreeStatementPacket.class),
                Arguments.of("void_as_default", FirebirdCommandPacketType.VOID, FirebirdUnsupportedCommandPacket.class));
    }
    
    private static Stream<Arguments> expectedLengthArguments() {
        return Stream.of(
                Arguments.of("info_database", FirebirdCommandPacketType.INFO_DATABASE, 16),
                Arguments.of("info_sql", FirebirdCommandPacketType.INFO_SQL, 16),
                Arguments.of("info_blob", FirebirdCommandPacketType.INFO_BLOB, 16),
                Arguments.of("transaction", FirebirdCommandPacketType.TRANSACTION, 8),
                Arguments.of("create_blob", FirebirdCommandPacketType.CREATE_BLOB, 16),
                Arguments.of("create_blob2", FirebirdCommandPacketType.CREATE_BLOB2, 16),
                Arguments.of("open_blob", FirebirdCommandPacketType.OPEN_BLOB, 16),
                Arguments.of("open_blob2", FirebirdCommandPacketType.OPEN_BLOB2, 16),
                Arguments.of("get_segment", FirebirdCommandPacketType.GET_SEGMENT, 12),
                Arguments.of("put_segment", FirebirdCommandPacketType.PUT_SEGMENT, 12),
                Arguments.of("cancel_blob", FirebirdCommandPacketType.CANCEL_BLOB, 8),
                Arguments.of("close_blob", FirebirdCommandPacketType.CLOSE_BLOB, 8),
                Arguments.of("seek_blob", FirebirdCommandPacketType.SEEK_BLOB, 16),
                Arguments.of("allocate_statement", FirebirdCommandPacketType.ALLOCATE_STATEMENT, 8),
                Arguments.of("prepare_statement", FirebirdCommandPacketType.PREPARE_STATEMENT, 20),
                Arguments.of("execute", FirebirdCommandPacketType.EXECUTE, 0),
                Arguments.of("execute2", FirebirdCommandPacketType.EXECUTE2, 0),
                Arguments.of("fetch", FirebirdCommandPacketType.FETCH, 16),
                Arguments.of("commit", FirebirdCommandPacketType.COMMIT, 8),
                Arguments.of("rollback", FirebirdCommandPacketType.ROLLBACK, 8),
                Arguments.of("free_statement", FirebirdCommandPacketType.FREE_STATEMENT, 12),
                Arguments.of("void_as_default", FirebirdCommandPacketType.VOID, 0));
    }
}
