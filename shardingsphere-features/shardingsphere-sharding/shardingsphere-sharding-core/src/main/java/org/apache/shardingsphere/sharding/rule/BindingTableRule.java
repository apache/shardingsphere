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

package org.apache.shardingsphere.sharding.rule;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sharding.exception.ActualTableNotFoundException;
import org.apache.shardingsphere.sharding.exception.BindingTableNotFoundException;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Binding table rule.
 * 
 * <p>Binding table is same sharding rule with different tables, use one of them can deduce other name of actual tables and data sources.</p>
 */
@RequiredArgsConstructor
@Getter
public final class BindingTableRule {
    
    private final Map<String, TableRule> tableRules = new LinkedHashMap<>();
    
    /**
     * Judge contains this logic table in this rule.
     * 
     * @param logicTable logic table name
     * @return contains this logic table or not
     */
    public boolean hasLogicTable(final String logicTable) {
        return tableRules.containsKey(logicTable.toLowerCase());
    }
    
    /**
     * Deduce actual table name from other actual table name in same binding table rule.
     * 
     * @param dataSource data source name
     * @param logicTable logic table name
     * @param otherLogicTable other logic table name in same binding table rule
     * @param otherActualTable other actual table name in same binding table rule
     * @return actual table name
     */
    public String getBindingActualTable(final String dataSource, final String logicTable, final String otherLogicTable, final String otherActualTable) {
        Optional<TableRule> otherLogicTableRule = Optional.ofNullable(tableRules.get(otherLogicTable.toLowerCase()));
        int index = otherLogicTableRule.map(optional -> optional.findActualTableIndex(dataSource, otherActualTable)).orElse(-1);
        if (-1 == index) {
            throw new ActualTableNotFoundException(dataSource, otherActualTable);
        }
        Optional<TableRule> tableRule = Optional.ofNullable(tableRules.get(logicTable.toLowerCase()));
        if (tableRule.isPresent()) {
            return tableRule.get().getActualDataNodes().get(index).getTableName();
        }
        throw new BindingTableNotFoundException(dataSource, logicTable, otherActualTable);
    }
    
    /**
     * Get list of logical table.
     *
     * @return logical tables.
     */
    public Collection<String> getAllLogicTables() {
        return tableRules.keySet();
    }
    
    Map<String, String> getLogicAndActualTables(final String dataSource, final String logicTable, final String actualTable, final Collection<String> availableLogicBindingTables) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String each : availableLogicBindingTables) {
            String availableLogicTable = each.toLowerCase();
            if (!availableLogicTable.equalsIgnoreCase(logicTable) && hasLogicTable(availableLogicTable)) {
                result.put(availableLogicTable, getBindingActualTable(dataSource, availableLogicTable, logicTable, actualTable));
            }
        }
        return result;
    }
}
