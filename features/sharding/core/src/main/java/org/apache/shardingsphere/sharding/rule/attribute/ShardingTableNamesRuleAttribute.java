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

import com.cedarsoftware.util.CaseInsensitiveSet;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rule.attribute.table.TableMapperRuleAttribute;
import org.apache.shardingsphere.sharding.rule.ShardingTable;

import java.util.Collection;

/**
 * Sharding table mapper rule attribute.
 */
public final class ShardingTableNamesRuleAttribute implements TableMapperRuleAttribute {
    
    private final Collection<String> logicalTableNames;
    
    private final Collection<String> actualTableNames;
    
    public ShardingTableNamesRuleAttribute(final Collection<ShardingTable> shardingTables) {
        logicalTableNames = createLogicalTableNames(shardingTables);
        actualTableNames = createActualTableNames(shardingTables);
    }
    
    private Collection<String> createLogicalTableNames(final Collection<ShardingTable> shardingTables) {
        Collection<String> result = new CaseInsensitiveSet<>(shardingTables.size());
        shardingTables.forEach(each -> result.add(each.getLogicTable()));
        return result;
    }
    
    private Collection<String> createActualTableNames(final Collection<ShardingTable> shardingTables) {
        Collection<String> result = new CaseInsensitiveSet<>();
        shardingTables.stream().flatMap(each -> each.getActualDataNodes().stream()).map(DataNode::getTableName).forEach(result::add);
        return result;
    }
    
    @Override
    public Collection<String> getLogicTableNames() {
        return logicalTableNames;
    }
    
    @Override
    public Collection<String> getActualTableNames() {
        return actualTableNames;
    }
    
    @Override
    public Collection<String> getDistributedTableNames() {
        return logicalTableNames;
    }
    
    @Override
    public Collection<String> getEnhancedTableNames() {
        return logicalTableNames;
    }
}
