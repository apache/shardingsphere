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

package org.apache.shardingsphere.core.rewrite.engine;

import com.google.common.base.Strings;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.parse.sql.context.limit.Limit;
import org.apache.shardingsphere.core.parse.sql.context.orderby.OrderItem;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.parse.sql.token.SQLToken;
import org.apache.shardingsphere.core.parse.sql.token.impl.AggregationDistinctToken;
import org.apache.shardingsphere.core.parse.sql.token.impl.IndexToken;
import org.apache.shardingsphere.core.parse.sql.token.impl.InsertColumnsToken;
import org.apache.shardingsphere.core.parse.sql.token.impl.OffsetToken;
import org.apache.shardingsphere.core.parse.sql.token.impl.OrderByToken;
import org.apache.shardingsphere.core.parse.sql.token.impl.RemoveToken;
import org.apache.shardingsphere.core.parse.sql.token.impl.RowCountToken;
import org.apache.shardingsphere.core.parse.sql.token.impl.SelectItemsToken;
import org.apache.shardingsphere.core.parse.sql.token.impl.TableToken;
import org.apache.shardingsphere.core.rewrite.builder.SQLBuilder;
import org.apache.shardingsphere.core.rewrite.placeholder.AggregationDistinctPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.IndexPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.InsertColumnsPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.LimitOffsetPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.LimitRowCountPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.OrderByPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.SelectItemsPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.TablePlaceholder;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.rule.ShardingRule;

/**
 * Sharding SQL pattern engine.
 * 
 * <p>Rewrite logic SQL to actual SQL, should rewrite table name and optimize something.</p>
 *
 * @author zhangliang
 * @author maxiaoguang
 * @author panjuan
 */
public final class ShardingSQLRewriteEngine {
    
    private final ShardingRule shardingRule;
    
    private final String originalSQL;
    
    private final DatabaseType databaseType;
    
    private final SQLRouteResult sqlRouteResult;
    
    private final SQLStatement sqlStatement;
    
    public ShardingSQLRewriteEngine(final ShardingRule shardingRule, final String originalSQL, final DatabaseType databaseType, final SQLStatement sqlStatement, final SQLRouteResult sqlRouteResult) {
        this.shardingRule = shardingRule;
        this.originalSQL = originalSQL;
        this.databaseType = databaseType;
        this.sqlRouteResult = sqlRouteResult;
        this.sqlStatement = sqlStatement;
    }
    
    public void pattern(final SQLBuilder sqlBuilder, final SQLToken sqlToken) {
        if (sqlToken instanceof TableToken) {
            appendTablePlaceholder(sqlBuilder, (TableToken) sqlToken);
        } else if (sqlToken instanceof IndexToken) {
            appendIndexPlaceholder(sqlBuilder, (IndexToken) sqlToken);
        } else if (sqlToken instanceof SelectItemsToken) {
            appendSelectItemsPlaceholder(sqlBuilder, (SelectItemsToken) sqlToken);
        } else if (sqlToken instanceof InsertColumnsToken) {
            appendInsertColumnsPlaceholder(sqlBuilder, (InsertColumnsToken) sqlToken);
        } else if (sqlToken instanceof RowCountToken) {
            appendLimitRowCountPlaceholder(sqlBuilder, (RowCountToken) sqlToken);
        } else if (sqlToken instanceof OffsetToken) {
            appendLimitOffsetPlaceholder(sqlBuilder, (OffsetToken) sqlToken);
        } else if (sqlToken instanceof OrderByToken) {
            appendOrderByPlaceholder(sqlBuilder);
        } else if (sqlToken instanceof AggregationDistinctToken) {
            appendAggregationDistinctPlaceholder(sqlBuilder, (AggregationDistinctToken) sqlToken);
        } else if (sqlToken instanceof RemoveToken) {
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
            SelectItemsPlaceholder selectItemsPlaceholder = new SelectItemsPlaceholder(selectItemsToken.isFirstOfItemsSpecial());
            selectItemsPlaceholder.getItems().addAll(selectItemsToken.getItems());
            sqlBuilder.appendPlaceholder(selectItemsPlaceholder);
        }
    }
    
    private void appendInsertColumnsPlaceholder(final SQLBuilder sqlBuilder, final InsertColumnsToken insertColumnsToken) {
        InsertColumnsPlaceholder columnsPlaceholder = new InsertColumnsPlaceholder(insertColumnsToken.isPartColumns());
        columnsPlaceholder.getColumns().addAll(insertColumnsToken.getColumns());
        sqlBuilder.appendPlaceholder(columnsPlaceholder);
    }
    
    private void appendLimitRowCountPlaceholder(final SQLBuilder sqlBuilder, final RowCountToken rowCountToken) {
        SelectStatement selectStatement = (SelectStatement) sqlStatement;
        sqlBuilder.appendPlaceholder(new LimitRowCountPlaceholder(getRowCount(rowCountToken, isRewrite(), selectStatement, sqlRouteResult.getLimit())));
    }
    
    private int getRowCount(final RowCountToken rowCountToken, final boolean isRewrite, final SelectStatement selectStatement, final Limit limit) {
        if (!isRewrite) {
            return rowCountToken.getRowCount();
        }
        if (isMaxRowCount(selectStatement)) {
            return Integer.MAX_VALUE;
        }
        return limit.isNeedRewriteRowCount(databaseType) ? rowCountToken.getRowCount() + limit.getOffsetValue() : rowCountToken.getRowCount();
    }
    
    private boolean isMaxRowCount(final SelectStatement selectStatement) {
        return (!selectStatement.getGroupByItems().isEmpty() || !selectStatement.getAggregationSelectItems().isEmpty()) && !selectStatement.isSameGroupByAndOrderByItems();
    }
    
    private void appendLimitOffsetPlaceholder(final SQLBuilder sqlBuilder, final OffsetToken offsetToken) {
        sqlBuilder.appendPlaceholder(new LimitOffsetPlaceholder(isRewrite() ? 0 : offsetToken.getOffset()));
    }
    
    private void appendOrderByPlaceholder(final SQLBuilder sqlBuilder) {
        SelectStatement selectStatement = (SelectStatement) sqlStatement;
        OrderByPlaceholder orderByPlaceholder = new OrderByPlaceholder();
        if (isRewrite()) {
            for (OrderItem each : selectStatement.getOrderByItems()) {
                String columnLabel = Strings.isNullOrEmpty(each.getColumnLabel()) ? String.valueOf(each.getIndex()) : each.getColumnLabel();
                orderByPlaceholder.getColumnLabels().add(columnLabel);
                orderByPlaceholder.getOrderDirections().add(each.getOrderDirection());
            }
            sqlBuilder.appendPlaceholder(orderByPlaceholder);
        }
    }
    
    private void appendAggregationDistinctPlaceholder(final SQLBuilder sqlBuilder, final AggregationDistinctToken distinctToken) {
        if (!isRewrite()) {
            sqlBuilder.appendLiterals(originalSQL.substring(distinctToken.getStartIndex(), distinctToken.getStopIndex() + 1));
        } else {
            sqlBuilder.appendPlaceholder(new AggregationDistinctPlaceholder(distinctToken.getColumnName().toLowerCase(), distinctToken.getAlias()));
        }
    }
    
    private boolean isRewrite() {
        return !sqlRouteResult.getRoutingResult().isSingleRouting();
    }
}
