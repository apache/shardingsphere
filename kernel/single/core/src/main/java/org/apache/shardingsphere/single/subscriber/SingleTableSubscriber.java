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
public final class SingleTableSubscriber implements RuleChangedSubscriber<AlterSingleTableEvent, DropSingleTableEvent> {
    
    private ContextManager contextManager;
    
    @Subscribe
    @Override
    public synchronized void renew(final AlterSingleTableEvent event) {
        if (!event.getActiveVersion().equals(contextManager.getInstanceContext().getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        String yamlContent = contextManager.getInstanceContext().getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion());
        SingleRuleConfiguration toBeChangedConfig = getToBeChangedConfiguration(yamlContent);
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        SingleRuleConfiguration changedConfig = getChangedConfiguration(toBeChangedConfig, database);
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), changedConfig));
    }
    
    @Subscribe
    @Override
    public synchronized void renew(final DropSingleTableEvent event) {
        if (!contextManager.getMetaDataContexts().getMetaData().containsDatabase(event.getDatabaseName())) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        SingleRuleConfiguration droppedConfig = getDroppedConfiguration(database);
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), droppedConfig));
    }
    
    private SingleRuleConfiguration getToBeChangedConfiguration(final String yamlContent) {
        return new YamlSingleRuleConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContent, YamlSingleRuleConfiguration.class));
    }
    
    private SingleRuleConfiguration getChangedConfiguration(final SingleRuleConfiguration toBeChangedConfig, final ShardingSphereDatabase database) {
        SingleRuleConfiguration result = database.getRuleMetaData().findSingleRule(SingleRule.class).map(SingleRule::getConfiguration).orElseGet(SingleRuleConfiguration::new);
        result.getTables().clear();
        result.getTables().addAll(toBeChangedConfig.getTables());
        toBeChangedConfig.getDefaultDataSource().ifPresent(optional -> result.setDefaultDataSource(toBeChangedConfig.getDefaultDataSource().get()));
        return result;
    }
    
    private SingleRuleConfiguration getDroppedConfiguration(final ShardingSphereDatabase database) {
        SingleRuleConfiguration result = database.getRuleMetaData().getSingleRule(SingleRule.class).getConfiguration();
        result.getTables().clear();
        result.setDefaultDataSource(null);
        return result;
    }
}
