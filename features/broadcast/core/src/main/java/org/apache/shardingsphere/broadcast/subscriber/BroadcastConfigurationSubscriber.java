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

import com.google.common.eventbus.Subscribe;
import lombok.Setter;
import org.apache.shardingsphere.broadcast.api.config.BroadcastRuleConfiguration;
import org.apache.shardingsphere.broadcast.event.config.AddBroadcastTableEvent;
import org.apache.shardingsphere.broadcast.event.config.AlterBroadcastTableEvent;
import org.apache.shardingsphere.broadcast.event.config.DeleteBroadcastTableEvent;
import org.apache.shardingsphere.broadcast.rule.BroadcastRule;
import org.apache.shardingsphere.broadcast.yaml.config.YamlBroadcastRuleConfiguration;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.event.config.DatabaseRuleConfigurationChangedEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.subsciber.RuleChangedSubscriber;

import java.util.Optional;

/**
 * Broadcast configuration subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
@Setter
public final class BroadcastConfigurationSubscriber implements RuleChangedSubscriber {
    
    private ContextManager contextManager;
    
    private InstanceContext instanceContext;
    
    /**
     * Renew with add broadcast configuration.
     *
     * @param event add broadcast configuration event
     */
    @Subscribe
    public synchronized void renew(final AddBroadcastTableEvent event) {
        if (!event.getActiveVersion().equals(instanceContext.getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        BroadcastRuleConfiguration needToAddedConfig = swapBroadcastTableRuleConfig(
                instanceContext.getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion()));
        Optional<BroadcastRule> rule = database.getRuleMetaData().findSingleRule(BroadcastRule.class);
        BroadcastRuleConfiguration config;
        if (rule.isPresent()) {
            config = rule.get().getConfiguration();
            config.getTables().clear();
            config.getTables().addAll(needToAddedConfig.getTables());
        } else {
            config = new BroadcastRuleConfiguration(needToAddedConfig.getTables());
        }
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with alter broadcast configuration.
     *
     * @param event alter broadcast configuration event
     */
    @Subscribe
    public synchronized void renew(final AlterBroadcastTableEvent event) {
        if (!event.getActiveVersion().equals(instanceContext.getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        BroadcastRuleConfiguration needToAlteredConfig = swapBroadcastTableRuleConfig(
                instanceContext.getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion()));
        BroadcastRuleConfiguration config = database.getRuleMetaData().getSingleRule(BroadcastRule.class).getConfiguration();
        config.getTables().clear();
        config.getTables().addAll(needToAlteredConfig.getTables());
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with delete broadcast configuration.
     *
     * @param event delete broadcast configuration event
     */
    @Subscribe
    public synchronized void renew(final DeleteBroadcastTableEvent event) {
        if (!contextManager.getMetaDataContexts().getMetaData().containsDatabase(event.getDatabaseName())) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        BroadcastRuleConfiguration config = database.getRuleMetaData().getSingleRule(BroadcastRule.class).getConfiguration();
        config.getTables().clear();
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    private BroadcastRuleConfiguration swapBroadcastTableRuleConfig(final String yamlContext) {
        YamlBroadcastRuleConfiguration yamlBroadcastRuleConfiguration = YamlEngine.unmarshal(yamlContext, YamlBroadcastRuleConfiguration.class);
        return new BroadcastRuleConfiguration(yamlBroadcastRuleConfiguration.getTables());
    }
}
