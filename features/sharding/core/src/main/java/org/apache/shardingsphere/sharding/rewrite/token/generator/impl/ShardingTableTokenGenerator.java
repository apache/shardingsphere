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

package org.apache.shardingsphere.sharding.rewrite.token.generator.impl;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CursorHeldSQLStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.aware.RouteContextAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.ShardingTableToken;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Sharding table token generator.
 */
@HighFrequencyInvocation
@RequiredArgsConstructor
@Setter
public final class ShardingTableTokenGenerator implements CollectionSQLTokenGenerator<SQLStatementContext>, RouteContextAware {
    
    private final ShardingRule rule;
    
    private RouteContext routeContext;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return !(sqlStatementContext instanceof CursorHeldSQLStatementContext) && (isAllBindingTables(sqlStatementContext) || routeContext.containsTableSharding());
    }
    
    private boolean isAllBindingTables(final SQLStatementContext sqlStatementContext) {
        Collection<String> shardingLogicTableNames = rule.getShardingLogicTableNames(sqlStatementContext.getTablesContext().getTableNames());
        return shardingLogicTableNames.size() > 1 && rule.isAllConfigBindingTables(shardingLogicTableNames);
    }
    
    @Override
    public Collection<SQLToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        Collection<SQLToken> result = new LinkedList<>();
        for (SimpleTableSegment each : sqlStatementContext.getTablesContext().getSimpleTables()) {
            TableNameSegment tableNameSegment = each.getTableName();
            if (rule.findShardingTable(tableNameSegment.getIdentifier().getValue()).isPresent()) {
                result.add(
                        new ShardingTableToken(tableNameSegment.getStartIndex(), tableNameSegment.getStopIndex(), tableNameSegment.getIdentifier(), sqlStatementContext, rule));
            }
        }
        return result;
    }
}
