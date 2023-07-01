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

package org.apache.shardingsphere.data.pipeline.common.sqlbuilder;

import org.apache.shardingsphere.data.pipeline.api.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.common.ingest.IngestDataChangeType;
import org.apache.shardingsphere.data.pipeline.common.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.common.ingest.record.RecordUtils;
import org.apache.shardingsphere.data.pipeline.spi.sqlbuilder.PipelineSQLBuilder;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class PipelineSQLBuilderTest {
    
    private final PipelineSQLBuilder pipelineSQLBuilder = new H2PipelineSQLBuilder();
    
    @Test
    void assertBuildDivisibleInventoryDumpSQL() {
        String actual = pipelineSQLBuilder.buildDivisibleInventoryDumpSQL(null, "t_order", Collections.singletonList("*"), "order_id");
        assertThat(actual, is("SELECT * FROM t_order WHERE order_id>=? AND order_id<=? ORDER BY order_id ASC"));
        actual = pipelineSQLBuilder.buildDivisibleInventoryDumpSQL(null, "t_order", Arrays.asList("order_id", "user_id", "status"), "order_id");
        assertThat(actual, is("SELECT order_id,user_id,status FROM t_order WHERE order_id>=? AND order_id<=? ORDER BY order_id ASC"));
    }
    
    @Test
    void assertBuildDivisibleInventoryDumpSQLNoEnd() {
        String actual = pipelineSQLBuilder.buildDivisibleInventoryDumpSQLNoEnd(null, "t_order", Collections.singletonList("*"), "order_id");
        assertThat(actual, is("SELECT * FROM t_order WHERE order_id>=? ORDER BY order_id ASC"));
        actual = pipelineSQLBuilder.buildDivisibleInventoryDumpSQLNoEnd(null, "t_order", Arrays.asList("order_id", "user_id", "status"), "order_id");
        assertThat(actual, is("SELECT order_id,user_id,status FROM t_order WHERE order_id>=? ORDER BY order_id ASC"));
    }
    
    @Test
    void assertBuildIndivisibleInventoryDumpSQL() {
        String actual = pipelineSQLBuilder.buildIndivisibleInventoryDumpSQL(null, "t_order", Collections.singletonList("*"), "order_id");
        assertThat(actual, is("SELECT * FROM t_order ORDER BY order_id ASC"));
        actual = pipelineSQLBuilder.buildIndivisibleInventoryDumpSQL(null, "t_order", Arrays.asList("order_id", "user_id", "status"), "order_id");
        assertThat(actual, is("SELECT order_id,user_id,status FROM t_order ORDER BY order_id ASC"));
    }
    
    @Test
    void assertBuildQueryAllOrderingSQLFirstQuery() {
        String actual = pipelineSQLBuilder.buildQueryAllOrderingSQL(null, "t_order", Collections.singletonList("*"), "order_id", true);
        assertThat(actual, is("SELECT * FROM t_order ORDER BY order_id ASC"));
        actual = pipelineSQLBuilder.buildQueryAllOrderingSQL(null, "t_order", Arrays.asList("order_id", "user_id", "status"), "order_id", true);
        assertThat(actual, is("SELECT order_id,user_id,status FROM t_order ORDER BY order_id ASC"));
    }
    
    @Test
    void assertBuildQueryAllOrderingSQLNonFirstQuery() {
        String actual = pipelineSQLBuilder.buildQueryAllOrderingSQL(null, "t_order", Collections.singletonList("*"), "order_id", false);
        assertThat(actual, is("SELECT * FROM t_order WHERE order_id>? ORDER BY order_id ASC"));
        actual = pipelineSQLBuilder.buildQueryAllOrderingSQL(null, "t_order", Arrays.asList("order_id", "user_id", "status"), "order_id", false);
        assertThat(actual, is("SELECT order_id,user_id,status FROM t_order WHERE order_id>? ORDER BY order_id ASC"));
    }
    
    @Test
    void assertBuildInsertSQL() {
        String actual = pipelineSQLBuilder.buildInsertSQL(null, mockDataRecord("t2"));
        assertThat(actual, is("INSERT INTO t2(id,sc,c1,c2,c3) VALUES(?,?,?,?,?)"));
    }
    
    @Test
    void assertBuildUpdateSQLWithPrimaryKey() {
        String actual = pipelineSQLBuilder.buildUpdateSQL(null, mockDataRecord("t2"), RecordUtils.extractPrimaryColumns(mockDataRecord("t2")));
        assertThat(actual, is("UPDATE t2 SET c1 = ?,c2 = ?,c3 = ? WHERE id = ?"));
    }
    
    @Test
    void assertBuildUpdateSQLWithShardingColumns() {
        DataRecord dataRecord = mockDataRecord("t2");
        String actual = pipelineSQLBuilder.buildUpdateSQL(null, dataRecord, mockConditionColumns(dataRecord));
        assertThat(actual, is("UPDATE t2 SET c1 = ?,c2 = ?,c3 = ? WHERE id = ? AND sc = ?"));
    }
    
    @Test
    void assertBuildDeleteSQLWithPrimaryKey() {
        String actual = pipelineSQLBuilder.buildDeleteSQL(null, mockDataRecord("t3"), RecordUtils.extractPrimaryColumns(mockDataRecord("t3")));
        assertThat(actual, is("DELETE FROM t3 WHERE id = ?"));
    }
    
    @Test
    void assertBuildDeleteSQLWithConditionColumns() {
        DataRecord dataRecord = mockDataRecord("t3");
        String actual = pipelineSQLBuilder.buildDeleteSQL(null, dataRecord, mockConditionColumns(dataRecord));
        assertThat(actual, is("DELETE FROM t3 WHERE id = ? AND sc = ?"));
    }
    
    private Collection<Column> mockConditionColumns(final DataRecord dataRecord) {
        return RecordUtils.extractConditionColumns(dataRecord, Collections.singleton("sc"));
    }
    
    private DataRecord mockDataRecord(final String tableName) {
        DataRecord result = new DataRecord(IngestDataChangeType.INSERT, tableName, new PlaceholderPosition(), 4);
        result.addColumn(new Column("id", "", false, true));
        result.addColumn(new Column("sc", "", false, false));
        result.addColumn(new Column("c1", "", true, false));
        result.addColumn(new Column("c2", "", true, false));
        result.addColumn(new Column("c3", "", true, false));
        return result;
    }
    
    @Test
    void assertBuildDeleteSQLWithoutUniqueKey() {
        String actual = pipelineSQLBuilder.buildDeleteSQL(null, mockDataRecordWithoutUniqueKey("t_order"),
                RecordUtils.extractConditionColumns(mockDataRecordWithoutUniqueKey("t_order"), Collections.emptySet()));
        assertThat(actual, is("DELETE FROM t_order WHERE id = ? AND name = ?"));
    }
    
    @Test
    void assertBuildUpdateSQLWithoutShardingColumns() {
        DataRecord dataRecord = mockDataRecordWithoutUniqueKey("t_order");
        String actual = pipelineSQLBuilder.buildUpdateSQL(null, dataRecord, mockConditionColumns(dataRecord));
        assertThat(actual, is("UPDATE t_order SET name = ? WHERE id = ? AND name = ?"));
    }
    
    private DataRecord mockDataRecordWithoutUniqueKey(final String tableName) {
        DataRecord result = new DataRecord(IngestDataChangeType.INSERT, tableName, new PlaceholderPosition(), 4);
        result.addColumn(new Column("id", "", false, false));
        result.addColumn(new Column("name", "", true, false));
        return result;
    }
}
