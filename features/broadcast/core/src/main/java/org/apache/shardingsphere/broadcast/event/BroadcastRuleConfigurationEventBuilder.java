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

package org.apache.shardingsphere.broadcast.event;

import com.google.common.base.Strings;
import org.apache.shardingsphere.broadcast.api.config.BroadcastRuleConfiguration;
import org.apache.shardingsphere.broadcast.event.config.AddBroadcastConfigurationEvent;
import org.apache.shardingsphere.broadcast.event.config.AlterBroadcastConfigurationEvent;
import org.apache.shardingsphere.broadcast.event.config.DeleteBroadcastConfigurationEvent;
import org.apache.shardingsphere.broadcast.metadata.converter.BroadcastNodeConverter;
import org.apache.shardingsphere.broadcast.yaml.config.YamlBroadcastRuleConfiguration;
import org.apache.shardingsphere.infra.rule.event.GovernanceEvent;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.spi.RuleConfigurationEventBuilder;

import java.util.Optional;

/**
 * Broadcast rule configuration event builder.
 */
public final class BroadcastRuleConfigurationEventBuilder implements RuleConfigurationEventBuilder {
    
    @Override
    public Optional<GovernanceEvent> build(final String databaseName, final DataChangedEvent event) {
        if (!BroadcastNodeConverter.isBroadcastPath(event.getKey()) || Strings.isNullOrEmpty(event.getValue())) {
            return Optional.empty();
        }
        if (BroadcastNodeConverter.isTablesPath(event.getKey()) && !Strings.isNullOrEmpty(event.getValue())) {
            return createBroadcastConfigEvent(databaseName, event);
        }
        return Optional.empty();
    }
    
    private Optional<GovernanceEvent> createBroadcastConfigEvent(final String databaseName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return Optional.of(new AddBroadcastConfigurationEvent(databaseName, swapBroadcastTableRuleConfig(event.getValue())));
        }
        if (Type.UPDATED == event.getType()) {
            return Optional.of(new AlterBroadcastConfigurationEvent(databaseName, swapBroadcastTableRuleConfig(event.getValue())));
        }
        return Optional.of(new DeleteBroadcastConfigurationEvent(databaseName));
    }
    
    private BroadcastRuleConfiguration swapBroadcastTableRuleConfig(final String yamlContext) {
        YamlBroadcastRuleConfiguration yamlBroadcastRuleConfiguration = YamlEngine.unmarshal(yamlContext, YamlBroadcastRuleConfiguration.class);
        return new BroadcastRuleConfiguration(yamlBroadcastRuleConfiguration.getTables());
    }
}
