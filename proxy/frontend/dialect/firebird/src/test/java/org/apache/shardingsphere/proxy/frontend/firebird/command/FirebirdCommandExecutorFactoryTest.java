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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class FirebirdCommandExecutorFactoryTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Test
    void assertNewInstanceWithInfoDatabase() throws SQLException {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.INFO_DATABASE, mock(FirebirdInfoPacket.class), connectionSession),
                instanceOf(FirebirdDatabaseInfoExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithTransaction() throws SQLException {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.TRANSACTION, mock(FirebirdStartTransactionPacket.class), connectionSession),
                instanceOf(FirebirdStartTransactionCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithAllocateStatement() throws SQLException {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.ALLOCATE_STATEMENT, mock(FirebirdAllocateStatementPacket.class), connectionSession),
                instanceOf(FirebirdAllocateStatementCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithPrepareStatement() throws SQLException {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.PREPARE_STATEMENT, mock(FirebirdPrepareStatementPacket.class), connectionSession),
                instanceOf(FirebirdPrepareStatementCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithExecuteStatement() throws SQLException {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.EXECUTE, mock(FirebirdExecuteStatementPacket.class), connectionSession),
                instanceOf(FirebirdExecuteStatementCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithFetch() throws SQLException {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.FETCH, mock(FirebirdFetchStatementPacket.class), connectionSession),
                instanceOf(FirebirdFetchStatementCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithInfoSQL() throws SQLException {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.INFO_SQL, mock(FirebirdInfoPacket.class), connectionSession),
                instanceOf(FirebirdSQLInfoExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithCommit() throws SQLException {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.COMMIT, mock(FirebirdCommitTransactionPacket.class), connectionSession),
                instanceOf(FirebirdCommitTransactionCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithRollback() throws SQLException {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.ROLLBACK, mock(FirebirdRollbackTransactionPacket.class), connectionSession),
                instanceOf(FirebirdRollbackTransactionCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithFreeStatement() throws SQLException {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.FREE_STATEMENT, mock(FirebirdFreeStatementPacket.class), connectionSession),
                instanceOf(FirebirdFreeStatementCommandExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithUnsupportedCommand() throws SQLException {
        assertThat(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.VOID, mock(CommandPacket.class), connectionSession), instanceOf(FirebirdUnsupportedCommandExecutor.class));
    }
}
