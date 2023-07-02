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
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.algorithm.YamlAlgorithmConfigurationSwapper;
import org.apache.shardingsphere.mode.event.config.DatabaseRuleConfigurationChangedEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.subsciber.RuleChangedSubscriber;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.event.algorithm.auditor.AlterShardingAuditorEvent;
import org.apache.shardingsphere.sharding.event.algorithm.auditor.DropShardingAuditorEvent;
import org.apache.shardingsphere.sharding.event.algorithm.keygenerator.AlterKeyGeneratorEvent;
import org.apache.shardingsphere.sharding.event.algorithm.keygenerator.DropKeyGeneratorEvent;
import org.apache.shardingsphere.sharding.event.algorithm.sharding.AlterShardingAlgorithmEvent;
import org.apache.shardingsphere.sharding.event.algorithm.sharding.DropShardingAlgorithmEvent;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

/**
 * Sharding algorithm subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
@Setter
public final class ShardingAlgorithmSubscriber implements RuleChangedSubscriber {
    
    private ContextManager contextManager;
    
    /**
     * Renew with alter sharding algorithm.
     *
     * @param event alter sharding algorithm event
     */
    @Subscribe
    public synchronized void renew(final AlterShardingAlgorithmEvent event) {
        if (!event.getActiveVersion().equals(contextManager.getInstanceContext().getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingRuleConfiguration config = database.getRuleMetaData().findSingleRule(ShardingRule.class)
                .map(optional -> (ShardingRuleConfiguration) optional.getConfiguration()).orElse(new ShardingRuleConfiguration());
        String yamlContent = contextManager.getInstanceContext().getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion());
        config.getShardingAlgorithms().put(event.getItemName(), new YamlAlgorithmConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContent, YamlAlgorithmConfiguration.class)));
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with alter sharding algorithm.
     *
     * @param event alter sharding algorithm event
     */
    @Subscribe
    public synchronized void renew(final AlterKeyGeneratorEvent event) {
        if (!event.getActiveVersion().equals(contextManager.getInstanceContext().getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingRuleConfiguration config = database.getRuleMetaData().findSingleRule(ShardingRule.class)
                .map(optional -> (ShardingRuleConfiguration) optional.getConfiguration()).orElse(new ShardingRuleConfiguration());
        String yamlContent = contextManager.getInstanceContext().getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion());
        config.getKeyGenerators().put(event.getItemName(), new YamlAlgorithmConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContent, YamlAlgorithmConfiguration.class)));
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with alter sharding algorithm.
     *
     * @param event alter sharding algorithm event
     */
    @Subscribe
    public synchronized void renew(final AlterShardingAuditorEvent event) {
        if (!event.getActiveVersion().equals(contextManager.getInstanceContext().getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingRuleConfiguration config = database.getRuleMetaData().findSingleRule(ShardingRule.class)
                .map(optional -> (ShardingRuleConfiguration) optional.getConfiguration()).orElse(new ShardingRuleConfiguration());
        String yamlContent = contextManager.getInstanceContext().getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion());
        config.getAuditors().put(event.getItemName(), new YamlAlgorithmConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContent, YamlAlgorithmConfiguration.class)));
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with delete sharding algorithm.
     *
     * @param event delete sharding algorithm event
     */
    @Subscribe
    public synchronized void renew(final DropShardingAlgorithmEvent event) {
        if (!contextManager.getMetaDataContexts().getMetaData().containsDatabase(event.getDatabaseName())) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.getShardingAlgorithms().remove(event.getItemName());
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with delete key generator.
     *
     * @param event delete key generator event
     */
    @Subscribe
    public synchronized void renew(final DropKeyGeneratorEvent event) {
        if (!contextManager.getMetaDataContexts().getMetaData().containsDatabase(event.getDatabaseName())) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.getKeyGenerators().remove(event.getItemName());
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with delete key generator.
     *
     * @param event delete key generator event
     */
    @Subscribe
    public synchronized void renew(final DropShardingAuditorEvent event) {
        if (!contextManager.getMetaDataContexts().getMetaData().containsDatabase(event.getDatabaseName())) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.getAuditors().remove(event.getItemName());
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
}
