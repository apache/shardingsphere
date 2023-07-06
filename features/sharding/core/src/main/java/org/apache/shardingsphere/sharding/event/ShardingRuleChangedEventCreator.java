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

package org.apache.shardingsphere.sharding.event;

import org.apache.shardingsphere.infra.rule.event.GovernanceEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.NamedRuleItemChangedEventCreator;
import org.apache.shardingsphere.mode.event.UniqueRuleItemChangedEventCreator;
import org.apache.shardingsphere.mode.spi.RuleChangedEventCreator;
import org.apache.shardingsphere.sharding.metadata.nodepath.ShardingRuleNodePathProvider;
import org.apache.shardingsphere.sharding.subscriber.DefaultDatabaseShardingStrategyChangedGenerator;
import org.apache.shardingsphere.sharding.subscriber.DefaultKeyGenerateStrategyChangedGenerator;
import org.apache.shardingsphere.sharding.subscriber.DefaultShardingAuditorStrategyChangedGenerator;
import org.apache.shardingsphere.sharding.subscriber.DefaultShardingColumnChangedGenerator;
import org.apache.shardingsphere.sharding.subscriber.DefaultTableShardingStrategyChangedGenerator;
import org.apache.shardingsphere.sharding.subscriber.ShardingAlgorithmChangedGenerator;
import org.apache.shardingsphere.sharding.subscriber.ShardingAuditorChangedGenerator;
import org.apache.shardingsphere.sharding.subscriber.ShardingAutoTableChangedGenerator;
import org.apache.shardingsphere.sharding.subscriber.ShardingCacheChangedGenerator;
import org.apache.shardingsphere.sharding.subscriber.ShardingTableChangedGenerator;
import org.apache.shardingsphere.sharding.subscriber.ShardingTableReferenceChangedGenerator;

/**
 * Sharding rule changed event creator.
 */
public final class ShardingRuleChangedEventCreator implements RuleChangedEventCreator {
    
    @Override
    public GovernanceEvent create(final String databaseName, final DataChangedEvent event, final String itemType, final String itemName) {
        return new NamedRuleItemChangedEventCreator().create(databaseName, itemName, event, getNamedRuleItemConfigurationChangedGeneratorType(itemType));
    }
    
    @Override
    public GovernanceEvent create(final String databaseName, final DataChangedEvent event, final String itemType) {
        return new UniqueRuleItemChangedEventCreator().create(databaseName, event, getUniqueRuleItemConfigurationChangedGeneratorType(itemType));
    }
    
    private String getNamedRuleItemConfigurationChangedGeneratorType(final String itemType) {
        switch (itemType) {
            case ShardingRuleNodePathProvider.TABLES:
                return ShardingTableChangedGenerator.TYPE;
            case ShardingRuleNodePathProvider.AUTO_TABLES:
                return ShardingAutoTableChangedGenerator.TYPE;
            case ShardingRuleNodePathProvider.BINDING_TABLES:
                return ShardingTableReferenceChangedGenerator.TYPE;
            case ShardingRuleNodePathProvider.ALGORITHMS:
                return ShardingAlgorithmChangedGenerator.TYPE;
            case ShardingRuleNodePathProvider.KEY_GENERATORS:
                return DefaultKeyGenerateStrategyChangedGenerator.TYPE;
            case ShardingRuleNodePathProvider.AUDITORS:
                return ShardingAuditorChangedGenerator.TYPE;
            default:
                throw new UnsupportedOperationException(itemType);
        }
    }
    
    private String getUniqueRuleItemConfigurationChangedGeneratorType(final String itemType) {
        switch (itemType) {
            case ShardingRuleNodePathProvider.DEFAULT_DATABASE_STRATEGY:
                return DefaultDatabaseShardingStrategyChangedGenerator.TYPE;
            case ShardingRuleNodePathProvider.DEFAULT_TABLE_STRATEGY:
                return DefaultTableShardingStrategyChangedGenerator.TYPE;
            case ShardingRuleNodePathProvider.DEFAULT_KEY_GENERATE_STRATEGY:
                return DefaultKeyGenerateStrategyChangedGenerator.TYPE;
            case ShardingRuleNodePathProvider.DEFAULT_AUDIT_STRATEGY:
                return DefaultShardingAuditorStrategyChangedGenerator.TYPE;
            case ShardingRuleNodePathProvider.DEFAULT_SHARDING_COLUMN:
                return DefaultShardingColumnChangedGenerator.TYPE;
            case ShardingRuleNodePathProvider.SHARDING_CACHE:
                return ShardingCacheChangedGenerator.TYPE;
            default:
                throw new UnsupportedOperationException(itemType);
        }
    }
    
    @Override
    public String getType() {
        return "sharding";
    }
}
