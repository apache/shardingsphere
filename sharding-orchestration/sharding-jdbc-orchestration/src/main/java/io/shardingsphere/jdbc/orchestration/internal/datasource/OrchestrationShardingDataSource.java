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

package io.shardingsphere.jdbc.orchestration.internal.datasource;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import io.shardingsphere.core.api.ConfigMapContext;
import io.shardingsphere.core.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.jdbc.orchestration.config.OrchestrationConfiguration;
import io.shardingsphere.jdbc.orchestration.internal.OrchestrationFacade;
import io.shardingsphere.jdbc.orchestration.internal.circuit.datasource.CircuitBreakerDataSource;
import io.shardingsphere.jdbc.orchestration.internal.config.ConfigurationService;
import io.shardingsphere.jdbc.orchestration.internal.event.config.ShardingConfigurationEventBusEvent;
import io.shardingsphere.jdbc.orchestration.internal.event.state.DisabledStateEventBusEvent;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Orchestration sharding datasource.
 *
 * @author panjuan
 */
public class OrchestrationShardingDataSource extends AbstractOrchestrationDataSource {
    
    private ShardingDataSource dataSource;
    
    public OrchestrationShardingDataSource(final ShardingDataSource shardingDataSource, final OrchestrationConfiguration orchestrationConfig) throws SQLException {
        super(new OrchestrationFacade(orchestrationConfig), shardingDataSource.getDataSourceMap());
        this.dataSource = shardingDataSource;
        initOrchestrationFacade(dataSource);
    }
    
    public OrchestrationShardingDataSource(final OrchestrationConfiguration orchestrationConfig) throws SQLException {
        super(new OrchestrationFacade(orchestrationConfig));
        ConfigurationService configService = getOrchestrationFacade().getConfigService();
        ShardingRuleConfiguration shardingRuleConfig = configService.loadShardingRuleConfiguration();
        Preconditions.checkNotNull(shardingRuleConfig, "Missing the sharding rule configuration on register center");
        dataSource = new ShardingDataSource(configService.loadDataSourceMap(),
                new ShardingRule(shardingRuleConfig, configService.loadDataSourceMap().keySet()), configService.loadShardingConfigMap(), configService.loadShardingProperties());
        initOrchestrationFacade(dataSource);
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
     * @throws SQLException sql exception
     */
    @Subscribe
    public void renew(final ShardingConfigurationEventBusEvent shardingEvent) throws SQLException {
        dataSource = new ShardingDataSource(shardingEvent.getDataSourceMap(), shardingEvent.getShardingRule(), new LinkedHashMap<String, Object>(), shardingEvent.getProps());
    }
    
    /**
     * Renew disable dataSource names.
     *
     * @param disabledStateEventBusEvent jdbc disabled event bus event
     */
    @Subscribe
    public void renew(final DisabledStateEventBusEvent disabledStateEventBusEvent) {
        Map<String, DataSource> newDataSourceMap = getAvailableDataSourceMap(disabledStateEventBusEvent.getDisabledDataSourceNames());
        dataSource = new ShardingDataSource(newDataSourceMap, dataSource.getShardingContext(), dataSource.getShardingProperties());
    }
}

