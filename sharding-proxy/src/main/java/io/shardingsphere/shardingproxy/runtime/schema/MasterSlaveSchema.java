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
import io.shardingsphere.api.config.MasterSlaveRuleConfiguration;
import io.shardingsphere.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.ShardingConstant;
import io.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import io.shardingsphere.core.metadata.ShardingMetaData;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.core.rule.MasterSlaveRule;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.orchestration.internal.config.event.MasterSlaveRuleChangedEvent;
import io.shardingsphere.orchestration.internal.rule.OrchestrationMasterSlaveRule;
import io.shardingsphere.orchestration.internal.state.event.DisabledStateEventBusEvent;
import io.shardingsphere.shardingproxy.backend.BackendExecutorContext;
import io.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import io.shardingsphere.shardingproxy.runtime.metadata.ProxyTableMetaDataConnectionManager;
import lombok.Getter;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Master-slave schema.
 *
 * @author panjuan
 */
@Getter
public final class MasterSlaveSchema extends LogicSchema {
    
    private MasterSlaveRule masterSlaveRule;
    
    private final ShardingMetaData metaData;
    
    public MasterSlaveSchema(final String name, final Map<String, DataSourceParameter> dataSources, final MasterSlaveRuleConfiguration masterSlaveRuleConfig, final boolean isUsingRegistry) {
        super(name, dataSources);
        masterSlaveRule = getMasterSlaveRule(masterSlaveRuleConfig, isUsingRegistry);
        metaData = getShardingMetaData();
    }
    
    private MasterSlaveRule getMasterSlaveRule(final MasterSlaveRuleConfiguration masterSlaveRule, final boolean isUsingRegistry) {
        return isUsingRegistry ? new OrchestrationMasterSlaveRule(masterSlaveRule) : new MasterSlaveRule(masterSlaveRule);
    }
    
    private ShardingMetaData getShardingMetaData() {
        return new ShardingMetaData(getDataSourceURLs(getDataSources()), new ShardingRule(new ShardingRuleConfiguration(), getDataSources().keySet()), 
                DatabaseType.MySQL, BackendExecutorContext.getInstance().getExecuteEngine(), new ProxyTableMetaDataConnectionManager(getBackendDataSource()), 
                GlobalRegistry.getInstance().getShardingProperties().<Integer>getValue(ShardingPropertiesConstant.MAX_CONNECTIONS_SIZE_PER_QUERY));
    }
    
    /**
     * Renew master-slave rule.
     *
     * @param masterSlaveEvent master-slave event.
     */
    @Subscribe
    public void renew(final MasterSlaveRuleChangedEvent masterSlaveEvent) {
        if (!getName().equals(masterSlaveEvent.getShardingSchemaName())) {
            return;
        }
        masterSlaveRule = new OrchestrationMasterSlaveRule(masterSlaveEvent.getMasterSlaveRuleConfig());
    }
    
    /**
     * Renew disabled data source names.
     *
     * @param disabledStateEventBusEvent jdbc disabled event bus event
     */
    @Subscribe
    public void renew(final DisabledStateEventBusEvent disabledStateEventBusEvent) {
        Map<String, Collection<String>> disabledSchemaDataSourceMap = disabledStateEventBusEvent.getDisabledSchemaDataSourceMap();
        if (!disabledSchemaDataSourceMap.keySet().contains(getName())) {
            return;
        }
        renew(disabledSchemaDataSourceMap.get(getName()));
    }
    
    private void renew(final Collection<String> disabledDataSourceNames) {
        DisabledStateEventBusEvent eventBusEvent = new DisabledStateEventBusEvent(Collections.singletonMap(ShardingConstant.LOGIC_SCHEMA_NAME, disabledDataSourceNames));
        ((OrchestrationMasterSlaveRule) masterSlaveRule).renew(eventBusEvent);
    }
}
