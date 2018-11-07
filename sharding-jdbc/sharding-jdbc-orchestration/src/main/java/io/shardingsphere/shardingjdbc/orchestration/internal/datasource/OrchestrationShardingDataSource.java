/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingjdbc.orchestration.internal.datasource;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import io.shardingsphere.api.ConfigMapContext;
import io.shardingsphere.api.config.RuleConfiguration;
import io.shardingsphere.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.constant.ShardingConstant;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.orchestration.config.OrchestrationConfiguration;
import io.shardingsphere.orchestration.internal.OrchestrationFacade;
import io.shardingsphere.orchestration.internal.config.service.ConfigurationService;
import io.shardingsphere.orchestration.internal.config.event.DataSourceChangedEvent;
import io.shardingsphere.orchestration.internal.config.event.PropertiesChangedEvent;
import io.shardingsphere.orchestration.internal.config.event.ShardingRuleChangedEvent;
import io.shardingsphere.orchestration.internal.rule.OrchestrationShardingRule;
import io.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import io.shardingsphere.shardingjdbc.orchestration.internal.circuit.datasource.CircuitBreakerDataSource;
import io.shardingsphere.shardingjdbc.orchestration.internal.util.DataSourceConverter;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Orchestration sharding datasource.
 *
 * @author panjuan
 */
public class OrchestrationShardingDataSource extends AbstractOrchestrationDataSource {
    
    private ShardingDataSource dataSource;
    
    public OrchestrationShardingDataSource(final OrchestrationConfiguration orchestrationConfig) throws SQLException {
        super(new OrchestrationFacade(orchestrationConfig, Collections.singletonList(ShardingConstant.LOGIC_SCHEMA_NAME)));
        ConfigurationService configService = getOrchestrationFacade().getConfigService();
        ShardingRuleConfiguration shardingRuleConfig = configService.loadShardingRuleConfiguration(ShardingConstant.LOGIC_SCHEMA_NAME);
        Preconditions.checkState(null != shardingRuleConfig && !shardingRuleConfig.getTableRuleConfigs().isEmpty(), "Missing the sharding rule configuration on register center");
        dataSource = new ShardingDataSource(DataSourceConverter.getDataSourceMap(configService.loadDataSourceConfigurations(ShardingConstant.LOGIC_SCHEMA_NAME)),
                new OrchestrationShardingRule(shardingRuleConfig, configService.loadDataSourceConfigurations(ShardingConstant.LOGIC_SCHEMA_NAME).keySet()),
                configService.loadConfigMap(), configService.loadProperties());
        getOrchestrationFacade().init();
    }
    
    public OrchestrationShardingDataSource(final ShardingDataSource shardingDataSource, final OrchestrationConfiguration orchestrationConfig) throws SQLException {
        super(new OrchestrationFacade(orchestrationConfig, Collections.singletonList(ShardingConstant.LOGIC_SCHEMA_NAME)), shardingDataSource.getDataSourceMap());
        dataSource = new ShardingDataSource(shardingDataSource.getDataSourceMap(), new OrchestrationShardingRule(shardingDataSource.getShardingContext().getShardingRule().getShardingRuleConfig(),
                shardingDataSource.getDataSourceMap().keySet()), ConfigMapContext.getInstance().getConfigMap(), shardingDataSource.getShardingContext().getShardingProperties().getProps());
        getOrchestrationFacade().init(Collections.singletonMap(ShardingConstant.LOGIC_SCHEMA_NAME, DataSourceConverter.getDataSourceConfigurationMap(dataSource.getDataSourceMap())),
                getRuleConfigurationMap(), null, ConfigMapContext.getInstance().getConfigMap(), dataSource.getShardingContext().getShardingProperties().getProps());
    }
    
    private Map<String, RuleConfiguration> getRuleConfigurationMap() {
        Map<String, RuleConfiguration> ruleConfigurationMap = new LinkedHashMap<>();
        ruleConfigurationMap.put(ShardingConstant.LOGIC_SCHEMA_NAME, dataSource.getShardingContext().getShardingRule().getShardingRuleConfig());
        return ruleConfigurationMap;
    }
    
    @Override
    public final Connection getConnection() {
        return isCircuitBreak() ? new CircuitBreakerDataSource().getConnection() : dataSource.getConnection();
    }
    
    @Override
    public final void close() {
        dataSource.close();
        getOrchestrationFacade().close();
    }
    
    /**
     * Renew sharding rule.
     *
     * @param shardingEvent sharding event
     */
    @Subscribe
    @SneakyThrows
    public final void renew(final ShardingRuleChangedEvent shardingEvent) {
        dataSource = new ShardingDataSource(dataSource.getDataSourceMap(), new ShardingRule(shardingEvent.getShardingRuleConfiguration(),
                dataSource.getDataSourceMap().keySet()), ConfigMapContext.getInstance().getConfigMap(), dataSource.getShardingContext().getShardingProperties().getProps());
    }
    
    /**
     * Renew sharding data source.
     *
     * @param dataSourceEvent data source event
     */
    @Subscribe
    @SneakyThrows
    public final void renew(final DataSourceChangedEvent dataSourceEvent) {
        dataSource.close();
        dataSource = new ShardingDataSource(DataSourceConverter.getDataSourceMap(dataSourceEvent.getDataSourceConfigurations()), dataSource.getShardingContext().getShardingRule(),
                ConfigMapContext.getInstance().getConfigMap(), dataSource.getShardingContext().getShardingProperties().getProps());
    }
    
    /**
     * Renew properties.
     *
     * @param propertiesEvent properties event
     */
    @SneakyThrows
    @Subscribe
    public void renew(final PropertiesChangedEvent propertiesEvent) {
        dataSource = new ShardingDataSource(dataSource.getDataSourceMap(),
                dataSource.getShardingContext().getShardingRule(), ConfigMapContext.getInstance().getConfigMap(), propertiesEvent.getProps());
    }
}
