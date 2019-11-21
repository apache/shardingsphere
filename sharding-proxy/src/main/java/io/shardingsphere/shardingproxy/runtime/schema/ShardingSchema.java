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

package io.shardingsphere.shardingproxy.runtime.schema;

import com.google.common.eventbus.Subscribe;
import io.shardingsphere.api.config.rule.ShardingRuleConfiguration;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import io.shardingsphere.core.metadata.ShardingMetaData;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.core.rule.MasterSlaveRule;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.orchestration.internal.registry.config.event.ShardingRuleChangedEvent;
import io.shardingsphere.orchestration.internal.registry.state.event.DisabledStateChangedEvent;
import io.shardingsphere.orchestration.internal.registry.state.schema.OrchestrationShardingSchema;
import io.shardingsphere.orchestration.internal.rule.OrchestrationMasterSlaveRule;
import io.shardingsphere.orchestration.internal.rule.OrchestrationShardingRule;
import io.shardingsphere.shardingproxy.backend.BackendExecutorContext;
import io.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import io.shardingsphere.shardingproxy.runtime.metadata.ProxyTableMetaDataConnectionManager;
import lombok.Getter;

import java.util.Collection;
import java.util.Map;

/**
 * Sharding schema.
 *
 * @author zhangliang
 * @author zhangyonglun
 * @author panjuan
 * @author zhaojun
 * @author wangkai
 */
@Getter
public final class ShardingSchema extends LogicSchema {
    
    private ShardingRule shardingRule;
    
    private final ShardingMetaData metaData;
    
    public ShardingSchema(final String name, 
                          final Map<String, DataSourceParameter> dataSources, final ShardingRuleConfiguration shardingRuleConfig, final boolean isCheckingMetaData, final boolean isUsingRegistry) {
        super(name, dataSources);
        shardingRule = createShardingRule(shardingRuleConfig, dataSources.keySet(), isUsingRegistry);
        metaData = createShardingMetaData(isCheckingMetaData);
    }
    
    private ShardingRule createShardingRule(final ShardingRuleConfiguration shardingRuleConfig, final Collection<String> dataSourceNames, final boolean isUsingRegistry) {
        return isUsingRegistry ? new OrchestrationShardingRule(shardingRuleConfig, dataSourceNames) : new ShardingRule(shardingRuleConfig, dataSourceNames);
    }
    
    private ShardingMetaData createShardingMetaData(final boolean isCheckingMetaData) {
        return new ShardingMetaData(getDataSourceURLs(getDataSources()), shardingRule, DatabaseType.MySQL, 
                BackendExecutorContext.getInstance().getExecuteEngine(), new ProxyTableMetaDataConnectionManager(getBackendDataSource()), 
                GlobalRegistry.getInstance().getShardingProperties().<Integer>getValue(ShardingPropertiesConstant.MAX_CONNECTIONS_SIZE_PER_QUERY), isCheckingMetaData);
    }
    
    /**
     * Renew sharding rule.
     *
     * @param shardingRuleChangedEvent sharding rule changed event.
     */
    @Subscribe
    public synchronized void renew(final ShardingRuleChangedEvent shardingRuleChangedEvent) {
        if (getName().equals(shardingRuleChangedEvent.getShardingSchemaName())) {
            shardingRule = new OrchestrationShardingRule(shardingRuleChangedEvent.getShardingRuleConfiguration(), getDataSources().keySet());
        }
    }
    
    /**
     * Renew disabled data source names.
     *
     * @param disabledStateChangedEvent disabled state changed event
     */
    @Subscribe
    public synchronized void renew(final DisabledStateChangedEvent disabledStateChangedEvent) {
        OrchestrationShardingSchema shardingSchema = disabledStateChangedEvent.getShardingSchema();
        if (getName().equals(shardingSchema.getSchemaName())) {
            for (MasterSlaveRule each : shardingRule.getMasterSlaveRules()) {
                ((OrchestrationMasterSlaveRule) each).updateDisabledDataSourceNames(shardingSchema.getDataSourceName(), disabledStateChangedEvent.isDisabled());
            }
        }
    }
}
