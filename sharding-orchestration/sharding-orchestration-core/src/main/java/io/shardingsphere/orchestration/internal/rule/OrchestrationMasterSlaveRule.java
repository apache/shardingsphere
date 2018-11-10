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

import com.google.common.eventbus.Subscribe;
import io.shardingsphere.api.config.MasterSlaveRuleConfiguration;
import io.shardingsphere.core.constant.ShardingConstant;
import io.shardingsphere.core.rule.MasterSlaveRule;
import io.shardingsphere.orchestration.internal.state.event.DisabledStateEventBusEvent;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Orchestration master slave rule.
 *
 * @author panjuan
 */
public final class OrchestrationMasterSlaveRule extends MasterSlaveRule {
    
    private final Collection<String> disabledDataSourceNames = new LinkedList<>();
    
    public OrchestrationMasterSlaveRule(final MasterSlaveRuleConfiguration config) {
        super(config);
    }
    
    /**
     * Get slave data source names.
     *
     * @return available slave data source name
     */
    @Override
    public Collection<String> getSlaveDataSourceNames() {
        Collection<String> result = new LinkedList<>(super.getSlaveDataSourceNames());
        for (String each : disabledDataSourceNames) {
            result.remove(each);
        }
        return result;
    }
    
    /**
     * Renew disable dataSource names.
     *
     * @param disabledStateEventBusEvent jdbc disabled event bus event
     */
    @Subscribe
    public void renew(final DisabledStateEventBusEvent disabledStateEventBusEvent) {
        disabledDataSourceNames.clear();
        disabledDataSourceNames.addAll(disabledStateEventBusEvent.getDisabledSchemaDataSourceMap().get(ShardingConstant.LOGIC_SCHEMA_NAME));
    }
}
