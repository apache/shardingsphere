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
import org.apache.shardingsphere.sharding.event.strategy.audit.CreateDefaultShardingAuditorStrategyEvent;
import org.apache.shardingsphere.sharding.event.strategy.audit.DropDefaultShardingAuditorStrategyEvent;
import org.apache.shardingsphere.sharding.event.strategy.database.AlterDefaultDatabaseShardingStrategyEvent;
import org.apache.shardingsphere.sharding.event.strategy.database.CreateDefaultDatabaseShardingStrategyEvent;
import org.apache.shardingsphere.sharding.event.strategy.database.DropDefaultDatabaseShardingStrategyEvent;
import org.apache.shardingsphere.sharding.event.strategy.keygenerate.AlterDefaultKeyGenerateStrategyEvent;
import org.apache.shardingsphere.sharding.event.strategy.keygenerate.CreateDefaultKeyGenerateStrategyEvent;
import org.apache.shardingsphere.sharding.event.strategy.keygenerate.DropDefaultKeyGenerateStrategyEvent;
import org.apache.shardingsphere.sharding.event.strategy.shardingcolumn.AlterDefaultShardingColumnEvent;
import org.apache.shardingsphere.sharding.event.strategy.shardingcolumn.CreateDefaultShardingColumnEvent;
import org.apache.shardingsphere.sharding.event.strategy.shardingcolumn.DropDefaultShardingColumnEvent;
import org.apache.shardingsphere.sharding.event.strategy.table.AlterDefaultTableShardingStrategyEvent;
import org.apache.shardingsphere.sharding.event.strategy.table.CreateDefaultTableShardingStrategyEvent;
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
     * Renew with create default database sharding strategy.
     *
     * @param event create default database sharding strategy event
     */
    @Subscribe
    public synchronized void renew(final CreateDefaultDatabaseShardingStrategyEvent event) {
        if (!event.getActiveVersion().equals(contextManager.getInstanceContext().getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingStrategyConfiguration needToAddedConfig = swapShardingStrategyConfig(
                contextManager.getInstanceContext().getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion()));
        ShardingRuleConfiguration config = database.getRuleMetaData().findSingleRule(ShardingRule.class)
                .map(optional -> (ShardingRuleConfiguration) optional.getConfiguration()).orElse(new ShardingRuleConfiguration());
        config.setDefaultDatabaseShardingStrategy(needToAddedConfig);
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
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
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingStrategyConfiguration needToAlteredConfig = swapShardingStrategyConfig(
                contextManager.getInstanceContext().getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion()));
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.setDefaultDatabaseShardingStrategy(needToAlteredConfig);
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
     * Renew with create default table sharding strategy.
     *
     * @param event create default table sharding strategy event
     */
    @Subscribe
    public synchronized void renew(final CreateDefaultTableShardingStrategyEvent event) {
        if (!event.getActiveVersion().equals(contextManager.getInstanceContext().getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingStrategyConfiguration needToAddedConfig = swapShardingStrategyConfig(
                contextManager.getInstanceContext().getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion()));
        ShardingRuleConfiguration config = database.getRuleMetaData().findSingleRule(ShardingRule.class)
                .map(optional -> (ShardingRuleConfiguration) optional.getConfiguration()).orElse(new ShardingRuleConfiguration());
        config.setDefaultTableShardingStrategy(needToAddedConfig);
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
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingStrategyConfiguration needToAlteredConfig = swapShardingStrategyConfig(
                contextManager.getInstanceContext().getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion()));
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.setDefaultTableShardingStrategy(needToAlteredConfig);
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
     * Renew with create default key generate strategy.
     *
     * @param event create default key generate strategy event
     */
    @Subscribe
    public synchronized void renew(final CreateDefaultKeyGenerateStrategyEvent event) {
        if (!event.getActiveVersion().equals(contextManager.getInstanceContext().getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        KeyGenerateStrategyConfiguration needToAddedConfig = swapKeyGenerateStrategyConfig(
                contextManager.getInstanceContext().getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion()));
        ShardingRuleConfiguration config = database.getRuleMetaData().findSingleRule(ShardingRule.class)
                .map(optional -> (ShardingRuleConfiguration) optional.getConfiguration()).orElse(new ShardingRuleConfiguration());
        config.setDefaultKeyGenerateStrategy(needToAddedConfig);
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
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        KeyGenerateStrategyConfiguration needToAlteredConfig = swapKeyGenerateStrategyConfig(
                contextManager.getInstanceContext().getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion()));
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.setDefaultKeyGenerateStrategy(needToAlteredConfig);
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
     * Renew with create default sharding auditor strategy.
     *
     * @param event create default sharding auditor strategy event
     */
    @Subscribe
    public synchronized void renew(final CreateDefaultShardingAuditorStrategyEvent event) {
        if (!event.getActiveVersion().equals(contextManager.getInstanceContext().getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingAuditStrategyConfiguration needToAddedConfig = swapShardingAuditorStrategyConfig(
                contextManager.getInstanceContext().getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion()));
        ShardingRuleConfiguration config = database.getRuleMetaData().findSingleRule(ShardingRule.class)
                .map(optional -> (ShardingRuleConfiguration) optional.getConfiguration()).orElse(new ShardingRuleConfiguration());
        config.setDefaultAuditStrategy(needToAddedConfig);
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
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingAuditStrategyConfiguration needToAlteredConfig = swapShardingAuditorStrategyConfig(
                contextManager.getInstanceContext().getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion()));
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.setDefaultAuditStrategy(needToAlteredConfig);
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
     * Renew with create default sharding column.
     *
     * @param event create default sharding column event
     */
    @Subscribe
    public synchronized void renew(final CreateDefaultShardingColumnEvent event) {
        if (!event.getActiveVersion().equals(contextManager.getInstanceContext().getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        String needToAddedConfig = contextManager.getInstanceContext().getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion());
        ShardingRuleConfiguration config = database.getRuleMetaData().findSingleRule(ShardingRule.class)
                .map(optional -> (ShardingRuleConfiguration) optional.getConfiguration()).orElse(new ShardingRuleConfiguration());
        config.setDefaultShardingColumn(needToAddedConfig);
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
        String needToAlteredConfig = contextManager.getInstanceContext().getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion());
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.setDefaultShardingColumn(needToAlteredConfig);
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
    
    private ShardingStrategyConfiguration swapShardingStrategyConfig(final String yamlContext) {
        return new YamlShardingStrategyConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContext, YamlShardingStrategyConfiguration.class));
    }
    
    private KeyGenerateStrategyConfiguration swapKeyGenerateStrategyConfig(final String yamlContext) {
        return new YamlKeyGenerateStrategyConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContext, YamlKeyGenerateStrategyConfiguration.class));
    }
    
    private ShardingAuditStrategyConfiguration swapShardingAuditorStrategyConfig(final String yamlContext) {
        return new YamlShardingAuditStrategyConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContext, YamlShardingAuditStrategyConfiguration.class));
    }
}
