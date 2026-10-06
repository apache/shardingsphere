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

package org.apache.shardingsphere.data.pipeline.core.sqlbuilder.sql;

import org.apache.shardingsphere.data.pipeline.core.constant.PipelineSQLOperationType;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.placeholder.IngestPlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.NormalColumn;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.RecordUtils;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.dialect.DialectPipelineSQLBuilder;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContext;
import org.apache.shardingsphere.infra.metadata.identifier.IdentifierCasePolicyResolver;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(DatabaseTypedSPILoader.class)
class PipelineImportSQLBuilderTest {
    
    private final PipelineImportSQLBuilder sqlBuilder = new PipelineImportSQLBuilder(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"),
            new DatabaseIdentifierContext(IdentifierCasePolicyResolver.resolveProtocol(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"))), false);
    
    private final PipelineImportSQLBuilder actualTableSQLBuilder = new PipelineImportSQLBuilder(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"),
            new DatabaseIdentifierContext(IdentifierCasePolicyFactory.newLowerCasePolicySet()), true);
    
    @Test
    void assertBuildInsertSQL() {
        String actual = sqlBuilder.buildInsertSQL(null, createDataRecordWithUniqueKey());
        assertThat(actual, is("INSERT INTO foo_tbl(id,foo_col,col1,col2,col3) VALUES(?,?,?,?,?)"));
    }
    
    @Test
    void assertBuildUpdateSQLWithUniqueKey() {
        DataRecord dataRecord = createDataRecordWithUniqueKey();
        String actual = sqlBuilder.buildUpdateSQL(null, dataRecord, mockConditionColumns(dataRecord));
        assertThat(actual, is("UPDATE foo_tbl SET col1 = ?,col2 = ?,col3 = ? WHERE id = ? AND foo_col = ?"));
    }
    
    @Test
    void assertBuildUpdateSQLWithoutUniqueKey() {
        DataRecord dataRecord = createDataRecordWithoutUniqueKey();
        String actual = sqlBuilder.buildUpdateSQL(null, dataRecord, mockConditionColumns(dataRecord));
        assertThat(actual, is("UPDATE foo_tbl SET foo_col = ? WHERE id = ? AND foo_col = ?"));
    }
    
    @Test
    void assertBuildUpdateSQLWithoutConditionColumns() {
        String actual = sqlBuilder.buildUpdateSQL(null, createDataRecordWithUniqueKey(), Collections.emptyList());
        assertThat(actual, is("UPDATE foo_tbl SET col1 = ?,col2 = ?,col3 = ?"));
    }
    
    @Test
    void assertBuildDeleteSQLWithUniqueKey() {
        DataRecord dataRecord = createDataRecordWithUniqueKey();
        String actual = sqlBuilder.buildDeleteSQL(null, dataRecord, mockConditionColumns(dataRecord));
        assertThat(actual, is("DELETE FROM foo_tbl WHERE id = ? AND foo_col = ?"));
    }
    
    @Test
    void assertBuildDeleteSQLWithoutUniqueKey() {
        String actual = sqlBuilder.buildDeleteSQL(null, createDataRecordWithoutUniqueKey(), RecordUtils.extractConditionColumns(createDataRecordWithoutUniqueKey(), Collections.emptySet()));
        assertThat(actual, is("DELETE FROM foo_tbl WHERE id = ? AND foo_col = ?"));
    }
    
    @Test
    void assertBuildDeleteSQLWithoutConditionColumns() {
        String actual = sqlBuilder.buildDeleteSQL(null, createDataRecordWithUniqueKey(), Collections.emptyList());
        assertThat(actual, is("DELETE FROM foo_tbl"));
    }
    
    private Collection<Column> mockConditionColumns(final DataRecord dataRecord) {
        return RecordUtils.extractConditionColumns(dataRecord, Collections.singleton("foo_col"));
    }
    
    private DataRecord createDataRecordWithUniqueKey() {
        DataRecord result = new DataRecord(PipelineSQLOperationType.INSERT, "foo_tbl", new IngestPlaceholderPosition(), 4);
        result.addColumn(new NormalColumn("id", "", false, true));
        result.addColumn(new NormalColumn("foo_col", "", false, false));
        for (int i = 1; i <= 3; i++) {
            result.addColumn(new NormalColumn("col" + i, "", true, false));
        }
        return result;
    }
    
    private DataRecord createDataRecordWithoutUniqueKey() {
        DataRecord result = new DataRecord(PipelineSQLOperationType.INSERT, "foo_tbl", new IngestPlaceholderPosition(), 4);
        result.addColumn(new NormalColumn("id", "", false, false));
        result.addColumn(new NormalColumn("foo_col", "", true, false));
        return result;
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getActualTableSchemas")
    void assertBuildInsertSQLWithActualTableSchema(final String name, final String schemaName, final String expectedTableName) {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        when((Object) DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType))
                .thenReturn(TypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType));
        when((Object) DatabaseTypedSPILoader.getService(DialectPipelineSQLBuilder.class, databaseType)).thenReturn(mock(DialectPipelineSQLBuilder.class));
        PipelineImportSQLBuilder builder = new PipelineImportSQLBuilder(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"),
                new DatabaseIdentifierContext(IdentifierCasePolicyFactory.newLowerCasePolicySet()), true);
        DataRecord dataRecord = new DataRecord(PipelineSQLOperationType.INSERT, "T_Order", new IngestPlaceholderPosition(), 1);
        dataRecord.addColumn(new NormalColumn("Value", "foo", true, false));
        assertThat(builder.buildInsertSQL(schemaName, dataRecord), is("INSERT INTO " + expectedTableName + "(\"value\") VALUES(?)"));
    }
    
    private static Stream<Arguments> getActualTableSchemas() {
        return Stream.of(
                Arguments.of("unquoted schema", "Foo_SCHEMA", "\"foo_schema\".\"T_Order\""),
                Arguments.of("quoted schema", "\"Foo_SCHEMA\"", "\"Foo_SCHEMA\".\"T_Order\""),
                Arguments.of("empty schema", "", "\"T_Order\""),
                Arguments.of("absent schema", null, "\"T_Order\""));
    }
    
    @Test
    void assertBuildInsertSQLWithActualTableName() {
        String actual = actualTableSQLBuilder.buildInsertSQL("UnusedSchema", createDataRecordWithActualTableName());
        assertThat(actual, is("INSERT INTO T_Order(id,value) VALUES(?,?)"));
    }
    
    private DataRecord createDataRecordWithActualTableName() {
        DataRecord result = new DataRecord(PipelineSQLOperationType.INSERT, "T_Order", new IngestPlaceholderPosition(), 2);
        result.addColumn(new NormalColumn("Id", 1, false, true));
        result.addColumn(new NormalColumn("Value", "foo", true, false));
        return result;
    }
    
    @Test
    void assertBuildUpdateSQLWithActualTableName() {
        DataRecord dataRecord = createDataRecordWithActualTableName();
        String actual = actualTableSQLBuilder.buildUpdateSQL(null, dataRecord, Collections.singleton(dataRecord.getColumns().get(0)));
        assertThat(actual, is("UPDATE T_Order SET value = ? WHERE id = ?"));
    }
    
    @Test
    void assertBuildDeleteSQLWithActualTableName() {
        DataRecord dataRecord = createDataRecordWithActualTableName();
        String actual = actualTableSQLBuilder.buildDeleteSQL(null, dataRecord, Collections.singleton(dataRecord.getColumns().get(0)));
        assertThat(actual, is("DELETE FROM T_Order WHERE id = ?"));
    }
    
}
