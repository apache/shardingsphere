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
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.event.config.AddEncryptConfigurationEvent;
import org.apache.shardingsphere.encrypt.event.config.AlterEncryptConfigurationEvent;
import org.apache.shardingsphere.encrypt.event.config.DeleteEncryptConfigurationEvent;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.RuleConfigurationSubscribeCoordinator;
import org.apache.shardingsphere.mode.event.config.DatabaseRuleConfigurationChangedEvent;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Encrypt configuration subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
@RequiredArgsConstructor
public final class EncryptConfigurationSubscriber implements RuleConfigurationSubscribeCoordinator {
    
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
    public synchronized void renew(final AddEncryptConfigurationEvent event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        EncryptTableRuleConfiguration needToAddedConfig = event.getConfig();
        Optional<EncryptRule> rule = database.getRuleMetaData().findSingleRule(EncryptRule.class);
        EncryptRuleConfiguration config;
        if (rule.isPresent()) {
            config = (EncryptRuleConfiguration) rule.get().getConfiguration();
            config.getTables().add(needToAddedConfig);
        } else {
            config = new EncryptRuleConfiguration(Collections.singletonList(needToAddedConfig), Collections.emptyMap());
        }
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with alter encrypt configuration.
     *
     * @param event alter encrypt configuration event
     */
    @Subscribe
    public synchronized void renew(final AlterEncryptConfigurationEvent event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        EncryptTableRuleConfiguration needToAlteredConfig = event.getConfig();
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
    public synchronized void renew(final DeleteEncryptConfigurationEvent event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        EncryptRuleConfiguration config = (EncryptRuleConfiguration) database.getRuleMetaData().getSingleRule(EncryptRule.class).getConfiguration();
        config.getTables().removeIf(each -> each.getName().equals(event.getTableName()));
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
}
