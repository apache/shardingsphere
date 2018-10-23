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
import io.shardingsphere.api.config.ShardingRuleConfiguration;
import io.shardingsphere.orchestration.config.OrchestrationConfiguration;
import io.shardingsphere.orchestration.config.OrchestrationType;
import io.shardingsphere.orchestration.internal.OrchestrationFacade;
import io.shardingsphere.orchestration.internal.config.ConfigurationService;
import io.shardingsphere.orchestration.internal.event.config.ShardingConfigurationEventBusEvent;
import io.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import io.shardingsphere.shardingjdbc.orchestration.internal.circuit.datasource.CircuitBreakerDataSource;
import io.shardingsphere.orchestration.internal.rule.OrchestrationShardingRule;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;

/**
 * Orchestration sharding datasource.
 *
 * @author panjuan
 */
public class OrchestrationShardingDataSource extends AbstractOrchestrationDataSource {
    
    private ShardingDataSource dataSource;
    
    public OrchestrationShardingDataSource(final ShardingDataSource shardingDataSource, final OrchestrationConfiguration orchestrationConfig) throws SQLException {
        super(new OrchestrationFacade(orchestrationConfig), shardingDataSource.getDataSourceMap());
        dataSource = new ShardingDataSource(shardingDataSource.getDataSourceMap(), new OrchestrationShardingRule(shardingDataSource.getShardingContext().getShardingRule().getShardingRuleConfig(),
                shardingDataSource.getDataSourceMap().keySet()), ConfigMapContext.getInstance().getShardingConfig(), shardingDataSource.getShardingProperties().getProps());
        initOrchestrationFacade(dataSource);
    }
    
    public OrchestrationShardingDataSource(final OrchestrationConfiguration orchestrationConfig) throws SQLException {
        super(new OrchestrationFacade(orchestrationConfig));
        ConfigurationService configService = getOrchestrationFacade().getConfigService();
        ShardingRuleConfiguration shardingRuleConfig = configService.loadShardingRuleConfiguration();
        Preconditions.checkState(null != shardingRuleConfig && !shardingRuleConfig.getTableRuleConfigs().isEmpty(), "Missing the sharding rule configuration on register center");
        dataSource = new ShardingDataSource(configService.loadDataSourceMap(),
                new OrchestrationShardingRule(shardingRuleConfig, configService.loadDataSourceMap().keySet()), configService.loadShardingConfigMap(), configService.loadShardingProperties());
        getOrchestrationFacade().init(OrchestrationType.SHARDING);
    }
    
    private void initOrchestrationFacade(final ShardingDataSource shardingDataSource) {
        getOrchestrationFacade().init(shardingDataSource.getDataSourceMap(), shardingDataSource.getShardingContext().getShardingRule().getShardingRuleConfig(),
                ConfigMapContext.getInstance().getShardingConfig(), shardingDataSource.getShardingProperties().getProps());
    }
    
    @Override
    public final Connection getConnection() {
        if (isCircuitBreak()) {
            return new CircuitBreakerDataSource().getConnection();
        }
        return dataSource.getConnection();
    }
    
    @Override
    public final void close() {
        dataSource.close();
        getOrchestrationFacade().close();
    }
    
    /**
     * Renew sharding data source.
     *
     * @param shardingEvent sharding configuration event bus event.
     * @throws SQLException SQL exception
     */
    @Subscribe
    public void renew(final ShardingConfigurationEventBusEvent shardingEvent) throws SQLException {
        dataSource = new ShardingDataSource(shardingEvent.getDataSourceMap(), shardingEvent.getShardingRule(), new LinkedHashMap<String, Object>(), shardingEvent.getProps());
    }
}
