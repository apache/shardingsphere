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

package org.apache.shardingsphere.data.pipeline.opengauss.ingest.incremental.dumper;

import org.apache.shardingsphere.data.pipeline.api.type.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.core.channel.memory.MemoryPipelineChannel;
import org.apache.shardingsphere.data.pipeline.core.exception.IngestException;
import org.apache.shardingsphere.data.pipeline.core.execute.AbstractPipelineLifecycleRunnable;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.DumperCommonContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.IncrementalDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.mapper.ActualAndLogicTableNameMapper;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.mapper.TableAndSchemaNameMapper;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.opengauss.ingest.incremental.wal.OpenGaussLogicalReplication;
import org.apache.shardingsphere.data.pipeline.opengauss.ingest.incremental.wal.decode.MppdbDecodingPlugin;
import org.apache.shardingsphere.data.pipeline.opengauss.ingest.incremental.wal.decode.OpenGaussLogSequenceNumber;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.WALEventConverter;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.WALPosition;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.AbstractRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.AbstractWALEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.BeginTXEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.CommitTXEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.WriteRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.position.slot.PostgreSQLSlotNameGenerator;
import org.apache.shardingsphere.infra.metadata.identifier.ShardingSphereIdentifier;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;
import org.opengauss.jdbc.PgConnection;
import org.opengauss.replication.LogSequenceNumber;
import org.opengauss.replication.PGReplicationStream;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class OpenGaussIncrementalDumperTest {
    
    @Test
    void assertRunBlockingRetriesFiveTimesThenThrowsIngestException() {
        AtomicInteger runningIndex = new AtomicInteger();
        boolean[] runningSequence = {true, false, true, false, true, false, true, false, true, false};
        OpenGaussIncrementalDumper dumper = mock(OpenGaussIncrementalDumper.class, withSettings()
                .useConstructor(createDumperContext(false), new WALPosition(new OpenGaussLogSequenceNumber(LogSequenceNumber.valueOf(1L))), new MemoryPipelineChannel(10, records -> {
                }), mock(PipelineTableMetaDataLoader.class))
                .defaultAnswer(invocation -> {
                    if ("isRunning".equals(invocation.getMethod().getName())) {
                        int index = runningIndex.getAndIncrement();
                        return index < runningSequence.length && runningSequence[index];
                    }
                    return invocation.callRealMethod();
                }));
        AtomicInteger attempts = new AtomicInteger();
        try (MockedStatic<DriverManager> driverManagerMock = mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
                attempts.incrementAndGet();
                throw new SQLException("failed");
            });
            assertThrows(IngestException.class, dumper::start);
        }
        assertThat(attempts.get(), is(5));
    }
    
    @Test
    void assertRunBlockingSkipSleepWhenStopped() throws ReflectiveOperationException, SQLException {
        IngestPosition position = new WALPosition(new OpenGaussLogSequenceNumber(LogSequenceNumber.valueOf(1L)));
        PipelineChannel channel = new MemoryPipelineChannel(10, records -> {
        });
        OpenGaussIncrementalDumper dumper = new OpenGaussIncrementalDumper(createDumperContext(false), position, channel, mock());
        AtomicReference<Boolean> running = getRunningState(dumper);
        OpenGaussLogicalReplication logicalReplication = mock(OpenGaussLogicalReplication.class);
        Plugins.getMemberAccessor().set(OpenGaussIncrementalDumper.class.getDeclaredField("logicalReplication"), dumper, logicalReplication);
        IncrementalDumperContext dumperContext = getDumperContext(dumper);
        PgConnection pgConnection = mock(PgConnection.class);
        when(logicalReplication.createConnection((StandardPipelineDataSourceConfiguration) dumperContext.getCommonContext().getDataSourceConfig())).thenReturn(pgConnection);
        when(pgConnection.unwrap(PgConnection.class)).thenReturn(pgConnection);
        try (MockedStatic<PostgreSQLSlotNameGenerator> slotNameMock = mockStatic(PostgreSQLSlotNameGenerator.class)) {
            slotNameMock.when(() -> PostgreSQLSlotNameGenerator.getUniqueSlotName(pgConnection, dumperContext.getJobId())).thenReturn("slot-1");
            when(logicalReplication.createReplicationStream(eq(pgConnection), any(), eq("slot-1"), eq(3))).thenAnswer(invocation -> {
                running.set(false);
                throw new SQLException("stopped");
            });
            try (MockedStatic<DriverManager> driverManagerMock = mockStatic(DriverManager.class)) {
                mockVersionQuery(driverManagerMock, "(openGauss 3.0 build )");
                dumper.start();
            }
        }
        assertFalse(running.get());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertDumpDecodeWithTXWhenMajorVersionAtLeastThree() throws ReflectiveOperationException, SQLException {
        IngestPosition position = new WALPosition(new OpenGaussLogSequenceNumber(LogSequenceNumber.valueOf(1L)));
        MemoryPipelineChannel channel = new MemoryPipelineChannel(10, records -> {
        });
        OpenGaussIncrementalDumper dumper = new OpenGaussIncrementalDumper(createDumperContext(true), position, channel, mock());
        OpenGaussLogicalReplication logicalReplication = mock(OpenGaussLogicalReplication.class);
        Plugins.getMemberAccessor().set(OpenGaussIncrementalDumper.class.getDeclaredField("logicalReplication"), dumper, logicalReplication);
        PgConnection pgConnection = mock(PgConnection.class);
        PGReplicationStream stream = mock(PGReplicationStream.class);
        IncrementalDumperContext dumperContext = getDumperContext(dumper);
        when(logicalReplication.createConnection((StandardPipelineDataSourceConfiguration) dumperContext.getCommonContext().getDataSourceConfig())).thenReturn(pgConnection);
        when(pgConnection.unwrap(PgConnection.class)).thenReturn(pgConnection);
        when(stream.getLastReceiveLSN()).thenReturn(LogSequenceNumber.valueOf(200L));
        when(stream.readPending()).thenReturn(null, ByteBuffer.wrap("1".getBytes()), ByteBuffer.wrap("2".getBytes()),
                ByteBuffer.wrap("3".getBytes()), ByteBuffer.wrap("4".getBytes()), ByteBuffer.wrap("5".getBytes()));
        Plugins.getMemberAccessor().set(OpenGaussIncrementalDumper.class.getDeclaredField("walEventConverter"), dumper, mock(WALEventConverter.class));
        AtomicReference<Boolean> running = getRunningState(dumper);
        try (
                MockedStatic<DriverManager> driverManagerMock = mockStatic(DriverManager.class);
                MockedStatic<PostgreSQLSlotNameGenerator> slotNameMock = mockStatic(PostgreSQLSlotNameGenerator.class);
                MockedConstruction<MppdbDecodingPlugin> ignoredConstruction = mockConstruction(MppdbDecodingPlugin.class, (mockedPlugin, context) -> {
                    AtomicInteger counter = new AtomicInteger();
                    when(mockedPlugin.decode(any(ByteBuffer.class), any(OpenGaussLogSequenceNumber.class)))
                            .thenAnswer(invocation -> createWALEventWithDecodeSequence(running, counter.incrementAndGet()));
                })) {
            slotNameMock.when(() -> PostgreSQLSlotNameGenerator.getUniqueSlotName(pgConnection, dumperContext.getJobId())).thenReturn("slot-1");
            when(logicalReplication.createReplicationStream(eq(pgConnection), any(), eq("slot-1"), eq(3))).thenReturn(stream);
            mockVersionQuery(driverManagerMock, "(openGauss 3.1.0 build )");
            dumper.start();
        }
        assertThat(channel.fetch(10, 0L).size(), is(3));
        AtomicReference<WALPosition> walPosition = (AtomicReference<WALPosition>) Plugins.getMemberAccessor().get(OpenGaussIncrementalDumper.class.getDeclaredField("walPosition"), dumper);
        assertThat(walPosition.get().getLogSequenceNumber().asString(), is(LogSequenceNumber.valueOf(5L).asString()));
        verify(stream).close();
    }
    
    private AbstractWALEvent createWALEventWithDecodeSequence(final AtomicReference<Boolean> running, final int index) {
        AbstractWALEvent result;
        switch (index) {
            case 1:
                result = new BeginTXEvent(1L, 10L);
                break;
            case 2:
                WriteRowEvent firstRowEvent = new WriteRowEvent();
                firstRowEvent.setTableName("t_order");
                firstRowEvent.setSchemaName("public");
                result = firstRowEvent;
                break;
            case 3:
                result = new BeginTXEvent(2L, 20L);
                break;
            case 4:
                WriteRowEvent secondRowEvent = new WriteRowEvent();
                secondRowEvent.setTableName("t_order");
                secondRowEvent.setSchemaName("public");
                result = secondRowEvent;
                break;
            default:
                result = new CommitTXEvent(3L, 30L);
                running.set(false);
        }
        result.setLogSequenceNumber(new OpenGaussLogSequenceNumber(LogSequenceNumber.valueOf(index)));
        return result;
    }
    
    @Test
    void assertDumpDecodeWithTXBeforeVersionThree() throws ReflectiveOperationException, SQLException {
        IngestPosition position = new WALPosition(new OpenGaussLogSequenceNumber(LogSequenceNumber.valueOf(1L)));
        MemoryPipelineChannel channel = new MemoryPipelineChannel(10, records -> {
        });
        OpenGaussIncrementalDumper dumper = new OpenGaussIncrementalDumper(createDumperContext(true), position, channel, mock());
        OpenGaussLogicalReplication logicalReplication = mock(OpenGaussLogicalReplication.class);
        Plugins.getMemberAccessor().set(OpenGaussIncrementalDumper.class.getDeclaredField("logicalReplication"), dumper, logicalReplication);
        PgConnection pgConnection = mock(PgConnection.class);
        PGReplicationStream stream = mock(PGReplicationStream.class);
        IncrementalDumperContext dumperContext = getDumperContext(dumper);
        when(logicalReplication.createConnection((StandardPipelineDataSourceConfiguration) dumperContext.getCommonContext().getDataSourceConfig())).thenReturn(pgConnection);
        when(pgConnection.unwrap(PgConnection.class)).thenReturn(pgConnection);
        when(stream.getLastReceiveLSN()).thenReturn(LogSequenceNumber.valueOf(20L));
        when(stream.readPending()).thenReturn(ByteBuffer.wrap("1".getBytes()), ByteBuffer.wrap("2".getBytes()), ByteBuffer.wrap("3".getBytes()));
        doThrow(new SQLException("close error")).when(stream).close();
        WALEventConverter walEventConverter = mock(WALEventConverter.class);
        Plugins.getMemberAccessor().set(OpenGaussIncrementalDumper.class.getDeclaredField("walEventConverter"), dumper, walEventConverter);
        ArgumentCaptor<AbstractWALEvent> eventCaptor = ArgumentCaptor.forClass(AbstractWALEvent.class);
        AtomicReference<Boolean> running = getRunningState(dumper);
        try (
                MockedStatic<DriverManager> driverManagerMock = mockStatic(DriverManager.class);
                MockedStatic<PostgreSQLSlotNameGenerator> slotNameMock = mockStatic(PostgreSQLSlotNameGenerator.class);
                MockedConstruction<MppdbDecodingPlugin> ignoredConstruction = mockConstruction(MppdbDecodingPlugin.class, (mockedPlugin, context) -> {
                    AtomicInteger counter = new AtomicInteger();
                    when(mockedPlugin.decode(any(ByteBuffer.class), any(OpenGaussLogSequenceNumber.class)))
                            .thenAnswer(invocation -> createWALEventWithDecodeForOldVersion(running, counter.incrementAndGet()));
                })) {
            slotNameMock.when(() -> PostgreSQLSlotNameGenerator.getUniqueSlotName(pgConnection, dumperContext.getJobId())).thenReturn("slot-1");
            when(logicalReplication.createReplicationStream(eq(pgConnection), any(), eq("slot-1"), eq(2))).thenReturn(stream);
            mockVersionQuery(driverManagerMock, "(openGauss 2.0 build )");
            dumper.start();
        }
        verify(walEventConverter, times(2)).convert(eventCaptor.capture());
        AbstractRowEvent rowEvent = (AbstractRowEvent) eventCaptor.getAllValues().get(0);
        assertThat(rowEvent.getCsn(), is(40L));
    }
    
    private AbstractWALEvent createWALEventWithDecodeForOldVersion(final AtomicReference<Boolean> running, final int index) {
        AbstractWALEvent result;
        if (1 == index) {
            result = new BeginTXEvent(1L, 10L);
        } else if (2 == index) {
            WriteRowEvent rowEvent = new WriteRowEvent();
            rowEvent.setTableName("t_order");
            rowEvent.setSchemaName("public");
            result = rowEvent;
        } else {
            result = new CommitTXEvent(3L, 40L);
            running.set(false);
        }
        result.setLogSequenceNumber(new OpenGaussLogSequenceNumber(LogSequenceNumber.valueOf(index)));
        return result;
    }
    
    @Test
    void assertDumpIgnoreTransactionProcessEvents() throws ReflectiveOperationException, SQLException {
        MemoryPipelineChannel channel = new MemoryPipelineChannel(10, records -> {
        });
        IngestPosition position = new WALPosition(new OpenGaussLogSequenceNumber(LogSequenceNumber.valueOf(1L)));
        OpenGaussIncrementalDumper dumper = new OpenGaussIncrementalDumper(createDumperContext(false), position, channel, mock());
        OpenGaussLogicalReplication logicalReplication = mock(OpenGaussLogicalReplication.class);
        Plugins.getMemberAccessor().set(OpenGaussIncrementalDumper.class.getDeclaredField("logicalReplication"), dumper, logicalReplication);
        PgConnection pgConnection = mock(PgConnection.class);
        PGReplicationStream stream = mock(PGReplicationStream.class);
        IncrementalDumperContext dumperContext = getDumperContext(dumper);
        when(logicalReplication.createConnection((StandardPipelineDataSourceConfiguration) dumperContext.getCommonContext().getDataSourceConfig())).thenReturn(pgConnection);
        when(pgConnection.unwrap(PgConnection.class)).thenReturn(pgConnection);
        when(stream.getLastReceiveLSN()).thenReturn(LogSequenceNumber.valueOf(50L));
        when(stream.readPending()).thenReturn(ByteBuffer.wrap("1".getBytes()), ByteBuffer.wrap("2".getBytes()));
        Plugins.getMemberAccessor().set(OpenGaussIncrementalDumper.class.getDeclaredField("walEventConverter"), dumper, mock(WALEventConverter.class));
        AtomicReference<Boolean> running = getRunningState(dumper);
        try (
                MockedStatic<DriverManager> driverManagerMock = mockStatic(DriverManager.class);
                MockedStatic<PostgreSQLSlotNameGenerator> slotNameMock = mockStatic(PostgreSQLSlotNameGenerator.class);
                MockedConstruction<MppdbDecodingPlugin> ignoredConstruction = mockConstruction(MppdbDecodingPlugin.class, (mockedPlugin, context) -> {
                    AtomicInteger counter = new AtomicInteger();
                    when(mockedPlugin.decode(any(ByteBuffer.class), any(OpenGaussLogSequenceNumber.class)))
                            .thenAnswer(invocation -> createWALEventWithDecodeIgnoreTX(running, counter.incrementAndGet()));
                })) {
            slotNameMock.when(() -> PostgreSQLSlotNameGenerator.getUniqueSlotName(pgConnection, dumperContext.getJobId())).thenReturn("slot-1");
            when(logicalReplication.createReplicationStream(eq(pgConnection), any(), eq("slot-1"), eq(5))).thenReturn(stream);
            mockVersionQuery(driverManagerMock, "(openGauss 5.0 build )");
            dumper.start();
        }
        assertThat(channel.fetch(10, 0L).size(), is(1));
        verify(stream).close();
    }
    
    private AbstractWALEvent createWALEventWithDecodeIgnoreTX(final AtomicReference<Boolean> running, final int index) {
        AbstractWALEvent result;
        if (1 == index) {
            result = new BeginTXEvent(1L, 10L);
        } else {
            WriteRowEvent rowEvent = new WriteRowEvent();
            rowEvent.setTableName("t_order");
            rowEvent.setSchemaName("public");
            result = rowEvent;
            running.set(false);
        }
        result.setLogSequenceNumber(new OpenGaussLogSequenceNumber(LogSequenceNumber.valueOf(index)));
        return result;
    }
    
    private IncrementalDumperContext createDumperContext(final boolean decodeWithTX) {
        Map<String, Object> poolProps = new HashMap<>(3, 1F);
        poolProps.put("url", "jdbc:postgresql://localhost:5432/test_db");
        poolProps.put("username", "root");
        poolProps.put("password", "root");
        DumperCommonContext commonContext = new DumperCommonContext(null, new StandardPipelineDataSourceConfiguration(poolProps),
                new ActualAndLogicTableNameMapper(Collections.singletonMap(new ShardingSphereIdentifier("t_order"), new ShardingSphereIdentifier("t_order"))),
                new TableAndSchemaNameMapper(Collections.emptyMap()));
        return new IncrementalDumperContext(commonContext, "0101", decodeWithTX);
    }
    
    @SuppressWarnings("unchecked")
    private AtomicReference<Boolean> getRunningState(final OpenGaussIncrementalDumper dumper) throws ReflectiveOperationException {
        return (AtomicReference<Boolean>) Plugins.getMemberAccessor().get(AbstractPipelineLifecycleRunnable.class.getDeclaredField("running"), dumper);
    }
    
    private IncrementalDumperContext getDumperContext(final OpenGaussIncrementalDumper dumper) throws ReflectiveOperationException {
        return (IncrementalDumperContext) Plugins.getMemberAccessor().get(OpenGaussIncrementalDumper.class.getDeclaredField("dumperContext"), dumper);
    }
    
    private void mockVersionQuery(final MockedStatic<DriverManager> driverManagerMock, final String versionText) throws SQLException {
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);
        driverManagerMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString())).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery("SELECT version()")).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(1)).thenReturn(versionText);
    }
}
