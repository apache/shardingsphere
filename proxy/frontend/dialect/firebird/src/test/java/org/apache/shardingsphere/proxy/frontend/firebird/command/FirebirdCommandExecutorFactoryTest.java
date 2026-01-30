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
import org.apache.shardingsphere.proxy.frontend.firebird.command.admin.FirebirdUnsupportedCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.executors.FirebirdCancelBlobCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.executors.FirebirdCloseBlobCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.executors.FirebirdCreateBlobCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.executors.FirebirdGetBlobSegmentCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.executors.FirebirdOpenBlobCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.executors.FirebirdPutBlobSegmentCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.executors.FirebirdSeekBlobCommandExecutor;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class FirebirdCommandExecutorFactoryTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Test
    void assertNewInstanceWithInfoDatabase() {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.INFO_DATABASE, mock(FirebirdInfoPacket.class), connectionSession), isA(FirebirdDatabaseInfoExecutor.class));
    }

    @Test
    void assertNewInstanceWithInfoBlob() {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.INFO_BLOB, mock(FirebirdInfoPacket.class), connectionSession), isA(FirebirdBlobInfoExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithTransaction() {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.TRANSACTION, mock(FirebirdStartTransactionPacket.class), connectionSession),
                isA(FirebirdStartTransactionCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithCreateBlob() {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.CREATE_BLOB, mock(FirebirdCreateBlobCommandPacket.class), connectionSession),
                isA(FirebirdCreateBlobCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithCreateBlob2() {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.CREATE_BLOB2, mock(FirebirdCreateBlobCommandPacket.class), connectionSession),
                isA(FirebirdCreateBlobCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithOpenBlob() {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.OPEN_BLOB, mock(FirebirdOpenBlobCommandPacket.class), connectionSession),
                isA(FirebirdOpenBlobCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithOpenBlob2() {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.OPEN_BLOB2, mock(FirebirdOpenBlobCommandPacket.class), connectionSession),
                isA(FirebirdOpenBlobCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithGetBlobSegment() {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.GET_SEGMENT, mock(FirebirdGetBlobSegmentCommandPacket.class), connectionSession),
                isA(FirebirdGetBlobSegmentCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithPutBlobSegment() {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.PUT_SEGMENT, mock(FirebirdPutBlobSegmentCommandPacket.class), connectionSession),
                isA(FirebirdPutBlobSegmentCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithCancelBlob() {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.CANCEL_BLOB, mock(FirebirdCancelBlobCommandPacket.class), connectionSession),
                isA(FirebirdCancelBlobCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithCloseBlob() {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.CLOSE_BLOB, mock(FirebirdCloseBlobCommandPacket.class), connectionSession),
                isA(FirebirdCloseBlobCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithSeekBlob() {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.SEEK_BLOB, mock(FirebirdSeekBlobCommandPacket.class), connectionSession),
                isA(FirebirdSeekBlobCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithAllocateStatement() {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.ALLOCATE_STATEMENT, mock(FirebirdAllocateStatementPacket.class), connectionSession),
                isA(FirebirdAllocateStatementCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithPrepareStatement() {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.PREPARE_STATEMENT, mock(FirebirdPrepareStatementPacket.class), connectionSession),
                isA(FirebirdPrepareStatementCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithExecuteStatement() {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.EXECUTE, mock(FirebirdExecuteStatementPacket.class), connectionSession),
                isA(FirebirdExecuteStatementCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithFetch() {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.FETCH, mock(FirebirdFetchStatementPacket.class), connectionSession),
                isA(FirebirdFetchStatementCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithInfoSQL() {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.INFO_SQL, mock(FirebirdInfoPacket.class), connectionSession), isA(FirebirdSQLInfoExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithCommit() {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.COMMIT, mock(FirebirdCommitTransactionPacket.class), connectionSession),
                isA(FirebirdCommitTransactionCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithRollback() {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.ROLLBACK, mock(FirebirdRollbackTransactionPacket.class), connectionSession),
                isA(FirebirdRollbackTransactionCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithFreeStatement() {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.FREE_STATEMENT, mock(FirebirdFreeStatementPacket.class), connectionSession),
                isA(FirebirdFreeStatementCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithUnsupportedCommand() {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.VOID, mock(CommandPacket.class), connectionSession), isA(FirebirdUnsupportedCommandExecutor.class));
    }
}
