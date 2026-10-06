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

import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.data.pipeline.api.type.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextManager;
import org.apache.shardingsphere.data.pipeline.core.datasource.rule.PipelineRuleConfigurationReviser;
import org.apache.shardingsphere.data.pipeline.spi.PipelineDataSourceCreator;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.pool.destroyer.DataSourcePoolDestroyer;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepositoryConfiguration;

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
        ShardingSpherePipelineDataSourceConfiguration config = (ShardingSpherePipelineDataSourceConfiguration) dataSourceConfig;
        Collection<RuleConfiguration> ruleConfigs = config.getCreationRuleConfigurations();
        removeAuthorityRuleConfiguration(ruleConfigs);
        reviseRuleConfiguration(ruleConfigs);
        return createShardingSphereDataSource(config, ruleConfigs);
    }
    
    private void removeAuthorityRuleConfiguration(final Collection<RuleConfiguration> ruleConfigs) {
        ruleConfigs.removeIf(AuthorityRuleConfiguration.class::isInstance);
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
    private void reviseRuleConfiguration(final Collection<RuleConfiguration> ruleConfigs) {
        OrderedSPILoader.getServices(PipelineRuleConfigurationReviser.class, ruleConfigs).forEach((key, value) -> value.revise(key));
    }
    
    private ModeConfiguration createStandaloneModeConfiguration() {
        return new ModeConfiguration("Standalone", new StandalonePersistRepositoryConfiguration("Memory", new Properties()));
    }
    
    private DataSource createShardingSphereDataSource(final ShardingSpherePipelineDataSourceConfiguration config, final Collection<RuleConfiguration> ruleConfigs) throws SQLException {
        Map<String, DataSourcePoolProperties> dataSourcePropsMap = config.getDataSourcePoolPropertiesMap();
        Map<String, DataSource> dataSourceMap = DataSourcePoolCreator.create(dataSourcePropsMap, false);
        try {
            return ShardingSphereDataSourceFactory.createDataSource(config.getDatabaseName(), createStandaloneModeConfiguration(), dataSourceMap, ruleConfigs, createConfigurationProperties());
            // CHECKSTYLE:OFF
        } catch (final SQLException | RuntimeException ex) {
            // CHECKSTYLE:ON
            dataSourceMap.values().stream().map(DataSourcePoolDestroyer::new).forEach(DataSourcePoolDestroyer::asyncDestroy);
            throw ex;
        }
    }
    
    @Override
    public String getType() {
        return ShardingSpherePipelineDataSourceConfiguration.TYPE;
    }
}
