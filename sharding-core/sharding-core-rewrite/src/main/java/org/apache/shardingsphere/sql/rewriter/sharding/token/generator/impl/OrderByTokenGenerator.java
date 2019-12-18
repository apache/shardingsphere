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

package org.apache.shardingsphere.sql.rewriter.sharding.token.generator.impl;

import org.apache.shardingsphere.sql.parser.relation.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.impl.SelectSQLStatementContext;
import org.apache.shardingsphere.sql.parser.core.constant.QuoteCharacter;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.rewriter.sharding.token.generator.IgnoreForSingleRoute;
import org.apache.shardingsphere.sql.rewriter.sql.token.generator.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.sql.rewriter.sharding.token.pojo.impl.OrderByToken;

/**
 * Order by token generator.
 *
 * @author zhangliang
 */
public final class OrderByTokenGenerator implements OptionalSQLTokenGenerator, IgnoreForSingleRoute {
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof SelectSQLStatementContext && ((SelectSQLStatementContext) sqlStatementContext).getOrderByContext().isGenerated();
    }
    
    @Override
    public OrderByToken generateSQLToken(final SQLStatementContext sqlStatementContext) {
        OrderByToken result = new OrderByToken(((SelectSQLStatementContext) sqlStatementContext).getGroupByContext().getLastIndex() + 1);
        String columnLabel;
        for (OrderByItem each : ((SelectSQLStatementContext) sqlStatementContext).getOrderByContext().getItems()) {
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
