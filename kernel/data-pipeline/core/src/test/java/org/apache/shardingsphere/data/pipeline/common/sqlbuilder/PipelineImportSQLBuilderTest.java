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
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class PipelineImportSQLBuilderTest {
    
    private final PipelineImportSQLBuilder importSQLBuilder = new PipelineImportSQLBuilder(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
    
    @Test
    void assertBuildInsertSQL() {
        String actual = importSQLBuilder.buildInsertSQL(null, mockDataRecord("t2", 3));
        assertThat(actual, is("INSERT INTO t2(id,sc,c1,c2,c3) VALUES(?,?,?,?,?)"));
    }
    
    @Test
    void assertBuildUpdateSQLWithShardingColumns() {
        DataRecord dataRecord = mockDataRecord("t2", 3);
        String actual = importSQLBuilder.buildUpdateSQL(null, dataRecord, mockConditionColumns(dataRecord));
        assertThat(actual, is("UPDATE t2 SET c1 = ?,c2 = ?,c3 = ? WHERE id = ? AND sc = ?"));
        dataRecord = mockDataRecord("t2", 2);
        actual = importSQLBuilder.buildUpdateSQL(null, dataRecord, mockConditionColumns(dataRecord));
        assertThat(actual, is("UPDATE t2 SET c1 = ?,c2 = ? WHERE id = ? AND sc = ?"));
    }
    
    @Test
    void assertBuildDeleteSQLWithConditionColumns() {
        DataRecord dataRecord = mockDataRecord("t3", 3);
        String actual = importSQLBuilder.buildDeleteSQL(null, dataRecord, mockConditionColumns(dataRecord));
        assertThat(actual, is("DELETE FROM t3 WHERE id = ? AND sc = ?"));
    }
    
    private Collection<Column> mockConditionColumns(final DataRecord dataRecord) {
        return RecordUtils.extractConditionColumns(dataRecord, Collections.singleton("sc"));
    }
    
    private DataRecord mockDataRecord(final String tableName, final int extraColumnCount) {
        DataRecord result = new DataRecord(IngestDataChangeType.INSERT, tableName, new PlaceholderPosition(), 4);
        result.addColumn(new Column("id", "", false, true));
        result.addColumn(new Column("sc", "", false, false));
        for (int i = 1; i <= extraColumnCount; i++) {
            result.addColumn(new Column("c" + i, "", true, false));
        }
        return result;
    }
    
    @Test
    void assertBuildDeleteSQLWithoutUniqueKey() {
        String actual = importSQLBuilder.buildDeleteSQL(null, mockDataRecordWithoutUniqueKey(),
                RecordUtils.extractConditionColumns(mockDataRecordWithoutUniqueKey(), Collections.emptySet()));
        assertThat(actual, is("DELETE FROM t_order WHERE id = ? AND name = ?"));
    }
    
    @Test
    void assertBuildUpdateSQLWithoutShardingColumns() {
        DataRecord dataRecord = mockDataRecordWithoutUniqueKey();
        String actual = importSQLBuilder.buildUpdateSQL(null, dataRecord, mockConditionColumns(dataRecord));
        assertThat(actual, is("UPDATE t_order SET name = ? WHERE id = ? AND name = ?"));
    }
    
    private DataRecord mockDataRecordWithoutUniqueKey() {
        DataRecord result = new DataRecord(IngestDataChangeType.INSERT, "t_order", new PlaceholderPosition(), 4);
        result.addColumn(new Column("id", "", false, false));
        result.addColumn(new Column("name", "", true, false));
        return result;
    }
}
