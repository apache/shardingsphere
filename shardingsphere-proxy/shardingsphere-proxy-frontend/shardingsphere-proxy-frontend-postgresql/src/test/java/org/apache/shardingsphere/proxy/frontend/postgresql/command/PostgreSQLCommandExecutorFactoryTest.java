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

package org.apache.shardingsphere.proxy.frontend.postgresql.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.PostgreSQLBinaryStatementRegistry;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.bind.PostgreSQLComBindPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.close.PostgreSQLComClosePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.execute.PostgreSQLComExecutePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.parse.PostgreSQLComParsePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.text.PostgreSQLComQueryPacket;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.generic.PostgreSQLComTerminationExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.binary.bind.PostgreSQLComBindExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.binary.close.PostgreSQLComCloseExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.binary.describe.PostgreSQLComDescribeExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.binary.execute.PostgreSQLComExecuteExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.binary.parse.PostgreSQLComParseExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.binary.sync.PostgreSQLComSyncExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.text.PostgreSQLComQueryExecutor;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.EmptyStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PostgreSQLCommandExecutorFactoryTest {
    
    @Mock
    private BackendConnection backendConnection;
    
    @Mock
    private PostgreSQLConnectionContext connectionContext;
    
    @Before
    public void setup() {
        PostgreSQLBinaryStatementRegistry.getInstance().register(1, "2", "", new EmptyStatement(), Collections.emptyList());
        when(backendConnection.getConnectionId()).thenReturn(1);
    }
    
    @Test
    public void assertPendingCommandExecutors() throws SQLException {
        PostgreSQLConnectionContext connectionContext = mock(PostgreSQLConnectionContext.class);
        Collection<CommandExecutor> pendingCommandExecutors = mock(Collection.class);
        when(connectionContext.getPendingExecutors()).thenReturn(pendingCommandExecutors);
        PostgreSQLCommandExecutorFactory.newInstance(PostgreSQLCommandPacketType.CLOSE_COMMAND, mock(PostgreSQLComClosePacket.class), backendConnection, connectionContext);
        PostgreSQLCommandExecutorFactory.newInstance(PostgreSQLCommandPacketType.BIND_COMMAND, mock(PostgreSQLComBindPacket.class), backendConnection, connectionContext);
        PostgreSQLCommandExecutorFactory.newInstance(PostgreSQLCommandPacketType.DESCRIBE_COMMAND, null, backendConnection, connectionContext);
        verify(pendingCommandExecutors).add(any(PostgreSQLComCloseExecutor.class));
        verify(pendingCommandExecutors).add(any(PostgreSQLComBindExecutor.class));
        verify(pendingCommandExecutors).add(any(PostgreSQLComDescribeExecutor.class));
    }
    
    @Test
    public void assertNewInstance() throws SQLException {
        when(backendConnection.getSchemaName()).thenReturn("schema");
        Collection<InputOutput> inputOutputs = Arrays.asList(
            new InputOutput(PostgreSQLCommandPacketType.SIMPLE_QUERY, PostgreSQLComQueryPacket.class, PostgreSQLComQueryExecutor.class),
            new InputOutput(PostgreSQLCommandPacketType.PARSE_COMMAND, PostgreSQLComParsePacket.class, PostgreSQLComParseExecutor.class),
            new InputOutput(PostgreSQLCommandPacketType.EXECUTE_COMMAND, PostgreSQLComExecutePacket.class, PostgreSQLComExecuteExecutor.class),
            new InputOutput(PostgreSQLCommandPacketType.SYNC_COMMAND, null, PostgreSQLComSyncExecutor.class),
            new InputOutput(PostgreSQLCommandPacketType.TERMINATE, null, PostgreSQLComTerminationExecutor.class)
        );
        for (InputOutput inputOutput : inputOutputs) {
            Class<? extends PostgreSQLCommandPacket> commandPacketClass = inputOutput.getCommandPacketClass();
            if (null == commandPacketClass) {
                commandPacketClass = PostgreSQLCommandPacket.class;
            }
            PostgreSQLCommandPacket packet = preparePacket(commandPacketClass);
            CommandExecutor actual = PostgreSQLCommandExecutorFactory.newInstance(inputOutput.getCommandPacketType(), packet, backendConnection, connectionContext);
            assertThat(actual, instanceOf(inputOutput.getResultClass()));
        }
    }
    
    private PostgreSQLCommandPacket preparePacket(final Class<? extends PostgreSQLCommandPacket> commandPacketClass) {
        PostgreSQLCommandPacket result = mock(commandPacketClass);
        if (result instanceof PostgreSQLComQueryPacket) {
            when(((PostgreSQLComQueryPacket) result).getSql()).thenReturn("");
        }
        if (result instanceof PostgreSQLComParsePacket) {
            when(((PostgreSQLComParsePacket) result).getStatementId()).thenReturn("S_0");
            when(((PostgreSQLComParsePacket) result).getSql()).thenReturn("");
        }
        return result;
    }
    
    @RequiredArgsConstructor
    @Getter
    private static final class InputOutput {
        
        private final PostgreSQLCommandPacketType commandPacketType;
        
        private final Class<? extends PostgreSQLCommandPacket> commandPacketClass;
        
        private final Class<? extends CommandExecutor> resultClass;
    }
}
