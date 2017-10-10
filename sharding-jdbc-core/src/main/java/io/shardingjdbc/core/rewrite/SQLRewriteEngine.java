/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.rewrite;


import io.shardingjdbc.core.rule.BindingTableRule;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.parsing.lexer.token.DefaultKeyword;
import io.shardingjdbc.core.parsing.parser.context.OrderItem;
import io.shardingjdbc.core.parsing.parser.context.limit.Limit;
import io.shardingjdbc.core.parsing.parser.sql.SQLStatement;
import io.shardingjdbc.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingjdbc.core.parsing.parser.token.ItemsToken;
import io.shardingjdbc.core.parsing.parser.token.OffsetToken;
import io.shardingjdbc.core.parsing.parser.token.OrderByToken;
import io.shardingjdbc.core.parsing.parser.token.RowCountToken;
import io.shardingjdbc.core.parsing.parser.token.SQLToken;
import io.shardingjdbc.core.parsing.parser.token.TableToken;
import io.shardingjdbc.core.routing.type.TableUnit;
import io.shardingjdbc.core.routing.type.complex.CartesianTableReference;
import com.google.common.base.Optional;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * SQL rewrite engine.
 * 
 * <p>Rewrite logic SQL to actual SQL, should rewrite table name and optimize something.</p>
 *
 * @author zhangliang
 */
public final class SQLRewriteEngine {
    
    private final ShardingRule shardingRule;
    
    private final String originalSQL;
    
    private final List<SQLToken> sqlTokens = new LinkedList<>();
    
    private final SQLStatement sqlStatement;
    
    /**
     * Constructs SQL rewrite engine.
     * 
     * @param shardingRule databases and tables sharding rule
     * @param originalSQL original SQL
     * @param sqlStatement SQL statement
     */
    public SQLRewriteEngine(final ShardingRule shardingRule, final String originalSQL, final SQLStatement sqlStatement) {
        this.shardingRule = shardingRule;
        this.originalSQL = originalSQL;
        this.sqlStatement = sqlStatement;
        sqlTokens.addAll(sqlStatement.getSqlTokens());
    }
    
    /**
     * rewrite SQL.
     *
     * @param isRewriteLimit is rewrite limit
     * @return SQL builder
     */
    public SQLBuilder rewrite(final boolean isRewriteLimit) {
        SQLBuilder result = new SQLBuilder();
        if (sqlTokens.isEmpty()) {
            result.appendLiterals(originalSQL);
            return result;
        }
        int count = 0;
        sortByBeginPosition();
        for (SQLToken each : sqlTokens) {
            if (0 == count) {
                result.appendLiterals(originalSQL.substring(0, each.getBeginPosition()));
            }
            if (each instanceof TableToken) {
                appendTableToken(result, (TableToken) each, count, sqlTokens);
            } else if (each instanceof ItemsToken) {
                appendItemsToken(result, (ItemsToken) each, count, sqlTokens);
            } else if (each instanceof RowCountToken) {
                appendLimitRowCount(result, (RowCountToken) each, count, sqlTokens, isRewriteLimit);
            } else if (each instanceof OffsetToken) {
                appendLimitOffsetToken(result, (OffsetToken) each, count, sqlTokens, isRewriteLimit);
            } else if (each instanceof OrderByToken) {
                appendOrderByToken(result, count, sqlTokens);
            }
            count++;
        }
        return result;
    }
    
    private void sortByBeginPosition() {
        Collections.sort(sqlTokens, new Comparator<SQLToken>() {
            
            @Override
            public int compare(final SQLToken o1, final SQLToken o2) {
                return o1.getBeginPosition() - o2.getBeginPosition();
            }
        });
    }
    
    private void appendTableToken(final SQLBuilder sqlBuilder, final TableToken tableToken, final int count, final List<SQLToken> sqlTokens) {
        String tableName = sqlStatement.getTables().getTableNames().contains(tableToken.getTableName()) ? tableToken.getTableName() : tableToken.getOriginalLiterals();
        sqlBuilder.appendTable(tableName);
        int beginPosition = tableToken.getBeginPosition() + tableToken.getOriginalLiterals().length();
        appendRest(sqlBuilder, count, sqlTokens, beginPosition);
    }
    
    private void appendItemsToken(final SQLBuilder sqlBuilder, final ItemsToken itemsToken, final int count, final List<SQLToken> sqlTokens) {
        for (String item : itemsToken.getItems()) {
            sqlBuilder.appendLiterals(", ");
            sqlBuilder.appendLiterals(item);
        }
        int beginPosition = itemsToken.getBeginPosition();
        appendRest(sqlBuilder, count, sqlTokens, beginPosition);
    }
    
    private void appendLimitRowCount(final SQLBuilder sqlBuilder, final RowCountToken rowCountToken, final int count, final List<SQLToken> sqlTokens, final boolean isRewrite) {
        SelectStatement selectStatement = (SelectStatement) sqlStatement;
        Limit limit = selectStatement.getLimit();
        if (!isRewrite) {
            sqlBuilder.appendLiterals(String.valueOf(rowCountToken.getRowCount()));
        } else if ((!selectStatement.getGroupByItems().isEmpty() || !selectStatement.getAggregationSelectItems().isEmpty()) && !selectStatement.isSameGroupByAndOrderByItems()) {
            sqlBuilder.appendLiterals(String.valueOf(Integer.MAX_VALUE));
        } else {
            sqlBuilder.appendLiterals(String.valueOf(limit.isRowCountRewriteFlag() ? rowCountToken.getRowCount() + limit.getOffsetValue() : rowCountToken.getRowCount()));
        }
        int beginPosition = rowCountToken.getBeginPosition() + String.valueOf(rowCountToken.getRowCount()).length();
        appendRest(sqlBuilder, count, sqlTokens, beginPosition);
    }
    
    private void appendLimitOffsetToken(final SQLBuilder sqlBuilder, final OffsetToken offsetToken, final int count, final List<SQLToken> sqlTokens, final boolean isRewrite) {
        sqlBuilder.appendLiterals(isRewrite ? "0" : String.valueOf(offsetToken.getOffset()));
        int beginPosition = offsetToken.getBeginPosition() + String.valueOf(offsetToken.getOffset()).length();
        appendRest(sqlBuilder, count, sqlTokens, beginPosition);
    }
    
    private void appendOrderByToken(final SQLBuilder sqlBuilder, final int count, final List<SQLToken> sqlTokens) {
        SelectStatement selectStatement = (SelectStatement) sqlStatement;
        StringBuilder orderByLiterals = new StringBuilder();
        orderByLiterals.append(" ").append(DefaultKeyword.ORDER).append(" ").append(DefaultKeyword.BY).append(" ");
        int i = 0;
        for (OrderItem each : selectStatement.getOrderByItems()) {
            if (0 == i) {
                orderByLiterals.append(each.getColumnLabel()).append(" ").append(each.getType().name());
            } else {
                orderByLiterals.append(",").append(each.getColumnLabel()).append(" ").append(each.getType().name());
            }
            i++;
        }
        orderByLiterals.append(" ");
        sqlBuilder.appendLiterals(orderByLiterals.toString());
        int beginPosition = ((SelectStatement) sqlStatement).getGroupByLastPosition();
        appendRest(sqlBuilder, count, sqlTokens, beginPosition);
    }
    
    private void appendRest(final SQLBuilder sqlBuilder, final int count, final List<SQLToken> sqlTokens, final int beginPosition) {
        int endPosition = sqlTokens.size() - 1 == count ? originalSQL.length() : sqlTokens.get(count + 1).getBeginPosition();
        sqlBuilder.appendLiterals(originalSQL.substring(beginPosition, endPosition));
    }
    
    /**
     * Generate SQL string.
     * 
     * @param tableUnit route table unit
     * @param sqlBuilder SQL builder
     * @return SQL string
     */
    public String generateSQL(final TableUnit tableUnit, final SQLBuilder sqlBuilder) {
        return sqlBuilder.toSQL(getTableTokens(tableUnit));
    }
    
    /**
     * Generate SQL string.
     *
     * @param cartesianTableReference cartesian table reference
     * @param sqlBuilder SQL builder
     * @return SQL string
     */
    public String generateSQL(final CartesianTableReference cartesianTableReference, final SQLBuilder sqlBuilder) {
        return sqlBuilder.toSQL(getTableTokens(cartesianTableReference));
    }
    
    private Map<String, String> getTableTokens(final TableUnit tableUnit) {
        Map<String, String> tableTokens = new HashMap<>();
        tableTokens.put(tableUnit.getLogicTableName(), tableUnit.getActualTableName());
        Optional<BindingTableRule> bindingTableRule = shardingRule.findBindingTableRule(tableUnit.getLogicTableName());
        if (bindingTableRule.isPresent()) {
            tableTokens.putAll(getBindingTableTokens(tableUnit, bindingTableRule.get()));
        }
        return tableTokens;
    }
    
    private Map<String, String> getTableTokens(final CartesianTableReference cartesianTableReference) {
        Map<String, String> tableTokens = new HashMap<>();
        for (TableUnit each : cartesianTableReference.getTableUnits()) {
            tableTokens.put(each.getLogicTableName(), each.getActualTableName());
            Optional<BindingTableRule> bindingTableRule = shardingRule.findBindingTableRule(each.getLogicTableName());
            if (bindingTableRule.isPresent()) {
                tableTokens.putAll(getBindingTableTokens(each, bindingTableRule.get()));
            }
        }
        return tableTokens;
    }
    
    private Map<String, String> getBindingTableTokens(final TableUnit tableUnit, final BindingTableRule bindingTableRule) {
        Map<String, String> result = new HashMap<>();
        for (String eachTable : sqlStatement.getTables().getTableNames()) {
            if (!eachTable.equalsIgnoreCase(tableUnit.getLogicTableName()) && bindingTableRule.hasLogicTable(eachTable)) {
                result.put(eachTable, bindingTableRule.getBindingActualTable(tableUnit.getDataSourceName(), eachTable, tableUnit.getActualTableName()));
            }
        }
        return result;
    }
}
