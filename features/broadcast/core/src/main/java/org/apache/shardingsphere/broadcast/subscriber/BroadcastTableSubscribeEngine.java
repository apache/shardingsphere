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

package org.apache.shardingsphere.broadcast.subscriber;

import org.apache.shardingsphere.broadcast.api.config.BroadcastRuleConfiguration;
import org.apache.shardingsphere.broadcast.rule.BroadcastRule;
import org.apache.shardingsphere.broadcast.yaml.config.YamlBroadcastRuleConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.event.rule.alter.AlterRuleItemEvent;
import org.apache.shardingsphere.infra.rule.event.rule.drop.DropRuleItemEvent;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.subsciber.RuleItemChangedSubscribeEngine;

import java.util.LinkedList;

/**
 * Broadcast table subscribe engine.
 */
public final class BroadcastTableSubscribeEngine extends RuleItemChangedSubscribeEngine<BroadcastRuleConfiguration, BroadcastRuleConfiguration> {
    
    public BroadcastTableSubscribeEngine(final ContextManager contextManager) {
        super(contextManager);
    }
    
    @Override
    protected BroadcastRuleConfiguration swapRuleItemConfigurationFromEvent(final AlterRuleItemEvent event, final String yamlContent) {
        return new BroadcastRuleConfiguration(YamlEngine.unmarshal(yamlContent, YamlBroadcastRuleConfiguration.class).getTables());
    }
    
    @Override
    protected BroadcastRuleConfiguration findRuleConfiguration(final ShardingSphereDatabase database) {
        return database.getRuleMetaData().findSingleRule(BroadcastRule.class).map(BroadcastRule::getConfiguration).orElseGet(() -> new BroadcastRuleConfiguration(new LinkedList<>()));
    }
    
    @Override
    protected void changeRuleItemConfiguration(final AlterRuleItemEvent event, final BroadcastRuleConfiguration currentRuleConfig, final BroadcastRuleConfiguration toBeChangedItemConfig) {
        currentRuleConfig.getTables().clear();
        currentRuleConfig.getTables().addAll(toBeChangedItemConfig.getTables());
    }
    
    @Override
    protected void dropRuleItemConfiguration(final DropRuleItemEvent event, final BroadcastRuleConfiguration currentRuleConfig) {
        currentRuleConfig.getTables().clear();
    }
}
