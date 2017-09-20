/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.rule;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;

/**
 * Binding table rule configuration.
 * 
 * <p>Binding table is same sharding rule with different tables, use one of them can deduce other name of actual tables and data sources.</p>
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public final class BindingTableRule {
    
    private final List<TableRule> tableRules;
    
    /**
     * Adjust contains this logic table in this rule.
     * 
     * @param logicTableName logic table name
     * @return contains this logic table or not
     */
    public boolean hasLogicTable(final String logicTableName) {
        for (TableRule each : tableRules) {
            if (each.getLogicTable().equalsIgnoreCase(logicTableName)) {
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
        Preconditions.checkState(-1 != index, String.format("Actual table [%s].[%s] is not in table config", dataSource, otherActualTable));
        for (TableRule each : tableRules) {
            if (each.getLogicTable().equalsIgnoreCase(logicTable)) {
                return each.getActualDataNodes().get(index).getTableName();
            }
        }
        throw new IllegalStateException(String.format("Cannot find binding actual table, data source: %s, logic table: %s, other actual table: %s", dataSource, logicTable, otherActualTable));
    }
    
    Collection<String> getAllLogicTables() {
        return Lists.transform(tableRules, new Function<TableRule, String>() {
            
            @Override
            public String apply(final TableRule input) {
                return input.getLogicTable();
            }
        });
    }
}
