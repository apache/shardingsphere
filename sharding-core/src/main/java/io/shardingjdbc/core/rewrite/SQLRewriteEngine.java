/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.rewrite;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.parsing.lexer.token.DefaultKeyword;
import io.shardingjdbc.core.parsing.lexer.token.Symbol;
import io.shardingjdbc.core.parsing.parser.context.OrderItem;
import io.shardingjdbc.core.parsing.parser.context.limit.Limit;
import io.shardingjdbc.core.parsing.parser.sql.SQLStatement;
import io.shardingjdbc.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingjdbc.core.parsing.parser.token.GeneratedKeyToken;
import io.shardingjdbc.core.parsing.parser.token.IndexToken;
import io.shardingjdbc.core.parsing.parser.token.ItemsToken;
import io.shardingjdbc.core.parsing.parser.token.OffsetToken;
import io.shardingjdbc.core.parsing.parser.token.OrderByToken;
import io.shardingjdbc.core.parsing.parser.token.RowCountToken;
import io.shardingjdbc.core.parsing.parser.token.SQLToken;
import io.shardingjdbc.core.parsing.parser.token.SchemaToken;
import io.shardingjdbc.core.parsing.parser.token.TableToken;
import io.shardingjdbc.core.rewrite.placeholder.IndexPlaceholder;
import io.shardingjdbc.core.rewrite.placeholder.SchemaPlaceholder;
import io.shardingjdbc.core.rewrite.placeholder.TablePlaceholder;
import io.shardingjdbc.core.routing.condition.GeneratedKey;
import io.shardingjdbc.core.routing.type.TableUnit;
import io.shardingjdbc.core.routing.type.complex.CartesianTableReference;
import io.shardingjdbc.core.rule.BindingTableRule;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.util.SQLUtil;

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
    
    private final DatabaseType databaseType;
    
    private final List<SQLToken> sqlTokens = new LinkedList<>();
    
    private final SQLStatement sqlStatement;
    
    private final GeneratedKey generatedKey;
    
    /**
     * Constructs SQL rewrite engine.
     * 
     * @param shardingRule databases and tables sharding rule
     * @param originalSQL original SQL
     * @param databaseType database type
     * @param sqlStatement SQL statement
     * @param generatedKey generated key
     */
    public SQLRewriteEngine(final ShardingRule shardingRule, final String originalSQL, final DatabaseType databaseType, final SQLStatement sqlStatement, final GeneratedKey generatedKey) {
        this.shardingRule = shardingRule;
        this.originalSQL = originalSQL;
        this.databaseType = databaseType;
        this.sqlStatement = sqlStatement;
        this.generatedKey = generatedKey;
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
                appendTablePlaceholder(result, (TableToken) each, count, sqlTokens);
            } else if (each instanceof SchemaToken) {
                appendSchemaPlaceholder(result, (SchemaToken) each, count, sqlTokens);
            } else if (each instanceof IndexToken) {
                appendIndexPlaceholder(result, (IndexToken) each, count, sqlTokens);
            } else if (each instanceof ItemsToken) {
                appendItemsToken(result, (ItemsToken) each, count, sqlTokens);
            } else if (each instanceof GeneratedKeyToken) {
                appendGenerateKeyToken(result, (GeneratedKeyToken) each, count, sqlTokens);
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
    
    private void appendTablePlaceholder(final SQLBuilder sqlBuilder, final TableToken tableToken, final int count, final List<SQLToken> sqlTokens) {
        sqlBuilder.appendPlaceholder(new TablePlaceholder(tableToken.getTableName().toLowerCase()));
        int beginPosition = tableToken.getBeginPosition() + tableToken.getOriginalLiterals().length();
        appendRest(sqlBuilder, count, sqlTokens, beginPosition);
    }
    
    private void appendSchemaPlaceholder(final SQLBuilder sqlBuilder, final SchemaToken schemaToken, final int count, final List<SQLToken> sqlTokens) {
        sqlBuilder.appendPlaceholder(new SchemaPlaceholder(schemaToken.getSchemaName().toLowerCase(), schemaToken.getTableName().toLowerCase()));
        int beginPosition = schemaToken.getBeginPosition() + schemaToken.getOriginalLiterals().length();
        appendRest(sqlBuilder, count, sqlTokens, beginPosition);
    }
    
    private void appendIndexPlaceholder(final SQLBuilder sqlBuilder, final IndexToken indexToken, final int count, final List<SQLToken> sqlTokens) {
        String indexName = indexToken.getIndexName().toLowerCase();
        String logicTableName = indexToken.getTableName().toLowerCase();
        if (Strings.isNullOrEmpty(logicTableName)) {
            logicTableName = shardingRule.getLogicTableName(indexName);
        }
        sqlBuilder.appendPlaceholder(new IndexPlaceholder(indexName, logicTableName));
        int beginPosition = indexToken.getBeginPosition() + indexToken.getOriginalLiterals().length();
        appendRest(sqlBuilder, count, sqlTokens, beginPosition);
    }
    
    private void appendItemsToken(final SQLBuilder sqlBuilder, final ItemsToken itemsToken, final int count, final List<SQLToken> sqlTokens) {
        for (String item : itemsToken.getItems()) {
            sqlBuilder.appendLiterals(", ");
            sqlBuilder.appendLiterals(SQLUtil.getOriginalValue(item, databaseType));
        }
        int beginPosition = itemsToken.getBeginPosition();
        appendRest(sqlBuilder, count, sqlTokens, beginPosition);
    }
    
    private void appendGenerateKeyToken(final SQLBuilder sqlBuilder, final GeneratedKeyToken generatedKeyToken, final int count, final List<SQLToken> sqlTokens) {
        ItemsToken valuesToken = new ItemsToken(generatedKeyToken.getBeginPosition());
        if (0 == sqlStatement.getParametersIndex()) {
            valuesToken.getItems().add(generatedKey.getValue().toString());
        } else {
            valuesToken.getItems().add(Symbol.QUESTION.getLiterals());
        }
        appendItemsToken(sqlBuilder, valuesToken, count, sqlTokens);
    }
    
    private void appendLimitRowCount(final SQLBuilder sqlBuilder, final RowCountToken rowCountToken, final int count, final List<SQLToken> sqlTokens, final boolean isRewrite) {
        SelectStatement selectStatement = (SelectStatement) sqlStatement;
        Limit limit = selectStatement.getLimit();
        if (!isRewrite) {
            sqlBuilder.appendLiterals(String.valueOf(rowCountToken.getRowCount()));
        } else if ((!selectStatement.getGroupByItems().isEmpty() || !selectStatement.getAggregationSelectItems().isEmpty()) && !selectStatement.isSameGroupByAndOrderByItems()) {
            sqlBuilder.appendLiterals(String.valueOf(Integer.MAX_VALUE));
        } else {
            sqlBuilder.appendLiterals(String.valueOf(limit.isNeedRewriteRowCount() ? rowCountToken.getRowCount() + limit.getOffsetValue() : rowCountToken.getRowCount()));
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
            String columnLabel = SQLUtil.getOriginalValue(each.getColumnLabel(), databaseType);
            if (0 == i) {
                orderByLiterals.append(columnLabel).append(" ").append(each.getOrderDirection().name());
            } else {
                orderByLiterals.append(",").append(columnLabel).append(" ").append(each.getOrderDirection().name());
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
        return sqlBuilder.toSQL(getTableTokens(tableUnit), shardingRule);
    }
    
    /**
     * Generate SQL string.
     *
     * @param cartesianTableReference cartesian table reference
     * @param sqlBuilder SQL builder
     * @return SQL string
     */
    public String generateSQL(final CartesianTableReference cartesianTableReference, final SQLBuilder sqlBuilder) {
        return sqlBuilder.toSQL(getTableTokens(cartesianTableReference), shardingRule);
    }
    
    private Map<String, String> getTableTokens(final TableUnit tableUnit) {
        String logicTableName = tableUnit.getLogicTableName().toLowerCase();
        Map<String, String> result = new HashMap<>();
        result.put(logicTableName, tableUnit.getActualTableName());
        Optional<BindingTableRule> bindingTableRule = shardingRule.findBindingTableRule(logicTableName);
        if (bindingTableRule.isPresent()) {
            result.putAll(getBindingTableTokens(tableUnit, bindingTableRule.get()));
        }
        return result;
    }
    
    private Map<String, String> getTableTokens(final CartesianTableReference cartesianTableReference) {
        Map<String, String> result = new HashMap<>();
        for (TableUnit each : cartesianTableReference.getTableUnits()) {
            String logicTableName = each.getLogicTableName().toLowerCase();
            result.put(logicTableName, each.getActualTableName());
            Optional<BindingTableRule> bindingTableRule = shardingRule.findBindingTableRule(logicTableName);
            if (bindingTableRule.isPresent()) {
                result.putAll(getBindingTableTokens(each, bindingTableRule.get()));
            }
        }
        return result;
    }
    
    private Map<String, String> getBindingTableTokens(final TableUnit tableUnit, final BindingTableRule bindingTableRule) {
        Map<String, String> result = new HashMap<>();
        for (String eachTable : sqlStatement.getTables().getTableNames()) {
            String tableName = eachTable.toLowerCase();
            if (!tableName.equals(tableUnit.getLogicTableName().toLowerCase()) && bindingTableRule.hasLogicTable(tableName)) {
                result.put(tableName, bindingTableRule.getBindingActualTable(tableUnit.getDataSourceName(), tableName, tableUnit.getActualTableName()));
            }
        }
        return result;
    }
}
