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
import lombok.SneakyThrows;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.PostgreSQLCommandPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.admin.PostgreSQLUnsupportedCommandPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLAggregatedCommandPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.PostgreSQLComBindPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.close.PostgreSQLComClosePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.describe.PostgreSQLComDescribePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.execute.PostgreSQLComExecutePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.flush.PostgreSQLComFlushPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.parse.PostgreSQLComParsePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.sync.PostgreSQLComSyncPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.simple.PostgreSQLComQueryPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.generic.PostgreSQLComTerminationPacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.generic.PostgreSQLComTerminationExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.generic.PostgreSQLUnsupportedCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.PostgreSQLAggregatedBatchedStatementsCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.PostgreSQLAggregatedCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.bind.PostgreSQLComBindExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.close.PostgreSQLComCloseExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.describe.PostgreSQLComDescribeExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.execute.PostgreSQLComExecuteExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.flush.PostgreSQLComFlushExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.parse.PostgreSQLComParseExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.sync.PostgreSQLComSyncExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.simple.PostgreSQLComQueryExecutor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostgreSQLCommandExecutorFactoryTest {
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Mock
    private PortalContext portalContext;
    
    @Test
    void assertNewInstance() throws SQLException {
        Collection<InputOutput> inputOutputs = Arrays.asList(
                new InputOutput(PostgreSQLCommandPacketType.SIMPLE_QUERY, PostgreSQLComQueryPacket.class, PostgreSQLComQueryExecutor.class),
                new InputOutput(PostgreSQLCommandPacketType.PARSE_COMMAND, PostgreSQLComParsePacket.class, PostgreSQLComParseExecutor.class),
                new InputOutput(PostgreSQLCommandPacketType.BIND_COMMAND, PostgreSQLComBindPacket.class, PostgreSQLComBindExecutor.class),
                new InputOutput(PostgreSQLCommandPacketType.DESCRIBE_COMMAND, PostgreSQLComDescribePacket.class, PostgreSQLComDescribeExecutor.class),
                new InputOutput(PostgreSQLCommandPacketType.EXECUTE_COMMAND, PostgreSQLComExecutePacket.class, PostgreSQLComExecuteExecutor.class),
                new InputOutput(PostgreSQLCommandPacketType.SYNC_COMMAND, PostgreSQLComSyncPacket.class, PostgreSQLComSyncExecutor.class),
                new InputOutput(PostgreSQLCommandPacketType.CLOSE_COMMAND, PostgreSQLComClosePacket.class, PostgreSQLComCloseExecutor.class),
                new InputOutput(PostgreSQLCommandPacketType.TERMINATE, PostgreSQLComTerminationPacket.class, PostgreSQLComTerminationExecutor.class),
                new InputOutput(PostgreSQLCommandPacketType.FLUSH_COMMAND, PostgreSQLComFlushPacket.class, PostgreSQLComFlushExecutor.class));
        for (InputOutput each : inputOutputs) {
            Class<? extends PostgreSQLCommandPacket> commandPacketClass = each.getCommandPacketClass();
            if (null == commandPacketClass) {
                commandPacketClass = PostgreSQLCommandPacket.class;
            }
            PostgreSQLCommandPacket packet = preparePacket(commandPacketClass);
            CommandExecutor actual = PostgreSQLCommandExecutorFactory.newInstance(each.getCommandPacketType(), packet, connectionSession, portalContext);
            assertThat(actual, isA(each.getResultClass()));
        }
    }
    
    private PostgreSQLCommandPacket preparePacket(final Class<? extends PostgreSQLCommandPacket> commandPacketClass) {
        PostgreSQLCommandPacket result = mock(commandPacketClass);
        if (result instanceof PostgreSQLComQueryPacket) {
            when(((PostgreSQLComQueryPacket) result).getSQL()).thenReturn("");
        }
        return result;
    }
    
    @Test
    void assertAggregatedPacketNotBatchedStatements() throws SQLException {
        PostgreSQLComParsePacket parsePacket = mock(PostgreSQLComParsePacket.class);
        when(parsePacket.getIdentifier()).thenReturn(PostgreSQLCommandPacketType.PARSE_COMMAND);
        PostgreSQLComBindPacket bindPacket = mock(PostgreSQLComBindPacket.class);
        when(bindPacket.getIdentifier()).thenReturn(PostgreSQLCommandPacketType.BIND_COMMAND);
        PostgreSQLComDescribePacket describePacket = mock(PostgreSQLComDescribePacket.class);
        when(describePacket.getIdentifier()).thenReturn(PostgreSQLCommandPacketType.DESCRIBE_COMMAND);
        PostgreSQLComExecutePacket executePacket = mock(PostgreSQLComExecutePacket.class);
        when(executePacket.getIdentifier()).thenReturn(PostgreSQLCommandPacketType.EXECUTE_COMMAND);
        PostgreSQLComSyncPacket syncPacket = mock(PostgreSQLComSyncPacket.class);
        when(syncPacket.getIdentifier()).thenReturn(PostgreSQLCommandPacketType.SYNC_COMMAND);
        PostgreSQLAggregatedCommandPacket packet = mock(PostgreSQLAggregatedCommandPacket.class);
        when(packet.isContainsBatchedStatements()).thenReturn(false);
        when(packet.getPackets()).thenReturn(Arrays.asList(parsePacket, bindPacket, describePacket, executePacket, syncPacket));
        CommandExecutor actual = PostgreSQLCommandExecutorFactory.newInstance(null, packet, connectionSession, portalContext);
        assertThat(actual, isA(PostgreSQLAggregatedCommandExecutor.class));
        Iterator<CommandExecutor> actualPacketsIterator = getExecutorsFromAggregatedCommandExecutor((PostgreSQLAggregatedCommandExecutor) actual).iterator();
        assertThat(actualPacketsIterator.next(), isA(PostgreSQLComParseExecutor.class));
        assertThat(actualPacketsIterator.next(), isA(PostgreSQLComBindExecutor.class));
        assertThat(actualPacketsIterator.next(), isA(PostgreSQLComDescribeExecutor.class));
        assertThat(actualPacketsIterator.next(), isA(PostgreSQLComExecuteExecutor.class));
        assertThat(actualPacketsIterator.next(), isA(PostgreSQLComSyncExecutor.class));
        assertFalse(actualPacketsIterator.hasNext());
    }
    
    @Test
    void assertAggregatedPacketIsBatchedStatements() throws SQLException {
        PostgreSQLComParsePacket parsePacket = mock(PostgreSQLComParsePacket.class);
        when(parsePacket.getIdentifier()).thenReturn(PostgreSQLCommandPacketType.PARSE_COMMAND);
        PostgreSQLComBindPacket bindPacket = mock(PostgreSQLComBindPacket.class);
        PostgreSQLComDescribePacket describePacket = mock(PostgreSQLComDescribePacket.class);
        PostgreSQLComExecutePacket executePacket = mock(PostgreSQLComExecutePacket.class);
        PostgreSQLComSyncPacket syncPacket = mock(PostgreSQLComSyncPacket.class);
        when(syncPacket.getIdentifier()).thenReturn(PostgreSQLCommandPacketType.SYNC_COMMAND);
        PostgreSQLAggregatedCommandPacket packet = mock(PostgreSQLAggregatedCommandPacket.class);
        when(packet.isContainsBatchedStatements()).thenReturn(true);
        when(packet.getPackets()).thenReturn(Arrays.asList(parsePacket, bindPacket, describePacket, executePacket, bindPacket, describePacket, executePacket, syncPacket));
        when(packet.getBatchPacketBeginIndex()).thenReturn(1);
        when(packet.getBatchPacketEndIndex()).thenReturn(6);
        CommandExecutor actual = PostgreSQLCommandExecutorFactory.newInstance(null, packet, connectionSession, portalContext);
        assertThat(actual, isA(PostgreSQLAggregatedCommandExecutor.class));
        Iterator<CommandExecutor> actualPacketsIterator = getExecutorsFromAggregatedCommandExecutor((PostgreSQLAggregatedCommandExecutor) actual).iterator();
        assertThat(actualPacketsIterator.next(), isA(PostgreSQLComParseExecutor.class));
        assertThat(actualPacketsIterator.next(), isA(PostgreSQLAggregatedBatchedStatementsCommandExecutor.class));
        assertThat(actualPacketsIterator.next(), isA(PostgreSQLComSyncExecutor.class));
        assertFalse(actualPacketsIterator.hasNext());
    }
    
    @Test
    void assertAggregatedFlushPacket() throws SQLException {
        PostgreSQLComFlushPacket flushPacket = mock(PostgreSQLComFlushPacket.class);
        when(flushPacket.getIdentifier()).thenReturn(PostgreSQLCommandPacketType.FLUSH_COMMAND);
        PostgreSQLComSyncPacket syncPacket = mock(PostgreSQLComSyncPacket.class);
        when(syncPacket.getIdentifier()).thenReturn(PostgreSQLCommandPacketType.SYNC_COMMAND);
        PostgreSQLAggregatedCommandPacket packet = mock(PostgreSQLAggregatedCommandPacket.class);
        when(packet.getPackets()).thenReturn(Arrays.asList(flushPacket, syncPacket));
        CommandExecutor actual = PostgreSQLCommandExecutorFactory.newInstance(null, packet, connectionSession, portalContext);
        assertThat(actual, isA(PostgreSQLAggregatedCommandExecutor.class));
        Iterator<CommandExecutor> actualPacketsIterator = getExecutorsFromAggregatedCommandExecutor((PostgreSQLAggregatedCommandExecutor) actual).iterator();
        assertThat(actualPacketsIterator.next(), isA(PostgreSQLComFlushExecutor.class));
        assertThat(actualPacketsIterator.next(), isA(PostgreSQLComSyncExecutor.class));
        assertFalse(actualPacketsIterator.hasNext());
    }

    @Test
    void assertUnsupportedCommandPacket() throws SQLException {
        PostgreSQLCommandPacket packet = new PostgreSQLUnsupportedCommandPacket(PostgreSQLCommandPacketType.PASSWORD);
        assertThat(PostgreSQLCommandExecutorFactory.newInstance(PostgreSQLCommandPacketType.PASSWORD, packet, connectionSession, portalContext), isA(PostgreSQLUnsupportedCommandExecutor.class));
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private List<CommandExecutor> getExecutorsFromAggregatedCommandExecutor(final PostgreSQLAggregatedCommandExecutor executor) {
        return (List<CommandExecutor>) Plugins.getMemberAccessor().get(PostgreSQLAggregatedCommandExecutor.class.getDeclaredField("executors"), executor);
    }
    
    @RequiredArgsConstructor
    @Getter
    private static final class InputOutput {
        
        private final PostgreSQLCommandPacketType commandPacketType;
        
        private final Class<? extends PostgreSQLCommandPacket> commandPacketClass;
        
        private final Class<? extends CommandExecutor> resultClass;
    }
}
