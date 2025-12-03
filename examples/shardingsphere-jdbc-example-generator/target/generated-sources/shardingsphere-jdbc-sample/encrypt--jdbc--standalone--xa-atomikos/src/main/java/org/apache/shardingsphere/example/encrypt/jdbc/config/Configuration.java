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

package org.apache.shardingsphere.example.encrypt.jdbc.config;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepositoryConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

public final class Configuration {
    
    private static final String HOST = "localhost";
    
    private static final int PORT = 3306;
    
    private static final String USER_NAME = "root";
    
    private static final String PASSWORD = "123456";
    
    public DataSource createDataSource() throws SQLException {
        return ShardingSphereDataSourceFactory.createDataSource(createModeConfiguration(), createDataSourceMap(), createRuleConfiguration(), createProperties());
    }
    
    private static ModeConfiguration createModeConfiguration() {
        return new ModeConfiguration("Standalone", new StandalonePersistRepositoryConfiguration("JDBC", new Properties()));
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new LinkedHashMap<>();
        result.put("ds_0", createDataSource("demo_ds_0"));
        return result;
    }
    
    private DataSource createDataSource(final String dataSourceName) {
        HikariDataSource result = new HikariDataSource();
        result.setDriverClassName("com.mysql.cj.jdbc.Driver");
        result.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8&allowPublicKeyRetrieval=true", HOST, PORT, dataSourceName));
        result.setUsername(USER_NAME);
        result.setPassword(PASSWORD);
        return result;
    }
    
    private Collection<RuleConfiguration> createRuleConfiguration() {
        Collection<RuleConfiguration> result = new LinkedList<>();
        result.add(createTransactionRuleConfiguration());
        result.add(createEncryptRuleConfiguration());
        return result; 
    }
    
    private EncryptRuleConfiguration createEncryptRuleConfiguration() {
        Properties props = new Properties();
        props.setProperty("aes-key-value", "123456");
        EncryptColumnRuleConfiguration columnConfigAes = new EncryptColumnRuleConfiguration("phone", new EncryptColumnItemRuleConfiguration("phone", "standard_encryptor"));
        EncryptTableRuleConfiguration orderItemRule = new EncryptTableRuleConfiguration("t_order_item", Collections.singleton(columnConfigAes));
        EncryptColumnRuleConfiguration statusColumnConfig =
            new EncryptColumnRuleConfiguration("status", new EncryptColumnItemRuleConfiguration("status", "standard_encryptor"));
        statusColumnConfig.setAssistedQuery(new EncryptColumnItemRuleConfiguration("status_assisted", "assisted_encryptor"));
        EncryptTableRuleConfiguration orderRule = new EncryptTableRuleConfiguration("t_order", Collections.singleton(statusColumnConfig)); 
        Map<String, AlgorithmConfiguration> encryptAlgorithmConfigs = new LinkedHashMap<>();
        encryptAlgorithmConfigs.put("standard_encryptor", new AlgorithmConfiguration("AES", props));
        encryptAlgorithmConfigs.put("assisted_encryptor", new AlgorithmConfiguration("assistedTest", props));
        return new EncryptRuleConfiguration(Arrays.asList(orderRule, orderItemRule), encryptAlgorithmConfigs);
    }
     
     private TransactionRuleConfiguration createTransactionRuleConfiguration() {
        return new TransactionRuleConfiguration("XA", "Atomikos", new Properties());
     }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty(ConfigurationPropertyKey.SQL_SHOW.getKey(), "true");
        return result;
    }
}
