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

import org.apache.shardingsphere.data.pipeline.api.type.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.constant.PipelineSQLOperationType;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.DumperCommonContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.IncrementalDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.mapper.ActualAndLogicTableNameMapper;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.mapper.TableAndSchemaNameMapper;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.PlaceholderRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.Record;
import org.apache.shardingsphere.infra.metadata.caseinsensitive.CaseInsensitiveIdentifier;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.StandardPipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.decode.PostgreSQLLogSequenceNumber;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.AbstractRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.BeginTXEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.CommitTXEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.DeleteRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.PlaceholderEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.UpdateRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.WriteRowEvent;
import org.apache.shardingsphere.infra.exception.core.external.sql.type.generic.UnsupportedSQLOperationException;
import org.junit.jupiter.api.AfterEach;
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
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WALEventConverterTest {
    
    private WALEventConverter walEventConverter;
    
    private final LogSequenceNumber logSequenceNumber = LogSequenceNumber.valueOf("0/14EFDB8");
    
    private PipelineTableMetaData pipelineTableMetaData;
    
    private PipelineDataSourceManager dataSourceManager;
    
    @BeforeEach
    void setUp() throws SQLException {
        IncrementalDumperContext dumperContext = mockDumperContext();
        dataSourceManager = new PipelineDataSourceManager();
        walEventConverter = new WALEventConverter(dumperContext, new StandardPipelineTableMetaDataLoader(dataSourceManager.getDataSource(dumperContext.getCommonContext().getDataSourceConfig())));
        initTableData(dumperContext);
        pipelineTableMetaData = new PipelineTableMetaData("t_order", mockOrderColumnsMetaDataMap(), Collections.emptyList());
    }
    
    private IncrementalDumperContext mockDumperContext() {
        DumperCommonContext commonContext = new DumperCommonContext(null,
                new StandardPipelineDataSourceConfiguration("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=PostgreSQL", "root", "root"),
                new ActualAndLogicTableNameMapper(Collections.singletonMap(new CaseInsensitiveIdentifier("t_order"), new CaseInsensitiveIdentifier("t_order"))),
                new TableAndSchemaNameMapper(Collections.emptyMap()));
        return new IncrementalDumperContext(commonContext, null, false);
    }
    
    private void initTableData(final IncrementalDumperContext dumperContext) throws SQLException {
        try (
                PipelineDataSourceManager dataSourceManager = new PipelineDataSourceManager();
                PipelineDataSourceWrapper dataSource = dataSourceManager.getDataSource(dumperContext.getCommonContext().getDataSourceConfig());
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id INT, status VARCHAR(12))");
            statement.execute("INSERT INTO t_order (order_id, user_id, status) VALUES (101, 1, 'OK'), (102, 1, 'OK')");
        }
    }
    
    private Map<CaseInsensitiveIdentifier, PipelineColumnMetaData> mockOrderColumnsMetaDataMap() {
        return mockOrderColumnsMetaDataList().stream().collect(Collectors.toMap(metaData -> new CaseInsensitiveIdentifier(metaData.getName()), Function.identity()));
    }
    
    private List<PipelineColumnMetaData> mockOrderColumnsMetaDataList() {
        List<PipelineColumnMetaData> result = new LinkedList<>();
        result.add(new PipelineColumnMetaData(1, "order_id", Types.INTEGER, "INT", false, true, true));
        result.add(new PipelineColumnMetaData(1, "user_id", Types.INTEGER, "INT", false, false, false));
        result.add(new PipelineColumnMetaData(1, "status", Types.VARCHAR, "VARCHAR", false, false, false));
        return result;
    }
    
    @AfterEach
    void clean() {
        dataSourceManager.close();
    }
    
    @Test
    void assertWriteRowEvent() throws ReflectiveOperationException {
        DataRecord actual = getDataRecord(createWriteRowEvent());
        assertThat(actual.getType(), is(PipelineSQLOperationType.INSERT));
        assertThat(actual.getColumnCount(), is(3));
    }
    
    private WriteRowEvent createWriteRowEvent() {
        WriteRowEvent result = new WriteRowEvent();
        result.setSchemaName("");
        result.setTableName("t_order");
        result.setAfterRow(Arrays.asList(101, 1, "OK"));
        return result;
    }
    
    private DataRecord getDataRecord(final WriteRowEvent rowsEvent) throws ReflectiveOperationException {
        Method method = WALEventConverter.class.getDeclaredMethod("handleWriteRowEvent", WriteRowEvent.class, PipelineTableMetaData.class);
        return (DataRecord) Plugins.getMemberAccessor().invoke(method, walEventConverter, rowsEvent, pipelineTableMetaData);
    }
    
    @Test
    void assertConvertBeginTXEvent() {
        BeginTXEvent beginTXEvent = new BeginTXEvent(100L, null);
        beginTXEvent.setLogSequenceNumber(new PostgreSQLLogSequenceNumber(logSequenceNumber));
        Record record = walEventConverter.convert(beginTXEvent);
        assertInstanceOf(PlaceholderRecord.class, record);
        assertThat(((WALPosition) record.getPosition()).getLogSequenceNumber().asString(), is(logSequenceNumber.asString()));
    }
    
    @Test
    void assertConvertCommitTXEvent() {
        CommitTXEvent commitTXEvent = new CommitTXEvent(1, 3468L);
        commitTXEvent.setLogSequenceNumber(new PostgreSQLLogSequenceNumber(logSequenceNumber));
        Record record = walEventConverter.convert(commitTXEvent);
        assertInstanceOf(PlaceholderRecord.class, record);
        assertThat(((WALPosition) record.getPosition()).getLogSequenceNumber().asString(), is(logSequenceNumber.asString()));
    }
    
    @Test
    void assertConvertWriteRowEvent() {
        Record record = walEventConverter.convert(mockWriteRowEvent());
        assertThat(record, instanceOf(DataRecord.class));
        assertThat(((DataRecord) record).getType(), is(PipelineSQLOperationType.INSERT));
    }
    
    @Test
    void assertConvertUpdateRowEvent() {
        Record record = walEventConverter.convert(mockUpdateRowEvent());
        assertThat(record, instanceOf(DataRecord.class));
        assertThat(((DataRecord) record).getType(), is(PipelineSQLOperationType.UPDATE));
    }
    
    @Test
    void assertConvertDeleteRowEvent() {
        Record record = walEventConverter.convert(mockDeleteRowEvent());
        assertThat(record, instanceOf(DataRecord.class));
        assertThat(((DataRecord) record).getType(), is(PipelineSQLOperationType.DELETE));
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
        event.setSchemaName("");
        event.setTableName("t_order");
        assertThrows(UnsupportedSQLOperationException.class, () -> walEventConverter.convert(event));
    }
    
    private AbstractRowEvent mockWriteRowEvent() {
        WriteRowEvent result = new WriteRowEvent();
        result.setSchemaName("");
        result.setTableName("t_order");
        result.setAfterRow(Arrays.asList("id", "user_id"));
        return result;
    }
    
    private AbstractRowEvent mockUpdateRowEvent() {
        UpdateRowEvent result = new UpdateRowEvent();
        result.setSchemaName("");
        result.setTableName("t_order");
        result.setAfterRow(Arrays.asList("id", "user_id"));
        return result;
    }
    
    private AbstractRowEvent mockDeleteRowEvent() {
        DeleteRowEvent result = new DeleteRowEvent();
        result.setSchemaName("");
        result.setTableName("t_order");
        result.setPrimaryKeys(Collections.singletonList("id"));
        return result;
    }
    
    private AbstractRowEvent mockUnknownTableEvent() {
        WriteRowEvent result = new WriteRowEvent();
        result.setSchemaName("");
        result.setTableName("t_other");
        return result;
    }
}
