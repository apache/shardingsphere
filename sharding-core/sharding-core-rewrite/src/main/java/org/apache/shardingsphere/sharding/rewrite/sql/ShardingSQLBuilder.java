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

package org.apache.shardingsphere.sharding.rewrite.sql;

import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.LogicAndActualTablesAware;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.RouteUnitAware;
import org.apache.shardingsphere.underlying.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.underlying.rewrite.sql.impl.AbstractSQLBuilder;
import org.apache.shardingsphere.underlying.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.underlying.route.context.RouteMapper;
import org.apache.shardingsphere.underlying.route.context.RouteUnit;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * SQL builder for sharding.
 */
public final class ShardingSQLBuilder extends AbstractSQLBuilder {
    
    private final ShardingRule shardingRule;
    
    private final RouteUnit routeUnit;
    
    public ShardingSQLBuilder(final SQLRewriteContext context, final ShardingRule shardingRule, final RouteUnit routeUnit) {
        super(context);
        this.shardingRule = shardingRule;
        this.routeUnit = routeUnit;
    }
    
    @Override
    protected String getSQLTokenText(final SQLToken sqlToken) {
        if (sqlToken instanceof RouteUnitAware) {
            return ((RouteUnitAware) sqlToken).toString(routeUnit);
        }
        if (sqlToken instanceof LogicAndActualTablesAware) {
            return ((LogicAndActualTablesAware) sqlToken).toString(getLogicAndActualTables());
        }
        return sqlToken.toString();
    }
    
    private Map<String, String> getLogicAndActualTables() {
        Collection<String> tableNames = getContext().getSqlStatementContext().getTablesContext().getTableNames();
        Map<String, String> result = new HashMap<>(tableNames.size(), 1);
        for (RouteMapper each : routeUnit.getTableMappers()) {
            result.put(each.getLogicName().toLowerCase(), each.getActualName());
            result.putAll(shardingRule.getLogicAndActualTablesFromBindingTable(routeUnit.getDataSourceMapper().getLogicName(), each.getLogicName(), each.getActualName(), tableNames));
        }
        return result;
    }
}
