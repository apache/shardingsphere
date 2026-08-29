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

package org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal;

import org.apache.shardingsphere.data.pipeline.api.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.type.StandardPipelineDataSourceConfiguration;
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
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.StandardPipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.decode.PostgreSQLLogSequenceNumber;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.AbstractRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.BeginTXEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.CommitTXEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.DeleteRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.PlaceholderEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.UpdateRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.WriteRowEvent;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.metadata.identifier.ShardingSphereIdentifier;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.postgresql.replication.LogSequenceNumber;

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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class WALEventConverterTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
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
        Map<String, Object> poolProps = new HashMap<>(3, 1F);
        poolProps.put("url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=PostgreSQL");
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
    
    @AfterEach
    void clean() {
        dataSourceManager.close();
    }
    
    @Test
    void assertWriteRowEvent() {
        DataRecord actual = (DataRecord) walEventConverter.convert(createWriteRowEvent(""));
        assertThat(actual.getType(), is(PipelineSQLOperationType.INSERT));
        assertThat(actual.getColumnCount(), is(3));
    }
    
    private WriteRowEvent createWriteRowEvent(final String schemaName) {
        WriteRowEvent result = new WriteRowEvent();
        result.setSchemaName(schemaName);
        result.setTableName("t_order");
        result.setAfterRow(Arrays.asList(101, 1, "OK"));
        return result;
    }
    
    @Test
    void assertConvertBeginTXEvent() {
        BeginTXEvent beginTXEvent = new BeginTXEvent(100L, null);
        beginTXEvent.setLogSequenceNumber(new PostgreSQLLogSequenceNumber(logSequenceNumber));
        Record record = walEventConverter.convert(beginTXEvent);
        assertThat(record, isA(PlaceholderRecord.class));
        assertThat(((WALPosition) record.getPosition()).getLogSequenceNumber().asString(), is(logSequenceNumber.asString()));
    }
    
    @Test
    void assertConvertCommitTXEvent() {
        CommitTXEvent commitTXEvent = new CommitTXEvent(1L, 3468L);
        commitTXEvent.setLogSequenceNumber(new PostgreSQLLogSequenceNumber(logSequenceNumber));
        Record record = walEventConverter.convert(commitTXEvent);
        assertThat(record, isA(PlaceholderRecord.class));
        assertThat(((WALPosition) record.getPosition()).getLogSequenceNumber().asString(), is(logSequenceNumber.asString()));
    }
    
    @Test
    void assertConvertWriteRowEvent() {
        Record record = walEventConverter.convert(mockWriteRowEvent());
        assertThat(record, isA(DataRecord.class));
        assertThat(((DataRecord) record).getType(), is(PipelineSQLOperationType.INSERT));
    }
    
    @Test
    void assertConvertUpdateRowEvent() {
        Record record = walEventConverter.convert(mockUpdateRowEvent());
        assertThat(record, isA(DataRecord.class));
        assertThat(((DataRecord) record).getType(), is(PipelineSQLOperationType.UPDATE));
    }
    
    @Test
    void assertConvertDeleteRowEvent() {
        Record record = walEventConverter.convert(mockDeleteRowEvent());
        assertThat(record, isA(DataRecord.class));
        assertThat(((DataRecord) record).getType(), is(PipelineSQLOperationType.DELETE));
    }
    
    @Test
    void assertConvertPlaceholderEvent() {
        Record record = walEventConverter.convert(new PlaceholderEvent());
        assertThat(record, isA(PlaceholderRecord.class));
    }
    
    @Test
    void assertUnknownTable() {
        assertThat(walEventConverter.convert(mockUnknownTableEvent()), isA(PlaceholderRecord.class));
    }
    
    @Test
    void assertConvertWithQuotedUpperCaseTable() {
        PipelineTableMetaDataLoader metaDataLoader = mock(PipelineTableMetaDataLoader.class);
        when(metaDataLoader.getTableMetaData("UPPER_SCHEMA", "UPPER_TABLE")).thenReturn(pipelineTableMetaData);
        WALEventConverter converter = createSchemaAwareWALEventConverter("UPPER_TABLE", "UPPER_SCHEMA", metaDataLoader);
        WriteRowEvent event = createWriteRowEvent("\"UPPER_SCHEMA\"");
        event.setTableName("\"UPPER_TABLE\"");
        assertThat(converter.convert(event), isA(DataRecord.class));
        verify(metaDataLoader).getTableMetaData("UPPER_SCHEMA", "UPPER_TABLE");
    }
    
    @Test
    void assertConvertWithQuotedTableCaseCollision() {
        PipelineTableMetaDataLoader metaDataLoader = mock(PipelineTableMetaDataLoader.class);
        WALEventConverter converter = createSchemaAwareWALEventConverter("UPPER_TABLE", "UPPER_SCHEMA", metaDataLoader);
        WriteRowEvent event = createWriteRowEvent("\"UPPER_SCHEMA\"");
        event.setTableName("\"upper_table\"");
        assertThat(converter.convert(event), isA(PlaceholderRecord.class));
        verifyNoInteractions(metaDataLoader);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideDifferentConcreteSchemaNames")
    void assertConvertWithDifferentConcreteSchema(final String name, final String expectedSchemaName, final String eventSchemaName) {
        PipelineTableMetaDataLoader metaDataLoader = mock(PipelineTableMetaDataLoader.class);
        when(metaDataLoader.getTableMetaData(expectedSchemaName, "t_order")).thenReturn(pipelineTableMetaData);
        WALEventConverter converter = createSchemaAwareWALEventConverter(expectedSchemaName, metaDataLoader);
        Record actual = converter.convert(createWriteRowEvent(eventSchemaName));
        assertThat(actual, isA(PlaceholderRecord.class));
        verifyNoInteractions(metaDataLoader);
    }
    
    private static Stream<Arguments> provideDifferentConcreteSchemaNames() {
        return Stream.of(
                Arguments.of("different schema", "test", "public"),
                Arguments.of("quoted upper-case schema", "public", "\"PUBLIC\""),
                Arguments.of("quoted schema and unquoted token", "CaseSchema", "CaseSchema"),
                Arguments.of("quoted and unquoted case collision", "CaseSchema", "caseschema"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideSameConcreteSchemaNames")
    void assertConvertWithSameConcreteSchema(final String name, final String expectedSchemaName, final String eventSchemaName) {
        PipelineTableMetaDataLoader metaDataLoader = mock(PipelineTableMetaDataLoader.class);
        when(metaDataLoader.getTableMetaData(expectedSchemaName, "t_order")).thenReturn(pipelineTableMetaData);
        WALEventConverter converter = createSchemaAwareWALEventConverter(expectedSchemaName, metaDataLoader);
        Record actual = converter.convert(createWriteRowEvent(eventSchemaName));
        assertThat(actual, isA(DataRecord.class));
        verify(metaDataLoader).getTableMetaData(expectedSchemaName, "t_order");
    }
    
    private static Stream<Arguments> provideSameConcreteSchemaNames() {
        return Stream.of(
                Arguments.of("lower-case schema", "public", "public"),
                Arguments.of("unquoted mixed-case token", "caseschema", "CaseSchema"),
                Arguments.of("quoted upper-case schema", "PUBLIC", "\"PUBLIC\""),
                Arguments.of("quoted mixed-case schema", "CaseSchema", "\"CaseSchema\""));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideUnspecifiedSchema")
    void assertConvertWithUnspecifiedSchema(final String name, final String expectedSchemaName, final String eventSchemaName) {
        PipelineTableMetaDataLoader metaDataLoader = mock(PipelineTableMetaDataLoader.class);
        when(metaDataLoader.getTableMetaData(expectedSchemaName, "t_order")).thenReturn(pipelineTableMetaData);
        WALEventConverter converter = createSchemaAwareWALEventConverter(expectedSchemaName, metaDataLoader);
        Record actual = converter.convert(createWriteRowEvent(eventSchemaName));
        assertThat(actual, isA(DataRecord.class));
        verify(metaDataLoader).getTableMetaData(expectedSchemaName, "t_order");
    }
    
    private static Stream<Arguments> provideUnspecifiedSchema() {
        return Stream.of(
                Arguments.of("null expected schema", null, "public"),
                Arguments.of("empty expected schema", "", "public"),
                Arguments.of("wildcard expected schema", "*", "public"),
                Arguments.of("null event schema", "public", null),
                Arguments.of("empty event schema", "public", ""),
                Arguments.of("wildcard event schema", "public", "*"));
    }
    
    private WALEventConverter createSchemaAwareWALEventConverter(final String expectedSchemaName, final PipelineTableMetaDataLoader metaDataLoader) {
        return createSchemaAwareWALEventConverter("t_order", expectedSchemaName, metaDataLoader);
    }
    
    private WALEventConverter createSchemaAwareWALEventConverter(final String actualTableName, final String expectedSchemaName, final PipelineTableMetaDataLoader metaDataLoader) {
        PipelineDataSourceConfiguration dataSourceConfig = mock(PipelineDataSourceConfiguration.class);
        when(dataSourceConfig.getDatabaseType()).thenReturn(databaseType);
        DumperCommonContext commonContext = new DumperCommonContext(null, dataSourceConfig,
                new ActualAndLogicTableNameMapper(Collections.singletonMap(new ShardingSphereIdentifier(actualTableName), new ShardingSphereIdentifier("t_order"))),
                new TableAndSchemaNameMapper(Collections.singletonMap("t_order", expectedSchemaName)));
        return new WALEventConverter(new IncrementalDumperContext(commonContext, null, false), metaDataLoader);
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
