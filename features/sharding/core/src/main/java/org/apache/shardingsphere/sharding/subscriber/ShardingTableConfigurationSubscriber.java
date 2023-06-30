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

package org.apache.shardingsphere.sharding.subscriber;

import com.google.common.eventbus.Subscribe;
import lombok.Setter;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.event.config.DatabaseRuleConfigurationChangedEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.subsciber.RuleChangedSubscriber;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.event.table.auto.AddShardingAutoTableEvent;
import org.apache.shardingsphere.sharding.event.table.auto.AlterShardingAutoTableEvent;
import org.apache.shardingsphere.sharding.event.table.auto.DeleteShardingAutoTableEvent;
import org.apache.shardingsphere.sharding.event.table.binding.AddShardingTableReferenceEvent;
import org.apache.shardingsphere.sharding.event.table.binding.AlterShardingTableReferenceEvent;
import org.apache.shardingsphere.sharding.event.table.binding.DeleteShardingTableReferenceEvent;
import org.apache.shardingsphere.sharding.event.table.sharding.AddShardingTableEvent;
import org.apache.shardingsphere.sharding.event.table.sharding.AlterShardingTableEvent;
import org.apache.shardingsphere.sharding.event.table.sharding.DeleteShardingTableEvent;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.swapper.rule.YamlShardingAutoTableRuleConfigurationSwapper;
import org.apache.shardingsphere.sharding.yaml.swapper.rule.YamlShardingTableReferenceRuleConfigurationConverter;
import org.apache.shardingsphere.sharding.yaml.swapper.rule.YamlShardingTableRuleConfigurationSwapper;

import java.util.Optional;

/**
 * Sharding table configuration subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
@Setter
public final class ShardingTableConfigurationSubscriber implements RuleChangedSubscriber {
    
    private ContextManager contextManager;
    
    /**
     * Renew with add sharding table configuration.
     *
     * @param event add sharding table configuration event
     */
    @Subscribe
    public synchronized void renew(final AddShardingTableEvent event) {
        if (!event.getActiveVersion().equals(contextManager.getInstanceContext().getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingTableRuleConfiguration needToAddedConfig = swapShardingTableRuleConfig(
                contextManager.getInstanceContext().getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion()));
        ShardingRuleConfiguration config = getShardingRuleConfiguration(database);
        // TODO refactor DistSQL to only persist config
        config.getTables().removeIf(each -> each.getLogicTable().equals(needToAddedConfig.getLogicTable()));
        config.getTables().add(needToAddedConfig);
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with add sharding auto table configuration.
     *
     * @param event add sharding auto table configuration event
     */
    @Subscribe
    public synchronized void renew(final AddShardingAutoTableEvent event) {
        if (!event.getActiveVersion().equals(contextManager.getInstanceContext().getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingAutoTableRuleConfiguration needToAddedConfig = swapShardingAutoTableRuleConfig(
                contextManager.getInstanceContext().getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion()));
        ShardingRuleConfiguration config = getShardingRuleConfiguration(database);
        // TODO refactor DistSQL to only persist config
        config.getAutoTables().removeIf(each -> each.getLogicTable().equals(needToAddedConfig.getLogicTable()));
        config.getAutoTables().add(needToAddedConfig);
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with add sharding auto table configuration.
     *
     * @param event add sharding auto table configuration event
     */
    @Subscribe
    public synchronized void renew(final AddShardingTableReferenceEvent event) {
        if (!event.getActiveVersion().equals(contextManager.getInstanceContext().getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingTableReferenceRuleConfiguration needToAddedConfig = swapShardingTableReferenceRuleConfig(
                contextManager.getInstanceContext().getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion()));
        ShardingRuleConfiguration config = getShardingRuleConfiguration(database);
        // TODO refactor DistSQL to only persist config
        config.getBindingTableGroups().removeIf(each -> each.getName().equals(needToAddedConfig.getName()));
        config.getBindingTableGroups().add(needToAddedConfig);
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with alter sharding table configuration.
     *
     * @param event alter sharding table configuration event
     */
    @Subscribe
    public synchronized void renew(final AlterShardingTableEvent event) {
        if (!event.getActiveVersion().equals(contextManager.getInstanceContext().getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingTableRuleConfiguration needToAlteredConfig = swapShardingTableRuleConfig(
                contextManager.getInstanceContext().getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion()));
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.getTables().removeIf(each -> each.getLogicTable().equals(event.getTableName()));
        config.getTables().add(needToAlteredConfig);
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with alter sharding auto table configuration.
     *
     * @param event alter sharding auto table configuration event
     */
    @Subscribe
    public synchronized void renew(final AlterShardingAutoTableEvent event) {
        if (!event.getActiveVersion().equals(contextManager.getInstanceContext().getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingAutoTableRuleConfiguration needToAlteredConfig = swapShardingAutoTableRuleConfig(
                contextManager.getInstanceContext().getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion()));
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.getAutoTables().removeIf(each -> each.getLogicTable().equals(event.getTableName()));
        config.getAutoTables().add(needToAlteredConfig);
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with alter sharding table reference configuration.
     *
     * @param event alter sharding table reference configuration event
     */
    @Subscribe
    public synchronized void renew(final AlterShardingTableReferenceEvent event) {
        if (!event.getActiveVersion().equals(contextManager.getInstanceContext().getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingTableReferenceRuleConfiguration needToAlteredConfig = swapShardingTableReferenceRuleConfig(
                contextManager.getInstanceContext().getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion()));
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.getBindingTableGroups().removeIf(each -> each.getName().equals(event.getTableName()));
        config.getBindingTableGroups().add(needToAlteredConfig);
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with delete sharding table configuration.
     *
     * @param event delete sharding table configuration event
     */
    @Subscribe
    public synchronized void renew(final DeleteShardingTableEvent event) {
        if (!contextManager.getMetaDataContexts().getMetaData().containsDatabase(event.getDatabaseName())) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.getTables().removeIf(each -> each.getLogicTable().equals(event.getTableName()));
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with delete sharding auto table configuration.
     *
     * @param event delete sharding auto table configuration event
     */
    @Subscribe
    public synchronized void renew(final DeleteShardingAutoTableEvent event) {
        if (!contextManager.getMetaDataContexts().getMetaData().containsDatabase(event.getDatabaseName())) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.getAutoTables().removeIf(each -> each.getLogicTable().equals(event.getTableName()));
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with delete sharding table reference configuration.
     *
     * @param event delete sharding table reference configuration event
     */
    @Subscribe
    public synchronized void renew(final DeleteShardingTableReferenceEvent event) {
        if (!contextManager.getMetaDataContexts().getMetaData().containsDatabase(event.getDatabaseName())) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.getBindingTableGroups().removeIf(each -> each.getName().equals(event.getTableName()));
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    private ShardingRuleConfiguration getShardingRuleConfiguration(final ShardingSphereDatabase database) {
        Optional<ShardingRule> rule = database.getRuleMetaData().findSingleRule(ShardingRule.class);
        ShardingRuleConfiguration result;
        if (rule.isPresent()) {
            result = (ShardingRuleConfiguration) rule.get().getConfiguration();
        } else {
            result = new ShardingRuleConfiguration();
        }
        return result;
    }
    
    private ShardingTableRuleConfiguration swapShardingTableRuleConfig(final String yamlContext) {
        return new YamlShardingTableRuleConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContext, YamlTableRuleConfiguration.class));
    }
    
    private ShardingAutoTableRuleConfiguration swapShardingAutoTableRuleConfig(final String yamlContext) {
        return new YamlShardingAutoTableRuleConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContext, YamlShardingAutoTableRuleConfiguration.class));
    }
    
    private ShardingTableReferenceRuleConfiguration swapShardingTableReferenceRuleConfig(final String yamlContext) {
        return YamlShardingTableReferenceRuleConfigurationConverter.convertToObject(yamlContext);
    }
}
