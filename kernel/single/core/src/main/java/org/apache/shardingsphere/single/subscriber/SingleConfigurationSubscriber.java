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

package org.apache.shardingsphere.single.subscriber;

import com.google.common.eventbus.Subscribe;
import lombok.Setter;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.event.config.DatabaseRuleConfigurationChangedEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.subsciber.RuleChangedSubscriber;
import org.apache.shardingsphere.single.api.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.event.config.AddSingleTableEvent;
import org.apache.shardingsphere.single.event.config.AlterSingleTableEvent;
import org.apache.shardingsphere.single.event.config.DeleteSingleTableEvent;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.single.yaml.config.pojo.YamlSingleRuleConfiguration;

import java.util.Optional;

/**
 * Single configuration subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
@Setter
public final class SingleConfigurationSubscriber implements RuleChangedSubscriber {
    
    private ContextManager contextManager;
    
    private InstanceContext instanceContext;
    
    /**
     * Renew with add single configuration.
     *
     * @param event add single configuration event
     */
    @Subscribe
    public synchronized void renew(final AddSingleTableEvent event) {
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        SingleRuleConfiguration needToAddedConfig = swapSingleTableRuleConfig(
                instanceContext.getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion()));
        Optional<SingleRule> rule = database.getRuleMetaData().findSingleRule(SingleRule.class);
        SingleRuleConfiguration config;
        if (rule.isPresent()) {
            config = rule.get().getConfiguration();
            config.getTables().clear();
            config.getTables().addAll(needToAddedConfig.getTables());
        } else {
            config = new SingleRuleConfiguration(needToAddedConfig.getTables(), needToAddedConfig.getDefaultDataSource().orElse(null));
        }
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with alter single configuration.
     *
     * @param event alter single configuration event
     */
    @Subscribe
    public synchronized void renew(final AlterSingleTableEvent event) {
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        SingleRuleConfiguration needToAlteredConfig = swapSingleTableRuleConfig(
                instanceContext.getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion()));
        SingleRuleConfiguration config = database.getRuleMetaData().getSingleRule(SingleRule.class).getConfiguration();
        config.setTables(needToAlteredConfig.getTables());
        config.setDefaultDataSource(needToAlteredConfig.getDefaultDataSource().orElse(null));
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with delete single configuration.
     *
     * @param event delete single configuration event
     */
    @Subscribe
    public synchronized void renew(final DeleteSingleTableEvent event) {
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        SingleRuleConfiguration config = database.getRuleMetaData().getSingleRule(SingleRule.class).getConfiguration();
        config.getTables().clear();
        config.setDefaultDataSource(null);
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    private SingleRuleConfiguration swapSingleTableRuleConfig(final String yamlContext) {
        SingleRuleConfiguration result = new SingleRuleConfiguration();
        YamlSingleRuleConfiguration yamlSingleRuleConfiguration = YamlEngine.unmarshal(yamlContext, YamlSingleRuleConfiguration.class);
        if (null != yamlSingleRuleConfiguration.getTables()) {
            result.getTables().addAll(yamlSingleRuleConfiguration.getTables());
        }
        result.setDefaultDataSource(yamlSingleRuleConfiguration.getDefaultDataSource());
        return result;
    }
}
