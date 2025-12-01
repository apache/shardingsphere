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

package org.apache.shardingsphere.data.pipeline.mysql.sqlbuilder;

import org.apache.shardingsphere.data.pipeline.core.constant.PipelineSQLOperationType;
import org.apache.shardingsphere.data.pipeline.core.exception.job.CreateTableSQLGenerateException;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.placeholder.IngestPlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.NormalColumn;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.dialect.DialectPipelineSQLBuilder;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MySQLPipelineSQLBuilderTest {
    
    private final DialectPipelineSQLBuilder sqlBuilder = DatabaseTypedSPILoader.getService(DialectPipelineSQLBuilder.class, TypedSPILoader.getService(DatabaseType.class, "MySQL"));
    
    @Test
    void assertBuildInsertSQLOnDuplicateClause() {
        Optional<String> actual = sqlBuilder.buildInsertOnDuplicateClause(createDataRecord());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("ON DUPLICATE KEY UPDATE `id`=VALUES(`id`),`sc`=VALUES(`sc`),`c1`=VALUES(`c1`),`c2`=VALUES(`c2`),`c3`=VALUES(`c3`)"));
    }
    
    private DataRecord createDataRecord() {
        DataRecord result = new DataRecord(PipelineSQLOperationType.INSERT, "foo_tbl", new IngestPlaceholderPosition(), 4);
        result.addColumn(new NormalColumn("id", "", false, true));
        result.addColumn(new NormalColumn("sc", "", false, false));
        result.addColumn(new NormalColumn("c1", "", true, false));
        result.addColumn(new NormalColumn("c2", "", true, false));
        result.addColumn(new NormalColumn("c3", "", true, false));
        return result;
    }
    
    @Test
    void assertBuildCheckEmptyTableSQL() {
        assertThat(sqlBuilder.buildCheckEmptyTableSQL("foo_tbl"), is("SELECT * FROM foo_tbl LIMIT 1"));
    }
    
    @Test
    void assertBuildEstimateCountSQL() {
        Optional<String> actual = sqlBuilder.buildEstimatedCountSQL("foo_catalog", "foo_tbl");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("SELECT TABLE_ROWS FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'foo_catalog' AND TABLE_NAME = 'foo_tbl'"));
    }
    
    @Test
    void assertBuildCRC32SQL() {
        Optional<String> actual = sqlBuilder.buildCRC32SQL("foo_tbl", "id");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("SELECT BIT_XOR(CAST(CRC32(id) AS UNSIGNED)) AS checksum, COUNT(1) AS cnt FROM foo_tbl"));
    }
    
    @Test
    void assertBuildCreateTableSQLs() throws SQLException {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("create table")).thenReturn("CREATE TABLE foo_tbl (id INT PRIMARY KEY)");
        when(connection.createStatement().executeQuery("SHOW CREATE TABLE foo_tbl")).thenReturn(resultSet);
        assertThat(sqlBuilder.buildCreateTableSQLs(new MockedDataSource(connection), "foo_schema", "foo_tbl"), is(Collections.singleton("CREATE TABLE foo_tbl (id INT PRIMARY KEY)")));
    }
    
    @Test
    void assertBuildCreateTableSQLsFailed() {
        assertThrows(CreateTableSQLGenerateException.class, () -> sqlBuilder.buildCreateTableSQLs(new MockedDataSource(), "foo_schema", "foo_tbl"));
    }
    
    @Test
    void assertWrapWithPageQuery() {
        assertThat(sqlBuilder.wrapWithPageQuery("SELECT * FROM foo_tbl"), is("SELECT * FROM foo_tbl LIMIT ?"));
    }
}
