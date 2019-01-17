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

package org.apache.shardingsphere.shardingjdbc.orchestration.internal.datasource;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.core.constant.ShardingConstant;
import org.apache.shardingsphere.orchestration.internal.eventbus.ShardingOrchestrationEventBus;
import org.apache.shardingsphere.orchestration.internal.registry.ShardingOrchestrationFacade;
import org.apache.shardingsphere.orchestration.internal.registry.state.event.CircuitStateChangedEvent;
import org.apache.shardingsphere.shardingjdbc.jdbc.adapter.AbstractDataSourceAdapter;
import org.apache.shardingsphere.shardingjdbc.orchestration.internal.util.DataSourceConverter;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;

/**
 * Abstract orchestration data source.
 *
 * @author panjuan
 */
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractOrchestrationDataSource extends AbstractDataSourceAdapter {
    
    private final EventBus eventBus = ShardingOrchestrationEventBus.getInstance();
    
    private final ShardingOrchestrationFacade shardingOrchestrationFacade;
    
    private boolean isCircuitBreak;
    
    public AbstractOrchestrationDataSource(final ShardingOrchestrationFacade shardingOrchestrationFacade, final Map<String, DataSource> dataSourceMap) throws SQLException {
        super(dataSourceMap);
        this.shardingOrchestrationFacade = shardingOrchestrationFacade;
        eventBus.register(this);
    }
    
    public AbstractOrchestrationDataSource(final ShardingOrchestrationFacade shardingOrchestrationFacade) throws SQLException {
        super(DataSourceConverter.getDataSourceMap(shardingOrchestrationFacade.getConfigService().loadDataSourceConfigurations(ShardingConstant.LOGIC_SCHEMA_NAME)));
        this.shardingOrchestrationFacade = shardingOrchestrationFacade;
        eventBus.register(this);
    }
    
    /**
     /**
     * Renew circuit breaker state.
     *
     * @param circuitStateChangedEvent circuit state changed event
     */
    @Subscribe
    public final synchronized void renew(final CircuitStateChangedEvent circuitStateChangedEvent) {
        isCircuitBreak = circuitStateChangedEvent.isCircuitBreak();
    }
}
