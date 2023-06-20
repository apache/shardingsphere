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

package org.apache.shardingsphere.shadow.subscriber;

import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.RuleConfigurationSubscribeCoordinator;
import org.apache.shardingsphere.mode.event.config.DatabaseRuleConfigurationChangedEvent;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.event.config.AddShadowConfigurationEvent;
import org.apache.shardingsphere.shadow.event.config.AlterShadowConfigurationEvent;
import org.apache.shardingsphere.shadow.event.config.DeleteShadowConfigurationEvent;
import org.apache.shardingsphere.shadow.rule.ShadowRule;

import java.util.Map;
import java.util.Optional;

/**
 * Shadow configuration subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
@RequiredArgsConstructor
public final class ShadowConfigurationSubscriber implements RuleConfigurationSubscribeCoordinator {
    
    private Map<String, ShardingSphereDatabase> databases;
    
    private InstanceContext instanceContext;
    
    @Override
    public void registerRuleConfigurationSubscriber(final Map<String, ShardingSphereDatabase> databases, final InstanceContext instanceContext) {
        this.databases = databases;
        this.instanceContext = instanceContext;
        instanceContext.getEventBusContext().register(this);
    }
    
    /**
     * Renew with add shadow configuration.
     *
     * @param event add shadow configuration event
     */
    @Subscribe
    public synchronized void renew(final AddShadowConfigurationEvent event) {
        if (event.getVersion() < instanceContext.getModeContextManager().getActiveVersionByKey(event.getVersionKey())) {
            return;
        }
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        ShadowDataSourceConfiguration needToAddedConfig = event.getConfig();
        Optional<ShadowRule> rule = database.getRuleMetaData().findSingleRule(ShadowRule.class);
        ShadowRuleConfiguration config;
        if (rule.isPresent()) {
            config = (ShadowRuleConfiguration) rule.get().getConfiguration();
            config.getDataSources().removeIf(each -> each.getName().equals(needToAddedConfig.getName()));
            config.getDataSources().add(needToAddedConfig);
        } else {
            config = new ShadowRuleConfiguration();
            config.getDataSources().add(needToAddedConfig);
        }
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with alter shadow configuration.
     *
     * @param event alter shadow configuration event
     */
    @Subscribe
    public synchronized void renew(final AlterShadowConfigurationEvent event) {
        if (event.getVersion() < instanceContext.getModeContextManager().getActiveVersionByKey(event.getVersionKey())) {
            return;
        }
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        ShadowDataSourceConfiguration needToAlteredConfig = event.getConfig();
        ShadowRuleConfiguration config = (ShadowRuleConfiguration) database.getRuleMetaData().getSingleRule(ShadowRule.class).getConfiguration();
        config.getDataSources().removeIf(each -> each.getName().equals(event.getDataSourceName()));
        config.getDataSources().add(needToAlteredConfig);
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with delete shadow configuration.
     *
     * @param event delete shadow configuration event
     */
    @Subscribe
    public synchronized void renew(final DeleteShadowConfigurationEvent event) {
        if (event.getVersion() < instanceContext.getModeContextManager().getActiveVersionByKey(event.getVersionKey())) {
            return;
        }
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        ShadowRuleConfiguration config = (ShadowRuleConfiguration) database.getRuleMetaData().getSingleRule(ShadowRule.class).getConfiguration();
        config.getDataSources().removeIf(each -> each.getName().equals(event.getDataSourceName()));
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
}
