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

package io.shardingsphere.jdbc.orchestration.internal;

import com.google.common.eventbus.Subscribe;
import io.shardingsphere.core.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.constant.properties.ShardingProperties;
import io.shardingsphere.jdbc.orchestration.internal.event.config.ShardingConfigurationEventBusEvent;
import io.shardingsphere.jdbc.orchestration.internal.event.state.CircuitStateEventBusEvent;
import io.shardingsphere.jdbc.orchestration.internal.event.state.DisabledStateEventBusEvent;
import io.shardingsphere.core.jdbc.adapter.AbstractDataSourceAdapter;
import io.shardingsphere.core.jdbc.core.connection.ShardingConnection;
import io.shardingsphere.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingsphere.core.rule.ShardingRule;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/**
 * Orchestration sharding datasource.
 *
 * @author caohao
 */
@Slf4j
public final class OrchestrationShardingDataSource extends AbstractDataSourceAdapter implements AutoCloseable {
    
    private final ShardingDataSource dataSource;
    
    private final OrchestrationFacade orchestrationFacade;
    
    public OrchestrationShardingDataSource(final Map<String, DataSource> dataSourceMap, final ShardingRuleConfiguration shardingRuleConfig,
                                           final Map<String, Object> configMap, final Properties props, final OrchestrationFacade orchestrationFacade) throws SQLException {
        super(dataSourceMap.values());
        this.dataSource = new ShardingDataSource(dataSourceMap, new ShardingRule(shardingRuleConfig, dataSourceMap.keySet()), configMap, props);
        this.orchestrationFacade = orchestrationFacade;
        this.orchestrationFacade.init(dataSourceMap, shardingRuleConfig, configMap, props);
    }
    
    @Override
    public ShardingConnection getConnection() {
        return dataSource.getConnection();
    }
    
    @Override
    public void close() {
        dataSource.close();
        orchestrationFacade.close();
    }
    
    /**
     * Renew disable dataSource names.
     *
     * @param disabledStateEventBusEvent jdbc disabled event bus event
     */
    @Subscribe
    public void renewDisabledDataSourceNames(final DisabledStateEventBusEvent disabledStateEventBusEvent) {
        dataSource.renewDisabledDataSourceNames(disabledStateEventBusEvent.getDisabledDataSourceNames());
    }
    
    /**
     * Renew circuit breaker dataSource names.
     *
     * @param circuitStateEventBusEvent jdbc disabled event bus event
     */
    @Subscribe
    public void renewCircuitBreakerDataSourceNames(final CircuitStateEventBusEvent circuitStateEventBusEvent) {
        dataSource.renewCircuitBreakerDataSourceNames(circuitStateEventBusEvent.isCircuitBreak());
    }
    
    /**
     * Renew sharding data source.
     *
     * @param shardingEvent sharding configuration event bus event.
     */
    @Subscribe
    public void renew(final ShardingConfigurationEventBusEvent shardingEvent) {
        super.renew(shardingEvent.getDataSourceMap().values());
        ShardingProperties shardingProperties = new ShardingProperties(null == shardingEvent.getProps() ? new Properties() : shardingEvent.getProps());
        dataSource.renew(shardingEvent.getDataSourceMap(), shardingEvent.getShardingRule(), shardingProperties);
    }
}
