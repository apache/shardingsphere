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
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.event.config.DatabaseRuleConfigurationChangedEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.subsciber.RuleChangedSubscriber;
import org.apache.shardingsphere.single.api.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.event.config.AlterSingleTableEvent;
import org.apache.shardingsphere.single.event.config.DropSingleTableEvent;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.single.yaml.config.pojo.YamlSingleRuleConfiguration;

import java.util.Optional;

/**
 * Single table subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
@Setter
public final class SingleTableSubscriber implements RuleChangedSubscriber {
    
    private ContextManager contextManager;
    
    /**
     * Renew with alter single table.
     *
     * @param event alter single table event
     */
    @Subscribe
    public synchronized void renew(final AlterSingleTableEvent event) {
        if (!event.getActiveVersion().equals(contextManager.getInstanceContext().getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        String yamlContext = contextManager.getInstanceContext().getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion());
        SingleRuleConfiguration toBeChangedConfig = swapSingleTableRuleConfig(yamlContext);
        Optional<SingleRule> rule = database.getRuleMetaData().findSingleRule(SingleRule.class);
        SingleRuleConfiguration config;
        if (rule.isPresent()) {
            config = rule.get().getConfiguration();
            config.getTables().clear();
            config.getTables().addAll(toBeChangedConfig.getTables());
        } else {
            config = new SingleRuleConfiguration(toBeChangedConfig.getTables(), toBeChangedConfig.getDefaultDataSource().orElse(null));
        }
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with drop single table.
     *
     * @param event drop single table event
     */
    @Subscribe
    public synchronized void renew(final DropSingleTableEvent event) {
        if (!contextManager.getMetaDataContexts().getMetaData().containsDatabase(event.getDatabaseName())) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        SingleRuleConfiguration config = database.getRuleMetaData().getSingleRule(SingleRule.class).getConfiguration();
        config.getTables().clear();
        config.setDefaultDataSource(null);
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    private SingleRuleConfiguration swapSingleTableRuleConfig(final String yamlContent) {
        SingleRuleConfiguration result = new SingleRuleConfiguration();
        YamlSingleRuleConfiguration yamlSingleRuleConfig = YamlEngine.unmarshal(yamlContent, YamlSingleRuleConfiguration.class);
        if (null != yamlSingleRuleConfig.getTables()) {
            result.getTables().addAll(yamlSingleRuleConfig.getTables());
        }
        result.setDefaultDataSource(yamlSingleRuleConfig.getDefaultDataSource());
        return result;
    }
}
