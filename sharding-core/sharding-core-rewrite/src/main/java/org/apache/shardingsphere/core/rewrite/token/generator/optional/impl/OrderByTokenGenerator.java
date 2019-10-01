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

package org.apache.shardingsphere.core.rewrite.token.generator.optional.impl;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.optimize.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.core.optimize.statement.SQLStatementContext;
import org.apache.shardingsphere.core.optimize.statement.impl.SelectSQLStatementContext;
import org.apache.shardingsphere.core.parse.core.constant.QuoteCharacter;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.core.rewrite.token.generator.IgnoreForSingleRoute;
import org.apache.shardingsphere.core.rewrite.token.generator.optional.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.pojo.impl.OrderByToken;

/**
 * Order by token generator.
 *
 * @author zhangliang
 */
public final class OrderByTokenGenerator implements OptionalSQLTokenGenerator, IgnoreForSingleRoute {
    
    @Override
    public Optional<OrderByToken> generateSQLToken(final SQLStatementContext sqlStatementContext) {
        if (!(sqlStatementContext instanceof SelectSQLStatementContext)) {
            return Optional.absent();
        }
        if (((SelectSQLStatementContext) sqlStatementContext).getOrderByContext().isGenerated()) {
            return Optional.of(createOrderByToken((SelectSQLStatementContext) sqlStatementContext));
        }
        return Optional.absent();
    }
    
    private OrderByToken createOrderByToken(final SelectSQLStatementContext selectSQLStatementContext) {
        OrderByToken result = new OrderByToken(selectSQLStatementContext.getGroupByContext().getLastIndex() + 1);
        String columnLabel;
        for (OrderByItem each : selectSQLStatementContext.getOrderByContext().getItems()) {
            if (each.getSegment() instanceof ColumnOrderByItemSegment) {
                ColumnOrderByItemSegment columnOrderByItemSegment = (ColumnOrderByItemSegment) each.getSegment();
                QuoteCharacter quoteCharacter = columnOrderByItemSegment.getColumn().getQuoteCharacter();
                columnLabel = quoteCharacter.getStartDelimiter() + columnOrderByItemSegment.getText() + quoteCharacter.getEndDelimiter();
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
}
