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
import io.shardingsphere.core.api.ConfigMapContext;
import io.shardingsphere.core.event.ShardingEventBusInstance;
import io.shardingsphere.core.jdbc.adapter.AbstractDataSourceAdapter;
import io.shardingsphere.core.jdbc.core.connection.ShardingConnection;
import io.shardingsphere.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingsphere.jdbc.orchestration.internal.event.config.ShardingConfigurationEventBusEvent;
import io.shardingsphere.jdbc.orchestration.internal.event.state.DisabledStateEventBusEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Orchestration sharding datasource.
 *
 * @author caohao
 */
@Slf4j
public final class OrchestrationShardingDataSource extends AbstractDataSourceAdapter implements AutoCloseable {
    
    @Getter
    private ShardingDataSource dataSource;
    
    private final OrchestrationFacade orchestrationFacade;
    
    private Map<String, DataSource> dataSourceMap;
    
    private boolean isCircuitBreak;
    
    public OrchestrationShardingDataSource(final ShardingDataSource shardingDataSource, final OrchestrationFacade orchestrationFacade) throws SQLException {
        super(shardingDataSource.getDataSourceMap().values());
        this.dataSource = shardingDataSource;
        this.orchestrationFacade = orchestrationFacade;
        this.orchestrationFacade.init(dataSourceMap, shardingDataSource.getShardingContext().getShardingRule().getShardingRuleConfig(), ConfigMapContext.getInstance().getShardingConfig(), shardingDataSource.getShardingProperties().getProps());
        this.dataSourceMap = shardingDataSource.getDataSourceMap();
        ShardingEventBusInstance.getInstance().register(this);
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
     * Renew sharding data source.
     *
     * @param shardingEvent sharding configuration event bus event.
     * @throws SQLException sql exception
     */
    @Subscribe
    public void renew(final ShardingConfigurationEventBusEvent shardingEvent) throws SQLException {
        super.renew(shardingEvent.getDataSourceMap().values());
        dataSource = new ShardingDataSource(shardingEvent.getDataSourceMap(), shardingEvent.getShardingRule(), new LinkedHashMap<String, Object>(), shardingEvent.getProps());
    }
    
    /**
     * Renew disable dataSource names.
     *
     * @param disabledStateEventBusEvent jdbc disabled event bus event
     */
    @Subscribe
    public void renewDisabledDataSourceNames(final DisabledStateEventBusEvent disabledStateEventBusEvent) {
        Map<String, DataSource> newDataSourceMap = getAvailableDataSourceMap(disabledStateEventBusEvent.getDisabledDataSourceNames());
        dataSource = new ShardingDataSource(newDataSourceMap, dataSource.getShardingContext(), dataSource.getShardingProperties(), dataSource.getDatabaseType());
    }
    
    private Map<String, DataSource> getAvailableDataSourceMap(final Collection<String> disabledDataSourceNames) {
        Map<String, DataSource> result = new LinkedHashMap<>(dataSourceMap);
        for (String each : disabledDataSourceNames) {
            result.remove(each);
        }
        return result;
    }
}
