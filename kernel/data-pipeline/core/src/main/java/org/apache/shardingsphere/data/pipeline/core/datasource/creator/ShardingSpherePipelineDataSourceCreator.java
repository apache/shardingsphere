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

package org.apache.shardingsphere.data.pipeline.core.datasource.creator;

import org.apache.shardingsphere.authority.yaml.config.YamlAuthorityRuleConfiguration;
import org.apache.shardingsphere.data.pipeline.api.type.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextManager;
import org.apache.shardingsphere.data.pipeline.core.datasource.yaml.PipelineYamlRuleConfigurationReviser;
import org.apache.shardingsphere.data.pipeline.spi.PipelineDataSourceCreator;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.destroyer.DataSourcePoolDestroyer;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.mode.YamlModeConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.mode.YamlPersistRepositoryConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.mode.YamlModeConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.mode.manager.ContextManager;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * ShardingSphere pipeline data source creator.
 */
public final class ShardingSpherePipelineDataSourceCreator implements PipelineDataSourceCreator {
    
    @Override
    public DataSource create(final Object dataSourceConfig) throws SQLException {
        YamlRootConfiguration yamlRootConfig = YamlEngine.unmarshal(YamlEngine.marshal(dataSourceConfig), YamlRootConfiguration.class);
        removeAuthorityRuleConfiguration(yamlRootConfig);
        yamlRootConfig.setProps(createConfigurationProperties());
        reviseYamlRuleConfiguration(yamlRootConfig);
        yamlRootConfig.setMode(createStandaloneModeConfiguration());
        return createShardingSphereDataSource(yamlRootConfig);
    }
    
    private void removeAuthorityRuleConfiguration(final YamlRootConfiguration yamlRootConfig) {
        yamlRootConfig.getRules().removeIf(YamlAuthorityRuleConfiguration.class::isInstance);
    }
    
    private Properties createConfigurationProperties() {
        Properties realtimeProps = getRealtimeProperties();
        Properties result = new Properties();
        for (String each : getConfigurationPropertyKeys()) {
            Object value = realtimeProps.get(each);
            if (null != value) {
                result.put(each, value);
            }
        }
        result.put(TemporaryConfigurationPropertyKey.SYSTEM_SCHEMA_METADATA_ASSEMBLY_ENABLED.getKey(), String.valueOf(Boolean.FALSE));
        // Set a large enough value to enable ConnectionMode.MEMORY_STRICTLY, make sure streaming query work.
        result.put(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY.getKey(), 100000);
        return result;
    }
    
    private Properties getRealtimeProperties() {
        ContextManager contextManager = PipelineContextManager.getProxyContext();
        if (null == contextManager) {
            return new Properties();
        }
        return contextManager.getMetaDataContexts().getMetaData().getProps().getProps();
    }
    
    private List<String> getConfigurationPropertyKeys() {
        List<String> result = new LinkedList<>();
        result.add(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE.getKey());
        result.add(ConfigurationPropertyKey.SQL_SHOW.getKey());
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private void reviseYamlRuleConfiguration(final YamlRootConfiguration yamlRootConfig) {
        OrderedSPILoader.getServices(PipelineYamlRuleConfigurationReviser.class, yamlRootConfig.getRules()).forEach((key, value) -> value.revise(key));
    }
    
    private YamlModeConfiguration createStandaloneModeConfiguration() {
        YamlModeConfiguration result = new YamlModeConfiguration();
        result.setType("Standalone");
        YamlPersistRepositoryConfiguration yamlRepositoryConfig = new YamlPersistRepositoryConfiguration();
        yamlRepositoryConfig.setType("Memory");
        result.setRepository(yamlRepositoryConfig);
        return result;
    }
    
    private DataSource createShardingSphereDataSource(final YamlRootConfiguration yamlRootConfig) throws SQLException {
        Map<String, DataSource> dataSourceMap = new YamlDataSourceConfigurationSwapper().swapToDataSources(yamlRootConfig.getDataSources(), false);
        try {
            return createShardingSphereDataSource(dataSourceMap, yamlRootConfig);
            // CHECKSTYLE:OFF
        } catch (final SQLException | RuntimeException ex) {
            // CHECKSTYLE:ON
            dataSourceMap.values().stream().map(DataSourcePoolDestroyer::new).forEach(DataSourcePoolDestroyer::asyncDestroy);
            throw ex;
        }
    }
    
    private DataSource createShardingSphereDataSource(final Map<String, DataSource> dataSourceMap, final YamlRootConfiguration yamlRootConfig) throws SQLException {
        ModeConfiguration modeConfig = new YamlModeConfigurationSwapper().swapToObject(yamlRootConfig.getMode());
        Collection<RuleConfiguration> ruleConfigs = new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(yamlRootConfig.getRules());
        return ShardingSphereDataSourceFactory.createDataSource(yamlRootConfig.getDatabaseName(), modeConfig, dataSourceMap, ruleConfigs, yamlRootConfig.getProps());
    }
    
    @Override
    public String getType() {
        return ShardingSpherePipelineDataSourceConfiguration.TYPE;
    }
}
