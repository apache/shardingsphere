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
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.event.table.AddShadowTableEvent;
import org.apache.shardingsphere.shadow.event.table.AlterShadowTableEvent;
import org.apache.shardingsphere.shadow.event.table.DeleteShadowTableEvent;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.yaml.config.table.YamlShadowTableConfiguration;
import org.apache.shardingsphere.shadow.yaml.swapper.table.YamlShadowTableConfigurationSwapper;

import java.util.Optional;

/**
 * Shadow table subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
@Setter
public final class ShadowTableSubscriber implements RuleChangedSubscriber {
    
    private ContextManager contextManager;
    
    private InstanceContext instanceContext;
    
    /**
     * Renew with add table.
     *
     * @param event add table event
     */
    @Subscribe
    public synchronized void renew(final AddShadowTableEvent event) {
        renew(event.getDatabaseName(), event.getTableName(), swapToTableConfig(
                instanceContext.getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion())));
    }
    
    /**
     * Renew with alter table.
     *
     * @param event alter table event
     */
    @Subscribe
    public synchronized void renew(final AlterShadowTableEvent event) {
        renew(event.getDatabaseName(), event.getTableName(), swapToTableConfig(
                instanceContext.getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion())));
    }
    
    private void renew(final String databaseName, final String tableName, final ShadowTableConfiguration needToAlteredConfig) {
        Optional<ShadowRule> rule = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(databaseName).getRuleMetaData().findSingleRule(ShadowRule.class);
        ShadowRuleConfiguration config;
        if (rule.isPresent()) {
            config = (ShadowRuleConfiguration) rule.get().getConfiguration();
        } else {
            config = new ShadowRuleConfiguration();
        }
        config.getTables().put(tableName, needToAlteredConfig);
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(databaseName, config));
    }
    
    /**
     * Renew with delete table.
     *
     * @param event delete table event
     */
    @Subscribe
    public synchronized void renew(final DeleteShadowTableEvent event) {
        if (!contextManager.getMetaDataContexts().getMetaData().containsDatabase(event.getDatabaseName())) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShadowRuleConfiguration config = (ShadowRuleConfiguration) database.getRuleMetaData().getSingleRule(ShadowRule.class).getConfiguration();
        config.getTables().remove(event.getTableName());
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    private ShadowTableConfiguration swapToTableConfig(final String yamlContext) {
        return new YamlShadowTableConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContext, YamlShadowTableConfiguration.class));
    }
}
