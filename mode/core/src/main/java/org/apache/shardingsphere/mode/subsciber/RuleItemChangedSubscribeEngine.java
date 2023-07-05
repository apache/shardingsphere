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

package org.apache.shardingsphere.mode.subsciber;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.event.rule.alter.AlterRuleItemEvent;
import org.apache.shardingsphere.infra.rule.event.rule.drop.DropRuleItemEvent;
import org.apache.shardingsphere.mode.event.config.DatabaseRuleConfigurationChangedEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;

/**
 * Rule item changed subscribe engine.
 * 
 * @param <T> type of rule configuration
 * @param <I> type of rule item configuration
 */
@RequiredArgsConstructor
public abstract class RuleItemChangedSubscribeEngine<T extends RuleConfiguration, I> {
    
    private final ContextManager contextManager;
    
    /**
     * Renew with alter rule item.
     *
     * @param event alter rule item event
     */
    public final void renew(final AlterRuleItemEvent event) {
        if (!event.getActiveVersion().equals(contextManager.getInstanceContext().getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        String yamlContent = contextManager.getInstanceContext().getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion());
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        T currentRuleConfig = findRuleConfiguration(database);
        changeRuleItemConfiguration(event, currentRuleConfig, swapRuleItemConfigurationFromEvent(event, yamlContent));
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), currentRuleConfig));
    }
    
    /**
     * Renew with drop rule item.
     *
     * @param event drop rule item event
     */
    public final void renew(final DropRuleItemEvent event) {
        if (!contextManager.getMetaDataContexts().getMetaData().containsDatabase(event.getDatabaseName())) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        T currentRuleConfig = findRuleConfiguration(database);
        dropRuleItemConfiguration(event, currentRuleConfig);
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), currentRuleConfig));
    }
    
    protected abstract I swapRuleItemConfigurationFromEvent(AlterRuleItemEvent event, String yamlContent);
    
    protected abstract T findRuleConfiguration(ShardingSphereDatabase database);
    
    protected abstract void changeRuleItemConfiguration(AlterRuleItemEvent event, T currentRuleConfig, I toBeChangedItemConfig);
    
    protected abstract void dropRuleItemConfiguration(DropRuleItemEvent event, T currentRuleConfig);
}
