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

package org.apache.shardingsphere.example.encrypt.jdbc;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public final class MemoryLocalEncryptJdbcConfiguration {
    
    private static final String HOST = "localhost";
    
    private static final int PORT = 3306;
    
    private static final String USER_NAME = "root";
    
    private static final String PASSWORD = "123456";
    
    /**
     * Create a DataSource object, which is an object rewritten by ShardingSphere itself 
     * and contains various rules for rewriting the original data storage. When in use, you only need to use this object.
     * @return
     * @throws SQLException
    */
    public DataSource getDataSource() throws SQLException {
        return ShardingSphereDataSourceFactory.createDataSource(createDataSource("demo_ds_0"), Collections.singleton(createEncryptRuleConfiguration()), new Properties());
    }
    
    private EncryptRuleConfiguration createEncryptRuleConfiguration() {
        Properties props = new Properties();
        props.setProperty("aes-key-value", "");
        EncryptColumnRuleConfiguration columnConfigAes = new EncryptColumnRuleConfiguration("user_id", "user_id", null, null, "name_encryptor");
        EncryptColumnRuleConfiguration columnConfigTest = new EncryptColumnRuleConfiguration("address_id", "address_id", null, null, "pwd_encryptor");
        EncryptTableRuleConfiguration encryptTableRuleConfig = new EncryptTableRuleConfiguration("t_order", Arrays.asList(columnConfigAes, columnConfigTest), true);
        Map<String, ShardingSphereAlgorithmConfiguration> encryptAlgorithmConfigs = new LinkedHashMap<>(2, 1);
        encryptAlgorithmConfigs.put("name_encryptor", new ShardingSphereAlgorithmConfiguration("AES", props));
        encryptAlgorithmConfigs.put("pwd_encryptor", new ShardingSphereAlgorithmConfiguration("AES", props));
        return new EncryptRuleConfiguration(Collections.singleton(encryptTableRuleConfig), encryptAlgorithmConfigs);
    }
    
    private DataSource createDataSource(final String dataSourceName) {
        HikariDataSource result = new HikariDataSource();
        result.setDriverClassName("com.mysql.jdbc.Driver");
        result.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8", HOST, PORT, dataSourceName));
        result.setUsername(USER_NAME);
        result.setPassword(PASSWORD);
        return result;
    }
}
