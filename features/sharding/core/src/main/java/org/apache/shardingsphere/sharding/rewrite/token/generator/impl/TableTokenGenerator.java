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

import lombok.Setter;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.TableAvailable;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.RouteContextAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.TableToken;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.aware.ShardingRuleAware;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Table token generator.
 */
@Setter
public final class TableTokenGenerator implements CollectionSQLTokenGenerator<SQLStatementContext>, ShardingRuleAware, RouteContextAware {
    
    private ShardingRule shardingRule;
    
    private RouteContext routeContext;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return isAllBindingTables(sqlStatementContext) || routeContext.containsTableSharding();
    }
    
    private boolean isAllBindingTables(final SQLStatementContext sqlStatementContext) {
        Collection<String> shardingLogicTableNames = shardingRule.getShardingLogicTableNames(sqlStatementContext.getTablesContext().getTableNames());
        return shardingLogicTableNames.size() > 1 && shardingRule.isAllBindingTables(shardingLogicTableNames);
    }
    
    @Override
    public Collection<SQLToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof TableAvailable ? generateSQLTokens((TableAvailable) sqlStatementContext) : Collections.emptyList();
    }
    
    private Collection<SQLToken> generateSQLTokens(final TableAvailable sqlStatementContext) {
        Collection<SQLToken> result = new LinkedList<>();
        for (SimpleTableSegment each : sqlStatementContext.getAllTables()) {
            TableNameSegment tableName = each.getTableName();
            if (shardingRule.findTableRule(tableName.getIdentifier().getValue()).isPresent()) {
                result.add(new TableToken(tableName.getStartIndex(), tableName.getStopIndex(), tableName.getIdentifier(), (SQLStatementContext) sqlStatementContext, shardingRule));
            }
        }
        return result;
    }
}
