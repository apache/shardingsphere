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

package io.shardingsphere.orchestration.internal.rule;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import io.shardingsphere.api.config.MasterSlaveRuleConfiguration;
import io.shardingsphere.core.constant.ShardingConstant;
import io.shardingsphere.core.rule.MasterSlaveRule;
import io.shardingsphere.orchestration.internal.eventbus.ShardingOrchestrationEventBus;
import io.shardingsphere.orchestration.internal.registry.state.event.DisabledStateChangedEvent;
import io.shardingsphere.orchestration.internal.registry.state.schema.OrchestrationShardingSchema;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;

/**
 * Orchestration master slave rule.
 *
 * @author panjuan
 */
public final class OrchestrationMasterSlaveRule extends MasterSlaveRule {
    
    private final EventBus eventBus = ShardingOrchestrationEventBus.getInstance();
    
    private final Collection<String> disabledDataSourceNames = new LinkedHashSet<>();
    
    public OrchestrationMasterSlaveRule(final MasterSlaveRuleConfiguration config) {
        super(config);
        eventBus.register(this);
    }
    
    /**
     * Get slave data source names.
     *
     * @return available slave data source names
     */
    @Override
    public Collection<String> getSlaveDataSourceNames() {
        if (disabledDataSourceNames.isEmpty()) {
            return super.getSlaveDataSourceNames();
        }
        Collection<String> result = new LinkedList<>(super.getSlaveDataSourceNames());
        result.removeAll(disabledDataSourceNames);
        return result;
    }
    
    /**
     * Renew disable data source names.
     *
     * @param disabledStateChangedEvent disabled state changed event
     */
    @Subscribe
    public synchronized void renew(final DisabledStateChangedEvent disabledStateChangedEvent) {
        OrchestrationShardingSchema shardingSchema = disabledStateChangedEvent.getShardingSchema();
        // TODO need to confirm, why use ShardingConstant.LOGIC_SCHEMA_NAME here
        if (ShardingConstant.LOGIC_SCHEMA_NAME.equals(shardingSchema.getSchemaName())) {
            updateDisabledDataSourceNames(shardingSchema.getDataSourceName(), disabledStateChangedEvent.isDisabled());
        }
    }
    
    /**
     * Update disabled data source names.
     * 
     * @param dataSourceName data source name
     * @param isDisabled is disabled
     */
    public void updateDisabledDataSourceNames(final String dataSourceName, final boolean isDisabled) {
        if (isDisabled) {
            disabledDataSourceNames.add(dataSourceName);
        } else {
            disabledDataSourceNames.remove(dataSourceName);
        }
    }
}
