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

package org.apache.shardingsphere.example.encrypt.table.raw.jdbc.config;

import org.apache.shardingsphere.encrypt.api.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.EncryptorRuleConfiguration;
import org.apache.shardingsphere.example.config.ExampleConfiguration;
import org.apache.shardingsphere.example.core.api.DataSourceUtil;
import org.apache.shardingsphere.shardingjdbc.api.EncryptDataSourceFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class EncryptDatabasesConfiguration implements ExampleConfiguration {

    @Override
    public DataSource getDataSource() {
        
        EncryptRuleConfiguration encryptRuleConfiguration = new EncryptRuleConfiguration();
        Properties properties = new Properties();
        properties.setProperty("aes.key.value", "123456");
        properties.setProperty("query.with.cipher.column", "true");
        EncryptorRuleConfiguration aesRuleConfiguration = new EncryptorRuleConfiguration("aes", properties);
        EncryptColumnRuleConfiguration columnConfigAes = new EncryptColumnRuleConfiguration("user_name_plain", "user_name", "", "name_encryptor");
        Map<String, EncryptColumnRuleConfiguration> columns = new HashMap<>();
        EncryptTableRuleConfiguration tableConfig = new EncryptTableRuleConfiguration(columns);
        columns.put("user_name", columnConfigAes);
        encryptRuleConfiguration.getEncryptors().put("name_encryptor", aesRuleConfiguration);
        encryptRuleConfiguration.getTables().put("t_user", tableConfig);
        EncryptorRuleConfiguration testRuleConfiguration = new EncryptorRuleConfiguration("assistedTest", properties);
        EncryptColumnRuleConfiguration columnConfigTest = new EncryptColumnRuleConfiguration("", "pwd", "assisted_query_pwd", "pwd_encryptor");
        columns.put("pwd", columnConfigTest);
        tableConfig.getColumns().putAll(columns);
        encryptRuleConfiguration.getEncryptors().put("pwd_encryptor", testRuleConfiguration);
        try {
            return EncryptDataSourceFactory.createDataSource(DataSourceUtil.createDataSource("demo_ds"), encryptRuleConfiguration, properties);
        } catch (final SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
