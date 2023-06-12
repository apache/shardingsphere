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

package org.apache.shardingsphere.sqlfederation.event;

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.config.rule.global.converter.GlobalRuleNodeConverter;
import org.apache.shardingsphere.infra.rule.event.GovernanceEvent;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.spi.RuleConfigurationEventBuilder;
import org.apache.shardingsphere.sqlfederation.api.config.SQLFederationRuleConfiguration;
import org.apache.shardingsphere.sqlfederation.event.config.AddSQLFederationConfigurationEvent;
import org.apache.shardingsphere.sqlfederation.event.config.AlterSQLFederationConfigurationEvent;
import org.apache.shardingsphere.sqlfederation.event.config.DeleteSQLFederationConfigurationEvent;
import org.apache.shardingsphere.sqlfederation.yaml.config.YamlSQLFederationRuleConfiguration;
import org.apache.shardingsphere.sqlfederation.yaml.swapper.YamlSQLFederationRuleConfigurationSwapper;

import java.util.Optional;

/**
 * SQL federation rule configuration event builder.
 */
public final class SQLFederationRuleConfigurationEventBuilder implements RuleConfigurationEventBuilder {
    
    private static final String SQL_FEDERATION = "sql_federation";
    
    @Override
    public Optional<GovernanceEvent> build(final String databaseName, final DataChangedEvent event) {
        if (!GlobalRuleNodeConverter.isExpectedRuleName(SQL_FEDERATION, event.getKey()) || Strings.isNullOrEmpty(event.getValue())) {
            return Optional.empty();
        }
        return buildGlobalClockRuleConfigurationEvent(databaseName, event);
    }
    
    private Optional<GovernanceEvent> buildGlobalClockRuleConfigurationEvent(final String databaseName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return Optional.of(new AddSQLFederationConfigurationEvent(databaseName, swapToConfig(event.getValue())));
        }
        if (Type.UPDATED == event.getType()) {
            return Optional.of(new AlterSQLFederationConfigurationEvent(databaseName, swapToConfig(event.getValue())));
        }
        return Optional.of(new DeleteSQLFederationConfigurationEvent(databaseName));
    }
    
    private SQLFederationRuleConfiguration swapToConfig(final String yamlContext) {
        return new YamlSQLFederationRuleConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContext, YamlSQLFederationRuleConfiguration.class));
    }
}
