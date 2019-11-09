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

package org.apache.shardingsphere.core.execute.sql.execute.result;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.constant.properties.ShardingProperties;
import org.apache.shardingsphere.core.preprocessor.segment.table.TablesContext;
import org.apache.shardingsphere.core.preprocessor.statement.SQLStatementContext;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.strategy.encrypt.EncryptTable;
import org.apache.shardingsphere.spi.encrypt.ShardingEncryptor;
import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class QueryResultMetaDataTest {
    
    private QueryResultMetaData queryResultMetaData;

    private ShardingEncryptor shardingEncryptor;
    
    @Before
    public void setUp() throws SQLException {
        final SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        final TablesContext tablesContext = mock(TablesContext.class);
        final ResultSetMetaData resultSetMetaData = getResultMetaData();
        final ShardingRule shardingRule = getShardingRule();
        final ShardingProperties shardingProperties = new ShardingProperties(new Properties());
        when(sqlStatementContext.getTablesContext()).thenReturn(tablesContext);
        when(tablesContext.getTableNames()).thenReturn(Collections.singleton("table"));
        queryResultMetaData = new QueryResultMetaData(resultSetMetaData, shardingRule, shardingProperties, sqlStatementContext);
    }
    
    @SuppressWarnings("unchecked")
    private ShardingRule getShardingRule() {
        shardingEncryptor = mock(ShardingEncryptor.class);
        ShardingRule result = mock(ShardingRule.class);
        EncryptRule encryptRule = mock(EncryptRule.class);
        EncryptTable encryptTable = mock(EncryptTable.class);
        when(encryptRule.findShardingEncryptor(anyString(), anyString())).thenReturn(Optional.of(shardingEncryptor));
        when(encryptRule.findEncryptTable(anyString())).thenReturn(Optional.of(encryptTable));
        when(encryptRule.getLogicColumn(anyString(), anyString())).thenReturn("column");
        when(encryptTable.getCipherColumns()).thenReturn(Collections.singleton("column"));
        when(result.getEncryptRule()).thenReturn(encryptRule);
        return result;
    }
    
    private ResultSetMetaData getResultMetaData() throws SQLException {
        ResultSetMetaData result = mock(ResultSetMetaData.class);
        when(result.getColumnCount()).thenReturn(1);
        when(result.getColumnName(anyInt())).thenReturn("column");
        when(result.getColumnLabel(anyInt())).thenReturn("label");
        when(result.getTableName(anyInt())).thenReturn("table");
        when(result.isCaseSensitive(anyInt())).thenReturn(false);
        return result;
    }

    @Test
    public void assertGetColumnCount() throws SQLException {
        assertThat(queryResultMetaData.getColumnCount(), is(1));
    }
    
    @Test
    public void assertGetColumnLabel() throws SQLException {
        assertThat(queryResultMetaData.getColumnLabel(1), is("label"));
    }
    
    @Test
    public void assertGetColumnName() throws SQLException {
        assertThat(queryResultMetaData.getColumnName(1), is("column"));
    }
    
    @Test
    public void assertGetColumnIndex() {
        assertThat(queryResultMetaData.getColumnIndex("label"), is(1));
    }
    
    @Test
    public void assertIsCaseSensitive() throws SQLException {
        assertFalse(queryResultMetaData.isCaseSensitive(1));
    }
    
    @Test
    public void assertGetShardingEncryptor() throws SQLException {
        assertTrue(queryResultMetaData.getShardingEncryptor(1).isPresent());
        assertThat(queryResultMetaData.getShardingEncryptor(1).get(), is(shardingEncryptor));
    }
}
