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

package org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal;

import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.api.config.TableNameSchemaNameMapping;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.PlaceholderRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.api.metadata.ActualTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.ColumnName;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.data.pipeline.common.datasource.DefaultPipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.common.ingest.IngestDataChangeType;
import org.apache.shardingsphere.data.pipeline.common.metadata.loader.StandardPipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.decode.PostgreSQLLogSequenceNumber;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.AbstractRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.BeginTXEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.CommitTXEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.DeleteRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.PlaceholderEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.UpdateRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.WriteRowEvent;
import org.apache.shardingsphere.infra.exception.core.external.sql.type.generic.UnsupportedSQLOperationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;
import org.postgresql.replication.LogSequenceNumber;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WALEventConverterTest {
    
    private DumperConfiguration dumperConfig;
    
    private WALEventConverter walEventConverter;
    
    private final LogSequenceNumber logSequenceNumber = LogSequenceNumber.valueOf("0/14EFDB8");
    
    private PipelineTableMetaData pipelineTableMetaData;
    
    @BeforeEach
    void setUp() {
        dumperConfig = mockDumperConfiguration();
        PipelineDataSourceManager dataSourceManager = new DefaultPipelineDataSourceManager();
        walEventConverter = new WALEventConverter(dumperConfig, new StandardPipelineTableMetaDataLoader(dataSourceManager.getDataSource(dumperConfig.getDataSourceConfig())));
        initTableData(dumperConfig);
        pipelineTableMetaData = new PipelineTableMetaData("t_order", mockOrderColumnsMetaDataMap(), Collections.emptyList());
    }
    
    private DumperConfiguration mockDumperConfiguration() {
        DumperConfiguration result = new DumperConfiguration();
        result.setDataSourceConfig(new StandardPipelineDataSourceConfiguration("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=PostgreSQL", "root", "root"));
        result.setTableNameMap(Collections.singletonMap(new ActualTableName("t_order"), new LogicTableName("t_order")));
        result.setTableNameSchemaNameMapping(new TableNameSchemaNameMapping(Collections.emptyMap()));
        return result;
    }
    
    @SneakyThrows(SQLException.class)
    private void initTableData(final DumperConfiguration dumperConfig) {
        try (
                PipelineDataSourceManager dataSourceManager = new DefaultPipelineDataSourceManager();
                PipelineDataSourceWrapper dataSource = dataSourceManager.getDataSource(dumperConfig.getDataSourceConfig());
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id INT, status VARCHAR(12))");
            statement.execute("INSERT INTO t_order (order_id, user_id, status) VALUES (101, 1, 'OK'), (102, 1, 'OK')");
        }
    }
    
    private Map<String, PipelineColumnMetaData> mockOrderColumnsMetaDataMap() {
        return mockOrderColumnsMetaDataList().stream().collect(Collectors.toMap(PipelineColumnMetaData::getName, Function.identity()));
    }
    
    private List<PipelineColumnMetaData> mockOrderColumnsMetaDataList() {
        List<PipelineColumnMetaData> result = new LinkedList<>();
        result.add(new PipelineColumnMetaData(1, "order_id", Types.INTEGER, "INT", false, true, true));
        result.add(new PipelineColumnMetaData(1, "user_id", Types.INTEGER, "INT", false, false, false));
        result.add(new PipelineColumnMetaData(1, "status", Types.VARCHAR, "VARCHAR", false, false, false));
        return result;
    }
    
    @Test
    void assertWriteRowEventWithoutCustomColumns() throws ReflectiveOperationException {
        assertWriteRowEvent0(null, 3);
    }
    
    @Test
    void assertWriteRowEventWithCustomColumns() throws ReflectiveOperationException {
        assertWriteRowEvent0(mockTargetTableColumnsMap(), 1);
    }
    
    private void assertWriteRowEvent0(final Map<LogicTableName, Set<ColumnName>> targetTableColumnsMap, final int expectedColumnCount) throws ReflectiveOperationException {
        dumperConfig.setTargetTableColumnsMap(targetTableColumnsMap);
        WriteRowEvent rowsEvent = new WriteRowEvent();
        rowsEvent.setDatabaseName("");
        rowsEvent.setTableName("t_order");
        rowsEvent.setAfterRow(Arrays.asList(101, 1, "OK"));
        Method method = WALEventConverter.class.getDeclaredMethod("handleWriteRowEvent", WriteRowEvent.class, PipelineTableMetaData.class);
        DataRecord actual = (DataRecord) Plugins.getMemberAccessor().invoke(method, walEventConverter, rowsEvent, pipelineTableMetaData);
        assertThat(actual.getType(), is(IngestDataChangeType.INSERT));
        assertThat(actual.getColumnCount(), is(expectedColumnCount));
    }
    
    private Map<LogicTableName, Set<ColumnName>> mockTargetTableColumnsMap() {
        return Collections.singletonMap(new LogicTableName("t_order"), Collections.singleton(new ColumnName("order_id")));
    }
    
    @Test
    void assertConvertBeginTXEvent() {
        BeginTXEvent beginTXEvent = new BeginTXEvent(100);
        beginTXEvent.setLogSequenceNumber(new PostgreSQLLogSequenceNumber(logSequenceNumber));
        Record record = walEventConverter.convert(beginTXEvent);
        assertTrue(record instanceof PlaceholderRecord);
        assertThat(((WALPosition) record.getPosition()).getLogSequenceNumber().asLong(), is(21953976L));
    }
    
    @Test
    void assertConvertCommitTXEvent() {
        CommitTXEvent commitTXEvent = new CommitTXEvent(1, 3468L);
        commitTXEvent.setLogSequenceNumber(new PostgreSQLLogSequenceNumber(logSequenceNumber));
        Record record = walEventConverter.convert(commitTXEvent);
        assertTrue(record instanceof PlaceholderRecord);
        assertThat(((WALPosition) record.getPosition()).getLogSequenceNumber().asLong(), is(21953976L));
    }
    
    @Test
    void assertConvertWriteRowEvent() {
        Record record = walEventConverter.convert(mockWriteRowEvent());
        assertThat(record, instanceOf(DataRecord.class));
        assertThat(((DataRecord) record).getType(), is(IngestDataChangeType.INSERT));
    }
    
    @Test
    void assertConvertUpdateRowEvent() {
        Record record = walEventConverter.convert(mockUpdateRowEvent());
        assertThat(record, instanceOf(DataRecord.class));
        assertThat(((DataRecord) record).getType(), is(IngestDataChangeType.UPDATE));
    }
    
    @Test
    void assertConvertDeleteRowEvent() {
        Record record = walEventConverter.convert(mockDeleteRowEvent());
        assertThat(record, instanceOf(DataRecord.class));
        assertThat(((DataRecord) record).getType(), is(IngestDataChangeType.DELETE));
    }
    
    @Test
    void assertConvertPlaceholderEvent() {
        Record record = walEventConverter.convert(new PlaceholderEvent());
        assertThat(record, instanceOf(PlaceholderRecord.class));
    }
    
    @Test
    void assertUnknownTable() {
        assertInstanceOf(PlaceholderRecord.class, walEventConverter.convert(mockUnknownTableEvent()));
    }
    
    @Test
    void assertConvertFailure() {
        AbstractRowEvent event = new AbstractRowEvent() {
        };
        event.setDatabaseName("");
        event.setTableName("t_order");
        assertThrows(UnsupportedSQLOperationException.class, () -> walEventConverter.convert(event));
    }
    
    private AbstractRowEvent mockWriteRowEvent() {
        WriteRowEvent result = new WriteRowEvent();
        result.setDatabaseName("");
        result.setTableName("t_order");
        result.setAfterRow(Arrays.asList("id", "user_id"));
        return result;
    }
    
    private AbstractRowEvent mockUpdateRowEvent() {
        UpdateRowEvent result = new UpdateRowEvent();
        result.setDatabaseName("");
        result.setTableName("t_order");
        result.setAfterRow(Arrays.asList("id", "user_id"));
        return result;
    }
    
    private AbstractRowEvent mockDeleteRowEvent() {
        DeleteRowEvent result = new DeleteRowEvent();
        result.setDatabaseName("");
        result.setTableName("t_order");
        result.setPrimaryKeys(Collections.singletonList("id"));
        return result;
    }
    
    private AbstractRowEvent mockUnknownTableEvent() {
        WriteRowEvent result = new WriteRowEvent();
        result.setDatabaseName("");
        result.setTableName("t_other");
        return result;
    }
}
