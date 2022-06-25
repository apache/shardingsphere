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

import lombok.Builder;
import lombok.Getter;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.instance.definition.InstanceDefinition;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

/**
 * Context manager builder parameter.
 */
@Builder
@Getter
public final class ContextManagerBuilderParameter {
    
    private final ModeConfiguration modeConfig;
    
    private final Map<String, DatabaseConfiguration> databaseConfigs;
    
    private final Collection<RuleConfiguration> globalRuleConfigs;
    
    private final Properties props;
    
    private final Collection<String> labels;
    
    private final InstanceDefinition instanceDefinition;
    
    /**
     * Whether is empty or not.
     * 
     * @return is empty or not
     */
    public boolean isEmpty() {
        return props.isEmpty() && globalRuleConfigs.isEmpty()
                && databaseConfigs.entrySet().stream().allMatch(entry -> entry.getValue().getDataSources().isEmpty() && entry.getValue().getRuleConfigurations().isEmpty());
    }
}
