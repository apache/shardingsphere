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

package org.apache.shardingsphere.example.mixed.jdbc;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

public final class MemoryLocalMixedJdbcConfiguration {
    
    private static final String HOST = "localhost";
    
    private static final int PORT = 3306;
    
    private static final String USER_NAME = "root";
    
    private static final String PASSWORD = "root";
    
    /**
     * Create a DataSource object, which is an object rewritten by ShardingSphere itself
     * and contains various rules for rewriting the original data storage. When in use, you only need to use this object.
     * @return datasource
     * @throws SQLException SQL exception
     */
    public DataSource getDataSource() throws SQLException {
        return ShardingSphereDataSourceFactory.createDataSource(createDataSourceMap(), createRuleConfiguration(), createShardingSphereProps());
    }
    
    private Properties createShardingSphereProps() {
        Properties result = new Properties();
        result.setProperty(ConfigurationPropertyKey.SQL_SHOW.getKey(), "true");
        return result;
    }
    
    private Collection<RuleConfiguration> createRuleConfiguration() {
        Collection<RuleConfiguration> result = new LinkedList<>();
        result.add(createEncryptRuleConfiguration());
        result.add(createReadwriteSplittingRuleConfiguration());
        result.add(createShardingRuleConfiguration());
        return result; 
    }
    
    private ShardingRuleConfiguration createShardingRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(getOrderTableRuleConfiguration());
        result.getTables().add(getOrderItemTableRuleConfiguration());
        result.getBroadcastTables().add("t_address");
        result.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "inline"));
        Properties props = new Properties();
        props.setProperty("algorithm-expression", "ds_${user_id % 2}");
        result.getShardingAlgorithms() .put("inline", new ShardingSphereAlgorithmConfiguration("INLINE", props));
        result.getKeyGenerators().put("snowflake", new ShardingSphereAlgorithmConfiguration("SNOWFLAKE", new Properties()));
        return result;
    }
    
    private static ShardingTableRuleConfiguration getOrderTableRuleConfiguration() {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("t_order");
        result.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("order_id", "snowflake"));
        return result;
    }
    
    private static ShardingTableRuleConfiguration getOrderItemTableRuleConfiguration() {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("t_order_item");
        result.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("order_item_id", "snowflake"));
        return result;
    }
    
    private ReadwriteSplittingRuleConfiguration createReadwriteSplittingRuleConfiguration() {
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceConfig = new ReadwriteSplittingDataSourceRuleConfiguration(
                "ds_0", "Static", getReadWriteProperties(), null);
        return new ReadwriteSplittingRuleConfiguration(Collections.singleton(dataSourceConfig), Collections.emptyMap());
    }
    
    private static Properties getReadWriteProperties() {
        Properties result = new Properties();
        result.setProperty("write-data-source-name", "ds_0");
        result.setProperty("read-data-source-names", "ds_1, ds_2");
        return result;
    }
    
    private EncryptRuleConfiguration createEncryptRuleConfiguration() {
        Properties props = new Properties();
        props.setProperty("aes-key-value", "123456");
        EncryptColumnRuleConfiguration columnConfigAes = new EncryptColumnRuleConfiguration("phone", "phone", "", "phone_plain", "phone_encryptor");
        EncryptColumnRuleConfiguration columnConfigTest = new EncryptColumnRuleConfiguration("status", "status", "assisted_query_status", "", "string_encryptor");
        EncryptTableRuleConfiguration orderItemRule = new EncryptTableRuleConfiguration("t_order_item", Collections.singleton(columnConfigAes), true);
        EncryptTableRuleConfiguration orderRule = new EncryptTableRuleConfiguration("t_order", Collections.singleton(columnConfigTest), true);
        Map<String, ShardingSphereAlgorithmConfiguration> encryptAlgorithmConfigs = new LinkedHashMap<>(2, 1);
        encryptAlgorithmConfigs.put("phone_encryptor", new ShardingSphereAlgorithmConfiguration("AES", props));
        encryptAlgorithmConfigs.put("string_encryptor", new ShardingSphereAlgorithmConfiguration("assistedTest", props));
        return new EncryptRuleConfiguration(Arrays.asList(orderRule, orderItemRule), encryptAlgorithmConfigs);
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("ds_0", createDataSource("demo_ds_0"));
        dataSourceMap.put("ds_1", createDataSource("demo_ds_1"));
        dataSourceMap.put("ds_2", createDataSource("demo_ds_2"));
        return dataSourceMap;
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
