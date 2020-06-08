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

import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.strategy.EncryptStrategyConfiguration;
import org.apache.shardingsphere.encrypt.api.config.strategy.impl.SPIEncryptStrategyConfiguration;
import org.apache.shardingsphere.example.config.ExampleConfiguration;
import org.apache.shardingsphere.example.core.api.DataSourceUtil;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

public final class EncryptDatabasesConfiguration implements ExampleConfiguration {
    
    @Override
    public DataSource getDataSource() {
        Properties properties = new Properties();
        properties.setProperty("aes.key.value", "123456");
        properties.setProperty("query.with.cipher.column", "true");
        EncryptColumnRuleConfiguration columnConfigAes = new EncryptColumnRuleConfiguration("user_name", "user_name", "", "user_name_plain", "name_encrypt_strategy");
        EncryptColumnRuleConfiguration columnConfigTest = new EncryptColumnRuleConfiguration("pwd", "pwd", "assisted_query_pwd", "", "pwd_encrypt_strategy");
        EncryptTableRuleConfiguration tableConfig = new EncryptTableRuleConfiguration("t_user", Arrays.asList(columnConfigAes, columnConfigTest));
        Collection<EncryptStrategyConfiguration> encryptStrategyConfigurations = new LinkedList<>();
        encryptStrategyConfigurations.add(new SPIEncryptStrategyConfiguration("name_encrypt_strategy", "aes", properties));
        encryptStrategyConfigurations.add(new SPIEncryptStrategyConfiguration("pwd_encrypt_strategy", "assistedTest", properties));
        EncryptRuleConfiguration encryptRuleConfiguration = new EncryptRuleConfiguration(encryptStrategyConfigurations, Collections.singleton(tableConfig));
        try {
            return ShardingSphereDataSourceFactory.createDataSource(DataSourceUtil.createDataSource("demo_ds"), Collections.singleton(encryptRuleConfiguration), properties);
        } catch (final SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
