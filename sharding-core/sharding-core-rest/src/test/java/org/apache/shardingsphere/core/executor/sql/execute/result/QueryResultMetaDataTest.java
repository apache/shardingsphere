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

package org.apache.shardingsphere.core.executor.sql.execute.result;

import com.google.common.base.Optional;
import lombok.SneakyThrows;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.strategy.encrypt.ShardingEncryptorEngine;
import org.apache.shardingsphere.spi.encrypt.ShardingEncryptor;
import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class QueryResultMetaDataTest {
    
    private QueryResultMetaData queryResultMetaData;
    
    private ShardingEncryptor shardingEncryptor;
    
    @Before
    @SneakyThrows
    public void setUp() {
        ResultSetMetaData resultSetMetaData = getResultMetaData();
        ShardingRule shardingRule = getShardingRule();
        queryResultMetaData = new QueryResultMetaData(resultSetMetaData, shardingRule.getAllActualTableNames(), shardingRule.getShardingEncryptorEngine());
    }
    
    private ShardingRule getShardingRule() {
        shardingEncryptor = mock(ShardingEncryptor.class);
        ShardingEncryptorEngine shardingEncryptorEngine = mock(ShardingEncryptorEngine.class);
        when(shardingEncryptorEngine.getShardingEncryptor(anyString(), anyString())).thenReturn(Optional.of(shardingEncryptor));
        ShardingRule result = mock(ShardingRule.class);
        when(result.getShardingEncryptorEngine()).thenReturn(shardingEncryptorEngine);
        when(result.getLogicTableNames(anyString())).thenReturn(Collections.<String>emptyList());
        return result;
    }
    
    private ResultSetMetaData getResultMetaData() throws SQLException {
        ResultSetMetaData result = mock(ResultSetMetaData.class);
        when(result.getColumnCount()).thenReturn(1);
        when(result.getColumnName(anyInt())).thenReturn("column");
        when(result.getColumnLabel(anyInt())).thenReturn("label");
        when(result.getTableName(anyInt())).thenReturn("table");
        return result;
    }
    
    @Test
    public void assertGetColumnCount() {
        assertThat(queryResultMetaData.getColumnCount(), is(1));
    }
    
    @Test
    public void assertGetColumnLabel() {
        assertThat(queryResultMetaData.getColumnLabel(1), is("label"));
    }
    
    @Test
    public void assertGetColumnName() {
        assertThat(queryResultMetaData.getColumnName(1), is("column"));
    }
    
    @Test
    public void assertGetColumnIndex() {
        assertThat(queryResultMetaData.getColumnIndex("label"), is(1));
    }
    
    @Test
    public void assertGetShardingEncryptor() {
        assertThat(queryResultMetaData.getShardingEncryptor(1).get(), is(shardingEncryptor));
    }
}
