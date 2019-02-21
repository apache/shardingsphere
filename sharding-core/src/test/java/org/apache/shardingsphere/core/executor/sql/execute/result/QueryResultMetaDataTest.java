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
import org.apache.shardingsphere.core.encrypt.ShardingEncryptorEngine;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.spi.algorithm.encrypt.ShardingEncryptor;
import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class QueryResultMetaDataTest {
    
    private QueryResultMetaData queryResultMetaData;
    
    @Before
    @SneakyThrows
    public void setUp() {
        ResultSetMetaData resultSetMetaData = getResultMetaData();
        ShardingRule shardingRule = getShardingRule();
        queryResultMetaData = new QueryResultMetaData(resultSetMetaData, shardingRule);
    }
    
    private ShardingRule getShardingRule() {
        ShardingEncryptor shardingEncryptor = mock(ShardingEncryptor.class);
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
    public void testGetColumnCount() {
    }
    
    @Test
    public void testGetColumnLabel() {
    }
    
    @Test
    public void testGetColumnName() {
    }
    
    @Test
    public void testGetColumnIndex() {
    }
    
    @Test
    public void testGetShardingEncryptor() {
    }
}
