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
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.postgresql.replication.LogSequenceNumber;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostgreSQLPipelineSQLBuilderTest {
    
    private final DialectPipelineSQLBuilder sqlBuilder = DatabaseTypedSPILoader.getService(DialectPipelineSQLBuilder.class, TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"));
    
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
        assertThat(actual.get(), is("ON CONFLICT (order_id) DO UPDATE SET user_id=EXCLUDED.user_id,status=EXCLUDED.status"));
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
}
