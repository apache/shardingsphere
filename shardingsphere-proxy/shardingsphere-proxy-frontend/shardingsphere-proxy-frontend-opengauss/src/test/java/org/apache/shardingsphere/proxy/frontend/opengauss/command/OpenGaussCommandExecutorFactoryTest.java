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

package org.apache.shardingsphere.proxy.frontend.opengauss.command;

import lombok.SneakyThrows;
import org.apache.shardingsphere.db.protocol.opengauss.packet.command.OpenGaussCommandPacketType;
import org.apache.shardingsphere.db.protocol.opengauss.packet.command.query.extended.bind.OpenGaussComBatchBindPacket;
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
import org.apache.shardingsphere.proxy.frontend.opengauss.command.query.extended.bind.OpenGaussComBatchBindExecutor;
import org.apache.shardingsphere.proxy.frontend.opengauss.command.query.simple.OpenGaussComQueryExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PostgreSQLConnectionContext;
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class OpenGaussCommandExecutorFactoryTest {
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Mock
    private PostgreSQLConnectionContext connectionContext;
    
    @Test
    public void assertNewOpenGaussBatchBindExecutor() throws SQLException {
        OpenGaussComBatchBindPacket batchBindPacket = mock(OpenGaussComBatchBindPacket.class);
        CommandExecutor actual = OpenGaussCommandExecutorFactory.newInstance(OpenGaussCommandPacketType.BATCH_BIND_COMMAND, batchBindPacket, connectionSession, connectionContext);
        assertTrue(actual instanceof OpenGaussComBatchBindExecutor);
    }
    
    @Test
    public void assertNewPostgreSQLSimpleQueryExecutor() throws SQLException {
        PostgreSQLComQueryPacket queryPacket = mock(PostgreSQLComQueryPacket.class);
        when(queryPacket.getSql()).thenReturn("");
        CommandExecutor actual = OpenGaussCommandExecutorFactory.newInstance(PostgreSQLCommandPacketType.SIMPLE_QUERY, queryPacket, connectionSession, connectionContext);
        assertTrue(actual instanceof OpenGaussComQueryExecutor);
    }
    
    @Test
    public void assertNewUnsupportedExecutor() throws SQLException {
        CommandExecutor actual = OpenGaussCommandExecutorFactory.newInstance(PostgreSQLCommandPacketType.FLUSH_COMMAND, null, connectionSession, connectionContext);
        assertTrue(actual instanceof PostgreSQLUnsupportedCommandExecutor);
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
        CommandExecutor actual = OpenGaussCommandExecutorFactory.newInstance(null, packet, connectionSession, connectionContext);
        assertTrue(actual instanceof PostgreSQLAggregatedCommandExecutor);
        Iterator<CommandExecutor> actualPacketsIterator = getExecutorsFromAggregatedCommandExecutor((PostgreSQLAggregatedCommandExecutor) actual).iterator();
        assertTrue(actualPacketsIterator.next() instanceof PostgreSQLComParseExecutor);
        assertTrue(actualPacketsIterator.next() instanceof PostgreSQLComBindExecutor);
        assertTrue(actualPacketsIterator.next() instanceof PostgreSQLComDescribeExecutor);
        assertTrue(actualPacketsIterator.next() instanceof PostgreSQLComExecuteExecutor);
        assertTrue(actualPacketsIterator.next() instanceof PostgreSQLComSyncExecutor);
        assertFalse(actualPacketsIterator.hasNext());
    }
    
    @Test
    public void assertAggregatedPacketIsBatchedStatements() throws SQLException {
        PostgreSQLComParsePacket parsePacket = mock(PostgreSQLComParsePacket.class);
        when(parsePacket.getIdentifier()).thenReturn(PostgreSQLCommandPacketType.PARSE_COMMAND);
        PostgreSQLComBindPacket bindPacket = mock(PostgreSQLComBindPacket.class);
        PostgreSQLComDescribePacket describePacket = mock(PostgreSQLComDescribePacket.class);
        PostgreSQLComExecutePacket executePacket = mock(PostgreSQLComExecutePacket.class);
        PostgreSQLComClosePacket closePacket = mock(PostgreSQLComClosePacket.class);
        when(closePacket.getIdentifier()).thenReturn(PostgreSQLCommandPacketType.CLOSE_COMMAND);
        PostgreSQLComSyncPacket syncPacket = mock(PostgreSQLComSyncPacket.class);
        when(syncPacket.getIdentifier()).thenReturn(PostgreSQLCommandPacketType.SYNC_COMMAND);
        PostgreSQLComTerminationPacket terminationPacket = mock(PostgreSQLComTerminationPacket.class);
        when(terminationPacket.getIdentifier()).thenReturn(PostgreSQLCommandPacketType.TERMINATE);
        PostgreSQLAggregatedCommandPacket packet = mock(PostgreSQLAggregatedCommandPacket.class);
        when(packet.isContainsBatchedStatements()).thenReturn(true);
        when(packet.getPackets()).thenReturn(
                Arrays.asList(parsePacket, bindPacket, describePacket, executePacket, bindPacket, describePacket, executePacket, closePacket, syncPacket, terminationPacket));
        when(packet.getFirstBindIndex()).thenReturn(1);
        when(packet.getLastExecuteIndex()).thenReturn(6);
        CommandExecutor actual = OpenGaussCommandExecutorFactory.newInstance(null, packet, connectionSession, connectionContext);
        assertTrue(actual instanceof PostgreSQLAggregatedCommandExecutor);
        Iterator<CommandExecutor> actualPacketsIterator = getExecutorsFromAggregatedCommandExecutor((PostgreSQLAggregatedCommandExecutor) actual).iterator();
        assertTrue(actualPacketsIterator.next() instanceof PostgreSQLComParseExecutor);
        assertTrue(actualPacketsIterator.next() instanceof PostgreSQLAggregatedBatchedStatementsCommandExecutor);
        assertTrue(actualPacketsIterator.next() instanceof PostgreSQLComCloseExecutor);
        assertTrue(actualPacketsIterator.next() instanceof PostgreSQLComSyncExecutor);
        assertTrue(actualPacketsIterator.next() instanceof PostgreSQLComTerminationExecutor);
        assertFalse(actualPacketsIterator.hasNext());
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows
    private static List<CommandExecutor> getExecutorsFromAggregatedCommandExecutor(final PostgreSQLAggregatedCommandExecutor executor) {
        Field field = PostgreSQLAggregatedCommandExecutor.class.getDeclaredField("executors");
        field.setAccessible(true);
        return (List<CommandExecutor>) field.get(executor);
    }
}
