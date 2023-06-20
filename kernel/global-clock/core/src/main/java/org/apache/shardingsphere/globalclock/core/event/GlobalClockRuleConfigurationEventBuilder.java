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

package org.apache.shardingsphere.globalclock.core.event;

import org.apache.shardingsphere.globalclock.api.config.GlobalClockRuleConfiguration;
import org.apache.shardingsphere.globalclock.core.rule.GlobalClockRule;
import org.apache.shardingsphere.globalclock.core.yaml.config.YamlGlobalClockRuleConfiguration;
import org.apache.shardingsphere.globalclock.core.yaml.swapper.YamlGlobalClockRuleConfigurationSwapper;
import org.apache.shardingsphere.infra.config.rule.global.converter.GlobalRuleNodeConverter;
import org.apache.shardingsphere.infra.config.rule.global.event.AlterGlobalRuleConfigurationEvent;
import org.apache.shardingsphere.infra.config.rule.global.event.DeleteGlobalRuleConfigurationEvent;
import org.apache.shardingsphere.infra.rule.event.GovernanceEvent;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.spi.GlobalRuleConfigurationEventBuilder;

import java.util.Optional;

/**
 * Global clock rule configuration event builder.
 */
public final class GlobalClockRuleConfigurationEventBuilder implements GlobalRuleConfigurationEventBuilder {
    
    private static final String GLOBAL_CLOCK = "global_clock";
    
    private static final String RULE_TYPE = GlobalClockRule.class.getSimpleName();
    
    @Override
    public Optional<GovernanceEvent> build(final DataChangedEvent event) {
        if (GlobalRuleNodeConverter.isActiveVersionPath(GLOBAL_CLOCK, event.getKey())) {
            return buildEvent(event);
        }
        return Optional.empty();
    }
    
    private Optional<GovernanceEvent> buildEvent(final DataChangedEvent event) {
        if (Type.ADDED == event.getType() || Type.UPDATED == event.getType()) {
            return Optional.of(new AlterGlobalRuleConfigurationEvent( RULE_TYPE, event.getKey(), event.getValue()));
        }
        return Optional.of(new DeleteGlobalRuleConfigurationEvent(RULE_TYPE));
    }
    
    private GlobalClockRuleConfiguration swapToConfig(final String yamlContext) {
        return new YamlGlobalClockRuleConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContext, YamlGlobalClockRuleConfiguration.class));
    }
}
