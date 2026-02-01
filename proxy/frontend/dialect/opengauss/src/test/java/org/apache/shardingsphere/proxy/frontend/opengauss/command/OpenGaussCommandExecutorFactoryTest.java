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

import io.netty.buffer.Unpooled;
import lombok.SneakyThrows;
import org.apache.shardingsphere.database.protocol.opengauss.packet.command.OpenGaussCommandPacketType;
import org.apache.shardingsphere.database.protocol.opengauss.packet.command.bind.OpenGaussComBatchBindPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.PostgreSQLCommandPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
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
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.opengauss.command.query.extended.bind.OpenGaussComBatchBindExecutor;
import org.apache.shardingsphere.proxy.frontend.opengauss.command.query.simple.OpenGaussComQueryExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PortalContext;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenGaussCommandExecutorFactoryTest {
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Mock
    private PortalContext portalContext;
    
    @Test
    void assertNewOpenGaussBatchBindExecutor() throws SQLException {
        OpenGaussComBatchBindPacket batchBindPacket = mock(OpenGaussComBatchBindPacket.class);
        CommandExecutor actual = OpenGaussCommandExecutorFactory.newInstance(OpenGaussCommandPacketType.BATCH_BIND_COMMAND, batchBindPacket, connectionSession, portalContext);
        assertThat(actual, isA(OpenGaussComBatchBindExecutor.class));
    }
    
    @Test
    void assertNewPostgreSQLSimpleQueryExecutor() throws SQLException {
        PostgreSQLComQueryPacket queryPacket = mock(PostgreSQLComQueryPacket.class);
        when(queryPacket.getSQL()).thenReturn("");
        CommandExecutor actual = OpenGaussCommandExecutorFactory.newInstance(PostgreSQLCommandPacketType.SIMPLE_QUERY, queryPacket, connectionSession, portalContext);
        assertThat(actual, isA(OpenGaussComQueryExecutor.class));
    }
    
    @Test
    void assertAggregatedPacketNotBatchedStatements() throws SQLException {
        PostgreSQLComParsePacket parsePacket = mock(PostgreSQLComParsePacket.class);
        when(parsePacket.getIdentifier()).thenReturn(PostgreSQLCommandPacketType.PARSE_COMMAND);
        PostgreSQLComFlushPacket flushPacket = new PostgreSQLComFlushPacket(new PostgreSQLPacketPayload(Unpooled.wrappedBuffer(new byte[4]), StandardCharsets.UTF_8));
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
        when(packet.getPackets()).thenReturn(Arrays.asList(parsePacket, flushPacket, bindPacket, describePacket, executePacket, syncPacket));
        CommandExecutor actual = OpenGaussCommandExecutorFactory.newInstance(null, packet, connectionSession, portalContext);
        assertThat(actual, isA(PostgreSQLAggregatedCommandExecutor.class));
        Iterator<CommandExecutor> actualPacketsIterator = getExecutorsFromAggregatedCommandExecutor((PostgreSQLAggregatedCommandExecutor) actual).iterator();
        assertThat(actualPacketsIterator.next(), isA(PostgreSQLComParseExecutor.class));
        assertThat(actualPacketsIterator.next(), isA(PostgreSQLComFlushExecutor.class));
        assertThat(actualPacketsIterator.next(), isA(PostgreSQLComBindExecutor.class));
        assertThat(actualPacketsIterator.next(), isA(PostgreSQLComDescribeExecutor.class));
        assertThat(actualPacketsIterator.next(), isA(PostgreSQLComExecuteExecutor.class));
        assertThat(actualPacketsIterator.next(), isA(PostgreSQLComSyncExecutor.class));
        assertFalse(actualPacketsIterator.hasNext());
    }
    
    @Test
    void assertAggregatedPacketIsBatchedStatements() throws SQLException {
        final PostgreSQLComParsePacket parsePacket = mock(PostgreSQLComParsePacket.class);
        when(parsePacket.getIdentifier()).thenReturn(PostgreSQLCommandPacketType.PARSE_COMMAND);
        final PostgreSQLComBindPacket bindPacket = mock(PostgreSQLComBindPacket.class);
        final PostgreSQLComDescribePacket describePacket = mock(PostgreSQLComDescribePacket.class);
        final PostgreSQLComExecutePacket executePacket = mock(PostgreSQLComExecutePacket.class);
        final PostgreSQLComClosePacket closePacket = mock(PostgreSQLComClosePacket.class);
        when(closePacket.getIdentifier()).thenReturn(PostgreSQLCommandPacketType.CLOSE_COMMAND);
        final PostgreSQLComSyncPacket syncPacket = mock(PostgreSQLComSyncPacket.class);
        when(syncPacket.getIdentifier()).thenReturn(PostgreSQLCommandPacketType.SYNC_COMMAND);
        final PostgreSQLComTerminationPacket terminationPacket = mock(PostgreSQLComTerminationPacket.class);
        when(terminationPacket.getIdentifier()).thenReturn(PostgreSQLCommandPacketType.TERMINATE);
        final PostgreSQLAggregatedCommandPacket packet = mock(PostgreSQLAggregatedCommandPacket.class);
        when(packet.isContainsBatchedStatements()).thenReturn(true);
        when(packet.getPackets()).thenReturn(
                Arrays.asList(parsePacket, bindPacket, describePacket, executePacket, bindPacket, describePacket, executePacket, closePacket, syncPacket, terminationPacket));
        when(packet.getBatchPacketBeginIndex()).thenReturn(1);
        when(packet.getBatchPacketEndIndex()).thenReturn(6);
        CommandExecutor actual = OpenGaussCommandExecutorFactory.newInstance(null, packet, connectionSession, portalContext);
        assertThat(actual, isA(PostgreSQLAggregatedCommandExecutor.class));
        Iterator<CommandExecutor> actualPacketsIterator = getExecutorsFromAggregatedCommandExecutor((PostgreSQLAggregatedCommandExecutor) actual).iterator();
        assertThat(actualPacketsIterator.next(), isA(PostgreSQLComParseExecutor.class));
        assertThat(actualPacketsIterator.next(), isA(PostgreSQLAggregatedBatchedStatementsCommandExecutor.class));
        assertThat(actualPacketsIterator.next(), isA(PostgreSQLComCloseExecutor.class));
        assertThat(actualPacketsIterator.next(), isA(PostgreSQLComSyncExecutor.class));
        assertThat(actualPacketsIterator.next(), isA(PostgreSQLComTerminationExecutor.class));
        assertFalse(actualPacketsIterator.hasNext());
    }
    
    @Test
    void assertAggregatedPacketWithBatchBindPacket() throws SQLException {
        final OpenGaussComBatchBindPacket batchBindPacket = mock(OpenGaussComBatchBindPacket.class);
        when(batchBindPacket.getIdentifier()).thenReturn(OpenGaussCommandPacketType.BATCH_BIND_COMMAND);
        final PostgreSQLComParsePacket parsePacket = mock(PostgreSQLComParsePacket.class);
        when(parsePacket.getIdentifier()).thenReturn(PostgreSQLCommandPacketType.PARSE_COMMAND);
        final PostgreSQLAggregatedCommandPacket packet = mock(PostgreSQLAggregatedCommandPacket.class);
        when(packet.isContainsBatchedStatements()).thenReturn(true);
        when(packet.getPackets()).thenReturn(Arrays.asList(batchBindPacket, parsePacket));
        CommandExecutor actual = OpenGaussCommandExecutorFactory.newInstance(null, packet, connectionSession, portalContext);
        assertThat(actual, isA(PostgreSQLAggregatedCommandExecutor.class));
        Iterator<CommandExecutor> actualPacketsIterator = getExecutorsFromAggregatedCommandExecutor((PostgreSQLAggregatedCommandExecutor) actual).iterator();
        assertThat(actualPacketsIterator.next(), isA(OpenGaussComBatchBindExecutor.class));
        assertThat(actualPacketsIterator.next(), isA(PostgreSQLComParseExecutor.class));
        assertFalse(actualPacketsIterator.hasNext());
    }
    
    @Test
    void assertUnsupportedCommandExecutor() throws SQLException {
        assertThat(OpenGaussCommandExecutorFactory.newInstance(PostgreSQLCommandPacketType.PASSWORD, mock(PostgreSQLCommandPacket.class), connectionSession, portalContext),
                isA(PostgreSQLUnsupportedCommandExecutor.class));
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private List<CommandExecutor> getExecutorsFromAggregatedCommandExecutor(final PostgreSQLAggregatedCommandExecutor executor) {
        return (List<CommandExecutor>) Plugins.getMemberAccessor().get(PostgreSQLAggregatedCommandExecutor.class.getDeclaredField("executors"), executor);
    }
}
