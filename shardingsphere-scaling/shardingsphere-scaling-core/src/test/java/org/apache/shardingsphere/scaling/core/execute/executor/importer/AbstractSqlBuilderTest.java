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

package org.apache.shardingsphere.scaling.core.execute.executor.importer;

import com.google.common.collect.Sets;
import org.apache.shardingsphere.scaling.core.execute.executor.record.Column;
import org.apache.shardingsphere.scaling.core.execute.executor.record.DataRecord;
import org.apache.shardingsphere.scaling.core.job.position.NopPosition;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AbstractSqlBuilderTest {
    
    @Mock
    private Map shardingColumnsMap;
    
    private AbstractSQLBuilder sqlBuilder;
    
    @Before
    public void setUp() {
        sqlBuilder = new AbstractSQLBuilder(shardingColumnsMap) {
            
            @Override
            protected String getLeftIdentifierQuoteString() {
                return "`";
            }
            
            @Override
            protected String getRightIdentifierQuoteString() {
                return "`";
            }
        };
    }
    
    @Test
    public void assertBuildInsertSQL() {
        PreparedSQL actual = sqlBuilder.buildInsertSQL(mockDataRecord("t1"));
        assertThat(actual.getSql(), is("INSERT INTO `t1`(`id`,`sc`,`c1`,`c2`,`c3`) VALUES(?,?,?,?,?)"));
        assertThat(actual.getValuesIndex().toArray(), Matchers.arrayContaining(0, 1, 2, 3, 4));
    }
    
    @Test
    public void assertBuildUpdateSQLWithPrimaryKey() {
        when(shardingColumnsMap.get("t2")).thenReturn(Sets.newHashSet());
        PreparedSQL actual = sqlBuilder.buildUpdateSQL(mockDataRecord("t2"));
        assertThat(actual.getSql(), is("UPDATE `t2` SET `c1` = ?,`c2` = ?,`c3` = ? WHERE `id` = ?"));
        assertThat(actual.getValuesIndex().toArray(), Matchers.arrayContaining(2, 3, 4, 0));
    }
    
    @Test
    public void assertBuildUpdateSQLWithShardingColumns() {
        when(shardingColumnsMap.get("t2")).thenReturn(Sets.newHashSet("sc"));
        DataRecord dataRecord = mockDataRecord("t2");
        PreparedSQL actual = sqlBuilder.buildUpdateSQL(dataRecord);
        assertThat(actual.getSql(), is("UPDATE `t2` SET `c1` = ?,`c2` = ?,`c3` = ? WHERE `id` = ? and `sc` = ?"));
        assertThat(actual.getValuesIndex().toArray(), Matchers.arrayContaining(2, 3, 4, 0, 1));
    }
    
    @Test
    public void assertBuildUpdateSQLWithShardingColumnsUseCache() {
        when(shardingColumnsMap.get("t2")).thenReturn(Sets.newHashSet("sc"));
        DataRecord dataRecord = mockDataRecord("t2");
        PreparedSQL actual = sqlBuilder.buildUpdateSQL(dataRecord);
        assertThat(actual.getSql(), is("UPDATE `t2` SET `c1` = ?,`c2` = ?,`c3` = ? WHERE `id` = ? and `sc` = ?"));
        assertThat(actual.getValuesIndex().toArray(), Matchers.arrayContaining(2, 3, 4, 0, 1));
        actual = sqlBuilder.buildUpdateSQL(mockDataRecord2("t2"));
        assertThat(actual.getSql(), is("UPDATE `t2` SET `c1` = ?,`c3` = ? WHERE `id` = ? and `sc` = ?"));
        assertThat(actual.getValuesIndex().toArray(), Matchers.arrayContaining(2, 4, 0, 1));
    }
    
    private DataRecord mockDataRecord2(final String tableName) {
        DataRecord result = new DataRecord(new NopPosition(), 4);
        result.setTableName(tableName);
        result.addColumn(new Column("id", "", false, true));
        result.addColumn(new Column("sc", "", false, false));
        result.addColumn(new Column("c1", "", true, false));
        result.addColumn(new Column("c2", "", false, false));
        result.addColumn(new Column("c3", "", true, false));
        return result;
    }
    
    @Test
    public void assertBuildDeleteSQLWithPrimaryKey() {
        when(shardingColumnsMap.get("t3")).thenReturn(Sets.newHashSet());
        PreparedSQL actual = sqlBuilder.buildDeleteSQL(mockDataRecord("t3"));
        assertThat(actual.getSql(), is("DELETE FROM `t3` WHERE `id` = ?"));
        assertThat(actual.getValuesIndex().toArray(), Matchers.arrayContaining(0));
    }
    
    @Test
    public void assertBuildDeleteSQLWithShardingColumns() {
        when(shardingColumnsMap.get("t3")).thenReturn(Sets.newHashSet("sc"));
        DataRecord dataRecord = mockDataRecord("t3");
        PreparedSQL actual = sqlBuilder.buildDeleteSQL(dataRecord);
        assertThat(actual.getSql(), is("DELETE FROM `t3` WHERE `id` = ? and `sc` = ?"));
        assertThat(actual.getValuesIndex().toArray(), Matchers.arrayContaining(0, 1));
    }
    
    private DataRecord mockDataRecord(final String tableName) {
        DataRecord result = new DataRecord(new NopPosition(), 4);
        result.setTableName(tableName);
        result.addColumn(new Column("id", "", false, true));
        result.addColumn(new Column("sc", "", false, false));
        result.addColumn(new Column("c1", "", true, false));
        result.addColumn(new Column("c2", "", true, false));
        result.addColumn(new Column("c3", "", true, false));
        return result;
    }
}
