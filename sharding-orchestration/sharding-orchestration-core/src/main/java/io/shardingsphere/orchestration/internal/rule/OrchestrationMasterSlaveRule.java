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

import io.shardingsphere.api.config.rule.MasterSlaveRuleConfiguration;
import io.shardingsphere.core.rule.MasterSlaveRule;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Orchestration master slave rule.
 *
 * @author panjuan
 */
public final class OrchestrationMasterSlaveRule extends MasterSlaveRule {
    
    private final Collection<String> disabledDataSourceNames = new HashSet<>();
    
    public OrchestrationMasterSlaveRule(final MasterSlaveRuleConfiguration config) {
        super(config);
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
