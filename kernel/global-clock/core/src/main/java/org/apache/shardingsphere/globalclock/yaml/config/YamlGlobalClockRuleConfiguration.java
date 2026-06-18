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

package org.apache.shardingsphere.globalclock.yaml.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.globalclock.config.GlobalClockRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlGlobalRuleConfiguration;
import org.apache.shardingsphere.mode.node.rule.tuple.annotation.RuleNodeTupleEntity;

import java.util.Properties;

/**
 * Global clock rule configuration for YAML.
 */
@RuleNodeTupleEntity(value = "global_clock", leaf = true)
@Getter
@Setter
public final class YamlGlobalClockRuleConfiguration implements YamlGlobalRuleConfiguration {
    
    private String type;
    
    private String provider;
    
    private boolean enabled;
    
    private Properties props = new Properties();
    
    @Override
    public Class<GlobalClockRuleConfiguration> getRuleConfigurationType() {
        return GlobalClockRuleConfiguration.class;
    }
}
