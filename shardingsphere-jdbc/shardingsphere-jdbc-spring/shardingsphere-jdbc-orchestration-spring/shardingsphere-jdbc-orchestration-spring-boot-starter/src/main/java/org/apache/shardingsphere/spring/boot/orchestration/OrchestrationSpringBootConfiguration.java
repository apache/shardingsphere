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

package org.apache.shardingsphere.spring.boot.orchestration;

import com.google.common.base.Preconditions;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.cluster.configuration.swapper.ClusterConfigurationYamlSwapper;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.driver.orchestration.internal.datasource.OrchestrationShardingSphereDataSource;
import org.apache.shardingsphere.spring.boot.orchestration.common.OrchestrationSpringBootRootConfiguration;
import org.apache.shardingsphere.spring.boot.orchestration.rule.LocalRulesCondition;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.orchestration.center.config.CenterConfiguration;
import org.apache.shardingsphere.orchestration.center.config.OrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.center.yaml.config.YamlCenterRepositoryConfiguration;
import org.apache.shardingsphere.orchestration.center.yaml.swapper.CenterRepositoryConfigurationYamlSwapper;
import org.apache.shardingsphere.spring.boot.datasource.DataSourceMapSetter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Orchestration spring boot configuration.
 */
@Configuration
@ComponentScan("org.apache.shardingsphere.spring.boot.converter")
@EnableConfigurationProperties(OrchestrationSpringBootRootConfiguration.class)
@ConditionalOnProperty(prefix = "spring.shardingsphere", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
public class OrchestrationSpringBootConfiguration implements EnvironmentAware {
    
    private final Map<String, DataSource> dataSourceMap = new LinkedHashMap<>();
    
    private final OrchestrationSpringBootRootConfiguration root;
    
    private final CenterRepositoryConfigurationYamlSwapper centerRepositorySwapper = new CenterRepositoryConfigurationYamlSwapper();
    
    /**
     * Get orchestration configuration.
     *
     * @return orchestration configuration
     */
    @Bean
    public OrchestrationConfiguration orchestrationConfiguration() {
        Preconditions.checkState(isValidOrchestrationConfiguration(), "The orchestration configuration is invalid, please configure orchestration");
        Map<String, CenterConfiguration> instanceConfigurationMap = new HashMap<>(root.getOrchestration().size(), 1);
        for (Entry<String, YamlCenterRepositoryConfiguration> entry : root.getOrchestration().entrySet()) {
            instanceConfigurationMap.put(entry.getKey(), centerRepositorySwapper.swapToObject(entry.getValue()));
        }
        return new OrchestrationConfiguration(instanceConfigurationMap);
    }
    
    private boolean isValidOrchestrationConfiguration() {
        return !CollectionUtils.isEmpty(root.getOrchestration());
    }
    
    /**
     * Get orchestration ShardingSphere data source bean by local configuration.
     *
     * @param rules rules configuration
     * @param orchestrationConfiguration orchestration configuration
     * @return orchestration sharding data source bean
     * @throws SQLException SQL exception
     */
    @Bean
    @Conditional(LocalRulesCondition.class)
    @Autowired(required = false)
    public DataSource localShardingSphereDataSource(final ObjectProvider<List<RuleConfiguration>> rules, final OrchestrationConfiguration orchestrationConfiguration) throws SQLException {
        List<RuleConfiguration> ruleConfigurations = Optional.ofNullable(rules.getIfAvailable()).orElse(Collections.emptyList());
        return null == root.getCluster() ? new OrchestrationShardingSphereDataSource(new ShardingSphereDataSource(dataSourceMap, ruleConfigurations, root.getProps()), orchestrationConfiguration)
                : new OrchestrationShardingSphereDataSource(new ShardingSphereDataSource(dataSourceMap, ruleConfigurations, root.getProps()), orchestrationConfiguration,
                new ClusterConfigurationYamlSwapper().swapToObject(root.getCluster()));
    }
    
    /**
     * Get data source bean from registry center.
     *
     * @param orchestrationConfiguration orchestration configuration
     * @return data source bean
     * @throws SQLException SQL Exception
     */
    @Bean
    @ConditionalOnMissingBean(DataSource.class)
    public DataSource dataSource(final OrchestrationConfiguration orchestrationConfiguration) throws SQLException {
        return null == root.getCluster() ? new OrchestrationShardingSphereDataSource(orchestrationConfiguration)
                : new OrchestrationShardingSphereDataSource(orchestrationConfiguration, new ClusterConfigurationYamlSwapper().swapToObject(root.getCluster()));
    }
    
    @Override
    public final void setEnvironment(final Environment environment) {
        dataSourceMap.putAll(DataSourceMapSetter.getDataSourceMap(environment));
    }
}
