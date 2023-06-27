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

package org.apache.shardingsphere.data.pipeline.opengauss.sqlbuilder;

import org.apache.shardingsphere.data.pipeline.api.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.common.ingest.IngestDataChangeType;
import org.apache.shardingsphere.data.pipeline.common.ingest.position.PlaceholderPosition;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenGaussPipelineSQLBuilderTest {
    
    private final OpenGaussPipelineSQLBuilder sqlBuilder = new OpenGaussPipelineSQLBuilder();
    
    @Test
    void assertBuildInsertSQL() {
        String actual = sqlBuilder.buildInsertSQL(null, mockDataRecord("t1"));
        assertThat(actual, is("INSERT INTO t1(id,c0,c1,c2,c3) VALUES(?,?,?,?,?) ON DUPLICATE KEY UPDATE c0=EXCLUDED.c0,c1=EXCLUDED.c1,c2=EXCLUDED.c2,c3=EXCLUDED.c3"));
    }
    
    private DataRecord mockDataRecord(final String tableName) {
        DataRecord result = new DataRecord(IngestDataChangeType.INSERT, tableName, new PlaceholderPosition(), 4);
        result.addColumn(new Column("id", "", false, true));
        result.addColumn(new Column("c0", "", false, false));
        result.addColumn(new Column("c1", "", true, false));
        result.addColumn(new Column("c2", "", true, false));
        result.addColumn(new Column("c3", "", true, false));
        return result;
    }
    
    @Test
    void assertQuoteKeyword() {
        String schemaName = "RECYCLEBIN";
        Optional<String> actualCreateSchemaSql = sqlBuilder.buildCreateSchemaSQL(schemaName);
        assertTrue(actualCreateSchemaSql.isPresent());
        assertThat(actualCreateSchemaSql.get(), is(String.format("CREATE SCHEMA %s", sqlBuilder.quote(schemaName))));
        String actualDropSQL = sqlBuilder.buildDropSQL(schemaName, "ALL");
        String expectedDropSQL = String.format("DROP TABLE IF EXISTS %s", String.join(".", sqlBuilder.quote(schemaName), sqlBuilder.quote("ALL")));
        assertThat(actualDropSQL, is(expectedDropSQL));
    }
    
    @Test
    void assertBuilderDropSQLWithoutKeyword() {
        String actualDropSQL = sqlBuilder.buildDropSQL("test_normal", "t_order");
        assertThat(actualDropSQL, is("DROP TABLE IF EXISTS test_normal.t_order"));
    }
}
