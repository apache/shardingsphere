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

import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.generator.IgnoreForSingleRoute;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.OrderByToken;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;

/**
 * Sharding order by token generator.
 */
@HighFrequencyInvocation
public final class ShardingOrderByTokenGenerator implements OptionalSQLTokenGenerator<SelectStatementContext>, IgnoreForSingleRoute {
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext && ((SelectStatementContext) sqlStatementContext).getOrderByContext().isGenerated();
    }
    
    @Override
    public OrderByToken generateSQLToken(final SelectStatementContext selectStatementContext) {
        OrderByToken result = new OrderByToken(getGenerateOrderByStartIndex(selectStatementContext));
        String columnLabel;
        for (OrderByItem each : selectStatementContext.getOrderByContext().getItems()) {
            if (each.getSegment() instanceof ColumnOrderByItemSegment) {
                ColumnOrderByItemSegment columnOrderByItemSegment = (ColumnOrderByItemSegment) each.getSegment();
                columnLabel = columnOrderByItemSegment.getText();
            } else if (each.getSegment() instanceof ExpressionOrderByItemSegment) {
                columnLabel = ((ExpressionOrderByItemSegment) each.getSegment()).getText();
            } else {
                columnLabel = String.valueOf(each.getIndex());
            }
            result.getColumnLabels().add(columnLabel);
            result.getOrderDirections().add(each.getSegment().getOrderDirection());
        }
        return result;
    }
    
    private int getGenerateOrderByStartIndex(final SelectStatementContext selectStatementContext) {
        SelectStatement sqlStatement = selectStatementContext.getSqlStatement();
        int stopIndex;
        if (sqlStatement.getWindow().isPresent()) {
            stopIndex = sqlStatement.getWindow().get().getStopIndex();
        } else if (sqlStatement.getHaving().isPresent()) {
            stopIndex = sqlStatement.getHaving().get().getStopIndex();
        } else if (sqlStatement.getGroupBy().isPresent()) {
            stopIndex = sqlStatement.getGroupBy().get().getStopIndex();
        } else if (sqlStatement.getWhere().isPresent()) {
            stopIndex = sqlStatement.getWhere().get().getStopIndex();
        } else {
            stopIndex = selectStatementContext.getTablesContext().getSimpleTables().stream().mapToInt(SimpleTableSegment::getStopIndex).max().orElse(0);
        }
        return stopIndex + 1;
    }
}
