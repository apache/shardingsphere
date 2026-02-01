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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

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
    
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("provideNonAggregatedCommandCases")
    void assertNonAggregatedCommand(final String name, final PostgreSQLCommandPacketType commandPacketType, final PostgreSQLCommandPacket packet,
                                    final Class<? extends CommandExecutor> expectedClass) throws SQLException {
        CommandExecutor actual = PostgreSQLCommandExecutorFactory.newInstance(commandPacketType, packet, connectionSession, portalContext);
        assertThat(actual, isA(expectedClass));
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
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private List<CommandExecutor> getExecutorsFromAggregatedCommandExecutor(final PostgreSQLAggregatedCommandExecutor executor) {
        return (List<CommandExecutor>) Plugins.getMemberAccessor().get(PostgreSQLAggregatedCommandExecutor.class.getDeclaredField("executors"), executor);
    }
    
    private static Stream<Arguments> provideNonAggregatedCommandCases() {
        return Stream.of(
                Arguments.of("SIMPLE_QUERY", PostgreSQLCommandPacketType.SIMPLE_QUERY, createCommandPacket(PostgreSQLComQueryPacket.class), PostgreSQLComQueryExecutor.class),
                Arguments.of("PARSE_COMMAND", PostgreSQLCommandPacketType.PARSE_COMMAND, createCommandPacket(PostgreSQLComParsePacket.class), PostgreSQLComParseExecutor.class),
                Arguments.of("BIND_COMMAND", PostgreSQLCommandPacketType.BIND_COMMAND, createCommandPacket(PostgreSQLComBindPacket.class), PostgreSQLComBindExecutor.class),
                Arguments.of("DESCRIBE_COMMAND", PostgreSQLCommandPacketType.DESCRIBE_COMMAND, createCommandPacket(PostgreSQLComDescribePacket.class), PostgreSQLComDescribeExecutor.class),
                Arguments.of("EXECUTE_COMMAND", PostgreSQLCommandPacketType.EXECUTE_COMMAND, createCommandPacket(PostgreSQLComExecutePacket.class), PostgreSQLComExecuteExecutor.class),
                Arguments.of("SYNC_COMMAND", PostgreSQLCommandPacketType.SYNC_COMMAND, createCommandPacket(PostgreSQLComSyncPacket.class), PostgreSQLComSyncExecutor.class),
                Arguments.of("CLOSE_COMMAND", PostgreSQLCommandPacketType.CLOSE_COMMAND, createCommandPacket(PostgreSQLComClosePacket.class), PostgreSQLComCloseExecutor.class),
                Arguments.of("TERMINATE", PostgreSQLCommandPacketType.TERMINATE, createCommandPacket(PostgreSQLComTerminationPacket.class), PostgreSQLComTerminationExecutor.class),
                Arguments.of("FLUSH_COMMAND", PostgreSQLCommandPacketType.FLUSH_COMMAND, createCommandPacket(PostgreSQLComFlushPacket.class), PostgreSQLComFlushExecutor.class),
                Arguments.of("PASSWORD_DEFAULT", PostgreSQLCommandPacketType.PASSWORD,
                        new PostgreSQLUnsupportedCommandPacket(PostgreSQLCommandPacketType.PASSWORD), PostgreSQLUnsupportedCommandExecutor.class));
    }
    
    private static PostgreSQLCommandPacket createCommandPacket(final Class<? extends PostgreSQLCommandPacket> commandPacketClass) {
        PostgreSQLCommandPacket result = mock(commandPacketClass);
        if (result instanceof PostgreSQLComQueryPacket) {
            when(((PostgreSQLComQueryPacket) result).getSQL()).thenReturn("");
        }
        return result;
    }
}
