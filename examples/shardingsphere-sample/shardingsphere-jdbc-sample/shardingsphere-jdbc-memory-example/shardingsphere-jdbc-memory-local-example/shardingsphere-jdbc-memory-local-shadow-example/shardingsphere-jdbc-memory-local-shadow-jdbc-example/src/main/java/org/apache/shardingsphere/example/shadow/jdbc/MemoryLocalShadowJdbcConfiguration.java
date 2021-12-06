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

package org.apache.shardingsphere.example.shadow.jdbc;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

public final class MemoryLocalShadowJdbcConfiguration {
    
    private static final String HOST = "localhost";
    
    private static final int PORT = 3306;
    
    private static final String USER_NAME = "root";
    
    private static final String PASSWORD = "root";
    
    public DataSource getDataSource() throws SQLException {
        Map<String, DataSource> dataSourceMap = createDataSourceMap();
        Collection<RuleConfiguration> ruleConfigurations = createRuleConfiguration();
        return ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, ruleConfigurations, createShardingSphereProps());
    }

    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new LinkedHashMap<>();
        result.put("demo_ds_0", createDataSource("demo_ds_0"));
        result.put("ds_shadow", createDataSource("ds_shadow"));
        return result;
    }

    private Properties createShardingSphereProps() {
        Properties result = new Properties();
        result.setProperty(ConfigurationPropertyKey.SQL_SHOW.getKey(), "true");
        result.setProperty(ConfigurationPropertyKey.SQL_COMMENT_PARSE_ENABLED.getKey(), "true");
        return result;
    }

    private Collection<RuleConfiguration> createRuleConfiguration() {
        Collection<RuleConfiguration> result = new LinkedList<>();
        result.add(createShadowRuleConfiguration());
        return result;
    }
    
    private RuleConfiguration createShadowRuleConfiguration() {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.setEnable(true);
        result.setShadowAlgorithms(createShadowAlgorithmConfigurations());
        result.setDataSources(createShadowDataSources());
        result.setTables(createShadowTables());
        return result;
    }

    private Map<String, ShadowTableConfiguration> createShadowTables() {
        Map<String, ShadowTableConfiguration> result = new LinkedHashMap<>();
        result.put("t_user", new ShadowTableConfiguration(createDataSourceNames(), createShadowAlgorithmNames()));
        return result;
    }
    
    private Collection<String> createShadowAlgorithmNames() {
        Collection<String> result = new LinkedList<>();
        result.add("user-id-insert-match-algorithm");
        result.add("user-id-delete-match-algorithm");
        result.add("user-id-select-match-algorithm");
        result.add("simple-hint-algorithm");
        return result;
    }
    
    private Collection<String> createDataSourceNames() {
        Collection<String> result = new LinkedList<>();
        result.add("shadow-data-source");
        return result;
    }

    private Map<String, ShadowDataSourceConfiguration> createShadowDataSources() {
        Map<String, ShadowDataSourceConfiguration> result = new LinkedHashMap<>();
        result.put("shadow-data-source", new ShadowDataSourceConfiguration("ds", "shadow-ds"));
        return result;
    }

    private Map<String, ShardingSphereAlgorithmConfiguration> createShadowAlgorithmConfigurations() {
        Map<String, ShardingSphereAlgorithmConfiguration> result = new LinkedHashMap<>();
        Properties userIdInsertProps = new Properties();
        userIdInsertProps.setProperty("operation", "insert");
        userIdInsertProps.setProperty("column", "user_type");
        userIdInsertProps.setProperty("value", "1");
        result.put("user-id-insert-match-algorithm", new ShardingSphereAlgorithmConfiguration("VALUE_MATCH", userIdInsertProps));
        Properties userIdDeleteProps = new Properties();
        userIdDeleteProps.setProperty("operation", "delete");
        userIdDeleteProps.setProperty("column", "user_type");
        userIdDeleteProps.setProperty("value", "1");
        result.put("user-id-delete-match-algorithm", new ShardingSphereAlgorithmConfiguration("VALUE_MATCH", userIdDeleteProps));
        Properties userIdSelectProps = new Properties();
        userIdSelectProps.setProperty("operation", "select");
        userIdSelectProps.setProperty("column", "user_type");
        userIdSelectProps.setProperty("value", "1");
        result.put("user-id-select-match-algorithm", new ShardingSphereAlgorithmConfiguration("VALUE_MATCH", userIdSelectProps));
        Properties noteAlgorithmProps = new Properties();
        noteAlgorithmProps.setProperty("shadow", "true");
        noteAlgorithmProps.setProperty("foo", "bar");
        result.put("simple-hint-algorithm", new ShardingSphereAlgorithmConfiguration("SIMPLE_HINT", noteAlgorithmProps));
        return result;
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
