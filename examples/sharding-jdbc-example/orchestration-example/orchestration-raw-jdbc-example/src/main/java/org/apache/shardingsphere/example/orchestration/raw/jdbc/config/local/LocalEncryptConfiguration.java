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

package org.apache.shardingsphere.example.orchestration.raw.jdbc.config.local;

import org.apache.shardingsphere.encrypt.api.config.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptorRuleConfiguration;
import org.apache.shardingsphere.example.config.ExampleConfiguration;
import org.apache.shardingsphere.example.core.api.DataSourceUtil;
import org.apache.shardingsphere.orchestration.center.config.CenterConfiguration;
import org.apache.shardingsphere.orchestration.center.config.OrchestrationConfiguration;
import org.apache.shardingsphere.shardingjdbc.orchestration.api.OrchestrationShardingSphereDataSourceFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class LocalEncryptConfiguration implements ExampleConfiguration {
    
    private final Map<String, CenterConfiguration> centerConfigurationMap;
    
    public LocalEncryptConfiguration(final Map<String, CenterConfiguration> centerConfigurationMap) {
        this.centerConfigurationMap = centerConfigurationMap;
    }
    
    @Override
    public DataSource getDataSource() throws SQLException {
        return OrchestrationShardingSphereDataSourceFactory.createDataSource(
                DataSourceUtil.createDataSource("demo_ds"), Collections.singleton(getEncryptRuleConfiguration()), new Properties(), getOrchestrationConfiguration());
    }
    
    private OrchestrationConfiguration getOrchestrationConfiguration() {
        return new OrchestrationConfiguration(centerConfigurationMap);
    }
    
    private EncryptRuleConfiguration getEncryptRuleConfiguration() {
        Properties properties = new Properties();
        properties.setProperty("aes.key.value", "123456");
        EncryptorRuleConfiguration aesRuleConfiguration = new EncryptorRuleConfiguration("aes", properties);
        EncryptColumnRuleConfiguration columnConfigAes = new EncryptColumnRuleConfiguration("", "status", "", "status_encryptor");
        Map<String, EncryptColumnRuleConfiguration> columns = new HashMap<>();
        EncryptTableRuleConfiguration tableConfig = new EncryptTableRuleConfiguration(columns);
        columns.put("status", columnConfigAes);
        tableConfig.getColumns().putAll(columns);
        Map<String, EncryptorRuleConfiguration> encryptors = new HashMap<>();
        encryptors.put("status_encryptor", aesRuleConfiguration);
        Map<String, EncryptTableRuleConfiguration> tables = new HashMap<>();
        tables.put("t_order", tableConfig);
        return new EncryptRuleConfiguration(encryptors, tables);
    }
}
