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

package org.apache.shardingsphere.sharding.rule.attribute;

import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rule.attribute.table.TableMapperRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.table.TableNamesMapper;
import org.apache.shardingsphere.sharding.rule.ShardingTable;

import java.util.Collection;

/**
 * Sharding table mapper rule attribute.
 */
public final class ShardingTableMapperRuleAttribute implements TableMapperRuleAttribute {
    
    private final TableNamesMapper logicalTableMapper;
    
    private final TableNamesMapper actualTableMapper;
    
    public ShardingTableMapperRuleAttribute(final Collection<ShardingTable> shardingTables) {
        logicalTableMapper = createLogicalTableMapper(shardingTables);
        actualTableMapper = createActualTableMapper(shardingTables);
    }
    
    private TableNamesMapper createLogicalTableMapper(final Collection<ShardingTable> shardingTables) {
        TableNamesMapper result = new TableNamesMapper();
        shardingTables.forEach(each -> result.put(each.getLogicTable()));
        return result;
    }
    
    private TableNamesMapper createActualTableMapper(final Collection<ShardingTable> shardingTables) {
        TableNamesMapper result = new TableNamesMapper();
        shardingTables.stream().flatMap(each -> each.getActualDataNodes().stream()).map(DataNode::getTableName).forEach(result::put);
        return result;
    }
    
    @Override
    public TableNamesMapper getLogicTableMapper() {
        return logicalTableMapper;
    }
    
    @Override
    public TableNamesMapper getActualTableMapper() {
        return actualTableMapper;
    }
    
    @Override
    public TableNamesMapper getDistributedTableMapper() {
        return logicalTableMapper;
    }
    
    @Override
    public TableNamesMapper getEnhancedTableMapper() {
        return logicalTableMapper;
    }
}
