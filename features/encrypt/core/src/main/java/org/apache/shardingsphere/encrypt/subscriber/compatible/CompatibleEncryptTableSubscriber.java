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
import org.apache.shardingsphere.encrypt.api.config.CompatibleEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.event.compatible.table.AddCompatibleEncryptTableEvent;
import org.apache.shardingsphere.encrypt.event.compatible.table.AlterCompatibleEncryptTableEvent;
import org.apache.shardingsphere.encrypt.event.compatible.table.DeleteCompatibleEncryptTableEvent;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.yaml.config.rule.YamlEncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.swapper.rule.YamlEncryptTableRuleConfigurationSwapper;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.RuleConfigurationSubscribeCoordinator;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.event.config.DatabaseRuleConfigurationChangedEvent;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Compatible encrypt table subscriber.
 * @deprecated compatible support will remove in next version.
 */
@Deprecated
@SuppressWarnings("UnstableApiUsage")
public final class CompatibleEncryptTableSubscriber implements RuleConfigurationSubscribeCoordinator {
    
    private Map<String, ShardingSphereDatabase> databases;
    
    private InstanceContext instanceContext;
    
    @Override
    public void registerRuleConfigurationSubscriber(final Map<String, ShardingSphereDatabase> databases, final InstanceContext instanceContext) {
        this.databases = databases;
        this.instanceContext = instanceContext;
        instanceContext.getEventBusContext().register(this);
    }
    
    /**
     * Renew with add encrypt configuration.
     *
     * @param event add encrypt configuration event
     */
    @Subscribe
    public synchronized void renew(final AddCompatibleEncryptTableEvent event) {
        if (!event.getActiveVersion().equals(instanceContext.getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        EncryptTableRuleConfiguration needToAddedConfig = swapEncryptTableRuleConfig(
                instanceContext.getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion()));
        Optional<EncryptRule> rule = database.getRuleMetaData().findSingleRule(EncryptRule.class);
        CompatibleEncryptRuleConfiguration config;
        if (rule.isPresent()) {
            config = (CompatibleEncryptRuleConfiguration) rule.get().getConfiguration();
            config.getTables().add(needToAddedConfig);
        } else {
            config = new CompatibleEncryptRuleConfiguration(Collections.singletonList(needToAddedConfig), Collections.emptyMap());
        }
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with alter encrypt configuration.
     *
     * @param event alter encrypt configuration event
     */
    @Subscribe
    public synchronized void renew(final AlterCompatibleEncryptTableEvent event) {
        if (!event.getActiveVersion().equals(instanceContext.getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        EncryptTableRuleConfiguration needToAlteredConfig = swapEncryptTableRuleConfig(
                instanceContext.getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion()));
        CompatibleEncryptRuleConfiguration config = (CompatibleEncryptRuleConfiguration) database.getRuleMetaData().getSingleRule(EncryptRule.class).getConfiguration();
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
    public synchronized void renew(final DeleteCompatibleEncryptTableEvent event) {
        if (!event.getActiveVersion().equals(instanceContext.getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        CompatibleEncryptRuleConfiguration config = (CompatibleEncryptRuleConfiguration) database.getRuleMetaData().getSingleRule(EncryptRule.class).getConfiguration();
        config.getTables().removeIf(each -> each.getName().equals(event.getTableName()));
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    private EncryptTableRuleConfiguration swapEncryptTableRuleConfig(final String yamlContext) {
        return new YamlEncryptTableRuleConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContext, YamlEncryptTableRuleConfiguration.class));
    }
}
