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

package org.apache.shardingsphere.shardingscaling.core.execute.executor.writer;

import lombok.SneakyThrows;
import org.apache.shardingsphere.shardingscaling.core.metadata.column.ColumnMetaData;
import org.apache.shardingsphere.shardingscaling.core.metadata.MetaDataManager;
import org.apache.shardingsphere.shardingscaling.core.metadata.table.TableMetaData;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AbstractSqlBuilderTest {
    
    @Mock
    private MetaDataManager metaDataManager;
    
    private TableMetaData tableMetaData;
    
    private AbstractSqlBuilder sqlBuilder;
    
    @Before
    @SneakyThrows
    public void setUp() {
        tableMetaData = new TableMetaData();
        sqlBuilder = new AbstractSqlBuilder(null) {
            
            @Override
            protected String getLeftIdentifierQuoteString() {
                return "`";
            }
    
            @Override
            protected String getRightIdentifierQuoteString() {
                return "`";
            }
        };
        FieldSetter.setField(sqlBuilder, AbstractSqlBuilder.class.getDeclaredField("metaDataManager"), metaDataManager);
    }
    
    @Test
    public void assertBuildInsertSql() {
        mockMetaData();
        when(metaDataManager.getTableMetaData("t1")).thenReturn(tableMetaData);
        String actual = sqlBuilder.buildInsertSql("t1");
        assertThat(actual, Matchers.is("INSERT INTO `t1`(`id`,`c1`,`c2`,`c3`) VALUES(?,?,?,?)"));
    }
    
    @Test
    public void assertBuildUpdateSql() {
        mockPrimaryKeys();
        when(metaDataManager.getTableMetaData("t2")).thenReturn(tableMetaData);
        String actual = sqlBuilder.buildUpdateSql("t2", mockUpdateColumn());
        assertThat(actual, Matchers.is("UPDATE `t2` SET `c1` = ?,`c2` = ?,`c3` = ? WHERE `id` = ?"));
    }
    
    @Test
    public void assertBuildDeleteSql() {
        mockPrimaryKeys();
        when(metaDataManager.getTableMetaData("t3")).thenReturn(tableMetaData);
        String actual = sqlBuilder.buildDeleteSql("t3");
        assertThat(actual, Matchers.is("DELETE FROM `t3` WHERE `id` = ?"));
    }
    
    private void mockPrimaryKeys() {
        tableMetaData.addAllPrimaryKey(Collections.singletonList("id"));
    }
    
    private void mockMetaData() {
        List<ColumnMetaData> columnMetaDataList = new ArrayList<>(4);
        ColumnMetaData id = new ColumnMetaData();
        id.setColumnName("id");
        columnMetaDataList.add(id);
        ColumnMetaData c1 = new ColumnMetaData();
        c1.setColumnName("c1");
        columnMetaDataList.add(c1);
        ColumnMetaData c2 = new ColumnMetaData();
        c2.setColumnName("c2");
        columnMetaDataList.add(c2);
        ColumnMetaData c3 = new ColumnMetaData();
        c3.setColumnName("c3");
        columnMetaDataList.add(c3);
        tableMetaData.addAllColumnMetaData(columnMetaDataList);
    }
    
    private List<ColumnMetaData> mockUpdateColumn() {
        List<ColumnMetaData> result = new ArrayList<>(3);
        ColumnMetaData c1 = new ColumnMetaData();
        c1.setColumnName("c1");
        result.add(c1);
        ColumnMetaData c2 = new ColumnMetaData();
        c2.setColumnName("c2");
        result.add(c2);
        ColumnMetaData c3 = new ColumnMetaData();
        c3.setColumnName("c3");
        result.add(c3);
        return result;
    }
}
