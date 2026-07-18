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

package org.apache.shardingsphere.sharding.metadata.reviser.table;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.table.TableMetaDataRevisionContext;
import org.apache.shardingsphere.sharding.metadata.reviser.column.ShardingColumnGeneratedReviser;
import org.apache.shardingsphere.sharding.metadata.reviser.constraint.ShardingConstraintReviser;
import org.apache.shardingsphere.sharding.metadata.reviser.index.ShardingIndexReviser;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;

import java.util.Optional;

/**
 * Sharding table meta data revision context.
 */
@RequiredArgsConstructor
public final class ShardingTableMetaDataRevisionContext implements TableMetaDataRevisionContext<ShardingRule> {
    
    private final ShardingTable shardingTable;
    
    @Override
    public String reviseTableName(final String originalName) {
        return shardingTable.getLogicTable();
    }
    
    @Override
    public Optional<ShardingColumnGeneratedReviser> getColumnGeneratedReviser() {
        return Optional.of(new ShardingColumnGeneratedReviser(shardingTable));
    }
    
    @Override
    public Optional<ShardingIndexReviser> getIndexReviser() {
        return Optional.of(new ShardingIndexReviser(shardingTable));
    }
    
    @Override
    public Optional<ShardingConstraintReviser> getConstraintReviser() {
        return Optional.of(new ShardingConstraintReviser(shardingTable));
    }
}
