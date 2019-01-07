/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.core.rewrite;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterators;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.metadata.datasource.ShardingDataSourceMetaData;
import io.shardingsphere.core.optimizer.condition.ShardingConditions;
import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import io.shardingsphere.core.parsing.parser.context.limit.Limit;
import io.shardingsphere.core.parsing.parser.context.orderby.OrderItem;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingsphere.core.parsing.parser.token.AggregationDistinctToken;
import io.shardingsphere.core.parsing.parser.token.IndexToken;
import io.shardingsphere.core.parsing.parser.token.InsertColumnToken;
import io.shardingsphere.core.parsing.parser.token.InsertValuesToken;
import io.shardingsphere.core.parsing.parser.token.ItemsToken;
import io.shardingsphere.core.parsing.parser.token.OffsetToken;
import io.shardingsphere.core.parsing.parser.token.OrderByToken;
import io.shardingsphere.core.parsing.parser.token.RemoveToken;
import io.shardingsphere.core.parsing.parser.token.RowCountToken;
import io.shardingsphere.core.parsing.parser.token.SQLToken;
import io.shardingsphere.core.parsing.parser.token.SchemaToken;
import io.shardingsphere.core.parsing.parser.token.TableToken;
import io.shardingsphere.core.rewrite.placeholder.AggregationDistinctPlaceholder;
import io.shardingsphere.core.rewrite.placeholder.IndexPlaceholder;
import io.shardingsphere.core.rewrite.placeholder.InsertValuesPlaceholder;
import io.shardingsphere.core.rewrite.placeholder.SchemaPlaceholder;
import io.shardingsphere.core.rewrite.placeholder.TablePlaceholder;
import io.shardingsphere.core.routing.SQLUnit;
import io.shardingsphere.core.routing.type.RoutingTable;
import io.shardingsphere.core.routing.type.TableUnit;
import io.shardingsphere.core.rule.BindingTableRule;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.core.util.SQLUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SQL rewrite engine.
 * 
 * <p>Rewrite logic SQL to actual SQL, should rewrite table name and optimize something.</p>
 *
 * @author zhangliang
 * @author maxiaoguang
 * @author panjuan
 */
public final class SQLRewriteEngine {
    
    private final ShardingRule shardingRule;
    
    private final String originalSQL;
    
    private final DatabaseType databaseType;
    
    private final SQLStatement sqlStatement;
    
    private final List<SQLToken> sqlTokens;
    
    private final ShardingConditions shardingConditions;
    
    private final List<Object> parameters;
    
    /**
     * Constructs SQL rewrite engine.
     * 
     * @param shardingRule databases and tables sharding rule
     * @param originalSQL original SQL
     * @param databaseType database type
     * @param sqlStatement SQL statement
     * @param shardingConditions sharding conditions
     * @param parameters parameters
     */
    public SQLRewriteEngine(final ShardingRule shardingRule, final String originalSQL, final DatabaseType databaseType,
                            final SQLStatement sqlStatement, final ShardingConditions shardingConditions, final List<Object> parameters) {
        this.shardingRule = shardingRule;
        this.originalSQL = originalSQL;
        this.databaseType = databaseType;
        this.sqlStatement = sqlStatement;
        sqlTokens = sqlStatement.getSQLTokens();
        this.shardingConditions = shardingConditions;
        this.parameters = parameters;
    }
    
    /**
     * rewrite SQL.
     *
     * @param isSingleRouting is rewrite
     * @return SQL builder
     */
    public SQLBuilder rewrite(final boolean isSingleRouting) {
        SQLBuilder result = new SQLBuilder(parameters);
        if (sqlTokens.isEmpty()) {
            return appendOriginalLiterals(result);
        }
        appendInitialLiterals(!isSingleRouting, result);
        appendTokensAndPlaceholders(!isSingleRouting, result);
        return result;
    }
    
    private SQLBuilder appendOriginalLiterals(final SQLBuilder sqlBuilder) {
        sqlBuilder.appendLiterals(originalSQL);
        return sqlBuilder;
    }
    
    private void appendInitialLiterals(final boolean isRewrite, final SQLBuilder sqlBuilder) {
        if (isRewrite && isContainsAggregationDistinctToken()) {
            appendAggregationDistinctLiteral(sqlBuilder);
        } else {
            sqlBuilder.appendLiterals(originalSQL.substring(0, sqlTokens.get(0).getBeginPosition()));
        }
    }
    
    private boolean isContainsAggregationDistinctToken() {
        return Iterators.tryFind(sqlTokens.iterator(), new Predicate<SQLToken>() {
            
            @Override
            public boolean apply(final SQLToken input) {
                return input instanceof AggregationDistinctToken;
            }
        }).isPresent();
    }
    
    private void appendAggregationDistinctLiteral(final SQLBuilder sqlBuilder) {
        int firstSelectItemStartPosition = ((SelectStatement) sqlStatement).getFirstSelectItemStartPosition();
        sqlBuilder.appendLiterals(originalSQL.substring(0, firstSelectItemStartPosition));
        sqlBuilder.appendLiterals("DISTINCT ");
        sqlBuilder.appendLiterals(originalSQL.substring(firstSelectItemStartPosition, sqlTokens.get(0).getBeginPosition()));
    }
    
    private void appendTokensAndPlaceholders(final boolean isRewrite, final SQLBuilder sqlBuilder) {
        int count = 0;
        for (SQLToken each : sqlTokens) {
            if (each instanceof TableToken) {
                appendTablePlaceholder(sqlBuilder, (TableToken) each, count);
            } else if (each instanceof SchemaToken) {
                appendSchemaPlaceholder(sqlBuilder, (SchemaToken) each, count);
            } else if (each instanceof IndexToken) {
                appendIndexPlaceholder(sqlBuilder, (IndexToken) each, count);
            } else if (each instanceof ItemsToken) {
                appendItemsToken(sqlBuilder, (ItemsToken) each, count, isRewrite);
            } else if (each instanceof InsertValuesToken) {
                appendInsertValuesToken(sqlBuilder, (InsertValuesToken) each, count);
            } else if (each instanceof RowCountToken) {
                appendLimitRowCount(sqlBuilder, (RowCountToken) each, count, isRewrite);
            } else if (each instanceof OffsetToken) {
                appendLimitOffsetToken(sqlBuilder, (OffsetToken) each, count, isRewrite);
            } else if (each instanceof OrderByToken) {
                appendOrderByToken(sqlBuilder, count, isRewrite);
            } else if (each instanceof InsertColumnToken) {
                appendSymbolToken(sqlBuilder, (InsertColumnToken) each, count);
            } else if (each instanceof AggregationDistinctToken) {
                appendAggregationDistinctPlaceholder(sqlBuilder, (AggregationDistinctToken) each, count, isRewrite);
            } else if (each instanceof RemoveToken) {
                appendRest(sqlBuilder, count, ((RemoveToken) each).getEndPosition());
            }
            count++;
        }
    }
    
    private void appendTablePlaceholder(final SQLBuilder sqlBuilder, final TableToken tableToken, final int count) {
        sqlBuilder.appendPlaceholder(new TablePlaceholder(tableToken.getTableName().toLowerCase(), tableToken.getOriginalLiterals()));
        int beginPosition = tableToken.getBeginPosition() + tableToken.getSkippedSchemaNameLength() + tableToken.getOriginalLiterals().length();
        appendRest(sqlBuilder, count, beginPosition);
    }
    
    private void appendSchemaPlaceholder(final SQLBuilder sqlBuilder, final SchemaToken schemaToken, final int count) {
        sqlBuilder.appendPlaceholder(new SchemaPlaceholder(schemaToken.getSchemaName().toLowerCase(), schemaToken.getTableName().toLowerCase()));
        int beginPosition = schemaToken.getBeginPosition() + schemaToken.getOriginalLiterals().length();
        appendRest(sqlBuilder, count, beginPosition);
    }
    
    private void appendIndexPlaceholder(final SQLBuilder sqlBuilder, final IndexToken indexToken, final int count) {
        String indexName = indexToken.getIndexName().toLowerCase();
        String logicTableName = indexToken.getTableName().toLowerCase();
        if (Strings.isNullOrEmpty(logicTableName)) {
            logicTableName = shardingRule.getLogicTableName(indexName);
        }
        sqlBuilder.appendPlaceholder(new IndexPlaceholder(indexName, logicTableName));
        int beginPosition = indexToken.getBeginPosition() + indexToken.getOriginalLiterals().length();
        appendRest(sqlBuilder, count, beginPosition);
    }
    
    private void appendItemsToken(final SQLBuilder sqlBuilder, final ItemsToken itemsToken, final int count, final boolean isRewrite) {
        boolean isRewriteItem = isRewrite || sqlStatement instanceof InsertStatement;
        for (int i = 0; i < itemsToken.getItems().size() && isRewriteItem; i++) {
            if (itemsToken.isFirstOfItemsSpecial() && 0 == i) {
                sqlBuilder.appendLiterals(SQLUtil.getOriginalValue(itemsToken.getItems().get(i), databaseType));
            } else {
                sqlBuilder.appendLiterals(", ");
                sqlBuilder.appendLiterals(SQLUtil.getOriginalValue(itemsToken.getItems().get(i), databaseType));
            }
        }
        appendRest(sqlBuilder, count, itemsToken.getBeginPosition());
    }
    
    private void appendInsertValuesToken(final SQLBuilder sqlBuilder, final InsertValuesToken insertValuesToken, final int count) {
        sqlBuilder.appendPlaceholder(new InsertValuesPlaceholder(insertValuesToken.getTableName().toLowerCase(), shardingConditions));
        appendRest(sqlBuilder, count, ((InsertStatement) sqlStatement).getInsertValuesListLastPosition());
    }
    
    private void appendLimitRowCount(final SQLBuilder sqlBuilder, final RowCountToken rowCountToken, final int count, final boolean isRewrite) {
        SelectStatement selectStatement = (SelectStatement) sqlStatement;
        Limit limit = selectStatement.getLimit();
        if (!isRewrite) {
            sqlBuilder.appendLiterals(String.valueOf(rowCountToken.getRowCount()));
        } else if ((!selectStatement.getGroupByItems().isEmpty() || !selectStatement.getAggregationSelectItems().isEmpty()) && !selectStatement.isSameGroupByAndOrderByItems()) {
            sqlBuilder.appendLiterals(String.valueOf(Integer.MAX_VALUE));
        } else {
            sqlBuilder.appendLiterals(String.valueOf(limit.isNeedRewriteRowCount(databaseType) ? rowCountToken.getRowCount() + limit.getOffsetValue() : rowCountToken.getRowCount()));
        }
        int beginPosition = rowCountToken.getBeginPosition() + String.valueOf(rowCountToken.getRowCount()).length();
        appendRest(sqlBuilder, count, beginPosition);
    }
    
    private void appendLimitOffsetToken(final SQLBuilder sqlBuilder, final OffsetToken offsetToken, final int count, final boolean isRewrite) {
        sqlBuilder.appendLiterals(isRewrite ? "0" : String.valueOf(offsetToken.getOffset()));
        int beginPosition = offsetToken.getBeginPosition() + String.valueOf(offsetToken.getOffset()).length();
        appendRest(sqlBuilder, count, beginPosition);
    }
    
    private void appendOrderByToken(final SQLBuilder sqlBuilder, final int count, final boolean isRewrite) {
        SelectStatement selectStatement = (SelectStatement) sqlStatement;
        if (isRewrite) {
            StringBuilder orderByLiterals = new StringBuilder();
            orderByLiterals.append(" ").append(DefaultKeyword.ORDER).append(" ").append(DefaultKeyword.BY).append(" ");
            int i = 0;
            for (OrderItem each : selectStatement.getOrderByItems()) {
                String columnLabel = Strings.isNullOrEmpty(each.getColumnLabel()) ? String.valueOf(each.getIndex())
                    : SQLUtil.getOriginalValue(each.getColumnLabel(), databaseType);
                if (0 == i) {
                    orderByLiterals.append(columnLabel).append(" ").append(each.getOrderDirection().name());
                } else {
                    orderByLiterals.append(",").append(columnLabel).append(" ").append(each.getOrderDirection().name());
                }
                i++;
            }
            orderByLiterals.append(" ");
            sqlBuilder.appendLiterals(orderByLiterals.toString());
        }
        int beginPosition = selectStatement.getGroupByLastPosition();
        appendRest(sqlBuilder, count, beginPosition);
    }
    
    private void appendSymbolToken(final SQLBuilder sqlBuilder, final InsertColumnToken insertColumnToken, final int count) {
        sqlBuilder.appendLiterals(insertColumnToken.getColumnName());
        appendRest(sqlBuilder, count, insertColumnToken.getBeginPosition());
    }
    
    private void appendAggregationDistinctPlaceholder(final SQLBuilder sqlBuilder, final AggregationDistinctToken distinctToken, final int count, final boolean isRewrite) {
        if (!isRewrite) {
            sqlBuilder.appendLiterals(distinctToken.getOriginalLiterals()); 
        } else {
            sqlBuilder.appendPlaceholder(new AggregationDistinctPlaceholder(distinctToken.getColumnName().toLowerCase(), null, distinctToken.getAlias()));
        }
        appendRest(sqlBuilder, count, distinctToken.getBeginPosition() + distinctToken.getOriginalLiterals().length());
    }
    
    private void appendRest(final SQLBuilder sqlBuilder, final int count, final int beginPosition) {
        int endPosition = sqlTokens.size() - 1 == count ? originalSQL.length() : sqlTokens.get(count + 1).getBeginPosition();
        sqlBuilder.appendLiterals(originalSQL.substring(beginPosition, endPosition));
    }
    
    /**
     * Generate SQL string.
     * 
     * @param tableUnit route table unit
     * @param sqlBuilder SQL builder
     * @param shardingDataSourceMetaData sharding data source meta data
     * @return SQL unit
     */
    public SQLUnit generateSQL(final TableUnit tableUnit, final SQLBuilder sqlBuilder, final ShardingDataSourceMetaData shardingDataSourceMetaData) {
        return sqlBuilder.toSQL(tableUnit, getTableTokens(tableUnit), shardingRule, shardingDataSourceMetaData);
    }
   
    private Map<String, String> getTableTokens(final TableUnit tableUnit) {
        Map<String, String> result = new HashMap<>();
        for (RoutingTable each : tableUnit.getRoutingTables()) {
            String logicTableName = each.getLogicTableName().toLowerCase();
            result.put(logicTableName, each.getActualTableName());
            Optional<BindingTableRule> bindingTableRule = shardingRule.findBindingTableRule(logicTableName);
            if (bindingTableRule.isPresent()) {
                result.putAll(getBindingTableTokens(tableUnit.getDataSourceName(), each, bindingTableRule.get()));
            }
        }
        return result;
    }
    
    private Map<String, String> getBindingTableTokens(final String dataSourceName, final RoutingTable routingTable, final BindingTableRule bindingTableRule) {
        Map<String, String> result = new HashMap<>();
        for (String each : sqlStatement.getTables().getTableNames()) {
            String tableName = each.toLowerCase();
            if (!tableName.equals(routingTable.getLogicTableName().toLowerCase()) && bindingTableRule.hasLogicTable(tableName)) {
                result.put(tableName, bindingTableRule.getBindingActualTable(dataSourceName, tableName, routingTable.getActualTableName()));
            }
        }
        return result;
    }
}
