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

package org.apache.shardingsphere.encrypt.subscriber;

import com.google.common.eventbus.Subscribe;
import lombok.Setter;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.event.table.AddEncryptTableEvent;
import org.apache.shardingsphere.encrypt.event.table.AlterEncryptTableEvent;
import org.apache.shardingsphere.encrypt.event.table.DeleteEncryptTableEvent;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.yaml.config.rule.YamlEncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.swapper.rule.YamlEncryptTableRuleConfigurationSwapper;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.event.config.DatabaseRuleConfigurationChangedEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.subsciber.RuleChangedSubscriber;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Encrypt table subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
@Setter
public final class EncryptTableSubscriber implements RuleChangedSubscriber {
    
    private ContextManager contextManager;
    
    private InstanceContext instanceContext;
    
    /**
     * Renew with add encrypt configuration.
     *
     * @param event add encrypt configuration event
     */
    @Subscribe
    public synchronized void renew(final AddEncryptTableEvent event) {
        if (!event.getActiveVersion().equals(instanceContext.getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        EncryptTableRuleConfiguration needToAddedConfig = swapEncryptTableRuleConfig(
                instanceContext.getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion()));
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), getEncryptRuleConfiguration(database, needToAddedConfig)));
    }
    
    /**
     * Renew with alter encrypt configuration.
     *
     * @param event alter encrypt configuration event
     */
    @Subscribe
    public synchronized void renew(final AlterEncryptTableEvent event) {
        if (!event.getActiveVersion().equals(instanceContext.getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        EncryptTableRuleConfiguration needToAlteredConfig = swapEncryptTableRuleConfig(
                instanceContext.getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion()));
        EncryptRuleConfiguration config = (EncryptRuleConfiguration) database.getRuleMetaData().getSingleRule(EncryptRule.class).getConfiguration();
        config.getTables().removeIf(each -> each.getName().equals(event.getTableName()));
        config.getTables().add(needToAlteredConfig);
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with delete encrypt configuration.
     *
     * @param event delete encrypt configuration event
     */
    @Subscribe
    public synchronized void renew(final DeleteEncryptTableEvent event) {
        if (!contextManager.getMetaDataContexts().getMetaData().containsDatabase(event.getDatabaseName())) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        EncryptRuleConfiguration config = (EncryptRuleConfiguration) database.getRuleMetaData().getSingleRule(EncryptRule.class).getConfiguration();
        config.getTables().removeIf(each -> each.getName().equals(event.getTableName()));
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    private EncryptTableRuleConfiguration swapEncryptTableRuleConfig(final String yamlContext) {
        return new YamlEncryptTableRuleConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContext, YamlEncryptTableRuleConfiguration.class));
    }
    
    private EncryptRuleConfiguration getEncryptRuleConfiguration(final ShardingSphereDatabase database, final EncryptTableRuleConfiguration needToAddedConfig) {
        Optional<EncryptRule> rule = database.getRuleMetaData().findSingleRule(EncryptRule.class);
        EncryptRuleConfiguration config = rule.map(encryptRule -> getEncryptRuleConfiguration((EncryptRuleConfiguration) encryptRule.getConfiguration()))
                .orElseGet(() -> new EncryptRuleConfiguration(new LinkedList<>(), new LinkedHashMap<>()));
        // TODO refactor DistSQL to only persist config
        config.getTables().removeIf(each -> each.getName().equals(needToAddedConfig.getName()));
        config.getTables().add(needToAddedConfig);
        return config;
    }
    
    private EncryptRuleConfiguration getEncryptRuleConfiguration(final EncryptRuleConfiguration config) {
        if (null == config.getTables()) {
            return new EncryptRuleConfiguration(new LinkedList<>(), config.getEncryptors());
        }
        return config;
    }
}
