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
import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Binding table rule.
 * 
 * <p>Binding table is same sharding rule with different tables, use one of them can deduce other name of actual tables and data sources.</p>
 */
@RequiredArgsConstructor
@Getter
public final class BindingTableRule {
    
    private final List<TableRule> tableRules;
    
    /**
     * Judge contains this logic table in this rule.
     * 
     * @param logicTable logic table name
     * @return contains this logic table or not
     */
    public boolean hasLogicTable(final String logicTable) {
        for (TableRule each : tableRules) {
            if (each.getLogicTable().equals(logicTable.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Deduce actual table name from other actual table name in same binding table rule.
     * 
     * @param dataSource data source name
     * @param logicTable logic table name
     * @param otherActualTable other actual table name in same binding table rule
     * @return actual table name
     */
    public String getBindingActualTable(final String dataSource, final String logicTable, final String otherActualTable) {
        int index = -1;
        for (TableRule each : tableRules) {
            index = each.findActualTableIndex(dataSource, otherActualTable);
            if (-1 != index) {
                break;
            }
        }
        if (-1 == index) {
            throw new ShardingSphereConfigurationException("Actual table [%s].[%s] is not in table config", dataSource, otherActualTable);
        }
        for (TableRule each : tableRules) {
            if (each.getLogicTable().equals(logicTable.toLowerCase())) {
                return each.getActualDataNodes().get(index).getTableName().toLowerCase();
            }
        }
        throw new ShardingSphereConfigurationException("Cannot find binding actual table, data source: %s, logic table: %s, other actual table: %s", dataSource, logicTable, otherActualTable);
    }
    
    Collection<String> getAllLogicTables() {
        return tableRules.stream().map(input -> input.getLogicTable().toLowerCase()).collect(Collectors.toList());
    }
    
    Map<String, String> getLogicAndActualTables(final String dataSource, final String logicTable, final String actualTable, final Collection<String> availableLogicBindingTables) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String each : availableLogicBindingTables) {
            String availableLogicTable = each.toLowerCase();
            if (!availableLogicTable.equalsIgnoreCase(logicTable) && hasLogicTable(availableLogicTable)) {
                result.put(availableLogicTable, getBindingActualTable(dataSource, availableLogicTable, actualTable));
            }
        }
        return result;
    }
}
