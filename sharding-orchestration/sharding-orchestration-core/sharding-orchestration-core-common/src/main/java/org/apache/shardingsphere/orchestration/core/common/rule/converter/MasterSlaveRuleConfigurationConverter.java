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

package org.apache.shardingsphere.orchestration.core.common.rule.converter;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.core.yaml.config.masterslave.YamlMasterSlaveRuleConfiguration;
import org.apache.shardingsphere.core.yaml.swapper.MasterSlaveRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.underlying.common.yaml.engine.YamlEngine;

/**
 * Master-slave rule configuration storage.
 */
public final class MasterSlaveRuleConfigurationConverter implements RuleConfigurationConverter<MasterSlaveRuleConfiguration> {
    
    @Override
    public boolean match(final String context) {
        return !context.contains("masterSalveRule:\n") && context.contains("masterDataSourceName:");
    }
    
    @Override
    public MasterSlaveRuleConfiguration unmarshal(final String context) {
        return new MasterSlaveRuleConfigurationYamlSwapper().swap(YamlEngine.unmarshal(context, YamlMasterSlaveRuleConfiguration.class));
    }
    
    @Override
    public String marshal(final MasterSlaveRuleConfiguration masterSlaveRuleConfiguration, final String shardingSchemaName) {
        Preconditions.checkState(null != masterSlaveRuleConfiguration && !masterSlaveRuleConfiguration.getMasterDataSourceName().isEmpty(),
                "No available master-slave rule configuration in `%s` for orchestration.", shardingSchemaName);
        return YamlEngine.marshal(new MasterSlaveRuleConfigurationYamlSwapper().swap(masterSlaveRuleConfiguration));
    }
}

