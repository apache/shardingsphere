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

package org.apache.shardingsphere.encrypt.subscriber.compatible;

import com.google.common.eventbus.Subscribe;
import lombok.Setter;
import org.apache.shardingsphere.encrypt.api.config.CompatibleEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.event.compatible.table.AddCompatibleEncryptTableEvent;
import org.apache.shardingsphere.encrypt.event.compatible.table.AlterCompatibleEncryptTableEvent;
import org.apache.shardingsphere.encrypt.event.compatible.table.DeleteCompatibleEncryptTableEvent;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.yaml.config.rule.YamlEncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.swapper.rule.YamlEncryptTableRuleConfigurationSwapper;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.event.config.DatabaseRuleConfigurationChangedEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.subsciber.RuleChangedSubscriber;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Compatible encrypt table subscriber.
 * @deprecated compatible support will remove in next version.
 */
@Deprecated
@SuppressWarnings("UnstableApiUsage")
@Setter
public final class CompatibleEncryptTableSubscriber implements RuleChangedSubscriber {
    
    private ContextManager contextManager;
    
    /**
     * Renew with add encrypt configuration.
     *
     * @param event add encrypt configuration event
     */
    @Subscribe
    public synchronized void renew(final AddCompatibleEncryptTableEvent event) {
        if (!event.getActiveVersion().equals(contextManager.getInstanceContext().getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        EncryptTableRuleConfiguration needToAddedConfig = swapEncryptTableRuleConfig(
                contextManager.getInstanceContext().getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion()));
        contextManager.getInstanceContext().getEventBusContext().post(
                new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), getCompatibleEncryptRuleConfiguration(database, needToAddedConfig)));
    }
    
    /**
     * Renew with alter encrypt configuration.
     *
     * @param event alter encrypt configuration event
     */
    @Subscribe
    public synchronized void renew(final AlterCompatibleEncryptTableEvent event) {
        if (!event.getActiveVersion().equals(contextManager.getInstanceContext().getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        EncryptTableRuleConfiguration needToAlteredConfig = swapEncryptTableRuleConfig(
                contextManager.getInstanceContext().getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion()));
        CompatibleEncryptRuleConfiguration config = (CompatibleEncryptRuleConfiguration) database.getRuleMetaData().getSingleRule(EncryptRule.class).getConfiguration();
        config.getTables().removeIf(each -> each.getName().equals(event.getTableName()));
        config.getTables().add(needToAlteredConfig);
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with delete encrypt configuration.
     *
     * @param event delete encrypt configuration event
     */
    @Subscribe
    public synchronized void renew(final DeleteCompatibleEncryptTableEvent event) {
        if (!contextManager.getMetaDataContexts().getMetaData().containsDatabase(event.getDatabaseName())) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        CompatibleEncryptRuleConfiguration config = (CompatibleEncryptRuleConfiguration) database.getRuleMetaData().getSingleRule(EncryptRule.class).getConfiguration();
        config.getTables().removeIf(each -> each.getName().equals(event.getTableName()));
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    private EncryptTableRuleConfiguration swapEncryptTableRuleConfig(final String yamlContext) {
        return new YamlEncryptTableRuleConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContext, YamlEncryptTableRuleConfiguration.class));
    }
    
    private CompatibleEncryptRuleConfiguration getCompatibleEncryptRuleConfiguration(final ShardingSphereDatabase database, final EncryptTableRuleConfiguration needToAddedConfig) {
        Optional<EncryptRule> rule = database.getRuleMetaData().findSingleRule(EncryptRule.class);
        CompatibleEncryptRuleConfiguration result = rule.map(encryptRule -> getCompatibleEncryptRuleConfiguration((CompatibleEncryptRuleConfiguration) encryptRule.getConfiguration()))
                .orElseGet(() -> new CompatibleEncryptRuleConfiguration(new LinkedList<>(), new LinkedHashMap<>()));
        // TODO refactor DistSQL to only persist config
        result.getTables().removeIf(each -> each.getName().equals(needToAddedConfig.getName()));
        result.getTables().add(needToAddedConfig);
        return result;
    }
    
    private CompatibleEncryptRuleConfiguration getCompatibleEncryptRuleConfiguration(final CompatibleEncryptRuleConfiguration result) {
        if (null == result.getTables()) {
            return new CompatibleEncryptRuleConfiguration(new LinkedList<>(), result.getEncryptors());
        }
        return result;
    }
}
