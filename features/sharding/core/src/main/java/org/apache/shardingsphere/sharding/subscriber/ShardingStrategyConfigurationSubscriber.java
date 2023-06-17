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
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.RuleConfigurationSubscribeCoordinator;
import org.apache.shardingsphere.mode.event.config.DatabaseRuleConfigurationChangedEvent;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.event.strategy.audit.AddShardingAuditorStrategyConfigurationEvent;
import org.apache.shardingsphere.sharding.event.strategy.audit.AlterShardingAuditorStrategyConfigurationEvent;
import org.apache.shardingsphere.sharding.event.strategy.audit.DeleteShardingAuditorStrategyConfigurationEvent;
import org.apache.shardingsphere.sharding.event.strategy.database.AddDatabaseShardingStrategyConfigurationEvent;
import org.apache.shardingsphere.sharding.event.strategy.database.AlterDatabaseShardingStrategyConfigurationEvent;
import org.apache.shardingsphere.sharding.event.strategy.database.DeleteDatabaseShardingStrategyConfigurationEvent;
import org.apache.shardingsphere.sharding.event.strategy.keygenerate.AddKeyGenerateStrategyConfigurationEvent;
import org.apache.shardingsphere.sharding.event.strategy.keygenerate.AlterKeyGenerateStrategyConfigurationEvent;
import org.apache.shardingsphere.sharding.event.strategy.keygenerate.DeleteKeyGenerateStrategyConfigurationEvent;
import org.apache.shardingsphere.sharding.event.strategy.shardingcolumn.AddDefaultShardingColumnEvent;
import org.apache.shardingsphere.sharding.event.strategy.shardingcolumn.AlterDefaultShardingColumnEvent;
import org.apache.shardingsphere.sharding.event.strategy.shardingcolumn.DeleteDefaultShardingColumnEvent;
import org.apache.shardingsphere.sharding.event.strategy.table.AddTableShardingStrategyConfigurationEvent;
import org.apache.shardingsphere.sharding.event.strategy.table.AlterTableShardingStrategyConfigurationEvent;
import org.apache.shardingsphere.sharding.event.strategy.table.DeleteTableShardingStrategyConfigurationEvent;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Map;
import java.util.Optional;

/**
 * Sharding strategy configuration subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
@RequiredArgsConstructor
public final class ShardingStrategyConfigurationSubscriber implements RuleConfigurationSubscribeCoordinator {
    
    private Map<String, ShardingSphereDatabase> databases;
    
    private InstanceContext instanceContext;
    
    @Override
    public void registerRuleConfigurationSubscriber(final Map<String, ShardingSphereDatabase> databases, final InstanceContext instanceContext) {
        this.databases = databases;
        this.instanceContext = instanceContext;
        instanceContext.getEventBusContext().register(this);
    }
    
    /**
     * Renew with add default database sharding strategy configuration.
     *
     * @param event add default database sharding strategy configuration event
     */
    @Subscribe
    public synchronized void renew(final AddDatabaseShardingStrategyConfigurationEvent event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        ShardingStrategyConfiguration needToAddedConfig = event.getConfig();
        Optional<ShardingRule> rule = database.getRuleMetaData().findSingleRule(ShardingRule.class);
        ShardingRuleConfiguration config;
        if (rule.isPresent()) {
            config = (ShardingRuleConfiguration) rule.get().getConfiguration();
            config.setDefaultDatabaseShardingStrategy(needToAddedConfig);
        } else {
            config = new ShardingRuleConfiguration();
            config.setDefaultDatabaseShardingStrategy(needToAddedConfig);
        }
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with add default table sharding strategy configuration.
     *
     * @param event add default table sharding strategy configuration event
     */
    @Subscribe
    public synchronized void renew(final AddTableShardingStrategyConfigurationEvent event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        ShardingStrategyConfiguration needToAddedConfig = event.getConfig();
        Optional<ShardingRule> rule = database.getRuleMetaData().findSingleRule(ShardingRule.class);
        ShardingRuleConfiguration config;
        if (rule.isPresent()) {
            config = (ShardingRuleConfiguration) rule.get().getConfiguration();
            config.setDefaultTableShardingStrategy(needToAddedConfig);
        } else {
            config = new ShardingRuleConfiguration();
            config.setDefaultTableShardingStrategy(needToAddedConfig);
        }
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with add default key generate strategy configuration.
     *
     * @param event add default key generate strategy configuration event
     */
    @Subscribe
    public synchronized void renew(final AddKeyGenerateStrategyConfigurationEvent event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        KeyGenerateStrategyConfiguration needToAddedConfig = event.getConfig();
        Optional<ShardingRule> rule = database.getRuleMetaData().findSingleRule(ShardingRule.class);
        ShardingRuleConfiguration config;
        if (rule.isPresent()) {
            config = (ShardingRuleConfiguration) rule.get().getConfiguration();
            config.setDefaultKeyGenerateStrategy(needToAddedConfig);
        } else {
            config = new ShardingRuleConfiguration();
            config.setDefaultKeyGenerateStrategy(needToAddedConfig);
        }
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with add default sharding auditor strategy configuration.
     *
     * @param event add default sharding auditor strategy configuration event
     */
    @Subscribe
    public synchronized void renew(final AddShardingAuditorStrategyConfigurationEvent event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        ShardingAuditStrategyConfiguration needToAddedConfig = event.getConfig();
        Optional<ShardingRule> rule = database.getRuleMetaData().findSingleRule(ShardingRule.class);
        ShardingRuleConfiguration config;
        if (rule.isPresent()) {
            config = (ShardingRuleConfiguration) rule.get().getConfiguration();
            config.setDefaultAuditStrategy(needToAddedConfig);
        } else {
            config = new ShardingRuleConfiguration();
            config.setDefaultAuditStrategy(needToAddedConfig);
        }
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with add default sharding column configuration.
     *
     * @param event add default sharding column configuration event
     */
    @Subscribe
    public synchronized void renew(final AddDefaultShardingColumnEvent event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        String needToAddedConfig = event.getConfig();
        Optional<ShardingRule> rule = database.getRuleMetaData().findSingleRule(ShardingRule.class);
        ShardingRuleConfiguration config;
        if (rule.isPresent()) {
            config = (ShardingRuleConfiguration) rule.get().getConfiguration();
            config.setDefaultShardingColumn(needToAddedConfig);
        } else {
            config = new ShardingRuleConfiguration();
            config.setDefaultShardingColumn(needToAddedConfig);
        }
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with alter default database sharding strategy configuration.
     *
     * @param event alter default database sharding strategy configuration event
     */
    @Subscribe
    public synchronized void renew(final AlterDatabaseShardingStrategyConfigurationEvent event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        ShardingStrategyConfiguration needToAlteredConfig = event.getConfig();
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.setDefaultDatabaseShardingStrategy(needToAlteredConfig);
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with alter default table sharding strategy configuration.
     *
     * @param event alter default table sharding strategy configuration event
     */
    @Subscribe
    public synchronized void renew(final AlterTableShardingStrategyConfigurationEvent event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        ShardingStrategyConfiguration needToAlteredConfig = event.getConfig();
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.setDefaultTableShardingStrategy(needToAlteredConfig);
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with alter default key generate strategy configuration.
     *
     * @param event alter default key generate strategy configuration event
     */
    @Subscribe
    public synchronized void renew(final AlterKeyGenerateStrategyConfigurationEvent event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        KeyGenerateStrategyConfiguration needToAlteredConfig = event.getConfig();
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.setDefaultKeyGenerateStrategy(needToAlteredConfig);
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with alter default sharding auditor strategy configuration.
     *
     * @param event alter default sharding auditor strategy configuration event
     */
    @Subscribe
    public synchronized void renew(final AlterShardingAuditorStrategyConfigurationEvent event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        ShardingAuditStrategyConfiguration needToAlteredConfig = event.getConfig();
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.setDefaultAuditStrategy(needToAlteredConfig);
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with alter default sharding column configuration.
     *
     * @param event alter default sharding column configuration event
     */
    @Subscribe
    public synchronized void renew(final AlterDefaultShardingColumnEvent event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        String needToAlteredConfig = event.getConfig();
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.setDefaultShardingColumn(needToAlteredConfig);
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with delete default database sharding strategy configuration.
     *
     * @param event delete default database sharding strategy configuration event
     */
    @Subscribe
    public synchronized void renew(final DeleteDatabaseShardingStrategyConfigurationEvent event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.setDefaultDatabaseShardingStrategy(null);
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with delete default table sharding strategy configuration.
     *
     * @param event delete default table sharding strategy configuration event
     */
    @Subscribe
    public synchronized void renew(final DeleteTableShardingStrategyConfigurationEvent event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.setDefaultTableShardingStrategy(null);
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with delete default key generate strategy configuration.
     *
     * @param event delete default key generate strategy configuration event
     */
    @Subscribe
    public synchronized void renew(final DeleteKeyGenerateStrategyConfigurationEvent event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.setDefaultKeyGenerateStrategy(null);
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with delete default sharding auditor strategy configuration.
     *
     * @param event delete default sharding auditor strategy configuration event
     */
    @Subscribe
    public synchronized void renew(final DeleteShardingAuditorStrategyConfigurationEvent event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.setDefaultAuditStrategy(null);
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with delete default sharding column configuration.
     *
     * @param event delete default sharding column configuration event
     */
    @Subscribe
    public synchronized void renew(final DeleteDefaultShardingColumnEvent event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.setDefaultAuditStrategy(null);
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
}
