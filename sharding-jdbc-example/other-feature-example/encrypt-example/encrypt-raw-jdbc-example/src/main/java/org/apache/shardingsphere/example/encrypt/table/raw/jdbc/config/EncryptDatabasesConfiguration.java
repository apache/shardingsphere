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

import org.apache.shardingsphere.api.config.encryptor.EncryptRuleConfiguration;
import org.apache.shardingsphere.api.config.encryptor.EncryptorRuleConfiguration;
import org.apache.shardingsphere.example.common.DataSourceUtil;
import org.apache.shardingsphere.example.config.ExampleConfiguration;
import org.apache.shardingsphere.shardingjdbc.api.EncryptDataSourceFactory;

import javax.sql.DataSource;
import java.util.Properties;

public class EncryptDatabasesConfiguration implements ExampleConfiguration {

    @Override
    public DataSource getDataSource() {
        EncryptRuleConfiguration encryptRuleConfiguration = new EncryptRuleConfiguration();
        Properties properties = new Properties();
        properties.setProperty("aes.key.value", "123456");
        EncryptorRuleConfiguration aesRuleConfiguration = new EncryptorRuleConfiguration("AES", "t_user.user_name", properties);
        EncryptorRuleConfiguration testRuleConfiguration = new EncryptorRuleConfiguration("assistedTest", "t_user.pwd", "t_user.assisted_query_pwd", properties);
        encryptRuleConfiguration.getEncryptorRuleConfigs().put("name_encryptor", aesRuleConfiguration);
        encryptRuleConfiguration.getEncryptorRuleConfigs().put("pwd_encryptor", testRuleConfiguration);
        return EncryptDataSourceFactory.createDataSource(DataSourceUtil.createDataSource("demo_ds"), encryptRuleConfiguration);
    }
}
