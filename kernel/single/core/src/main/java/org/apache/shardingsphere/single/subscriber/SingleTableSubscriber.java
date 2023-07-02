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
import org.apache.shardingsphere.single.yaml.config.swapper.YamlSingleRuleConfigurationSwapper;

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
        String yamlContent = contextManager.getInstanceContext().getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion());
        SingleRuleConfiguration toBeChangedConfig = new YamlSingleRuleConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContent, YamlSingleRuleConfiguration.class));
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        SingleRuleConfiguration currentConfig = database.getRuleMetaData().findSingleRule(SingleRule.class).map(SingleRule::getConfiguration).orElseGet(SingleRuleConfiguration::new);
        updateCurrentConfiguration(currentConfig, toBeChangedConfig);
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), currentConfig));
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
        SingleRuleConfiguration currentConfig = database.getRuleMetaData().getSingleRule(SingleRule.class).getConfiguration();
        removeCurrentConfiguration(currentConfig);
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), currentConfig));
    }
    
    private void updateCurrentConfiguration(final SingleRuleConfiguration currentConfig, final SingleRuleConfiguration toBeChangedConfig) {
        currentConfig.getTables().clear();
        currentConfig.getTables().addAll(toBeChangedConfig.getTables());
        toBeChangedConfig.getDefaultDataSource().ifPresent(optional -> currentConfig.setDefaultDataSource(toBeChangedConfig.getDefaultDataSource().get()));
    }
    
    private void removeCurrentConfiguration(final SingleRuleConfiguration currentConfig) {
        currentConfig.getTables().clear();
        currentConfig.setDefaultDataSource(null);
    }
}
