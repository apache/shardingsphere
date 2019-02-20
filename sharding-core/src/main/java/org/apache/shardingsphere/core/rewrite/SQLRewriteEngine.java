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

package org.apache.shardingsphere.core.rewrite;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.core.metadata.datasource.ShardingDataSourceMetaData;
import org.apache.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Column;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Condition;
import org.apache.shardingsphere.core.parsing.parser.context.limit.Limit;
import org.apache.shardingsphere.core.parsing.parser.context.orderby.OrderItem;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLNumberExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLPlaceholderExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLTextExpression;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.parsing.parser.sql.dml.DMLStatement;
import org.apache.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import org.apache.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import org.apache.shardingsphere.core.parsing.parser.token.AggregationDistinctToken;
import org.apache.shardingsphere.core.parsing.parser.token.EncryptColumnToken;
import org.apache.shardingsphere.core.parsing.parser.token.IndexToken;
import org.apache.shardingsphere.core.parsing.parser.token.InsertColumnToken;
import org.apache.shardingsphere.core.parsing.parser.token.InsertValuesToken;
import org.apache.shardingsphere.core.parsing.parser.token.ItemsToken;
import org.apache.shardingsphere.core.parsing.parser.token.OffsetToken;
import org.apache.shardingsphere.core.parsing.parser.token.OrderByToken;
import org.apache.shardingsphere.core.parsing.parser.token.RemoveToken;
import org.apache.shardingsphere.core.parsing.parser.token.RowCountToken;
import org.apache.shardingsphere.core.parsing.parser.token.SQLToken;
import org.apache.shardingsphere.core.parsing.parser.token.SchemaToken;
import org.apache.shardingsphere.core.parsing.parser.token.TableToken;
import org.apache.shardingsphere.core.rewrite.placeholder.AggregationDistinctPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.EncryptUpdateItemColumnPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.EncryptWhereColumnPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.IndexPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.InsertValuesPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.SchemaPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.TablePlaceholder;
import org.apache.shardingsphere.core.routing.SQLUnit;
import org.apache.shardingsphere.core.routing.type.RoutingTable;
import org.apache.shardingsphere.core.routing.type.TableUnit;
import org.apache.shardingsphere.core.rule.BindingTableRule;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.spi.hook.SPIRewriteHook;
import org.apache.shardingsphere.core.util.SQLUtil;
import org.apache.shardingsphere.spi.algorithm.encrypt.ShardingEncryptor;
import org.apache.shardingsphere.spi.algorithm.encrypt.ShardingQueryAssistedEncryptor;
import org.apache.shardingsphere.spi.hook.RewriteHook;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
    
    private final List<Object> parameters;
    
    private final RewriteHook rewriteHook = new SPIRewriteHook();
    
    /**
     * Constructs SQL rewrite engine.
     * 
     * @param shardingRule databases and tables sharding rule
     * @param originalSQL original SQL
     * @param databaseType database type
     * @param sqlStatement SQL statement
     * @param parameters parameters
     */
    public SQLRewriteEngine(final ShardingRule shardingRule, final String originalSQL, final DatabaseType databaseType, final SQLStatement sqlStatement, final List<Object> parameters) {
        this.shardingRule = shardingRule;
        this.originalSQL = originalSQL;
        this.databaseType = databaseType;
        this.sqlStatement = sqlStatement;
        sqlTokens = sqlStatement.getSQLTokens();
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
            sqlBuilder.appendLiterals(originalSQL.substring(0, sqlTokens.get(0).getStartIndex()));
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
        int firstSelectItemStartIndex = ((SelectStatement) sqlStatement).getFirstSelectItemStartIndex();
        sqlBuilder.appendLiterals(originalSQL.substring(0, firstSelectItemStartIndex));
        sqlBuilder.appendLiterals("DISTINCT ");
        sqlBuilder.appendLiterals(originalSQL.substring(firstSelectItemStartIndex, sqlTokens.get(0).getStartIndex()));
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
            } else if (each instanceof EncryptColumnToken) {
                appendEncryptColumnPlaceholder(sqlBuilder, (EncryptColumnToken) each, count);
            } else if (each instanceof RemoveToken) {
                appendRest(sqlBuilder, count, ((RemoveToken) each).getStopIndex());
            }
            count++;
        }
    }
    
    private void appendTablePlaceholder(final SQLBuilder sqlBuilder, final TableToken tableToken, final int count) {
        sqlBuilder.appendPlaceholder(new TablePlaceholder(tableToken.getTableName().toLowerCase(), tableToken.getLeftDelimiter(), tableToken.getRightDelimiter()));
        int beginPosition = tableToken.getStartIndex() + tableToken.getLength();
        appendRest(sqlBuilder, count, beginPosition);
    }
    
    private void appendSchemaPlaceholder(final SQLBuilder sqlBuilder, final SchemaToken schemaToken, final int count) {
        String schemaName = originalSQL.substring(schemaToken.getStartIndex(), schemaToken.getStopIndex() + 1);
        sqlBuilder.appendPlaceholder(new SchemaPlaceholder(schemaName.toLowerCase(), schemaToken.getTableName().toLowerCase()));
        appendRest(sqlBuilder, count, schemaToken.getStopIndex() + 1);
    }
    
    private void appendIndexPlaceholder(final SQLBuilder sqlBuilder, final IndexToken indexToken, final int count) {
        String indexName = originalSQL.substring(indexToken.getStartIndex(), indexToken.getStopIndex() + 1);
        String logicTableName = indexToken.getTableName().toLowerCase();
        if (Strings.isNullOrEmpty(logicTableName)) {
            logicTableName = shardingRule.getLogicTableName(indexName);
        }
        sqlBuilder.appendPlaceholder(new IndexPlaceholder(indexName, logicTableName));
        appendRest(sqlBuilder, count, indexToken.getStopIndex() + 1);
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
        appendRest(sqlBuilder, count, itemsToken.getStartIndex());
    }
    
    private void appendInsertValuesToken(final SQLBuilder sqlBuilder, final InsertValuesToken insertValuesToken, final int count) {
        sqlBuilder.appendPlaceholder(new InsertValuesPlaceholder(sqlStatement.getTables().getSingleTableName(), 
                insertValuesToken.getType(), insertValuesToken.getColumnNames(), insertValuesToken.getColumnValues()));
        appendRest(sqlBuilder, count, ((InsertStatement) sqlStatement).getInsertValuesListLastIndex() + 1);
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
        int beginPosition = rowCountToken.getStartIndex() + String.valueOf(rowCountToken.getRowCount()).length();
        appendRest(sqlBuilder, count, beginPosition);
    }
    
    private void appendLimitOffsetToken(final SQLBuilder sqlBuilder, final OffsetToken offsetToken, final int count, final boolean isRewrite) {
        sqlBuilder.appendLiterals(isRewrite ? "0" : String.valueOf(offsetToken.getOffset()));
        int beginPosition = offsetToken.getStartIndex() + String.valueOf(offsetToken.getOffset()).length();
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
        int beginPosition = selectStatement.getGroupByLastIndex() + 1;
        appendRest(sqlBuilder, count, beginPosition);
    }
    
    private void appendSymbolToken(final SQLBuilder sqlBuilder, final InsertColumnToken insertColumnToken, final int count) {
        sqlBuilder.appendLiterals(insertColumnToken.getColumnName());
        appendRest(sqlBuilder, count, insertColumnToken.getStartIndex());
    }
    
    private void appendAggregationDistinctPlaceholder(final SQLBuilder sqlBuilder, final AggregationDistinctToken distinctToken, final int count, final boolean isRewrite) {
        if (!isRewrite) {
            sqlBuilder.appendLiterals(originalSQL.substring(distinctToken.getStartIndex(), distinctToken.getStopIndex() + 1)); 
        } else {
            sqlBuilder.appendPlaceholder(new AggregationDistinctPlaceholder(distinctToken.getColumnName().toLowerCase(), null, distinctToken.getAlias()));
        }
        appendRest(sqlBuilder, count, distinctToken.getStopIndex() + 1);
    }
    
    private void appendEncryptColumnPlaceholder(final SQLBuilder sqlBuilder, final EncryptColumnToken encryptColumnToken, final int count) {
        Optional<Condition> encryptCondition = getEncryptCondition(encryptColumnToken);
        Preconditions.checkArgument(!encryptColumnToken.isInWhere() || encryptCondition.isPresent(), "Can not find encrypt condition");
        EncryptWhereColumnPlaceholder result = encryptColumnToken.isInWhere() 
                ? getEncryptColumnPlaceholderFromConditions(encryptColumnToken, encryptCondition.get()) : getEncryptColumnPlaceholderFromUpdateItem(encryptColumnToken);
        sqlBuilder.appendPlaceholder(result);
        appendRest(sqlBuilder, count, encryptColumnToken.getStopIndex() + 1);
    }
    
    private EncryptWhereColumnPlaceholder getEncryptColumnPlaceholderFromConditions(final EncryptColumnToken encryptColumnToken, final Condition encryptCondition) {
        List<Comparable<?>> encryptColumnValues = getEncryptColumnValues(encryptColumnToken, encryptCondition.getConditionValues(parameters));
        encryptParameters(encryptCondition.getPositionIndexMap(), encryptColumnValues);
        return new EncryptWhereColumnPlaceholder(encryptColumnToken.getColumn().getTableName(), getEncryptColumnName(encryptColumnToken), 
                getPositionValues(encryptCondition.getPositionValueMap().keySet(), encryptColumnValues), encryptCondition.getPositionIndexMap().keySet(), encryptCondition.getOperator());
    }
    
    private EncryptUpdateItemColumnPlaceholder getEncryptColumnPlaceholderFromUpdateItem(final EncryptColumnToken encryptColumnToken) {
        List<Comparable<?>> encryptColumnValues = getEncryptColumnValues(encryptColumnToken, getOriginalColumnValuesFromUpdateItem(encryptColumnToken));
        encryptParameters(getPositionIndexesFromUpdateItem(encryptColumnToken), encryptColumnValues);
        return new EncryptUpdateItemColumnPlaceholder(encryptColumnToken.getColumn().getTableName(), getEncryptColumnName(encryptColumnToken), 
                getPositionValues(Collections.singletonList(0), encryptColumnValues).values().iterator().next(), getPlaceholderPositionFromUpdateItem(encryptColumnToken));
    }
    
    private Optional<Condition> getEncryptCondition(final EncryptColumnToken encryptColumnToken) {
        List<Condition> conditions = sqlStatement.getEncryptConditions().getOrCondition().findConditions(encryptColumnToken.getColumn());
        if (0 == conditions.size()) {
            return Optional.absent();
        }
        if (1 == conditions.size()) {
            return Optional.of(conditions.iterator().next());
        }
        return Optional.of(conditions.get(getEncryptConditionIndex(encryptColumnToken)));
    }
    
    private int getEncryptConditionIndex(final EncryptColumnToken encryptColumnToken) {
        List<SQLToken> result = new ArrayList<>(Collections2.filter(sqlTokens, new Predicate<SQLToken>() {
            
            @Override
            public boolean apply(final SQLToken input) {
                return input instanceof EncryptColumnToken && ((EncryptColumnToken) input).getColumn().equals(encryptColumnToken.getColumn());
            }
        }));
        return result.indexOf(encryptColumnToken);
    }
    
    private List<Comparable<?>> getOriginalColumnValuesFromUpdateItem(final EncryptColumnToken encryptColumnToken) {
        List<Comparable<?>> result = new LinkedList<>();
        SQLExpression sqlExpression = ((DMLStatement) sqlStatement).getUpdateColumnValues().get(encryptColumnToken.getColumn());
        if (sqlExpression instanceof SQLPlaceholderExpression) {
            result.add(parameters.get(((SQLPlaceholderExpression) sqlExpression).getIndex()).toString());
        } else if (sqlExpression instanceof SQLTextExpression) {
            result.add(((SQLTextExpression) sqlExpression).getText());
        } else if (sqlExpression instanceof SQLNumberExpression) {
            result.add((Comparable) ((SQLNumberExpression) sqlExpression).getNumber());
        }
        return result;
    }
    
    private List<Comparable<?>> getEncryptColumnValues(final EncryptColumnToken encryptColumnToken, final List<Comparable<?>> originalColumnValues) {
        final ShardingEncryptor shardingEncryptor = getShardingEncryptor(encryptColumnToken);
        if (shardingEncryptor instanceof ShardingQueryAssistedEncryptor) {
            return Lists.transform(originalColumnValues, new Function<Comparable<?>, Comparable<?>>() {
                
                @Override
                public Comparable<?> apply(final Comparable<?> input) {
                    return ((ShardingQueryAssistedEncryptor) shardingEncryptor).queryAssistedEncrypt(input.toString());
                }
            });
        }
        return Lists.transform(originalColumnValues, new Function<Comparable<?>, Comparable<?>>() {
            
            @Override
            public Comparable<?> apply(final Comparable<?> input) {
                return String.valueOf(shardingEncryptor.encrypt(input.toString()));
            }
        });
    }
    
    private Map<Integer, Integer> getPositionIndexesFromUpdateItem(final EncryptColumnToken encryptColumnToken) {
        SQLExpression result = ((DMLStatement) sqlStatement).getUpdateColumnValues().get(encryptColumnToken.getColumn());
        if (result instanceof SQLPlaceholderExpression) {
            return Collections.singletonMap(0, ((SQLPlaceholderExpression) result).getIndex());
        }
        return new LinkedHashMap<>();
    }
    
    private void encryptParameters(final Map<Integer, Integer> positionIndexes, final List<Comparable<?>> encryptColumnValues) {
        if (!positionIndexes.isEmpty()) {
            for (Entry<Integer, Integer> entry : positionIndexes.entrySet()) {
                parameters.set(entry.getValue(), encryptColumnValues.get(entry.getKey()));
            }
        }
    }
    
    private String getEncryptColumnName(final EncryptColumnToken encryptColumnToken) {
        String result = getEncryptAssistedColumnName(encryptColumnToken);
        if (result != null)
            return result;
        return encryptColumnToken.getColumn().getName();
    }
    
    private String getEncryptAssistedColumnName(final EncryptColumnToken encryptColumnToken) {
        Column column = encryptColumnToken.getColumn();
        Optional<String> result = shardingRule.getTableRule(column.getTableName()).getShardingEncryptorStrategy().getAssistedQueryColumn(column.getName());
        Preconditions.checkArgument(result.isPresent(), "Can not find the assistedColumn of %s", encryptColumnToken.getColumn().getName());
        return result.get();
    }
    
    private ShardingEncryptor getShardingEncryptor(final EncryptColumnToken encryptColumnToken) {
        return shardingRule.getShardingEncryptorEngine().getShardingEncryptor(encryptColumnToken.getColumn().getTableName(), encryptColumnToken.getColumn().getName()).get();
    }
    
    private Map<Integer, Comparable<?>> getPositionValues(final Collection<Integer> valuePositions, final List<Comparable<?>> encryptColumnValues) {
        Map<Integer, Comparable<?>> result = new LinkedHashMap<>();
        for (int each : valuePositions) {
            result.put(each, encryptColumnValues.get(each));
        }
        return result;
    }
    
    private int getPlaceholderPositionFromUpdateItem(final EncryptColumnToken encryptColumnToken) {
        SQLExpression sqlExpression = ((DMLStatement) sqlStatement).getUpdateColumnValues().get(encryptColumnToken.getColumn());
        if (sqlExpression instanceof SQLPlaceholderExpression) {
            return ((SQLPlaceholderExpression) sqlExpression).getIndex();
        }
        return -1;
    }
    
    private void appendRest(final SQLBuilder sqlBuilder, final int count, final int beginPosition) {
        int endPosition = sqlTokens.size() - 1 == count ? originalSQL.length() : sqlTokens.get(count + 1).getStartIndex();
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
        rewriteHook.start(tableUnit);
        try {
            SQLUnit result = sqlBuilder.toSQL(tableUnit, getTableTokens(tableUnit), shardingRule, shardingDataSourceMetaData);
            rewriteHook.finishSuccess(result);
            return result;
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            rewriteHook.finishFailure(ex);
            throw ex;
        }
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
