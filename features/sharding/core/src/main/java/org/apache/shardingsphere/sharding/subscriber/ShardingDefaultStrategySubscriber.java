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
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.event.strategy.audit.AlterDefaultShardingAuditorStrategyEvent;
import org.apache.shardingsphere.sharding.event.strategy.audit.DropDefaultShardingAuditorStrategyEvent;
import org.apache.shardingsphere.sharding.event.strategy.database.AlterDefaultDatabaseShardingStrategyEvent;
import org.apache.shardingsphere.sharding.event.strategy.database.DropDefaultDatabaseShardingStrategyEvent;
import org.apache.shardingsphere.sharding.event.strategy.keygenerate.AlterDefaultKeyGenerateStrategyEvent;
import org.apache.shardingsphere.sharding.event.strategy.keygenerate.DropDefaultKeyGenerateStrategyEvent;
import org.apache.shardingsphere.sharding.event.strategy.shardingcolumn.AlterDefaultShardingColumnEvent;
import org.apache.shardingsphere.sharding.event.strategy.shardingcolumn.DropDefaultShardingColumnEvent;
import org.apache.shardingsphere.sharding.event.strategy.table.AlterDefaultTableShardingStrategyEvent;
import org.apache.shardingsphere.sharding.event.strategy.table.DropDefaultTableShardingStrategyEvent;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.yaml.config.strategy.audit.YamlShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.keygen.YamlKeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.swapper.strategy.YamlKeyGenerateStrategyConfigurationSwapper;
import org.apache.shardingsphere.sharding.yaml.swapper.strategy.YamlShardingAuditStrategyConfigurationSwapper;
import org.apache.shardingsphere.sharding.yaml.swapper.strategy.YamlShardingStrategyConfigurationSwapper;

/**
 * Sharding default strategy subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
@Setter
public final class ShardingDefaultStrategySubscriber implements RuleChangedSubscriber {
    
    private ContextManager contextManager;
    
    /**
     * Renew with alter default database sharding strategy.
     *
     * @param event alter default database sharding strategy event
     */
    @Subscribe
    public synchronized void renew(final AlterDefaultDatabaseShardingStrategyEvent event) {
        if (!event.getActiveVersion().equals(contextManager.getInstanceContext().getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        String yamlContext = contextManager.getInstanceContext().getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion());
        ShardingStrategyConfiguration toBeChangedConfig = new YamlShardingStrategyConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContext, YamlShardingStrategyConfiguration.class));
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingRuleConfiguration config = database.getRuleMetaData().findSingleRule(ShardingRule.class)
                .map(optional -> (ShardingRuleConfiguration) optional.getConfiguration()).orElseGet(ShardingRuleConfiguration::new);
        config.setDefaultDatabaseShardingStrategy(toBeChangedConfig);
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with drop default database sharding strategy.
     *
     * @param event drop default database sharding strategy event
     */
    @Subscribe
    public synchronized void renew(final DropDefaultDatabaseShardingStrategyEvent event) {
        if (!contextManager.getMetaDataContexts().getMetaData().containsDatabase(event.getDatabaseName())) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.setDefaultDatabaseShardingStrategy(null);
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with alter default table sharding strategy.
     *
     * @param event alter default table sharding strategy event
     */
    @Subscribe
    public synchronized void renew(final AlterDefaultTableShardingStrategyEvent event) {
        if (!event.getActiveVersion().equals(contextManager.getInstanceContext().getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        String yamlContext = contextManager.getInstanceContext().getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion());
        ShardingStrategyConfiguration toBeChangedConfig = new YamlShardingStrategyConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContext, YamlShardingStrategyConfiguration.class));
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingRuleConfiguration config = database.getRuleMetaData().findSingleRule(ShardingRule.class)
                .map(optional -> (ShardingRuleConfiguration) optional.getConfiguration()).orElseGet(ShardingRuleConfiguration::new);
        config.setDefaultTableShardingStrategy(toBeChangedConfig);
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with drop default table sharding strategy.
     *
     * @param event drop default table sharding strategy event
     */
    @Subscribe
    public synchronized void renew(final DropDefaultTableShardingStrategyEvent event) {
        if (!contextManager.getMetaDataContexts().getMetaData().containsDatabase(event.getDatabaseName())) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.setDefaultTableShardingStrategy(null);
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with alter default key generate strategy.
     *
     * @param event alter default key generate strategy event
     */
    @Subscribe
    public synchronized void renew(final AlterDefaultKeyGenerateStrategyEvent event) {
        if (!event.getActiveVersion().equals(contextManager.getInstanceContext().getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        String yamlContext = contextManager.getInstanceContext().getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion());
        KeyGenerateStrategyConfiguration toBeChangedConfig = new YamlKeyGenerateStrategyConfigurationSwapper().swapToObject(
                YamlEngine.unmarshal(yamlContext, YamlKeyGenerateStrategyConfiguration.class));
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingRuleConfiguration config = database.getRuleMetaData().findSingleRule(ShardingRule.class)
                .map(optional -> (ShardingRuleConfiguration) optional.getConfiguration()).orElseGet(ShardingRuleConfiguration::new);
        config.setDefaultKeyGenerateStrategy(toBeChangedConfig);
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with drop default key generate strategy.
     *
     * @param event drop default key generate strategy event
     */
    @Subscribe
    public synchronized void renew(final DropDefaultKeyGenerateStrategyEvent event) {
        if (!contextManager.getMetaDataContexts().getMetaData().containsDatabase(event.getDatabaseName())) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.setDefaultKeyGenerateStrategy(null);
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with alter default sharding auditor strategy.
     *
     * @param event alter default sharding auditor strategy event
     */
    @Subscribe
    public synchronized void renew(final AlterDefaultShardingAuditorStrategyEvent event) {
        if (!event.getActiveVersion().equals(contextManager.getInstanceContext().getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        String yamlContext = contextManager.getInstanceContext().getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion());
        ShardingAuditStrategyConfiguration toBeChangedConfig = new YamlShardingAuditStrategyConfigurationSwapper().swapToObject(
                YamlEngine.unmarshal(yamlContext, YamlShardingAuditStrategyConfiguration.class));
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingRuleConfiguration config = database.getRuleMetaData().findSingleRule(ShardingRule.class)
                .map(optional -> (ShardingRuleConfiguration) optional.getConfiguration()).orElseGet(ShardingRuleConfiguration::new);
        config.setDefaultAuditStrategy(toBeChangedConfig);
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with drop default sharding auditor strategy.
     *
     * @param event drop default sharding auditor strategy event
     */
    @Subscribe
    public synchronized void renew(final DropDefaultShardingAuditorStrategyEvent event) {
        if (!contextManager.getMetaDataContexts().getMetaData().containsDatabase(event.getDatabaseName())) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.setDefaultAuditStrategy(null);
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with alter default sharding column.
     *
     * @param event alter default sharding column event
     */
    @Subscribe
    public synchronized void renew(final AlterDefaultShardingColumnEvent event) {
        if (!event.getActiveVersion().equals(contextManager.getInstanceContext().getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        String toBeChangedConfig = contextManager.getInstanceContext().getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion());
        ShardingRuleConfiguration config = database.getRuleMetaData().findSingleRule(ShardingRule.class)
                .map(optional -> (ShardingRuleConfiguration) optional.getConfiguration()).orElseGet(ShardingRuleConfiguration::new);
        config.setDefaultShardingColumn(toBeChangedConfig);
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with delete default sharding column.
     *
     * @param event delete default sharding column event
     */
    @Subscribe
    public synchronized void renew(final DropDefaultShardingColumnEvent event) {
        if (!contextManager.getMetaDataContexts().getMetaData().containsDatabase(event.getDatabaseName())) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.setDefaultAuditStrategy(null);
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
}
