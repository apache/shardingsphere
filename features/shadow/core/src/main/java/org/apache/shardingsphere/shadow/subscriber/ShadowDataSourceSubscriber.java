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
import lombok.Setter;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.event.config.DatabaseRuleConfigurationChangedEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.subsciber.RuleChangedSubscriber;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.event.datasource.AddShadowDataSourceEvent;
import org.apache.shardingsphere.shadow.event.datasource.AlterShadowDataSourceEvent;
import org.apache.shardingsphere.shadow.event.datasource.DeleteShadowDataSourceEvent;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.yaml.config.datasource.YamlShadowDataSourceConfiguration;

import java.util.Optional;

/**
 * Shadow configuration subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
@Setter
public final class ShadowDataSourceSubscriber implements RuleChangedSubscriber {
    
    private ContextManager contextManager;
    
    private InstanceContext instanceContext;
    
    /**
     * Renew with add shadow configuration.
     *
     * @param event add shadow configuration event
     */
    @Subscribe
    public synchronized void renew(final AddShadowDataSourceEvent event) {
        if (!event.getActiveVersion().equals(instanceContext.getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        ShadowDataSourceConfiguration needToAddedConfig = swapShadowDataSourceRuleConfig(event.getDataSourceName(),
                instanceContext.getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion()));
        Optional<ShadowRule> rule = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName()).getRuleMetaData().findSingleRule(ShadowRule.class);
        ShadowRuleConfiguration config;
        if (rule.isPresent()) {
            config = (ShadowRuleConfiguration) rule.get().getConfiguration();
        } else {
            config = new ShadowRuleConfiguration();
        }
        // TODO refactor DistSQL to only persist config
        config.getDataSources().removeIf(each -> each.getName().equals(needToAddedConfig.getName()));
        config.getDataSources().add(needToAddedConfig);
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with alter shadow configuration.
     *
     * @param event alter shadow configuration event
     */
    @Subscribe
    public synchronized void renew(final AlterShadowDataSourceEvent event) {
        if (!event.getActiveVersion().equals(instanceContext.getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShadowDataSourceConfiguration needToAlteredConfig = swapShadowDataSourceRuleConfig(event.getDataSourceName(),
                instanceContext.getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion()));
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
    public synchronized void renew(final DeleteShadowDataSourceEvent event) {
        if (!contextManager.getMetaDataContexts().getMetaData().containsDatabase(event.getDatabaseName())) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShadowRuleConfiguration config = (ShadowRuleConfiguration) database.getRuleMetaData().getSingleRule(ShadowRule.class).getConfiguration();
        config.getDataSources().removeIf(each -> each.getName().equals(event.getDataSourceName()));
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    private ShadowDataSourceConfiguration swapShadowDataSourceRuleConfig(final String dataSourceName, final String yamlContext) {
        YamlShadowDataSourceConfiguration yamlConfig = YamlEngine.unmarshal(yamlContext, YamlShadowDataSourceConfiguration.class);
        return new ShadowDataSourceConfiguration(dataSourceName, yamlConfig.getProductionDataSourceName(), yamlConfig.getShadowDataSourceName());
    }
}
