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

package org.apache.shardingsphere.sharding.metadata.reviser;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.MetaDataReviseEntry;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.metadata.reviser.column.ShardingColumnGeneratedReviser;
import org.apache.shardingsphere.sharding.metadata.reviser.constraint.ShardingConstraintReviser;
import org.apache.shardingsphere.sharding.metadata.reviser.index.ShardingIndexReviser;
import org.apache.shardingsphere.sharding.metadata.reviser.schema.ShardingSchemaTableAggregationReviser;
import org.apache.shardingsphere.sharding.metadata.reviser.table.ShardingTableNameReviser;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Optional;

/**
 * Sharding meta data revise entry.
 */
public final class ShardingMetaDataReviseEntry implements MetaDataReviseEntry<ShardingRule> {
    
    @Override
    public Optional<ShardingSchemaTableAggregationReviser> getSchemaTableAggregationReviser(final ConfigurationProperties props) {
        return Optional.of(new ShardingSchemaTableAggregationReviser(props.getValue(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED)));
    }
    
    @Override
    public Optional<ShardingTableNameReviser> getTableNameReviser() {
        return Optional.of(new ShardingTableNameReviser());
    }
    
    @Override
    public Optional<ShardingColumnGeneratedReviser> getColumnGeneratedReviser(final ShardingRule rule, final String tableName) {
        return rule.findShardingTableByActualTable(tableName).map(ShardingColumnGeneratedReviser::new);
    }
    
    @Override
    public Optional<ShardingIndexReviser> getIndexReviser(final ShardingRule rule, final String tableName) {
        return rule.findShardingTableByActualTable(tableName).map(ShardingIndexReviser::new);
    }
    
    @Override
    public Optional<ShardingConstraintReviser> getConstraintReviser(final ShardingRule rule, final String tableName) {
        return rule.findShardingTableByActualTable(tableName).map(ShardingConstraintReviser::new);
    }
    
    @Override
    public int getOrder() {
        return ShardingOrder.ORDER;
    }
    
    @Override
    public Class<ShardingRule> getTypeClass() {
        return ShardingRule.class;
    }
}
