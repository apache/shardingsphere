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

package org.apache.shardingsphere.mode.manager;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

/**
 * Context manager builder parameter.
 */
@RequiredArgsConstructor
@Getter
public final class ContextManagerBuilderParameter {
    
    @Getter(AccessLevel.NONE)
    private final ModeConfiguration modeConfig;
    
    private final Map<String, DatabaseConfiguration> databaseConfigs;
    
    private final Collection<RuleConfiguration> globalRuleConfigs;
    
    private final Properties props;
    
    private final Collection<String> labels;
    
    private final InstanceMetaData instanceMetaData;
    
    private final boolean force;
    
    /**
     * Whether parameter is empty.
     * 
     * @return parameter is empty or not
     */
    public boolean isEmpty() {
        return globalRuleConfigs.isEmpty() && props.isEmpty()
                && databaseConfigs.entrySet().stream().allMatch(entry -> entry.getValue().getDataSources().isEmpty() && entry.getValue().getRuleConfigurations().isEmpty());
    }
    
    /**
     * Get mode configuration.
     * 
     * @return mode configuration
     */
    public ModeConfiguration getModeConfiguration() {
        return null == modeConfig ? new ModeConfiguration("Standalone", null) : modeConfig;
    }
}
