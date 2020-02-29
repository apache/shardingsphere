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

import com.google.common.base.Preconditions;
import lombok.Setter;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.rule.aware.ShardingRuleAware;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.impl.TableToken;
import org.apache.shardingsphere.sql.parser.relation.segment.table.TablesContext;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.generic.TableSegmentsAvailable;
import org.apache.shardingsphere.underlying.rewrite.sql.token.generator.CollectionSQLTokenGenerator;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Table token generator.
 */
@Setter
public final class TableTokenGenerator implements CollectionSQLTokenGenerator, ShardingRuleAware {
    
    private ShardingRule shardingRule; 
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return true;
    }
    
    @Override
    public Collection<TableToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        if (!(sqlStatementContext.getSqlStatement() instanceof TableSegmentsAvailable)) {
            return Collections.emptyList();
        }
        Collection<TableToken> result = new LinkedList<>();
        for (TableSegment each : ((TableSegmentsAvailable) sqlStatementContext.getSqlStatement()).getAllTables()) {
            if (shardingRule.findTableRule(each.getIdentifier().getValue()).isPresent()) {
                result.add(new TableToken(each.getStartIndex(), each.getStopIndex(), each.getIdentifier()));
            }
        }
        if (sqlStatementContext.getSqlStatement() instanceof SelectStatement) {
            if (((SelectStatement) sqlStatementContext.getSqlStatement()).getGroupBy().isPresent()) {
                result.addAll(generateSQLTokens(sqlStatementContext, ((SelectStatement) sqlStatementContext.getSqlStatement()).getGroupBy().get().getGroupByItems()));
            }
            if (((SelectStatement) sqlStatementContext.getSqlStatement()).getOrderBy().isPresent()) {
                result.addAll(generateSQLTokens(sqlStatementContext, ((SelectStatement) sqlStatementContext.getSqlStatement()).getOrderBy().get().getOrderByItems()));
            }
        }
        return result;
    }
    
    private Collection<TableToken> generateSQLTokens(final SQLStatementContext sqlStatementContext, final Collection<OrderByItemSegment> orderBys) {
        Collection<TableToken> result = new LinkedList<>();
        for (OrderByItemSegment each : orderBys) {
            if (isToGenerateTableToken(sqlStatementContext.getTablesContext(), each)) {
                Preconditions.checkState(((ColumnOrderByItemSegment) each).getColumn().getOwner().isPresent());
                OwnerSegment segment = ((ColumnOrderByItemSegment) each).getColumn().getOwner().get();
                result.add(new TableToken(segment.getStartIndex(), segment.getStopIndex(), segment.getIdentifier()));
            }
        }
        return result;
    }
    
    private boolean isToGenerateTableToken(final TablesContext tablesContext, final OrderByItemSegment orderByItemSegment) {
        return orderByItemSegment instanceof ColumnOrderByItemSegment && ((ColumnOrderByItemSegment) orderByItemSegment).getColumn().getOwner().isPresent() 
                && isTable(((ColumnOrderByItemSegment) orderByItemSegment).getColumn().getOwner().get(), tablesContext);
    }
    
    private boolean isTable(final OwnerSegment owner, final TablesContext tablesContext) {
        return !tablesContext.findTableFromAlias(owner.getIdentifier().getValue()).isPresent();
    }
}
