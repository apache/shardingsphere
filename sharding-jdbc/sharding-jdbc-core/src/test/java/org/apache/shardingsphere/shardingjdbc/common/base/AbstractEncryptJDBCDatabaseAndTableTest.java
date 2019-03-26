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

package org.apache.shardingsphere.shardingjdbc.common.base;

import org.apache.shardingsphere.api.config.encryptor.EncryptRuleConfiguration;
import org.apache.shardingsphere.api.config.encryptor.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.api.config.encryptor.EncryptorConfiguration;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.EncryptDataSource;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

public abstract class AbstractEncryptJDBCDatabaseAndTableTest extends AbstractSQLTest {
    
    private static EncryptDataSource encryptDataSource;

    @BeforeClass
    public void initEncryptDataSource() {
        if (null != encryptDataSource) {
            return;
        }
        Map<DatabaseType, Map<String, DataSource>> dataSources = createDataSourceMap(Collections.singleton("encrypt"));
        encryptDataSource = new EncryptDataSource(dataSources.values().iterator().next().values().iterator().next(), createEncryptRuleConfiguration());
    }
    
    private EncryptRuleConfiguration createEncryptRuleConfiguration() {
        EncryptorConfiguration encryptorConfig = new EncryptorConfiguration("test", "pwd", new Properties());
        EncryptTableRuleConfiguration encryptTableRuleConfig = new EncryptTableRuleConfiguration();
        encryptTableRuleConfig.setTable("t_encrypt");
        encryptTableRuleConfig.setEncryptorConfig(encryptorConfig);
        EncryptorConfiguration encryptorQueryConfig = new EncryptorConfiguration("assistedTest", "pwd", "assist_pwd", new Properties());
        EncryptTableRuleConfiguration encryptQueryTableRuleConfig = new EncryptTableRuleConfiguration();
        encryptQueryTableRuleConfig.setTable("t_query_encrypt");
        encryptQueryTableRuleConfig.setEncryptorConfig(encryptorQueryConfig);
        EncryptRuleConfiguration result = new EncryptRuleConfiguration();
        result.getTableRuleConfigs().add(encryptTableRuleConfig);
        result.getTableRuleConfigs().add(encryptQueryTableRuleConfig);
        return result;
    }

    protected final EncryptDataSource getEncryptDataSource() {
        return encryptDataSource;
    }
    
    @AfterClass
    public static void clear() {
        if (encryptDataSource == null) {
            return;
        }
        encryptDataSource.close();
        encryptDataSource = null;
    }
}
