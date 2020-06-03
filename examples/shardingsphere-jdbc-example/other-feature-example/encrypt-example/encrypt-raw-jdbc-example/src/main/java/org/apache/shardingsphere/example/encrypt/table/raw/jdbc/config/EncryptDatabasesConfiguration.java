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
import org.apache.shardingsphere.encrypt.api.config.EncryptColumnConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptorConfiguration;
import org.apache.shardingsphere.example.config.ExampleConfiguration;
import org.apache.shardingsphere.example.core.api.DataSourceUtil;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

public class EncryptDatabasesConfiguration implements ExampleConfiguration {
    
    @Override
    public DataSource getDataSource() {
        Properties properties = new Properties();
        properties.setProperty("aes.key.value", "123456");
        properties.setProperty("query.with.cipher.column", "true");
        EncryptColumnConfiguration columnConfigAes = new EncryptColumnConfiguration("user_name", "user_name_plain", "user_name", "", "name_encryptor");
        EncryptColumnConfiguration columnConfigTest = new EncryptColumnConfiguration("pwd", "", "pwd", "assisted_query_pwd", "pwd_encryptor");
        EncryptTableRuleConfiguration tableConfig = new EncryptTableRuleConfiguration("t_user", Arrays.asList(columnConfigAes, columnConfigTest));
        Collection<EncryptorConfiguration> encryptors = new LinkedList<>();
        encryptors.add(new EncryptorConfiguration("name_encryptor", "aes", properties));
        encryptors.add(new EncryptorConfiguration("pwd_encryptor", "assistedTest", properties));
        EncryptRuleConfiguration encryptRuleConfiguration = new EncryptRuleConfiguration(encryptors, Collections.singleton(tableConfig));
        try {
            return ShardingSphereDataSourceFactory.createDataSource(DataSourceUtil.createDataSource("demo_ds"), Collections.singleton(encryptRuleConfiguration), properties);
        } catch (final SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
