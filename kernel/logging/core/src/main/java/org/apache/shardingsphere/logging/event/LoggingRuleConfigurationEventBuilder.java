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

package org.apache.shardingsphere.logging.event;

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.config.rule.global.converter.GlobalRuleNodeConverter;
import org.apache.shardingsphere.infra.config.rule.global.event.AlterGlobalRuleConfigurationEvent;
import org.apache.shardingsphere.infra.config.rule.global.event.DeleteGlobalRuleConfigurationEvent;
import org.apache.shardingsphere.infra.rule.event.GovernanceEvent;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.logging.config.LoggingRuleConfiguration;
import org.apache.shardingsphere.logging.rule.LoggingRule;
import org.apache.shardingsphere.logging.yaml.config.YamlLoggingRuleConfiguration;
import org.apache.shardingsphere.logging.yaml.swapper.YamlLoggingRuleConfigurationSwapper;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.spi.RuleConfigurationEventBuilder;

import java.util.Optional;

/**
 *  Logging rule configuration event builder.
 */
public final class LoggingRuleConfigurationEventBuilder implements RuleConfigurationEventBuilder {
    
    private static final String LOGGING = "logging";
    
    private static final String RULE_TYPE = LoggingRule.class.getSimpleName();
    
    @Override
    public Optional<GovernanceEvent> build(final String databaseName, final DataChangedEvent event) {
        if (!GlobalRuleNodeConverter.isExpectedRuleName(LOGGING, event.getKey()) || Strings.isNullOrEmpty(event.getValue())) {
            return Optional.empty();
        }
        return buildLoggingRuleConfigurationEvent(databaseName, event);
    }
    
    private Optional<GovernanceEvent> buildLoggingRuleConfigurationEvent(final String databaseName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType() || Type.UPDATED == event.getType()) {
            return Optional.of(new AlterGlobalRuleConfigurationEvent(databaseName, swapToConfig(event.getValue()), RULE_TYPE));
        }
        return Optional.of(new DeleteGlobalRuleConfigurationEvent(databaseName, RULE_TYPE));
    }
    
    private LoggingRuleConfiguration swapToConfig(final String yamlContext) {
        return new YamlLoggingRuleConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContext, YamlLoggingRuleConfiguration.class));
    }
}
