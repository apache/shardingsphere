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

package org.apache.shardingsphere.core.rewrite.feature.sharding.sql;

import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.core.rewrite.sql.SQLBuilder;
import org.apache.shardingsphere.core.rewrite.sql.token.pojo.Alterable;
import org.apache.shardingsphere.core.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.core.rewrite.sql.token.pojo.Substitutable;
import org.apache.shardingsphere.core.route.type.RoutingUnit;
import org.apache.shardingsphere.core.route.type.TableUnit;
import org.apache.shardingsphere.core.rule.BindingTableRule;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SQL builder for sharding.
 *
 * @author gaohongtao
 * @author zhangliang
 * @author maxiaoguang
 * @author panjuan
 */
@RequiredArgsConstructor
public final class ShardingSQLBuilder implements SQLBuilder {
    
    private final SQLRewriteContext context;
    
    private final ShardingRule shardingRule;
    
    private final RoutingUnit routingUnit;
    
    @Override
    public String toSQL() {
        if (context.getSqlTokens().isEmpty()) {
            return context.getSql();
        }
        Collections.sort(context.getSqlTokens());
        StringBuilder result = new StringBuilder();
        result.append(context.getSql().substring(0, context.getSqlTokens().get(0).getStartIndex()));
        for (SQLToken each : context.getSqlTokens()) {
            result.append(getSQLTokenText(each, routingUnit, getLogicAndActualTables()));
            result.append(getConjunctionText(each));
        }
        return result.toString();
    }
    
    private Map<String, String> getLogicAndActualTables() {
        Map<String, String> result = new HashMap<>();
        Collection<String> tableNames = context.getSqlStatementContext().getTablesContext().getTableNames();
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
    
    private String getSQLTokenText(final SQLToken sqlToken, final RoutingUnit routingUnit, final Map<String, String> logicAndActualTables) {
        return sqlToken instanceof Alterable ? ((Alterable) sqlToken).toString(routingUnit, logicAndActualTables) : sqlToken.toString();
    }
    
    private String getConjunctionText(final SQLToken sqlToken) {
        return context.getSql().substring(getStartIndex(sqlToken), getStopIndex(sqlToken));
    }
    
    private int getStartIndex(final SQLToken sqlToken) {
        int startIndex = sqlToken instanceof Substitutable ? ((Substitutable) sqlToken).getStopIndex() + 1 : sqlToken.getStartIndex();
        return Math.min(startIndex, context.getSql().length());
    }
    
    private int getStopIndex(final SQLToken sqlToken) {
        int currentSQLTokenIndex = context.getSqlTokens().indexOf(sqlToken);
        return context.getSqlTokens().size() - 1 == currentSQLTokenIndex ? context.getSql().length() : context.getSqlTokens().get(currentSQLTokenIndex + 1).getStartIndex();
    }
}
