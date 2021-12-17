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

package org.apache.shardingsphere.example.db.discovery.jdbc;

import com.google.common.collect.Lists;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryHeartBeatConfiguration;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class MemoryLocalDbDiscoveryJdbcConfiguration {
    
    private static final String USER_NAME = "root";
    
    private static final String PASSWORD = "123456";
    
    /**
     * Create a DataSource object, which is an object rewritten by ShardingSphere itself 
     * and contains various rules for rewriting the original data storage. When in use, you only need to use this object.
     * @return
     * @throws SQLException
     */
    public DataSource getDataSource() throws SQLException {
        return ShardingSphereDataSourceFactory.createDataSource(createDataSourceMap(), Collections.singleton(createDatabaseDiscoveryRuleConfiguration()), new Properties());
    }

    private DatabaseDiscoveryRuleConfiguration createDatabaseDiscoveryRuleConfiguration() {
        return new DatabaseDiscoveryRuleConfiguration(createDataSources(), createDiscoveryHeartbeats(), createDiscoveryTypes());
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("ds_0", createPrimaryDataSource());
        dataSourceMap.put("ds_0_replica_0", createReplicaDataSource1());
        dataSourceMap.put("ds_0_replica_1", createReplicaDataSource2());
        return dataSourceMap;
    }
    
    private Collection<DatabaseDiscoveryDataSourceRuleConfiguration> createDataSources() {
        DatabaseDiscoveryDataSourceRuleConfiguration dsRuleConf1 = new DatabaseDiscoveryDataSourceRuleConfiguration("rule", Lists.newArrayList("ds_0", "ds_0_replica_0", "ds_0_replica_1"), "mgr-heartbeat", "mgr");
        return Lists.newArrayList(dsRuleConf1);
    }

    private Map<String, DatabaseDiscoveryHeartBeatConfiguration> createDiscoveryHeartbeats() {
        Map<String, DatabaseDiscoveryHeartBeatConfiguration> discoveryHeartBeatConfiguration = new HashMap<>(1, 1);
        Properties props = new Properties();
        props.put("keep-alive-cron", "0/5 * * * * ?");
        discoveryHeartBeatConfiguration.put("mgr-heartbeat", new DatabaseDiscoveryHeartBeatConfiguration(props));
        return discoveryHeartBeatConfiguration;
    }

    private Map<String, ShardingSphereAlgorithmConfiguration> createDiscoveryTypes() {
        Map<String, ShardingSphereAlgorithmConfiguration> discoveryTypes = new HashMap<>(1, 1);
        Properties props = new Properties();
        props.put("keep-alive-cron", "0/5 * * * * ?");
        props.put("group-name", "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        discoveryTypes.put("mgr", new ShardingSphereAlgorithmConfiguration("MGR", props));
        return discoveryTypes;
    }
    
    private DataSource createPrimaryDataSource() {
        HikariDataSource result = new HikariDataSource();
        result.setDriverClassName("com.mysql.jdbc.Driver");
        result.setJdbcUrl("jdbc:mysql://172.72.0.15:3306/ds_0?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8");
        result.setUsername(USER_NAME);
        result.setPassword(PASSWORD);
        return result;
    }

    private DataSource createReplicaDataSource1() {
        HikariDataSource result = new HikariDataSource();
        result.setDriverClassName("com.mysql.jdbc.Driver");
        result.setJdbcUrl("jdbc:mysql://172.72.0.16:3306/ds_0_replica_0?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8");
        result.setUsername(USER_NAME);
        result.setPassword(PASSWORD);
        return result;
    }

    private DataSource createReplicaDataSource2() {
        HikariDataSource result = new HikariDataSource();
        result.setDriverClassName("com.mysql.jdbc.Driver");
        result.setJdbcUrl("jdbc:mysql://172.72.0.17:3306/ds_0_replica_1?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8");
        result.setUsername(USER_NAME);
        result.setPassword(PASSWORD);
        return result;
    }
}
