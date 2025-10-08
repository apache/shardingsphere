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
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdCancelBlobCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdCloseBlobCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdCreateBlobCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdGetBlobSegmentCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdOpenBlobCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdPutBlobSegmentCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdSeekBlobCommandPacket;
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
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.info.FirebirdDatabaseInfoExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.info.FirebirdSQLInfoExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.FirebirdCancelBlobCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.FirebirdCloseBlobCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.FirebirdCreateBlobCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.FirebirdGetBlobSegmentCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.FirebirdOpenBlobCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.FirebirdPutBlobSegmentCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.FirebirdSeekBlobCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.FirebirdAllocateStatementCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.FirebirdFetchStatementCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.FirebirdFreeStatementCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.execute.FirebirdExecuteStatementCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.prepare.FirebirdPrepareStatementCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdCommitTransactionCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdRollbackTransactionCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdStartTransactionCommandExecutor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class FirebirdCommandExecutorFactoryTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Test
    void assertNewInstanceWithInfoDatabase() throws SQLException {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.INFO_DATABASE, mock(FirebirdInfoPacket.class), connectionSession), isA(FirebirdDatabaseInfoExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithTransaction() throws SQLException {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.TRANSACTION, mock(FirebirdStartTransactionPacket.class), connectionSession),
                isA(FirebirdStartTransactionCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithCreateBlob() throws SQLException {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.CREATE_BLOB, mock(FirebirdCreateBlobCommandPacket.class), connectionSession),
                isA(FirebirdCreateBlobCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithCreateBlob2() throws SQLException {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.CREATE_BLOB2, mock(FirebirdCreateBlobCommandPacket.class), connectionSession),
                isA(FirebirdCreateBlobCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithOpenBlob() throws SQLException {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.OPEN_BLOB, mock(FirebirdOpenBlobCommandPacket.class), connectionSession),
                isA(FirebirdOpenBlobCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithOpenBlob2() throws SQLException {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.OPEN_BLOB2, mock(FirebirdOpenBlobCommandPacket.class), connectionSession),
                isA(FirebirdOpenBlobCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithGetBlobSegment() throws SQLException {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.GET_SEGMENT, mock(FirebirdGetBlobSegmentCommandPacket.class), connectionSession),
                isA(FirebirdGetBlobSegmentCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithPutBlobSegment() throws SQLException {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.PUT_SEGMENT, mock(FirebirdPutBlobSegmentCommandPacket.class), connectionSession),
                isA(FirebirdPutBlobSegmentCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithCancelBlob() throws SQLException {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.CANCEL_BLOB, mock(FirebirdCancelBlobCommandPacket.class), connectionSession),
                isA(FirebirdCancelBlobCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithCloseBlob() throws SQLException {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.CLOSE_BLOB, mock(FirebirdCloseBlobCommandPacket.class), connectionSession),
                isA(FirebirdCloseBlobCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithSeekBlob() throws SQLException {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.SEEK_BLOB, mock(FirebirdSeekBlobCommandPacket.class), connectionSession),
                isA(FirebirdSeekBlobCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithAllocateStatement() throws SQLException {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.ALLOCATE_STATEMENT, mock(FirebirdAllocateStatementPacket.class), connectionSession),
                isA(FirebirdAllocateStatementCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithPrepareStatement() throws SQLException {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.PREPARE_STATEMENT, mock(FirebirdPrepareStatementPacket.class), connectionSession),
                isA(FirebirdPrepareStatementCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithExecuteStatement() throws SQLException {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.EXECUTE, mock(FirebirdExecuteStatementPacket.class), connectionSession),
                isA(FirebirdExecuteStatementCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithFetch() throws SQLException {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.FETCH, mock(FirebirdFetchStatementPacket.class), connectionSession),
                isA(FirebirdFetchStatementCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithInfoSQL() throws SQLException {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.INFO_SQL, mock(FirebirdInfoPacket.class), connectionSession), isA(FirebirdSQLInfoExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithCommit() throws SQLException {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.COMMIT, mock(FirebirdCommitTransactionPacket.class), connectionSession),
                isA(FirebirdCommitTransactionCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithRollback() throws SQLException {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.ROLLBACK, mock(FirebirdRollbackTransactionPacket.class), connectionSession),
                isA(FirebirdRollbackTransactionCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithFreeStatement() throws SQLException {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.FREE_STATEMENT, mock(FirebirdFreeStatementPacket.class), connectionSession),
                isA(FirebirdFreeStatementCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithUnsupportedCommand() throws SQLException {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.VOID, mock(CommandPacket.class), connectionSession), isA(FirebirdUnsupportedCommandExecutor.class));
    }
}
