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
<#assign package = feature?replace('-', '')?replace(',', '.') />

package org.apache.shardingsphere.example.${package}.${framework}.config;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
<#if mode=="standalone">
import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepositoryConfiguration;
<#else>
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
</#if>
<#if feature?contains("sharding")>
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.broadcast.config.BroadcastRuleConfiguration;
</#if>
<#if feature?contains("readwrite-splitting")>
import org.apache.shardingsphere.readwritesplitting.config.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
</#if>
<#if feature?contains("encrypt")>
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.encrypt.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptTableRuleConfiguration;
</#if>
<#if feature?contains("shadow")>
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
</#if>
<#if feature?contains("mask")>
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.mask.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.config.rule.MaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.config.rule.MaskTableRuleConfiguration;    
</#if>
<#if transaction!="local">
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
</#if>

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
    
    <#assign repository = repository!'JDBC'>
    
    private static final String HOST = "${host}";
    
    private static final int PORT = ${port};
    
    private static final String USER_NAME = "${username}";
    
    private static final String PASSWORD = "${password}";
    
    public DataSource createDataSource() throws SQLException {
        return ShardingSphereDataSourceFactory.createDataSource(createModeConfiguration(), createDataSourceMap(), createRuleConfiguration(), createProperties());
    }
    
    private static ModeConfiguration createModeConfiguration() {
    <#if mode=="cluster-zookeeper">
        return new ModeConfiguration("Cluster", new ClusterPersistRepositoryConfiguration("ZooKeeper", "${namespace}", "localhost:2181", new Properties()));
    </#if>
    <#if mode=="cluster-etcd">
        return new ModeConfiguration("Cluster", new ClusterPersistRepositoryConfiguration("etcd", "${namespace}", "localhost:2379", new Properties()));
    </#if>
    <#if mode=="standalone">
        return new ModeConfiguration("Standalone", new StandalonePersistRepositoryConfiguration("${repository}", new Properties()));
    </#if> 
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new LinkedHashMap<>();
        result.put("ds_0", createDataSource("demo_ds_0"));
    <#if feature!="encrypt" && feature!="mask">
        result.put("ds_1", createDataSource("demo_ds_1"));
        <#if feature!="shadow">
        result.put("ds_2", createDataSource("demo_ds_2"));
        </#if>
    </#if>
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
        result.add(createSingleRuleConfiguration());
    <#if transaction!="local">
        result.add(createTransactionRuleConfiguration());
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
        result.add(createBroadcastRuleConfiguration());
    </#if>
    <#if feature?contains("mask")>
        result.add(createMaskRuleConfiguration());
    </#if>
        return result;
    }
<#list feature?split(",") as item>
    <#include "${item}.ftl">
</#list>
     <#if transaction!="local">
     
     private TransactionRuleConfiguration createTransactionRuleConfiguration() {
     <#if transaction=="xa-atomikos">
        return new TransactionRuleConfiguration("XA", "Atomikos", new Properties());
     <#elseif transaction=="xa-narayana">
        return new TransactionRuleConfiguration("XA", "Narayana", new Properties());
     <#elseif transaction=="base-seata">
        return new TransactionRuleConfiguration("BASE", "Seata", new Properties());
     </#if>
     }
    </#if>

    private SingleRuleConfiguration createSingleRuleConfiguration() {
        return new SingleRuleConfiguration(Collections.singletonList("*.*"), null);
    }

    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty(ConfigurationPropertyKey.SQL_SHOW.getKey(), "true");
        return result;
    }
}
