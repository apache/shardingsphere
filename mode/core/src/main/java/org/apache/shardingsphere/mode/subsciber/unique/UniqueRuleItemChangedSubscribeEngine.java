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

package org.apache.shardingsphere.mode.subsciber.unique;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.event.rule.alter.AlterUniqueRuleItemEvent;
import org.apache.shardingsphere.infra.rule.event.rule.drop.DropUniqueRuleItemEvent;
import org.apache.shardingsphere.mode.event.config.DatabaseRuleConfigurationChangedEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.subsciber.unique.callback.UniqueRuleItemAlteredSubscribeCallback;
import org.apache.shardingsphere.mode.subsciber.unique.callback.UniqueRuleItemDroppedSubscribeCallback;

/**
 * Unique rule item changed subscribe engine.
 * 
 * @param <T> type of rule configuration
 */
@RequiredArgsConstructor
public final class UniqueRuleItemChangedSubscribeEngine<T extends RuleConfiguration> {
    
    private final ContextManager contextManager;
    
    /**
     * Renew with alter rule item.
     *
     * @param event alter rule item event
     * @param callback rule item altered subscribe callback
     */
    public void renew(final AlterUniqueRuleItemEvent event, final UniqueRuleItemAlteredSubscribeCallback<T> callback) {
        if (!event.getActiveVersion().equals(contextManager.getInstanceContext().getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        String yamlContent = contextManager.getInstanceContext().getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion());
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        T toBeChangedConfig = callback.getToBeChangedConfiguration(yamlContent, database);
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), toBeChangedConfig));
    }
    
    /**
     * Renew with drop rule item.
     *
     * @param event drop rule item event
     * @param callback rule item dropped subscribe callback
     */
    public void renew(final DropUniqueRuleItemEvent event, final UniqueRuleItemDroppedSubscribeCallback<T> callback) {
        if (!contextManager.getMetaDataContexts().getMetaData().containsDatabase(event.getDatabaseName())) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        T toBeDroppedConfig = callback.getToBeDroppedConfiguration(database);
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), toBeDroppedConfig));
    }
}
