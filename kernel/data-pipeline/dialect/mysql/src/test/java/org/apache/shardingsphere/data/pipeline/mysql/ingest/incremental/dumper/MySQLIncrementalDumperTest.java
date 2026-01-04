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

package org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.dumper;

import org.apache.shardingsphere.data.pipeline.api.type.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.core.constant.PipelineSQLOperationType;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSource;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.DumperCommonContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.IncrementalDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.mapper.ActualAndLogicTableNameMapper;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.mapper.TableAndSchemaNameMapper;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.PlaceholderRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.event.MySQLBaseBinlogEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.event.PlaceholderBinlogEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.event.rows.MySQLBaseRowsBinlogEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.event.rows.MySQLDeleteRowsBinlogEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.event.rows.MySQLUpdateRowsBinlogEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.event.rows.MySQLWriteRowsBinlogEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.position.MySQLBinlogPosition;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.client.MySQLBinlogClient;
import org.apache.shardingsphere.infra.metadata.identifier.ShardingSphereIdentifier;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDriver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MySQLIncrementalDumperTest {
    
    private IncrementalDumperContext dumperContext;
    
    private PipelineTableMetaDataLoader metaDataLoader;
    
    @BeforeAll
    static void init() throws ClassNotFoundException {
        Class.forName(MockedDriver.class.getName());
    }
    
    @BeforeEach
    void setUp() throws SQLException {
        dumperContext = createDumperContext();
        initTableData(dumperContext);
        metaDataLoader = mock(PipelineTableMetaDataLoader.class);
        when(metaDataLoader.getTableMetaData(any(), any())).thenReturn(new PipelineTableMetaData("t_order", mockOrderColumnsMetaDataMap(), Collections.emptyList()));
    }
    
    private IncrementalDumperContext createDumperContext() {
        Map<String, Object> poolProps = new HashMap<>(3, 1F);
        poolProps.put("url", "jdbc:mock://127.0.0.1:3306/test");
        poolProps.put("username", "root");
        poolProps.put("password", "root");
        DumperCommonContext commonContext = new DumperCommonContext(null,
                new StandardPipelineDataSourceConfiguration(poolProps),
                new ActualAndLogicTableNameMapper(Collections.singletonMap(new ShardingSphereIdentifier("t_order"), new ShardingSphereIdentifier("t_order"))),
                new TableAndSchemaNameMapper(Collections.emptyMap()));
        return new IncrementalDumperContext(commonContext, null, false);
    }
    
    private void initTableData(final IncrementalDumperContext dumperContext) throws SQLException {
        try (
                PipelineDataSourceManager dataSourceManager = new PipelineDataSourceManager();
                PipelineDataSource dataSource = dataSourceManager.getDataSource(dumperContext.getCommonContext().getDataSourceConfig());
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id INT, status VARCHAR(12))");
            statement.execute("INSERT INTO t_order (order_id, user_id, status) VALUES (101, 1, 'OK'), (102, 1, 'OK')");
        }
    }
    
    private Map<ShardingSphereIdentifier, PipelineColumnMetaData> mockOrderColumnsMetaDataMap() {
        return mockOrderColumnsMetaDataList().stream().collect(Collectors.toMap(metaData -> new ShardingSphereIdentifier(metaData.getName()), Function.identity()));
    }
    
    private List<PipelineColumnMetaData> mockOrderColumnsMetaDataList() {
        List<PipelineColumnMetaData> result = new LinkedList<>();
        result.add(new PipelineColumnMetaData(1, "order_id", Types.INTEGER, "INT", false, true, true));
        result.add(new PipelineColumnMetaData(1, "user_id", Types.INTEGER, "INT", false, false, false));
        result.add(new PipelineColumnMetaData(1, "status", Types.VARCHAR, "VARCHAR", false, false, false));
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertRunBlockingCoversAllBranches() throws ReflectiveOperationException, InterruptedException {
        MySQLBaseRowsBinlogEvent unsupportedEvent = mock(MySQLBaseRowsBinlogEvent.class);
        when(unsupportedEvent.getDatabaseName()).thenReturn("test");
        when(unsupportedEvent.getTableName()).thenReturn("t_order");
        MySQLWriteRowsBinlogEvent filteredEvent = new MySQLWriteRowsBinlogEvent("binlog-000001", 13L, 2L, "other_db", "t_order", Collections.singletonList(new Serializable[]{101, 1, "OK"}));
        MySQLWriteRowsBinlogEvent writeEvent = new MySQLWriteRowsBinlogEvent("binlog-000001", 4L, 5L, "test", "t_order", Collections.singletonList(new Serializable[]{101, 1, "OK"}));
        MySQLUpdateRowsBinlogEvent updateEvent = new MySQLUpdateRowsBinlogEvent("binlog-000001", 5L, 6L, "test", "t_order",
                Collections.singletonList(new Serializable[]{101, 1, "OK"}), Collections.singletonList(new Serializable[]{101, 1, "UPDATED"}));
        MySQLDeleteRowsBinlogEvent deleteEvent = new MySQLDeleteRowsBinlogEvent("binlog-000001", 6L, 7L, "test", "t_order", Collections.singletonList(new Serializable[]{101, 1, "OK"}));
        List<MySQLBaseBinlogEvent> firstPollEvents = Arrays.asList(new PlaceholderBinlogEvent("binlog-000001", 3L, 1L), writeEvent, updateEvent, deleteEvent, filteredEvent, unsupportedEvent);
        PipelineChannel channel = mock(PipelineChannel.class);
        MySQLIncrementalDumper dumper = new MySQLIncrementalDumper(dumperContext, new MySQLBinlogPosition("binlog-000001", 4L), channel, metaDataLoader);
        MySQLBinlogClient client = mock(MySQLBinlogClient.class);
        AtomicInteger counter = new AtomicInteger();
        when(client.poll()).thenAnswer(invocation -> {
            if (0 == counter.getAndIncrement()) {
                return firstPollEvents;
            }
            dumper.stop();
            return Collections.singletonList(unsupportedEvent);
        });
        Plugins.getMemberAccessor().set(MySQLIncrementalDumper.class.getDeclaredField("client"), dumper, client);
        Thread dumperThread = new Thread(dumper::start);
        dumperThread.start();
        dumperThread.join(1000L);
        verify(client).connect();
        verify(client).subscribe("binlog-000001", 4L);
        verify(client, timeout(1000L)).closeChannel(true);
        ArgumentCaptor<List<Record>> captor = ArgumentCaptor.forClass(List.class);
        verify(channel, timeout(1000L)).push(captor.capture());
        List<Record> pushed = captor.getValue();
        assertThat(pushed.size(), is(5));
        assertThat(pushed.get(0), isA(PlaceholderRecord.class));
        assertThat(pushed.get(0).getCommitTime(), is(1000L));
        assertThat(((DataRecord) pushed.get(1)).getType(), is(PipelineSQLOperationType.INSERT));
        DataRecord updatedRecord = (DataRecord) pushed.get(2);
        assertThat(updatedRecord.getType(), is(PipelineSQLOperationType.UPDATE));
        assertTrue(updatedRecord.getColumn(2).isUpdated());
        assertThat(((DataRecord) pushed.get(3)).getType(), is(PipelineSQLOperationType.DELETE));
        assertThat(pushed.get(4), isA(PlaceholderRecord.class));
        assertFalse(dumperThread.isAlive());
    }
}
