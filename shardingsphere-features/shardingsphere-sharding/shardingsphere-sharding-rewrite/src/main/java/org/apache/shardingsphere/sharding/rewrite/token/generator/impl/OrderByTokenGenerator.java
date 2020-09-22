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

import org.apache.shardingsphere.infra.rewrite.sql.token.generator.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.generator.IgnoreForSingleRoute;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.OrderByToken;
import org.apache.shardingsphere.sql.parser.binder.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

/**
 * Order by token generator.
 */
public final class OrderByTokenGenerator implements OptionalSQLTokenGenerator<SelectStatementContext>, IgnoreForSingleRoute {
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext && ((SelectStatementContext) sqlStatementContext).getOrderByContext().isGenerated();
    }
    
    @Override
    public OrderByToken generateSQLToken(final SelectStatementContext selectStatementContext) {
        OrderByToken result = new OrderByToken(generateOrderByIndex(selectStatementContext));
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
    
    private int generateOrderByIndex(final SelectStatementContext selectStatementContext) {
        if (selectStatementContext.getGroupByContext().getLastIndex() > 0) {
            return selectStatementContext.getGroupByContext().getLastIndex() + 1;
        }
        SelectStatement selectStatement = selectStatementContext.getSqlStatement();
        if (selectStatement.getWhere().isPresent()) {
            return selectStatement.getWhere().get().getStopIndex() + 1;
        } else {
            return selectStatementContext.getSimpleTableSegments().stream().mapToInt(SimpleTableSegment::getStopIndex).max().getAsInt() + 1;
        }
    }
}
