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

package org.apache.shardingsphere.sql.rewriter.sharding.sql;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.route.type.RoutingUnit;
import org.apache.shardingsphere.core.route.type.TableUnit;
import org.apache.shardingsphere.core.rule.BindingTableRule;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.sql.rewriter.context.SQLRewriteContext;
import org.apache.shardingsphere.sql.rewriter.sharding.token.pojo.LogicAndActualTablesAware;
import org.apache.shardingsphere.sql.rewriter.sharding.token.pojo.RoutingUnitAware;
import org.apache.shardingsphere.sql.rewriter.sql.impl.AbstractSQLBuilder;
import org.apache.shardingsphere.sql.rewriter.sql.token.pojo.SQLToken;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SQL builder for sharding.
 *
 * @author zhangliang
 */
public final class ShardingSQLBuilder extends AbstractSQLBuilder {
    
    private final ShardingRule shardingRule;
    
    private final RoutingUnit routingUnit;
    
    public ShardingSQLBuilder(final SQLRewriteContext context, final ShardingRule shardingRule, final RoutingUnit routingUnit) {
        super(context);
        this.shardingRule = shardingRule;
        this.routingUnit = routingUnit;
    }
    
    @Override
    protected String getSQLTokenText(final SQLToken sqlToken) {
        if (sqlToken instanceof RoutingUnitAware) {
            return ((RoutingUnitAware) sqlToken).toString(routingUnit);
        }
        if (sqlToken instanceof LogicAndActualTablesAware) {
            return ((LogicAndActualTablesAware) sqlToken).toString(getLogicAndActualTables());
        }
        return sqlToken.toString();
    }
    
    private Map<String, String> getLogicAndActualTables() {
        Map<String, String> result = new HashMap<>();
        Collection<String> tableNames = getContext().getSqlStatementContext().getTablesContext().getTableNames();
        for (TableUnit each : routingUnit.getTableUnits()) {
            String logicTableName = each.getLogicTableName().toLowerCase();
            result.put(logicTableName, each.getActualTableName());
            result.putAll(getLogicAndActualTablesFromBindingTable(routingUnit.getMasterSlaveLogicDataSourceName(), each, tableNames));
        }
        return result;
    }
    
    private Map<String, String> getLogicAndActualTablesFromBindingTable(final String dataSourceName, final TableUnit tableUnit, final Collection<String> tableNames) {
        Map<String, String> result = new LinkedHashMap<>();
        Optional<BindingTableRule> bindingTableRule = shardingRule.findBindingTableRule(tableUnit.getLogicTableName());
        if (bindingTableRule.isPresent()) {
            result.putAll(getLogicAndActualTablesFromBindingTable(dataSourceName, tableUnit, tableNames, bindingTableRule.get()));
        }
        return result;
    }
    
    private Map<String, String> getLogicAndActualTablesFromBindingTable(
            final String dataSourceName, final TableUnit tableUnit, final Collection<String> parsedTableNames, final BindingTableRule bindingTableRule) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String each : parsedTableNames) {
            String tableName = each.toLowerCase();
            if (!tableName.equals(tableUnit.getLogicTableName().toLowerCase()) && bindingTableRule.hasLogicTable(tableName)) {
                result.put(tableName, bindingTableRule.getBindingActualTable(dataSourceName, tableName, tableUnit.getActualTableName()));
            }
        }
        return result;
    }
}
