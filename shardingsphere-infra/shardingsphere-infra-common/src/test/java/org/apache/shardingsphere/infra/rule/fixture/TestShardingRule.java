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
import org.apache.shardingsphere.infra.rule.type.DataNodeBasedRule;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
public final class TestShardingRule implements DataNodeBasedRule {
    
    private final Collection<TestTableRule> tableRules;
    
    @Override
    public Map<String, Collection<DataNode>> getAllDataNodes() {
        return tableRules.stream().collect(Collectors.toMap(TestTableRule::getLogicTable, TestTableRule::getActualDataNodes, (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    @Override
    public Collection<String> getAllActualTables() {
        return tableRules.stream().flatMap(each -> each.getActualDataNodes().stream().map(DataNode::getTableName)).collect(Collectors.toSet());
    }
    
    @Override
    public Optional<String> findFirstActualTable(final String logicTable) {
        return findTableRule(logicTable).map(tableRule -> tableRule.getActualDataNodes().get(0).getTableName());
    }

    private Optional<TestTableRule> findTableRule(final String logicTableName) {
        return tableRules.stream().filter(each -> each.getLogicTable().equalsIgnoreCase(logicTableName)).findFirst();
    }
    
    @Override
    public boolean isNeedAccumulate(final Collection<String> tables) {
        return false;
    }
    
    @Override
    public Optional<String> findLogicTableByActualTable(final String actualTable) {
        return Optional.empty();
    }
}
