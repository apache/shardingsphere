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

package org.apache.shardingsphere.sharding.rewrite.token.pojo;

import lombok.Getter;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.RouteUnitAware;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.Substitutable;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Index token.
 */
public final class IndexToken extends SQLToken implements Substitutable, RouteUnitAware {
    
    @Getter
    private final int stopIndex;
    
    private final IdentifierValue identifier;
    
    private final SQLStatementContext sqlStatementContext;
    
    private final ShardingRule shardingRule;
    
    public IndexToken(final int startIndex, final int stopIndex, final IdentifierValue identifier, final SQLStatementContext sqlStatementContext, final ShardingRule shardingRule) {
        super(startIndex);
        this.stopIndex = stopIndex;
        this.identifier = identifier;
        this.sqlStatementContext = sqlStatementContext;
        this.shardingRule = shardingRule;
    }
    
    @Override
    public String toString(final RouteUnit routeUnit) {
        StringBuilder result = new StringBuilder();
        result.append(identifier.getQuoteCharacter().getStartDelimiter()).append(identifier.getValue());
        Map<String, String> logicAndActualTables = getLogicAndActualTables(routeUnit);
        if (!logicAndActualTables.isEmpty()) {
            result.append("_").append(logicAndActualTables.values().iterator().next());
        }
        result.append(identifier.getQuoteCharacter().getEndDelimiter());
        return result.toString();
    }
    
    private Map<String, String> getLogicAndActualTables(final RouteUnit routeUnit) {
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        Map<String, String> result = new HashMap<>(tableNames.size(), 1);
        for (RouteMapper each : routeUnit.getTableMappers()) {
            result.put(each.getLogicName().toLowerCase(), each.getActualName());
            result.putAll(shardingRule.getLogicAndActualTablesFromBindingTable(routeUnit.getDataSourceMapper().getLogicName(), each.getLogicName(), each.getActualName(), tableNames));
        }
        return result;
    }
}
