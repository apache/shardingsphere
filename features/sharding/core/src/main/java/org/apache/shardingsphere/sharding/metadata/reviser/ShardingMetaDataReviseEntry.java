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
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.reviser.schema.SchemaTableAggregationReviser;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.spi.MetaDataReviseEntry;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Optional;

/**
 * Sharding meta data revise entry.
 */
public final class ShardingMetaDataReviseEntry implements MetaDataReviseEntry<ShardingRule> {
    
    @Override
    public Optional<? extends SchemaTableAggregationReviser<ShardingRule>> getSchemaTableAggregationReviser(final ShardingRule rule, final ConfigurationProperties props) {
        return MetaDataReviseEntry.super.getSchemaTableAggregationReviser(rule, props);
    }
    
    @Override
    public Optional<ShardingTableNameReviser> getTableNameReviser() {
        return Optional.of(new ShardingTableNameReviser());
    }
    
    @Override
    public Optional<ShardingColumnGeneratedReviser> getColumnGeneratedReviser(final ShardingRule rule, final String tableName) {
        return rule.findTableRuleByActualTable(tableName).map(ShardingColumnGeneratedReviser::new);
    }
    
    @Override
    public Optional<ShardingIndexReviser> getIndexReviser(final ShardingRule rule, final String tableName) {
        return rule.findTableRuleByActualTable(tableName).map(ShardingIndexReviser::new);
    }
    
    @Override
    public Optional<ShardingConstraintReviser> getConstraintReviser(final ShardingRule rule, final String tableName) {
        return rule.findTableRuleByActualTable(tableName).map(ShardingConstraintReviser::new);
    }
    
    @Override
    public String getType() {
        return ShardingRule.class.getSimpleName();
    }
}
