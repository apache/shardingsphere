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

package org.apache.shardingsphere.core.rewrite;

import org.apache.shardingsphere.api.config.encryptor.EncryptRuleConfiguration;
import org.apache.shardingsphere.api.config.encryptor.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.api.config.encryptor.EncryptorConfiguration;
import org.apache.shardingsphere.core.metadata.table.ColumnMetaData;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.metadata.table.TableMetaData;
import org.junit.Before;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public final class EncryptSQLRewriteEngineTest {
    
    @Before
    public void setUp() {
        
    }
    
    private EncryptRuleConfiguration createEncryptRuleConfiguration() {
        EncryptorConfiguration encryptorConfig = new EncryptorConfiguration("test", "col1, col2", new Properties());
        EncryptTableRuleConfiguration encryptTableRuleConfig = new EncryptTableRuleConfiguration();
        encryptTableRuleConfig.setTable("t_encrypt");
        encryptTableRuleConfig.setEncryptorConfig(encryptorConfig);
        EncryptorConfiguration encryptorQueryConfig = new EncryptorConfiguration("assistedTest", "col1, col2", "query1, query2", new Properties());
        EncryptTableRuleConfiguration encryptQueryTableRuleConfig = new EncryptTableRuleConfiguration();
        encryptQueryTableRuleConfig.setTable("t_query_encrypt");
        encryptQueryTableRuleConfig.setEncryptorConfig(encryptorQueryConfig);
        EncryptRuleConfiguration result = new EncryptRuleConfiguration();
        result.getTableRuleConfigs().add(encryptTableRuleConfig);
        result.getTableRuleConfigs().add(encryptQueryTableRuleConfig);
        return result;
    }
    
    private ShardingTableMetaData createShardingTableMetaData() {
        ColumnMetaData columnMetaData1 = new ColumnMetaData("col1", "VARCHAR(10)", false);
        ColumnMetaData columnMetaData2 = new ColumnMetaData("col2", "VARCHAR(10)", false);
        ColumnMetaData queryColumnMetaData1 = new ColumnMetaData("query1", "VARCHAR(10)", false);
        ColumnMetaData queryColumnMetaData2 = new ColumnMetaData("query2", "VARCHAR(10)", false);
        TableMetaData encryptTableMetaData = new TableMetaData(Arrays.asList(columnMetaData1, columnMetaData2));
        TableMetaData queryTableMetaData = new TableMetaData(Arrays.asList(columnMetaData1, columnMetaData2, queryColumnMetaData1, queryColumnMetaData2));
        Map<String, TableMetaData> tables = new LinkedHashMap<>();
        tables.put("t_encrypt", encryptTableMetaData);
        tables.put("t_query_encrypt", queryTableMetaData);
        return new ShardingTableMetaData(tables);
    }
}
