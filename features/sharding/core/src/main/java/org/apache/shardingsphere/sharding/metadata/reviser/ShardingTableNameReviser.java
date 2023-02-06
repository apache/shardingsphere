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

import org.apache.shardingsphere.infra.metadata.database.schema.decorator.reviser.table.TableNameReviser;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;

import java.util.Optional;

/**
 * Sharding table name reviser.
 */
public final class ShardingTableNameReviser implements TableNameReviser<ShardingRule, TableRule> {
    
    @Override
    public String revise(final String originalName, final TableRule tableRule) {
        return tableRule.getLogicTable();
    }
    
    @Override
    public Optional<TableRule> findTableRule(final String name, final ShardingRule rule) {
        return rule.findTableRuleByActualTable(name);
    }
}
