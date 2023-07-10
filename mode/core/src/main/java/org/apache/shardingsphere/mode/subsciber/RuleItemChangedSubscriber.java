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

import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.event.rule.alter.AlterRuleItemEvent;
import org.apache.shardingsphere.infra.rule.event.rule.drop.DropRuleItemEvent;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.event.config.AlterDatabaseRuleConfigurationEvent;
import org.apache.shardingsphere.mode.event.config.DropDatabaseRuleConfigurationEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.spi.RuleItemConfigurationChangedProcessor;

/**
 * Rule item changed subscriber.
 */
@RequiredArgsConstructor
public final class RuleItemChangedSubscriber {
    
    private final ContextManager contextManager;
    
    /**
     * Renew with alter rule item.
     *
     * @param event alter rule item event
     */
    @SuppressWarnings({"UnstableApiUsage", "rawtypes", "unchecked"})
    @Subscribe
    public void renew(final AlterRuleItemEvent event) {
        if (!event.getActiveVersion().equals(contextManager.getMetaDataContexts().getPersistService().getMetaDataVersionPersistService().getActiveVersionByFullPath(event.getActiveVersionKey()))) {
            return;
        }
        RuleItemConfigurationChangedProcessor processor = TypedSPILoader.getService(RuleItemConfigurationChangedProcessor.class, event.getType());
        String yamlContent =
                contextManager.getMetaDataContexts().getPersistService().getMetaDataVersionPersistService().getVersionPathByActiveVersion(event.getActiveVersionKey(), event.getActiveVersion());
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        RuleConfiguration currentRuleConfig = processor.findRuleConfiguration(database);
        synchronized (this) {
            processor.changeRuleItemConfiguration(event, currentRuleConfig, processor.swapRuleItemConfiguration(event, yamlContent));
            contextManager.getInstanceContext().getEventBusContext().post(new AlterDatabaseRuleConfigurationEvent(event.getDatabaseName(), currentRuleConfig));
        }
    }
    
    /**
     * Renew with drop rule item.
     *
     * @param event drop rule item event
     */
    @SuppressWarnings({"UnstableApiUsage", "rawtypes", "unchecked"})
    @Subscribe
    public void renew(final DropRuleItemEvent event) {
        if (!contextManager.getMetaDataContexts().getMetaData().containsDatabase(event.getDatabaseName())) {
            return;
        }
        RuleItemConfigurationChangedProcessor processor = TypedSPILoader.getService(RuleItemConfigurationChangedProcessor.class, event.getType());
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        RuleConfiguration currentRuleConfig = processor.findRuleConfiguration(database);
        synchronized (this) {
            processor.dropRuleItemConfiguration(event, currentRuleConfig);
            contextManager.getInstanceContext().getEventBusContext().post(new DropDatabaseRuleConfigurationEvent(event.getDatabaseName(), currentRuleConfig));
        }
    }
}
