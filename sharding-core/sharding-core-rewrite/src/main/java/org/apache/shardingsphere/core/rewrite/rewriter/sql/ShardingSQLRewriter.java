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

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.optimize.result.OptimizeResult;
import org.apache.shardingsphere.core.optimize.result.insert.InsertOptimizeResult;
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
import org.apache.shardingsphere.core.rewrite.placeholder.TablePlaceholder;
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
import org.apache.shardingsphere.core.rewrite.token.pojo.TableToken;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.pagination.Pagination;
import org.apache.shardingsphere.core.rule.ShardingRule;

/**
 * SQL rewriter for sharding.
 * 
 * @author zhangliang
 * @author maxiaoguang
 * @author panjuan
 */
public final class ShardingSQLRewriter implements SQLRewriter {
    
    private final ShardingRule shardingRule;
    
    private final DatabaseType databaseType;
    
    private final SQLRouteResult sqlRouteResult;
    
    private final InsertOptimizeResult insertOptimizeResult;
    
    public ShardingSQLRewriter(final ShardingRule shardingRule, final DatabaseType databaseType, final SQLRouteResult sqlRouteResult, final OptimizeResult optimizeResult) {
        this.shardingRule = shardingRule;
        this.databaseType = databaseType;
        this.sqlRouteResult = sqlRouteResult;
        this.insertOptimizeResult = getInsertOptimizeResult(optimizeResult);
    }
    
    private InsertOptimizeResult getInsertOptimizeResult(final OptimizeResult optimizeResult) {
        if (null == optimizeResult) {
            return null;
        }
        Optional<InsertOptimizeResult> insertOptimizeResult = optimizeResult.getInsertOptimizeResult();
        if (!insertOptimizeResult.isPresent()) {
            return null;
        }
        return insertOptimizeResult.get();
    }
    
    @Override
    public void rewrite(final SQLBuilder sqlBuilder, final ParameterBuilder parameterBuilder, final SQLToken sqlToken) {
        if (sqlToken instanceof SelectItemPrefixToken) {
            appendSelectItemPrefixPlaceholder(sqlBuilder);
        } else if (sqlToken instanceof TableToken) {
            appendTablePlaceholder(sqlBuilder, (TableToken) sqlToken);
        } else if (sqlToken instanceof IndexToken) {
            appendIndexPlaceholder(sqlBuilder, (IndexToken) sqlToken);
        } else if (sqlToken instanceof SelectItemsToken) {
            appendSelectItemsPlaceholder(sqlBuilder, (SelectItemsToken) sqlToken);
        } else if (sqlToken instanceof RowCountToken) {
            appendLimitRowCountPlaceholder(sqlBuilder, (RowCountToken) sqlToken);
        } else if (sqlToken instanceof OffsetToken) {
            appendLimitOffsetPlaceholder(sqlBuilder, (OffsetToken) sqlToken);
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
    
    private void appendTablePlaceholder(final SQLBuilder sqlBuilder, final TableToken tableToken) {
        sqlBuilder.appendPlaceholder(new TablePlaceholder(tableToken.getTableName().toLowerCase(), tableToken.getQuoteCharacter()));
    }
    
    private void appendIndexPlaceholder(final SQLBuilder sqlBuilder, final IndexToken indexToken) {
        String logicTableName = indexToken.getTableName().toLowerCase();
        if (Strings.isNullOrEmpty(logicTableName)) {
            logicTableName = shardingRule.getLogicTableName(indexToken.getIndexName());
        }
        sqlBuilder.appendPlaceholder(new IndexPlaceholder(indexToken.getIndexName(), logicTableName, indexToken.getQuoteCharacter()));
    }
    
    private void appendSelectItemsPlaceholder(final SQLBuilder sqlBuilder, final SelectItemsToken selectItemsToken) {
        if (isRewrite()) {
            SelectItemsPlaceholder selectItemsPlaceholder = new SelectItemsPlaceholder();
            selectItemsPlaceholder.getItems().addAll(selectItemsToken.getItems());
            sqlBuilder.appendPlaceholder(selectItemsPlaceholder);
        }
    }
    
    private void appendLimitRowCountPlaceholder(final SQLBuilder sqlBuilder, final RowCountToken rowCountToken) {
        SelectStatement selectStatement = (SelectStatement) sqlRouteResult.getSqlStatement();
        sqlBuilder.appendPlaceholder(new LimitRowCountPlaceholder(getRowCount(rowCountToken, isRewrite(), selectStatement, sqlRouteResult.getPagination())));
    }
    
    private int getRowCount(final RowCountToken rowCountToken, final boolean isRewrite, final SelectStatement selectStatement, final Pagination pagination) {
        if (!isRewrite) {
            return rowCountToken.getRowCount();
        }
        if (isMaxRowCount(selectStatement)) {
            return Integer.MAX_VALUE;
        }
        return pagination.isNeedRewriteRowCount(databaseType.name()) ? rowCountToken.getRowCount() + pagination.getOffsetValue() : rowCountToken.getRowCount();
    }
    
    private boolean isMaxRowCount(final SelectStatement selectStatement) {
        return (!selectStatement.getGroupByItems().isEmpty() || !selectStatement.getAggregationSelectItems().isEmpty()) && !selectStatement.isSameGroupByAndOrderByItems();
    }
    
    private void appendLimitOffsetPlaceholder(final SQLBuilder sqlBuilder, final OffsetToken offsetToken) {
        sqlBuilder.appendPlaceholder(new LimitOffsetPlaceholder(isRewrite() ? 0 : offsetToken.getOffset()));
    }
    
    private void appendOrderByPlaceholder(final SQLBuilder sqlBuilder) {
        SelectStatement selectStatement = (SelectStatement) sqlRouteResult.getSqlStatement();
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
            sqlBuilder.appendLiterals(sqlRouteResult.getSqlStatement().getLogicSQL().substring(distinctToken.getStartIndex(), distinctToken.getStopIndex() + 1));
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
