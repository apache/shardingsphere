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

package org.apache.shardingsphere.infra.rule.fixture;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rule.DataNodeRoutedRule;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
/**
 * TestShardingRule.
 */
public final class TestShardingRule implements DataNodeRoutedRule {
    
    private final Collection<TestTableRule> tableRules;
    
    /**
     * Get all data nodes.
     *
     * @return all data nodes map, key is logic table name, values are data node collection belong to the key
     */
    @Override
    public Map<String, Collection<DataNode>> getAllDataNodes() {
        return tableRules.stream().collect(Collectors.toMap(TestTableRule::getLogicTable, TestTableRule::getActualDataNodes));
    }
    
    /**
     * Get all actual tables.
     *
     * @return all actual tables
     */
    @Override
    public Collection<String> getAllActualTables() {
        return tableRules.stream().flatMap(each -> each.getActualDataNodes().stream().map(DataNode::getTableName)).collect(Collectors.toSet());
    }
    
    /**
     * Find first actual table name.
     *
     * @param logicTable logic table name
     * @return the first actual table name
     */
    @Override
    public Optional<String> findFirstActualTable(final String logicTable) {
        return findTableRule(logicTable).map(tableRule -> tableRule.getActualDataNodes().get(0).getTableName());
    }
    
    /**
     * Is need accumulate.
     *
     * @param tables table names
     * @return need accumulate
     */
    @Override
    public boolean isNeedAccumulate(final Collection<String> tables) {
        return false;
    }
    
    /**
     * Find logic table name via actual table name.
     *
     * @param actualTable actual table name
     * @return logic table name
     */
    @Override
    public Optional<String> findLogicTableByActualTable(final String actualTable) {
        return Optional.empty();
    }
    
    /**
     * Find table rule.
     *
     * @param logicTableName logic table name
     * @return table rule
     */
    public Optional<TestTableRule> findTableRule(final String logicTableName) {
        return tableRules.stream().filter(each -> each.getLogicTable().equalsIgnoreCase(logicTableName)).findFirst();
    }
    
}
