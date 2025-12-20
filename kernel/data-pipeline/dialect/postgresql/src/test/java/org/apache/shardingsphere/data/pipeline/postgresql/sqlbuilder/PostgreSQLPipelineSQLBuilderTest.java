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

package org.apache.shardingsphere.data.pipeline.postgresql.sqlbuilder;

import org.apache.shardingsphere.data.pipeline.core.constant.PipelineSQLOperationType;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.NormalColumn;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.dialect.DialectPipelineSQLBuilder;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.WALPosition;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.decode.PostgreSQLLogSequenceNumber;
import org.apache.shardingsphere.data.pipeline.postgresql.sqlbuilder.ddl.column.PostgreSQLColumnPropertiesAppender;
import org.apache.shardingsphere.data.pipeline.postgresql.sqlbuilder.ddl.constraints.PostgreSQLConstraintsPropertiesAppender;
import org.apache.shardingsphere.data.pipeline.postgresql.sqlbuilder.ddl.index.PostgreSQLIndexSQLGenerator;
import org.apache.shardingsphere.data.pipeline.postgresql.sqlbuilder.ddl.table.PostgreSQLTablePropertiesLoader;
import org.apache.shardingsphere.data.pipeline.postgresql.sqlbuilder.template.PostgreSQLPipelineFreemarkerManager;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.postgresql.replication.LogSequenceNumber;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class PostgreSQLPipelineSQLBuilderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    private final DialectPipelineSQLBuilder sqlBuilder = DatabaseTypedSPILoader.getService(DialectPipelineSQLBuilder.class, databaseType);
    
    @Test
    void assertBuildCreateSchemaSQL() {
        Optional<String> actual = sqlBuilder.buildCreateSchemaSQL("foo_schema");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("CREATE SCHEMA IF NOT EXISTS foo_schema"));
    }
    
    @Test
    void assertBuildInsertSQLOnDuplicateClauseWithEmptyUniqueKey() {
        Optional<String> actual = sqlBuilder.buildInsertOnDuplicateClause(
                new DataRecord(PipelineSQLOperationType.INSERT, "foo_tbl", new WALPosition(new PostgreSQLLogSequenceNumber(LogSequenceNumber.valueOf(100L))), 2));
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertBuildInsertSQLOnDuplicateClause() {
        Optional<String> actual = sqlBuilder.buildInsertOnDuplicateClause(createDataRecord());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("ON CONFLICT (\"order_id\") DO UPDATE SET \"user_id\"=EXCLUDED.\"user_id\",\"status\"=EXCLUDED.\"status\""));
    }
    
    private DataRecord createDataRecord() {
        DataRecord result = new DataRecord(PipelineSQLOperationType.INSERT, "foo_tbl", new WALPosition(new PostgreSQLLogSequenceNumber(LogSequenceNumber.valueOf(100L))), 2);
        result.addColumn(new NormalColumn("order_id", 1, true, true));
        result.addColumn(new NormalColumn("user_id", 2, true, false));
        result.addColumn(new NormalColumn("status", "ok", true, false));
        return result;
    }
    
    @Test
    void assertBuildCheckEmptyTableSQL() {
        assertThat(sqlBuilder.buildCheckEmptyTableSQL("foo_tbl"), is("SELECT * FROM foo_tbl LIMIT 1"));
    }
    
    @Test
    void assertBuildEstimatedCountSQL() {
        Optional<String> actual = sqlBuilder.buildEstimatedCountSQL("foo_catalog", "foo_tbl");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("SELECT reltuples::integer FROM pg_class WHERE oid='foo_tbl'::regclass::oid;"));
    }
    
    @Test
    void assertBuildCRC32SQL() {
        Optional<String> actual = sqlBuilder.buildCRC32SQL("foo_tbl", "foo_col");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("SELECT pg_catalog.pg_checksum_table('foo_tbl', true)"));
    }
    
    @Test
    void assertBuildQueryCurrentPositionSQL() {
        Optional<String> actual = sqlBuilder.buildQueryCurrentPositionSQL();
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("SELECT * FROM pg_current_wal_lsn()"));
    }
    
    @Test
    void assertWrapWithPageQuery() {
        assertThat(sqlBuilder.wrapWithPageQuery("SELECT * FROM foo_tbl"), is("SELECT * FROM foo_tbl LIMIT ?"));
    }
    
    @Test
    void assertBuildCreateTableSQLs() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getDatabaseMajorVersion()).thenReturn(10);
        when(metaData.getDatabaseMinorVersion()).thenReturn(1);
        Map<String, Object> materials = new HashMap<>(1, 1F);
        Collection<Map<String, Object>> columns = new ArrayList<>(3);
        Map<String, Object> arrayColumn = new HashMap<>(2, 1F);
        arrayColumn.put("cltype", "int4[]");
        columns.add(arrayColumn);
        Map<String, Object> normalColumn = new HashMap<>(2, 1F);
        normalColumn.put("cltype", "text");
        columns.add(normalColumn);
        Map<String, Object> columnWithoutType = new HashMap<>(1, 1F);
        columnWithoutType.put("name", "no_type");
        columns.add(columnWithoutType);
        materials.put("columns", columns);
        try (
                MockedStatic<PostgreSQLPipelineFreemarkerManager> freemarkerManager = mockStatic(PostgreSQLPipelineFreemarkerManager.class);
                MockedConstruction<PostgreSQLTablePropertiesLoader> ignoredLoader =
                        mockConstruction(PostgreSQLTablePropertiesLoader.class, (mocked, mockContext) -> when(mocked.load()).thenReturn(materials));
                MockedConstruction<PostgreSQLColumnPropertiesAppender> ignoredColumnAppender = mockConstruction(PostgreSQLColumnPropertiesAppender.class);
                MockedConstruction<PostgreSQLConstraintsPropertiesAppender> ignoredConstraintsAppender = mockConstruction(PostgreSQLConstraintsPropertiesAppender.class);
                MockedConstruction<PostgreSQLIndexSQLGenerator> ignoredIndexGenerator =
                        mockConstruction(PostgreSQLIndexSQLGenerator.class, (mocked, mockContext) -> when(mocked.generate(materials)).thenReturn("CREATE INDEX foo_index"))) {
            freemarkerManager.when(() -> PostgreSQLPipelineFreemarkerManager.getSQLByVersion(materials, "component/table/%s/create.ftl", 10, 1)).thenReturn("CREATE TABLE foo;");
            Collection<String> actual = sqlBuilder.buildCreateTableSQLs(dataSource, "public", "foo_tbl");
            List<String> actualList = actual.stream().map(String::trim).collect(Collectors.toList());
            assertThat(actualList.size(), is(2));
            assertThat(actualList.get(0), is("CREATE TABLE foo"));
            assertThat(actualList.get(1), is("CREATE INDEX foo_index"));
            assertThat(arrayColumn.get("cltype"), is("int4"));
            assertTrue((boolean) arrayColumn.get("hasSqrBracket"));
            assertFalse((boolean) normalColumn.get("hasSqrBracket"));
            assertFalse(columnWithoutType.containsKey("hasSqrBracket"));
        }
    }
}
