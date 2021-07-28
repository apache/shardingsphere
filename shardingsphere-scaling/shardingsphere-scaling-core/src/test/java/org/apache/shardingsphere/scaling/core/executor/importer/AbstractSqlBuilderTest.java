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

package org.apache.shardingsphere.scaling.core.executor.importer;

import org.apache.shardingsphere.scaling.core.common.record.Column;
import org.apache.shardingsphere.scaling.core.common.record.DataRecord;
import org.apache.shardingsphere.scaling.core.common.record.RecordUtil;
import org.apache.shardingsphere.scaling.core.common.sqlbuilder.ScalingSQLBuilder;
import org.apache.shardingsphere.scaling.core.fixture.FixtureSQLBuilder;
import org.apache.shardingsphere.scaling.core.job.position.PlaceholderPosition;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class AbstractSqlBuilderTest {
    
    private final ScalingSQLBuilder scalingSQLBuilder = new FixtureSQLBuilder(Collections.emptyMap());
    
    @Test
    public void assertBuildInsertSQL() {
        String actual = scalingSQLBuilder.buildInsertSQL(mockDataRecord("t1"));
        assertThat(actual, is("INSERT INTO `t1`(`id`,`sc`,`c1`,`c2`,`c3`) VALUES(?,?,?,?,?)"));
    }
    
    @Test
    public void assertBuildUpdateSQLWithPrimaryKey() {
        String actual = scalingSQLBuilder.buildUpdateSQL(mockDataRecord("t2"), RecordUtil.extractPrimaryColumns(mockDataRecord("t2")));
        assertThat(actual, is("UPDATE `t2` SET `c1` = ?,`c2` = ?,`c3` = ? WHERE `id` = ?"));
    }
    
    @Test
    public void assertBuildUpdateSQLWithShardingColumns() {
        DataRecord dataRecord = mockDataRecord("t2");
        String actual = scalingSQLBuilder.buildUpdateSQL(dataRecord, mockConditionColumns(dataRecord));
        assertThat(actual, is("UPDATE `t2` SET `c1` = ?,`c2` = ?,`c3` = ? WHERE `id` = ? and `sc` = ?"));
    }
    
    @Test
    public void assertBuildDeleteSQLWithPrimaryKey() {
        String actual = scalingSQLBuilder.buildDeleteSQL(mockDataRecord("t3"), RecordUtil.extractPrimaryColumns(mockDataRecord("t3")));
        assertThat(actual, is("DELETE FROM `t3` WHERE `id` = ?"));
    }
    
    @Test
    public void assertBuildDeleteSQLWithConditionColumns() {
        DataRecord dataRecord = mockDataRecord("t3");
        String actual = scalingSQLBuilder.buildDeleteSQL(dataRecord, mockConditionColumns(dataRecord));
        assertThat(actual, is("DELETE FROM `t3` WHERE `id` = ? and `sc` = ?"));
    }
    
    private Collection<Column> mockConditionColumns(final DataRecord dataRecord) {
        return RecordUtil.extractConditionColumns(dataRecord, Collections.singleton("sc"));
    }
    
    private DataRecord mockDataRecord(final String tableName) {
        DataRecord result = new DataRecord(new PlaceholderPosition(), 4);
        result.setTableName(tableName);
        result.addColumn(new Column("id", "", false, true));
        result.addColumn(new Column("sc", "", false, false));
        result.addColumn(new Column("c1", "", true, false));
        result.addColumn(new Column("c2", "", true, false));
        result.addColumn(new Column("c3", "", true, false));
        return result;
    }
}
