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

package org.apache.shardingsphere.core.rewrite.rewriter.sql;

import org.apache.shardingsphere.core.optimize.statement.sharding.InsertClauseOptimizedStatement;
import org.apache.shardingsphere.core.optimize.statement.OptimizedStatement;
import org.apache.shardingsphere.core.optimize.statement.sharding.insert.InsertOptimizeResult;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.TextOrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.rewrite.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.builder.SQLBuilder;
import org.apache.shardingsphere.core.rewrite.placeholder.AggregationDistinctPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.IndexPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.InsertGeneratedKeyPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.InsertSetAddGeneratedKeyPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.LimitOffsetPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.LimitRowCountPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.OrderByPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.SelectItemPrefixPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.SelectItemsPlaceholder;
import org.apache.shardingsphere.core.rewrite.token.pojo.AggregationDistinctToken;
import org.apache.shardingsphere.core.rewrite.token.pojo.IndexToken;
import org.apache.shardingsphere.core.rewrite.token.pojo.InsertGeneratedKeyToken;
import org.apache.shardingsphere.core.rewrite.token.pojo.InsertSetAddGeneratedKeyToken;
import org.apache.shardingsphere.core.rewrite.token.pojo.OffsetToken;
import org.apache.shardingsphere.core.rewrite.token.pojo.OrderByToken;
import org.apache.shardingsphere.core.rewrite.token.pojo.RowCountToken;
import org.apache.shardingsphere.core.rewrite.token.pojo.SQLToken;
import org.apache.shardingsphere.core.rewrite.token.pojo.SelectItemPrefixToken;
import org.apache.shardingsphere.core.rewrite.token.pojo.SelectItemsToken;
import org.apache.shardingsphere.core.route.SQLRouteResult;

/**
 * SQL rewriter for sharding.
 * 
 * @author zhangliang
 * @author maxiaoguang
 * @author panjuan
 */
public final class ShardingSQLRewriter implements SQLRewriter {
    
    private final SQLRouteResult sqlRouteResult;
    
    private final InsertOptimizeResult insertOptimizeResult;
    
    public ShardingSQLRewriter(final SQLRouteResult sqlRouteResult, final OptimizedStatement optimizedStatement) {
        this.sqlRouteResult = sqlRouteResult;
        this.insertOptimizeResult = getInsertOptimizeResult(optimizedStatement);
    }
    
    private InsertOptimizeResult getInsertOptimizeResult(final OptimizedStatement optimizedStatement) {
        if (null == optimizedStatement || !(optimizedStatement instanceof InsertClauseOptimizedStatement)) {
            return null;
        }
        return ((InsertClauseOptimizedStatement) optimizedStatement).getInsertOptimizeResult();
    }
    
    @Override
    public void rewrite(final SQLBuilder sqlBuilder, final ParameterBuilder parameterBuilder, final SQLToken sqlToken) {
        if (sqlToken instanceof SelectItemPrefixToken) {
            appendSelectItemPrefixPlaceholder(sqlBuilder);
        } else if (sqlToken instanceof IndexToken) {
            appendIndexPlaceholder(sqlBuilder, (IndexToken) sqlToken);
        } else if (sqlToken instanceof SelectItemsToken) {
            appendSelectItemsPlaceholder(sqlBuilder, (SelectItemsToken) sqlToken);
        } else if (sqlToken instanceof OffsetToken) {
            appendOffsetPlaceholder(sqlBuilder, (OffsetToken) sqlToken);
        } else if (sqlToken instanceof RowCountToken) {
            appendRowCountPlaceholder(sqlBuilder, (RowCountToken) sqlToken);
        } else if (sqlToken instanceof OrderByToken) {
            appendOrderByPlaceholder(sqlBuilder);
        } else if (sqlToken instanceof AggregationDistinctToken) {
            appendAggregationDistinctPlaceholder(sqlBuilder, (AggregationDistinctToken) sqlToken);
        } else if (sqlToken instanceof InsertGeneratedKeyToken) {
            appendInsertGeneratedKeyPlaceholder(sqlBuilder, (InsertGeneratedKeyToken) sqlToken);
        } else if (sqlToken instanceof InsertSetAddGeneratedKeyToken) {
            appendInsertSetAddGeneratedKeyPlaceholder(sqlBuilder, (InsertSetAddGeneratedKeyToken) sqlToken, insertOptimizeResult);
        }
    }
    
    private void appendSelectItemPrefixPlaceholder(final SQLBuilder sqlBuilder) {
        if (isRewrite()) {
            sqlBuilder.appendPlaceholder(new SelectItemPrefixPlaceholder());
        }
    }
    
    private void appendIndexPlaceholder(final SQLBuilder sqlBuilder, final IndexToken indexToken) {
        sqlBuilder.appendPlaceholder(new IndexPlaceholder(indexToken.getIndexName(), indexToken.getQuoteCharacter()));
    }
    
    private void appendSelectItemsPlaceholder(final SQLBuilder sqlBuilder, final SelectItemsToken selectItemsToken) {
        if (isRewrite()) {
            SelectItemsPlaceholder selectItemsPlaceholder = new SelectItemsPlaceholder();
            selectItemsPlaceholder.getItems().addAll(selectItemsToken.getItems());
            sqlBuilder.appendPlaceholder(selectItemsPlaceholder);
        }
    }
    
    private void appendOffsetPlaceholder(final SQLBuilder sqlBuilder, final OffsetToken offsetToken) {
        sqlBuilder.appendPlaceholder(new LimitOffsetPlaceholder(offsetToken.getRevisedOffset()));
    }
    
    private void appendRowCountPlaceholder(final SQLBuilder sqlBuilder, final RowCountToken rowCountToken) {
        sqlBuilder.appendPlaceholder(new LimitRowCountPlaceholder(rowCountToken.getRevisedRowCount()));
    }
    
    private void appendOrderByPlaceholder(final SQLBuilder sqlBuilder) {
        SelectStatement selectStatement = (SelectStatement) sqlRouteResult.getOptimizedStatement().getSQLStatement();
        OrderByPlaceholder orderByPlaceholder = new OrderByPlaceholder();
        if (isRewrite()) {
            for (OrderByItemSegment each : selectStatement.getOrderByItems()) {
                String columnLabel = each instanceof TextOrderByItemSegment ? ((TextOrderByItemSegment) each).getText() : String.valueOf(each.getIndex());
                orderByPlaceholder.getColumnLabels().add(columnLabel);
                orderByPlaceholder.getOrderDirections().add(each.getOrderDirection());
            }
            sqlBuilder.appendPlaceholder(orderByPlaceholder);
        }
    }
    
    private void appendAggregationDistinctPlaceholder(final SQLBuilder sqlBuilder, final AggregationDistinctToken distinctToken) {
        if (!isRewrite()) {
            sqlBuilder.appendLiterals(sqlRouteResult.getOptimizedStatement().getSQLStatement().getLogicSQL().substring(distinctToken.getStartIndex(), distinctToken.getStopIndex() + 1));
        } else {
            sqlBuilder.appendPlaceholder(new AggregationDistinctPlaceholder(distinctToken.getColumnName().toLowerCase(), distinctToken.getDerivedAlias()));
        }
    }
    
    private void appendInsertGeneratedKeyPlaceholder(final SQLBuilder sqlBuilder, final InsertGeneratedKeyToken insertGeneratedKeyToken) {
        sqlBuilder.appendPlaceholder(new InsertGeneratedKeyPlaceholder(insertGeneratedKeyToken.getColumn(), insertGeneratedKeyToken.isToAddCloseParenthesis()));
    }
    
    private void appendInsertSetAddGeneratedKeyPlaceholder(
            final SQLBuilder sqlBuilder, final InsertSetAddGeneratedKeyToken insertSetAddGeneratedKeyToken, final InsertOptimizeResult insertOptimizeResult) {
        String columnName = insertSetAddGeneratedKeyToken.getColumnName();
        sqlBuilder.appendPlaceholder(new InsertSetAddGeneratedKeyPlaceholder(columnName, insertOptimizeResult.getUnits().get(0).getColumnSQLExpression(columnName)));
    }
    
    private boolean isRewrite() {
        return !sqlRouteResult.getRoutingResult().isSingleRouting();
    }
}
