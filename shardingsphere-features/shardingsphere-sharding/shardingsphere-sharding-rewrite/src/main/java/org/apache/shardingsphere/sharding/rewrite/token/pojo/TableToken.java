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

import com.google.common.base.Joiner;
import lombok.Getter;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.RouteUnitAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.Substitutable;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.SimpleTableSegment;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Table token.
 */
public final class TableToken extends SQLToken implements Substitutable, RouteUnitAware {
    
    @Getter
    private final int stopIndex;
    
    private final SimpleTableSegment tableSegment;
    
    private final SQLStatementContext sqlStatementContext;
    
    private final ShardingRule shardingRule;
    
    public TableToken(final int startIndex, final int stopIndex, final SimpleTableSegment tableSegment, final SQLStatementContext sqlStatementContext, final ShardingRule shardingRule) {
        super(startIndex);
        this.stopIndex = stopIndex;
        this.tableSegment = tableSegment;
        this.sqlStatementContext = sqlStatementContext;
        this.shardingRule = shardingRule;
    }
    
    @Override
    public String toString(final RouteUnit routeUnit) {
        String actualTableName = getLogicAndActualTables(routeUnit).get(tableSegment.getTableName().getIdentifier().getValue().toLowerCase());
        actualTableName = null == actualTableName ? tableSegment.getTableName().getIdentifier().getValue().toLowerCase() : actualTableName;
        String owner = "";
        if (tableSegment.getOwner().isPresent() && routeUnit.getDataSourceMapper().getLogicName().equals(tableSegment.getOwner().get().getIdentifier().getValue())) {
            owner = tableSegment.getOwner().get().getIdentifier().getQuoteCharacter().getStartDelimiter() + routeUnit.getDataSourceMapper().getActualName()
                    + tableSegment.getOwner().get().getIdentifier().getQuoteCharacter().getEndDelimiter() + ".";
        }
        return Joiner.on("").join(owner, tableSegment.getTableName().getIdentifier().getQuoteCharacter().getStartDelimiter(),
                actualTableName, tableSegment.getTableName().getIdentifier().getQuoteCharacter().getEndDelimiter());
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
