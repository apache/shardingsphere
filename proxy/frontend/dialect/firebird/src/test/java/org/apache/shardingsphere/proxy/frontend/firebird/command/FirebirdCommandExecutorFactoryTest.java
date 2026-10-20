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

package org.apache.shardingsphere.proxy.frontend.firebird.command;

import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchCancelCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchCreateCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchExecuteCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchMessageCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchReleaseCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdBatchBlobSegmentsCommandPacket;
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
import org.apache.shardingsphere.database.protocol.packet.command.CommandPacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.admin.FirebirdUnsupportedCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.batch.FirebirdBatchCancelCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.batch.FirebirdBatchCreateCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.batch.FirebirdBatchExecuteCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.batch.FirebirdBatchMessageCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.batch.FirebirdBatchReleaseCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.FirebirdBatchBlobSegmentsCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.FirebirdCancelBlobCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.FirebirdCloseBlobCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.FirebirdCreateBlobCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.FirebirdGetBlobSegmentCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.FirebirdOpenBlobCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.FirebirdPutBlobSegmentCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.FirebirdSeekBlobCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.info.FirebirdBlobInfoExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.info.FirebirdDatabaseInfoExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.info.FirebirdSQLInfoExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.allocate.FirebirdAllocateStatementCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.execute.FirebirdExecuteStatementCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.fetch.FirebirdFetchStatementCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.free.FirebirdFreeStatementCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.prepare.FirebirdPrepareStatementCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdCommitTransactionCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdRollbackTransactionCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdStartTransactionCommandExecutor;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class FirebirdCommandExecutorFactoryTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("newInstanceArguments")
    void assertNewInstance(final String name, final FirebirdCommandPacketType commandPacketType, final Class<? extends CommandPacket> commandPacketClass,
                           final Class<? extends CommandExecutor> expectedExecutorClass) {
        assertThat(FirebirdCommandExecutorFactory.newInstance(commandPacketType, mock(commandPacketClass), connectionSession), isA(expectedExecutorClass));
    }
    
    private static Stream<Arguments> newInstanceArguments() {
        return Stream.of(
                Arguments.of("info_database", FirebirdCommandPacketType.INFO_DATABASE, FirebirdInfoPacket.class, FirebirdDatabaseInfoExecutor.class),
                Arguments.of("info_blob", FirebirdCommandPacketType.INFO_BLOB, FirebirdInfoPacket.class, FirebirdBlobInfoExecutor.class),
                Arguments.of("transaction", FirebirdCommandPacketType.TRANSACTION, FirebirdStartTransactionPacket.class, FirebirdStartTransactionCommandExecutor.class),
                Arguments.of("create_blob", FirebirdCommandPacketType.CREATE_BLOB, FirebirdCreateBlobCommandPacket.class, FirebirdCreateBlobCommandExecutor.class),
                Arguments.of("create_blob2", FirebirdCommandPacketType.CREATE_BLOB2, FirebirdCreateBlobCommandPacket.class, FirebirdCreateBlobCommandExecutor.class),
                Arguments.of("open_blob", FirebirdCommandPacketType.OPEN_BLOB, FirebirdOpenBlobCommandPacket.class, FirebirdOpenBlobCommandExecutor.class),
                Arguments.of("open_blob2", FirebirdCommandPacketType.OPEN_BLOB2, FirebirdOpenBlobCommandPacket.class, FirebirdOpenBlobCommandExecutor.class),
                Arguments.of("get_blob_segment", FirebirdCommandPacketType.GET_SEGMENT, FirebirdGetBlobSegmentCommandPacket.class, FirebirdGetBlobSegmentCommandExecutor.class),
                Arguments.of("put_blob_segment", FirebirdCommandPacketType.PUT_SEGMENT, FirebirdPutBlobSegmentCommandPacket.class, FirebirdPutBlobSegmentCommandExecutor.class),
                Arguments.of("batch_segments_with_blob", FirebirdCommandPacketType.BATCH_SEGMENTS, FirebirdBatchBlobSegmentsCommandPacket.class, FirebirdBatchBlobSegmentsCommandExecutor.class),
                Arguments.of("seek_blob", FirebirdCommandPacketType.SEEK_BLOB, FirebirdSeekBlobCommandPacket.class, FirebirdSeekBlobCommandExecutor.class),
                Arguments.of("cancel_blob", FirebirdCommandPacketType.CANCEL_BLOB, FirebirdCancelBlobCommandPacket.class, FirebirdCancelBlobCommandExecutor.class),
                Arguments.of("close_blob", FirebirdCommandPacketType.CLOSE_BLOB, FirebirdCloseBlobCommandPacket.class, FirebirdCloseBlobCommandExecutor.class),
                Arguments.of("allocate_statement", FirebirdCommandPacketType.ALLOCATE_STATEMENT, FirebirdAllocateStatementPacket.class, FirebirdAllocateStatementCommandExecutor.class),
                Arguments.of("prepare_statement", FirebirdCommandPacketType.PREPARE_STATEMENT, FirebirdPrepareStatementPacket.class, FirebirdPrepareStatementCommandExecutor.class),
                Arguments.of("execute_statement", FirebirdCommandPacketType.EXECUTE, FirebirdExecuteStatementPacket.class, FirebirdExecuteStatementCommandExecutor.class),
                Arguments.of("fetch", FirebirdCommandPacketType.FETCH, FirebirdFetchStatementPacket.class, FirebirdFetchStatementCommandExecutor.class),
                Arguments.of("info_s_q_l", FirebirdCommandPacketType.INFO_SQL, FirebirdInfoPacket.class, FirebirdSQLInfoExecutor.class),
                Arguments.of("commit", FirebirdCommandPacketType.COMMIT, FirebirdCommitTransactionPacket.class, FirebirdCommitTransactionCommandExecutor.class),
                Arguments.of("rollback", FirebirdCommandPacketType.ROLLBACK, FirebirdRollbackTransactionPacket.class, FirebirdRollbackTransactionCommandExecutor.class),
                Arguments.of("batch_create", FirebirdCommandPacketType.BATCH_CREATE, FirebirdBatchCreateCommandPacket.class, FirebirdBatchCreateCommandExecutor.class),
                Arguments.of("batch_message", FirebirdCommandPacketType.BATCH_MSG, FirebirdBatchMessageCommandPacket.class, FirebirdBatchMessageCommandExecutor.class),
                Arguments.of("batch_execute", FirebirdCommandPacketType.BATCH_EXEC, FirebirdBatchExecuteCommandPacket.class, FirebirdBatchExecuteCommandExecutor.class),
                Arguments.of("batch_release", FirebirdCommandPacketType.BATCH_RLS, FirebirdBatchReleaseCommandPacket.class, FirebirdBatchReleaseCommandExecutor.class),
                Arguments.of("batch_cancel", FirebirdCommandPacketType.BATCH_CANCEL, FirebirdBatchCancelCommandPacket.class, FirebirdBatchCancelCommandExecutor.class),
                Arguments.of("free_statement", FirebirdCommandPacketType.FREE_STATEMENT, FirebirdFreeStatementPacket.class, FirebirdFreeStatementCommandExecutor.class),
                Arguments.of("unsupported_command", FirebirdCommandPacketType.VOID, CommandPacket.class, FirebirdUnsupportedCommandExecutor.class));
    }
}
