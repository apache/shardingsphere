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
import org.apache.shardingsphere.sharding.event.strategy.audit.AddShardingAuditorStrategyConfigurationEvent;
import org.apache.shardingsphere.sharding.event.strategy.audit.AlterShardingAuditorStrategyConfigurationEvent;
import org.apache.shardingsphere.sharding.event.strategy.audit.DeleteShardingAuditorStrategyConfigurationEvent;
import org.apache.shardingsphere.sharding.event.strategy.database.AddDefaultDatabaseShardingStrategyEvent;
import org.apache.shardingsphere.sharding.event.strategy.database.AlterDefaultDatabaseShardingStrategyEvent;
import org.apache.shardingsphere.sharding.event.strategy.database.DeleteDefaultDatabaseShardingStrategyEvent;
import org.apache.shardingsphere.sharding.event.strategy.keygenerate.AddDefaultKeyGenerateStrategyEvent;
import org.apache.shardingsphere.sharding.event.strategy.keygenerate.AlterDefaultKeyGenerateStrategyEvent;
import org.apache.shardingsphere.sharding.event.strategy.keygenerate.DeleteDefaultKeyGenerateStrategyEvent;
import org.apache.shardingsphere.sharding.event.strategy.shardingcolumn.AddDefaultShardingColumnEvent;
import org.apache.shardingsphere.sharding.event.strategy.shardingcolumn.AlterDefaultShardingColumnEvent;
import org.apache.shardingsphere.sharding.event.strategy.shardingcolumn.DeleteDefaultShardingColumnEvent;
import org.apache.shardingsphere.sharding.event.strategy.table.AddDefaultTableShardingStrategyEvent;
import org.apache.shardingsphere.sharding.event.strategy.table.AlterDefaultTableShardingStrategyEvent;
import org.apache.shardingsphere.sharding.event.strategy.table.DeleteDefaultTableShardingStrategyEvent;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.yaml.config.strategy.audit.YamlShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.keygen.YamlKeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.swapper.strategy.YamlKeyGenerateStrategyConfigurationSwapper;
import org.apache.shardingsphere.sharding.yaml.swapper.strategy.YamlShardingAuditStrategyConfigurationSwapper;
import org.apache.shardingsphere.sharding.yaml.swapper.strategy.YamlShardingStrategyConfigurationSwapper;

import java.util.Optional;

/**
 * Sharding strategy configuration subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
@Setter
public final class ShardingStrategyConfigurationSubscriber implements RuleChangedSubscriber {
    
    private ContextManager contextManager;
    
    /**
     * Renew with add default database sharding strategy configuration.
     *
     * @param event add default database sharding strategy configuration event
     */
    @Subscribe
    public synchronized void renew(final AddDefaultDatabaseShardingStrategyEvent event) {
        if (!event.getActiveVersion().equals(contextManager.getInstanceContext().getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingStrategyConfiguration needToAddedConfig = swapShardingStrategyConfig(
                contextManager.getInstanceContext().getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion()));
        ShardingRuleConfiguration config = getShardingRuleConfiguration(database);
        config.setDefaultDatabaseShardingStrategy(needToAddedConfig);
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with add default table sharding strategy configuration.
     *
     * @param event add default table sharding strategy configuration event
     */
    @Subscribe
    public synchronized void renew(final AddDefaultTableShardingStrategyEvent event) {
        if (!event.getActiveVersion().equals(contextManager.getInstanceContext().getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingStrategyConfiguration needToAddedConfig = swapShardingStrategyConfig(
                contextManager.getInstanceContext().getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion()));
        ShardingRuleConfiguration config = getShardingRuleConfiguration(database);
        config.setDefaultTableShardingStrategy(needToAddedConfig);
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with add default key generate strategy configuration.
     *
     * @param event add default key generate strategy configuration event
     */
    @Subscribe
    public synchronized void renew(final AddDefaultKeyGenerateStrategyEvent event) {
        if (!event.getActiveVersion().equals(contextManager.getInstanceContext().getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        KeyGenerateStrategyConfiguration needToAddedConfig = swapKeyGenerateStrategyConfig(
                contextManager.getInstanceContext().getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion()));
        ShardingRuleConfiguration config = getShardingRuleConfiguration(database);
        config.setDefaultKeyGenerateStrategy(needToAddedConfig);
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with add default sharding auditor strategy configuration.
     *
     * @param event add default sharding auditor strategy configuration event
     */
    @Subscribe
    public synchronized void renew(final AddShardingAuditorStrategyConfigurationEvent event) {
        if (!event.getActiveVersion().equals(contextManager.getInstanceContext().getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingAuditStrategyConfiguration needToAddedConfig = swapShardingAuditorStrategyConfig(
                contextManager.getInstanceContext().getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion()));
        ShardingRuleConfiguration config = getShardingRuleConfiguration(database);
        config.setDefaultAuditStrategy(needToAddedConfig);
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with add default sharding column configuration.
     *
     * @param event add default sharding column configuration event
     */
    @Subscribe
    public synchronized void renew(final AddDefaultShardingColumnEvent event) {
        if (!event.getActiveVersion().equals(contextManager.getInstanceContext().getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        String needToAddedConfig = contextManager.getInstanceContext().getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion());
        ShardingRuleConfiguration config = getShardingRuleConfiguration(database);
        config.setDefaultShardingColumn(needToAddedConfig);
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with alter default database sharding strategy configuration.
     *
     * @param event alter default database sharding strategy configuration event
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
     * Renew with alter default table sharding strategy configuration.
     *
     * @param event alter default table sharding strategy configuration event
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
     * Renew with alter default key generate strategy configuration.
     *
     * @param event alter default key generate strategy configuration event
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
     * Renew with alter default sharding auditor strategy configuration.
     *
     * @param event alter default sharding auditor strategy configuration event
     */
    @Subscribe
    public synchronized void renew(final AlterShardingAuditorStrategyConfigurationEvent event) {
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
     * Renew with alter default sharding column configuration.
     *
     * @param event alter default sharding column configuration event
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
     * Renew with delete default database sharding strategy configuration.
     *
     * @param event delete default database sharding strategy configuration event
     */
    @Subscribe
    public synchronized void renew(final DeleteDefaultDatabaseShardingStrategyEvent event) {
        if (!contextManager.getMetaDataContexts().getMetaData().containsDatabase(event.getDatabaseName())) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.setDefaultDatabaseShardingStrategy(null);
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with delete default table sharding strategy configuration.
     *
     * @param event delete default table sharding strategy configuration event
     */
    @Subscribe
    public synchronized void renew(final DeleteDefaultTableShardingStrategyEvent event) {
        if (!contextManager.getMetaDataContexts().getMetaData().containsDatabase(event.getDatabaseName())) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.setDefaultTableShardingStrategy(null);
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with delete default key generate strategy configuration.
     *
     * @param event delete default key generate strategy configuration event
     */
    @Subscribe
    public synchronized void renew(final DeleteDefaultKeyGenerateStrategyEvent event) {
        if (!contextManager.getMetaDataContexts().getMetaData().containsDatabase(event.getDatabaseName())) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.setDefaultKeyGenerateStrategy(null);
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with delete default sharding auditor strategy configuration.
     *
     * @param event delete default sharding auditor strategy configuration event
     */
    @Subscribe
    public synchronized void renew(final DeleteShardingAuditorStrategyConfigurationEvent event) {
        if (!contextManager.getMetaDataContexts().getMetaData().containsDatabase(event.getDatabaseName())) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.setDefaultAuditStrategy(null);
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with delete default sharding column configuration.
     *
     * @param event delete default sharding column configuration event
     */
    @Subscribe
    public synchronized void renew(final DeleteDefaultShardingColumnEvent event) {
        if (!contextManager.getMetaDataContexts().getMetaData().containsDatabase(event.getDatabaseName())) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.setDefaultAuditStrategy(null);
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
