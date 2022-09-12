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
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.PostgreSQLAggregatedCommandPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.bind.PostgreSQLComBindPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.close.PostgreSQLComClosePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.describe.PostgreSQLComDescribePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.execute.PostgreSQLComExecutePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.parse.PostgreSQLComParsePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.sync.PostgreSQLComSyncPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.simple.PostgreSQLComQueryPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLComTerminationPacket;
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
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.parse.PostgreSQLComParseExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.sync.PostgreSQLComSyncExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.simple.PostgreSQLComQueryExecutor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PostgreSQLCommandExecutorFactoryTest {
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Mock
    private PostgreSQLConnectionContext connectionContext;
    
    @Test
    public void assertNewInstance() throws SQLException {
        Collection<InputOutput> inputOutputs = Arrays.asList(
                new InputOutput(PostgreSQLCommandPacketType.SIMPLE_QUERY, PostgreSQLComQueryPacket.class, PostgreSQLComQueryExecutor.class),
                new InputOutput(PostgreSQLCommandPacketType.PARSE_COMMAND, PostgreSQLComParsePacket.class, PostgreSQLComParseExecutor.class),
                new InputOutput(PostgreSQLCommandPacketType.BIND_COMMAND, PostgreSQLComBindPacket.class, PostgreSQLComBindExecutor.class),
                new InputOutput(PostgreSQLCommandPacketType.DESCRIBE_COMMAND, PostgreSQLComDescribePacket.class, PostgreSQLComDescribeExecutor.class),
                new InputOutput(PostgreSQLCommandPacketType.EXECUTE_COMMAND, PostgreSQLComExecutePacket.class, PostgreSQLComExecuteExecutor.class),
                new InputOutput(PostgreSQLCommandPacketType.SYNC_COMMAND, PostgreSQLComSyncPacket.class, PostgreSQLComSyncExecutor.class),
                new InputOutput(PostgreSQLCommandPacketType.CLOSE_COMMAND, PostgreSQLComClosePacket.class, PostgreSQLComCloseExecutor.class),
                new InputOutput(PostgreSQLCommandPacketType.TERMINATE, PostgreSQLComTerminationPacket.class, PostgreSQLComTerminationExecutor.class),
                new InputOutput(PostgreSQLCommandPacketType.FLUSH_COMMAND, null, PostgreSQLUnsupportedCommandExecutor.class));
        for (InputOutput each : inputOutputs) {
            Class<? extends PostgreSQLCommandPacket> commandPacketClass = each.getCommandPacketClass();
            if (null == commandPacketClass) {
                commandPacketClass = PostgreSQLCommandPacket.class;
            }
            PostgreSQLCommandPacket packet = preparePacket(commandPacketClass);
            CommandExecutor actual = PostgreSQLCommandExecutorFactory.newInstance(each.getCommandPacketType(), packet, connectionSession, connectionContext);
            assertThat(actual, instanceOf(each.getResultClass()));
        }
    }
    
    private PostgreSQLCommandPacket preparePacket(final Class<? extends PostgreSQLCommandPacket> commandPacketClass) {
        PostgreSQLCommandPacket result = mock(commandPacketClass);
        if (result instanceof PostgreSQLComQueryPacket) {
            when(((PostgreSQLComQueryPacket) result).getSql()).thenReturn("");
        }
        return result;
    }
    
    @Test
    public void assertAggregatedPacketNotBatchedStatements() throws SQLException {
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
        CommandExecutor actual = PostgreSQLCommandExecutorFactory.newInstance(null, packet, connectionSession, connectionContext);
        assertThat(actual, instanceOf(PostgreSQLAggregatedCommandExecutor.class));
        Iterator<CommandExecutor> actualPacketsIterator = getExecutorsFromAggregatedCommandExecutor((PostgreSQLAggregatedCommandExecutor) actual).iterator();
        assertThat(actualPacketsIterator.next(), instanceOf(PostgreSQLComParseExecutor.class));
        assertThat(actualPacketsIterator.next(), instanceOf(PostgreSQLComBindExecutor.class));
        assertThat(actualPacketsIterator.next(), instanceOf(PostgreSQLComDescribeExecutor.class));
        assertThat(actualPacketsIterator.next(), instanceOf(PostgreSQLComExecuteExecutor.class));
        assertThat(actualPacketsIterator.next(), instanceOf(PostgreSQLComSyncExecutor.class));
        assertFalse(actualPacketsIterator.hasNext());
    }
    
    @Test
    public void assertAggregatedPacketIsBatchedStatements() throws SQLException {
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
        when(packet.getFirstBindIndex()).thenReturn(1);
        when(packet.getLastExecuteIndex()).thenReturn(6);
        CommandExecutor actual = PostgreSQLCommandExecutorFactory.newInstance(null, packet, connectionSession, connectionContext);
        assertThat(actual, instanceOf(PostgreSQLAggregatedCommandExecutor.class));
        Iterator<CommandExecutor> actualPacketsIterator = getExecutorsFromAggregatedCommandExecutor((PostgreSQLAggregatedCommandExecutor) actual).iterator();
        assertThat(actualPacketsIterator.next(), instanceOf(PostgreSQLComParseExecutor.class));
        assertThat(actualPacketsIterator.next(), instanceOf(PostgreSQLAggregatedBatchedStatementsCommandExecutor.class));
        assertThat(actualPacketsIterator.next(), instanceOf(PostgreSQLComSyncExecutor.class));
        assertFalse(actualPacketsIterator.hasNext());
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows
    private static List<CommandExecutor> getExecutorsFromAggregatedCommandExecutor(final PostgreSQLAggregatedCommandExecutor executor) {
        Field field = PostgreSQLAggregatedCommandExecutor.class.getDeclaredField("executors");
        field.setAccessible(true);
        return (List<CommandExecutor>) field.get(executor);
    }
    
    @RequiredArgsConstructor
    @Getter
    private static final class InputOutput {
        
        private final PostgreSQLCommandPacketType commandPacketType;
        
        private final Class<? extends PostgreSQLCommandPacket> commandPacketClass;
        
        private final Class<? extends CommandExecutor> resultClass;
    }
}
