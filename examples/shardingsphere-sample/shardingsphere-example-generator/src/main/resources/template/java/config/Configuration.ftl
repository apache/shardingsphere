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

<#assign package="" />
<#if feature?split(",")?size gt 1>
    <#assign package="mixed" />
<#else>
    <#assign package = feature?replace('-', '.') />
</#if>
package org.apache.shardingsphere.example.${package}.${framework};

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
<#if feature?contains("sharding")>
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepositoryConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
</#if>
<#if feature?contains("readwrite-splitting")>
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
</#if>
<#if feature?contains("encrypt")>
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
</#if>
<#if feature?contains("shadow")>
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
</#if>
<#if feature?contains("db-discovery")>
import com.google.common.collect.Lists;
import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryHeartBeatConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
</#if>

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

<#assign featureName="" />
<#if feature?split(",")?size gt 1>
    <#assign featureName="Mixed" />
<#else>
    <#list feature?split("-") as item>
        <#assign featureName=featureName + item?cap_first />
    </#list>
</#if>
public final class Configuration {
    
    private static final String HOST = "${host}";
    
    private static final int PORT = ${(port)?c};
    
    private static final String USER_NAME = "${username}";
    
    private static final String PASSWORD = "${(password)?string}";
    
    /**
     * Create a DataSource object, which is an object rewritten by ShardingSphere itself
     * and contains various rules for rewriting the original data storage. When in use, you only need to use this object.
     * @return datasource
     * @throws SQLException SQL exception
     */
    public DataSource getDataSource() throws SQLException {
    <#if mode=="memory">
        return ShardingSphereDataSourceFactory.createDataSource(createDataSourceMap(), createRuleConfiguration(), createShardingSphereProps());
    <#else>
        return ShardingSphereDataSourceFactory.createDataSource(createModeConfiguration(), createDataSourceMap(), createRuleConfiguration(), createShardingSphereProps());
    </#if>
    }
<#if mode!="memory">
    
    private static ModeConfiguration createModeConfiguration() {
    <#if mode=="cluster">
        return new ModeConfiguration("Cluster", new ClusterPersistRepositoryConfiguration("ZooKeeper", "governance-sharding-data-source", "localhost:2181", new Properties()), true);
    </#if>
    <#if mode=="standalone">
        return new ModeConfiguration("Standalone", new StandalonePersistRepositoryConfiguration("File", new Properties()), true);
    </#if> 
    }
</#if>
    
    private Properties createShardingSphereProps() {
        Properties result = new Properties();
        result.setProperty(ConfigurationPropertyKey.SQL_SHOW.getKey(), "true");
        return result;
    }
    
    private Collection<RuleConfiguration> createRuleConfiguration() {
        Collection<RuleConfiguration> result = new LinkedList<>();
    <#if feature?contains("db-discovery")>
        result.add(createDatabaseDiscoveryRuleConfiguration());
    </#if>
    <#if feature?contains("encrypt")>
        result.add(createEncryptRuleConfiguration());
    </#if>
    <#if feature?contains("readwrite-splitting")>
        result.add(createReadwriteSplittingRuleConfiguration());
    </#if>
    <#if feature?contains("shadow")>
        result.add(createShadowRuleConfiguration());
        result.add(createSQLParserRuleConfiguration());
    </#if>
    <#if feature?contains("sharding")>
        result.add(createShardingRuleConfiguration());
    </#if>
        return result; 
    }
<#list feature?split(",") as item>
    <#include "${item}.ftl">
</#list>
    
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
