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
import org.apache.shardingsphere.sharding.api.config.cache.ShardingCacheConfiguration;
import org.apache.shardingsphere.sharding.event.cache.AlterShardingCacheEvent;
import org.apache.shardingsphere.sharding.event.cache.DropShardingCacheEvent;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.yaml.config.cache.YamlShardingCacheConfiguration;
import org.apache.shardingsphere.sharding.yaml.swapper.cache.YamlShardingCacheConfigurationSwapper;

/**
 * Sharding cache subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
@Setter
public final class ShardingCacheSubscriber implements RuleChangedSubscriber {
    
    private ContextManager contextManager;
    
    /**
     * Renew with alter sharding cache.
     *
     * @param event alter sharding cache event
     */
    @Subscribe
    public synchronized void renew(final AlterShardingCacheEvent event) {
        if (!event.getActiveVersion().equals(contextManager.getInstanceContext().getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingCacheConfiguration toBeChangedConfig = swapToShardingCacheConfig(
                contextManager.getInstanceContext().getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion()));
        ShardingRuleConfiguration config = database.getRuleMetaData().findSingleRule(ShardingRule.class)
                .map(optional -> (ShardingRuleConfiguration) optional.getConfiguration()).orElse(new ShardingRuleConfiguration());
        config.setShardingCache(toBeChangedConfig);
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with drop sharding cache.
     *
     * @param event drop sharding cache event
     */
    @Subscribe
    public synchronized void renew(final DropShardingCacheEvent event) {
        if (!contextManager.getMetaDataContexts().getMetaData().containsDatabase(event.getDatabaseName())) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.setShardingCache(null);
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    private ShardingCacheConfiguration swapToShardingCacheConfig(final String yamlContext) {
        return new YamlShardingCacheConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContext, YamlShardingCacheConfiguration.class));
    }
}
