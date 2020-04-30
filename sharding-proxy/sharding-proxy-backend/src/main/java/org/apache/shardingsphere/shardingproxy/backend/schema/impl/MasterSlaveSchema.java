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

package org.apache.shardingsphere.shardingproxy.backend.schema.impl;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.core.log.ConfigurationLogger;
import org.apache.shardingsphere.core.rule.MasterSlaveRule;
import org.apache.shardingsphere.orchestration.core.common.event.MasterSlaveRuleChangedEvent;
import org.apache.shardingsphere.orchestration.core.registrycenter.event.DisabledStateChangedEvent;
import org.apache.shardingsphere.orchestration.core.registrycenter.schema.OrchestrationShardingSchema;
import org.apache.shardingsphere.shardingproxy.backend.schema.LogicSchema;
import org.apache.shardingsphere.shardingproxy.config.yaml.YamlDataSourceParameter;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

/**
 * Master-slave schema.
 */
public final class MasterSlaveSchema extends LogicSchema {
    
    public MasterSlaveSchema(final String name,
                             final Map<String, YamlDataSourceParameter> dataSources, final MasterSlaveRuleConfiguration masterSlaveRuleConfig, final boolean isUsingRegistry) throws SQLException {
        super(name, dataSources, Collections.singletonList(createMasterSlaveRule(masterSlaveRuleConfig, isUsingRegistry)));
    }
    
    private static MasterSlaveRule createMasterSlaveRule(final MasterSlaveRuleConfiguration masterSlaveRuleConfig, final boolean isUsingRegistry) {
        return new MasterSlaveRule(masterSlaveRuleConfig);
    }
    
    /**
     * Renew master-slave rule.
     *
     * @param masterSlaveRuleChangedEvent master-slave rule changed event.
     */
    @Subscribe
    public synchronized void renew(final MasterSlaveRuleChangedEvent masterSlaveRuleChangedEvent) {
        if (getName().equals(masterSlaveRuleChangedEvent.getShardingSchemaName())) {
            ConfigurationLogger.log(masterSlaveRuleChangedEvent.getMasterSlaveRuleConfiguration());
            setRules(Collections.singletonList(new MasterSlaveRule(masterSlaveRuleChangedEvent.getMasterSlaveRuleConfiguration())));
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
            ((MasterSlaveRule) getRules().iterator().next()).updateDisabledDataSourceNames(shardingSchema.getDataSourceName(), disabledStateChangedEvent.isDisabled());
        }
    }
}
