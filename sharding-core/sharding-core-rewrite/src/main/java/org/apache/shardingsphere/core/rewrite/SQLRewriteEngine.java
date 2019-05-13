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
import com.google.common.base.Strings;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.metadata.datasource.ShardingDataSourceMetaData;
import org.apache.shardingsphere.core.optimize.result.OptimizeResult;
import org.apache.shardingsphere.core.optimize.result.insert.InsertOptimizeResult;
import org.apache.shardingsphere.core.optimize.result.insert.InsertOptimizeResultUnit;
import org.apache.shardingsphere.core.parse.antlr.sql.Substitutable;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.AbstractSQLStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.UpdateStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.token.AggregationDistinctToken;
import org.apache.shardingsphere.core.parse.antlr.sql.token.EncryptColumnToken;
import org.apache.shardingsphere.core.parse.antlr.sql.token.IndexToken;
import org.apache.shardingsphere.core.parse.antlr.sql.token.InsertSetToken;
import org.apache.shardingsphere.core.parse.antlr.sql.token.InsertValuesToken;
import org.apache.shardingsphere.core.parse.antlr.sql.token.OffsetToken;
import org.apache.shardingsphere.core.parse.antlr.sql.token.OrderByToken;
import org.apache.shardingsphere.core.parse.antlr.sql.token.RemoveToken;
import org.apache.shardingsphere.core.parse.antlr.sql.token.RowCountToken;
import org.apache.shardingsphere.core.parse.antlr.sql.token.SQLToken;
import org.apache.shardingsphere.core.parse.antlr.sql.token.SchemaToken;
import org.apache.shardingsphere.core.parse.antlr.sql.token.SelectItemsToken;
import org.apache.shardingsphere.core.parse.antlr.sql.token.TableToken;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.Condition;
import org.apache.shardingsphere.core.parse.old.parser.context.limit.Limit;
import org.apache.shardingsphere.core.parse.old.parser.context.orderby.OrderItem;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLParameterMarkerExpression;
import org.apache.shardingsphere.core.parse.util.SQLUtil;
import org.apache.shardingsphere.core.rewrite.placeholder.AggregationDistinctPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.EncryptUpdateItemColumnPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.EncryptWhereColumnPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.IndexPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.InsertSetPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.InsertValuesPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.LimitOffsetPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.LimitRowCountPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.OrderByPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.SchemaPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.SelectItemsPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.ShardingPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.TablePlaceholder;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.SQLUnit;
import org.apache.shardingsphere.core.route.type.TableUnit;
import org.apache.shardingsphere.core.route.type.RoutingUnit;
import org.apache.shardingsphere.core.rule.BindingTableRule;
import org.apache.shardingsphere.core.rule.ColumnNode;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.strategy.encrypt.ShardingEncryptorEngine;
import org.apache.shardingsphere.spi.encrypt.ShardingEncryptor;
import org.apache.shardingsphere.spi.encrypt.ShardingQueryAssistedEncryptor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
    
    private final SQLRouteResult sqlRouteResult;
    
    private final SQLStatement sqlStatement;
    
    private final List<Object> parameters;
    
    private final Map<Integer, Object> appendedIndexAndParameters;
    
    private final OptimizeResult optimizeResult;
    
    private final ShardingDataSourceMetaData dataSourceMetaData;
    
    /**
     * Constructs SQL rewrite engine.
     * 
     * @param shardingRule databases and tables sharding rule
     * @param originalSQL original SQL
     * @param databaseType database type
     * @param sqlRouteResult SQL route result
     * @param parameters parameters
     */
    public SQLRewriteEngine(final ShardingRule shardingRule, final String originalSQL, 
                            final DatabaseType databaseType, final SQLRouteResult sqlRouteResult, final List<Object> parameters, final ShardingDataSourceMetaData dataSourceMetaData) {
        this.shardingRule = shardingRule;
        this.originalSQL = originalSQL;
        this.databaseType = databaseType;
        this.sqlRouteResult = sqlRouteResult;
        sqlStatement = sqlRouteResult.getSqlStatement();
        this.parameters = parameters;
        appendedIndexAndParameters = new LinkedHashMap<>();
        this.optimizeResult = sqlRouteResult.getOptimizeResult();
        this.dataSourceMetaData = dataSourceMetaData;
    }
    
    /**
     * rewrite SQL.
     *
     * @return SQL builder
     */
    public SQLBuilder rewrite() {
        SQLBuilder result = new SQLBuilder(parameters);
        if (sqlStatement.getSQLTokens().isEmpty()) {
            return appendOriginalLiterals(result);
        }
        appendInitialLiterals(!sqlRouteResult.getRoutingResult().isSingleRouting(), result);
        appendTokensAndPlaceholders(!sqlRouteResult.getRoutingResult().isSingleRouting(), result);
        reviseParameters();
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
            sqlBuilder.appendLiterals(originalSQL.substring(0, sqlStatement.getSQLTokens().get(0).getStartIndex()));
        }
    }
    
    private boolean isContainsAggregationDistinctToken() {
        return Iterators.tryFind(sqlStatement.getSQLTokens().iterator(), new Predicate<SQLToken>() {
            
            @Override
            public boolean apply(final SQLToken input) {
                return input instanceof AggregationDistinctToken;
            }
        }).isPresent();
    }
    
    private void appendAggregationDistinctLiteral(final SQLBuilder sqlBuilder) {
        StringBuilder stringBuilder = new StringBuilder();
        int firstSelectItemStartIndex = ((SelectStatement) sqlStatement).getFirstSelectItemStartIndex();
        stringBuilder.append(originalSQL.substring(0, firstSelectItemStartIndex)).append("DISTINCT ")
                .append(originalSQL.substring(firstSelectItemStartIndex, sqlStatement.getSQLTokens().get(0).getStartIndex()));
        sqlBuilder.appendLiterals(stringBuilder.toString());
    }
    
    private void appendTokensAndPlaceholders(final boolean isRewrite, final SQLBuilder sqlBuilder) {
        int count = 0;
        for (SQLToken each : sqlStatement.getSQLTokens()) {
            if (each instanceof TableToken) {
                appendTablePlaceholder(sqlBuilder, (TableToken) each, count);
            } else if (each instanceof SchemaToken) {
                appendSchemaPlaceholder(sqlBuilder, (SchemaToken) each, count);
            } else if (each instanceof IndexToken) {
                appendIndexPlaceholder(sqlBuilder, (IndexToken) each, count);
            } else if (each instanceof SelectItemsToken) {
                appendSelectItemsPlaceholder(sqlBuilder, (SelectItemsToken) each, count, isRewrite);
            } else if (each instanceof InsertValuesToken) {
                appendInsertValuesPlaceholder(sqlBuilder, (InsertValuesToken) each, count, optimizeResult.getInsertOptimizeResult().get());
            } else if (each instanceof InsertSetToken) {
                appendInsertSetPlaceholder(sqlBuilder, (InsertSetToken) each, count, optimizeResult.getInsertOptimizeResult().get());
            } else if (each instanceof RowCountToken) {
                appendLimitRowCountPlaceholder(sqlBuilder, (RowCountToken) each, count, isRewrite);
            } else if (each instanceof OffsetToken) {
                appendLimitOffsetPlaceholder(sqlBuilder, (OffsetToken) each, count, isRewrite);
            } else if (each instanceof OrderByToken) {
                appendOrderByPlaceholder(sqlBuilder, (OrderByToken) each, count, isRewrite);
            } else if (each instanceof AggregationDistinctToken) {
                appendAggregationDistinctPlaceholder(sqlBuilder, (AggregationDistinctToken) each, count, isRewrite);
            } else if (each instanceof EncryptColumnToken) {
                appendEncryptColumnPlaceholder(sqlBuilder, (EncryptColumnToken) each, count);
            } else if (each instanceof RemoveToken) {
                appendRest(sqlBuilder, count, getStopIndex(each));
            }
            count++;
        }
    }
    
    private void appendTablePlaceholder(final SQLBuilder sqlBuilder, final TableToken tableToken, final int count) {
        sqlBuilder.appendPlaceholder(new TablePlaceholder(tableToken.getTableName().toLowerCase(), tableToken.getQuoteCharacter()));
        appendRest(sqlBuilder, count, tableToken.getStopIndex() + 1);
    }
    
    private void appendSchemaPlaceholder(final SQLBuilder sqlBuilder, final SchemaToken schemaToken, final int count) {
        String schemaName = originalSQL.substring(schemaToken.getStartIndex(), schemaToken.getStopIndex() + 1);
        sqlBuilder.appendPlaceholder(new SchemaPlaceholder(schemaName.toLowerCase(), schemaToken.getTableName().toLowerCase(), shardingRule, dataSourceMetaData));
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
    
    private void appendSelectItemsPlaceholder(final SQLBuilder sqlBuilder, final SelectItemsToken selectItemsToken, final int count, final boolean isRewrite) {
        boolean isRewriteItem = isRewrite || sqlStatement instanceof InsertStatement;
        if (isRewriteItem) {
            SelectItemsPlaceholder selectItemsPlaceholder = new SelectItemsPlaceholder(selectItemsToken.isFirstOfItemsSpecial());
            selectItemsPlaceholder.getItems().addAll(Lists.transform(selectItemsToken.getItems(), new Function<String, String>() {
                
                @Override
                public String apply(final String input) {
                    return SQLUtil.getOriginalValue(input, databaseType);
                }
            }));
            sqlBuilder.appendPlaceholder(selectItemsPlaceholder);
        }
        appendRest(sqlBuilder, count, getStopIndex(selectItemsToken));
    }
    
    private void appendInsertValuesPlaceholder(final SQLBuilder sqlBuilder, final InsertValuesToken insertValuesToken, final int count, final InsertOptimizeResult insertOptimizeResult) {
        for (InsertOptimizeResultUnit each : insertOptimizeResult.getUnits()) {
            encryptInsertOptimizeResultUnit(insertOptimizeResult.getColumnNames(), each);
        }
        sqlBuilder.appendPlaceholder(new InsertValuesPlaceholder(sqlStatement.getTables().getSingleTableName(), insertOptimizeResult.getColumnNames(), insertOptimizeResult.getUnits()));
        appendRest(sqlBuilder, count, getStopIndex(insertValuesToken));
    }
    
    private void appendInsertSetPlaceholder(final SQLBuilder sqlBuilder, final InsertSetToken insertSetToken, final int count, final InsertOptimizeResult insertOptimizeResult) {
        for (InsertOptimizeResultUnit each : insertOptimizeResult.getUnits()) {
            encryptInsertOptimizeResultUnit(insertOptimizeResult.getColumnNames(), each);
        }
        sqlBuilder.appendPlaceholder(new InsertSetPlaceholder(sqlStatement.getTables().getSingleTableName(), insertOptimizeResult.getColumnNames(), insertOptimizeResult.getUnits()));
        appendRest(sqlBuilder, count, getStopIndex(insertSetToken));
    }
    
    private void encryptInsertOptimizeResultUnit(final Collection<String> columnNames, final InsertOptimizeResultUnit unit) {
        for (String each : columnNames) {
            Optional<ShardingEncryptor> shardingEncryptor = shardingRule.getShardingEncryptorEngine().getShardingEncryptor(sqlStatement.getTables().getSingleTableName(), each);
            if (shardingEncryptor.isPresent()) {
                encryptInsertOptimizeResultUnit(unit, each, shardingEncryptor.get());
            }
        }
    }
    
    private void encryptInsertOptimizeResultUnit(final InsertOptimizeResultUnit unit, final String columnName, final ShardingEncryptor shardingEncryptor) {
        if (shardingEncryptor instanceof ShardingQueryAssistedEncryptor) {
            String assistedColumnName = shardingRule.getShardingEncryptorEngine().getAssistedQueryColumn(sqlStatement.getTables().getSingleTableName(), columnName).get();
            unit.setColumnValue(assistedColumnName, ((ShardingQueryAssistedEncryptor) shardingEncryptor).queryAssistedEncrypt(unit.getColumnValue(columnName).toString()));
        }
        unit.setColumnValue(columnName, shardingEncryptor.encrypt(unit.getColumnValue(columnName)));
    }
    
    private void appendLimitRowCountPlaceholder(final SQLBuilder sqlBuilder, final RowCountToken rowCountToken, final int count, final boolean isRewrite) {
        SelectStatement selectStatement = (SelectStatement) sqlStatement;
        sqlBuilder.appendPlaceholder(new LimitRowCountPlaceholder(getRowCount(rowCountToken, isRewrite, selectStatement, sqlRouteResult.getLimit())));
        appendRest(sqlBuilder, count, getStopIndex(rowCountToken));
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
    
    private void appendLimitOffsetPlaceholder(final SQLBuilder sqlBuilder, final OffsetToken offsetToken, final int count, final boolean isRewrite) {
        sqlBuilder.appendPlaceholder(new LimitOffsetPlaceholder(isRewrite ? 0 : offsetToken.getOffset()));
        appendRest(sqlBuilder, count, getStopIndex(offsetToken));
    }
    
    private void appendOrderByPlaceholder(final SQLBuilder sqlBuilder, final OrderByToken orderByToken, final int count, final boolean isRewrite) {
        SelectStatement selectStatement = (SelectStatement) sqlStatement;
        OrderByPlaceholder orderByPlaceholder = new OrderByPlaceholder();
        if (isRewrite) {
            for (OrderItem each : selectStatement.getOrderByItems()) {
                String columnLabel = Strings.isNullOrEmpty(each.getColumnLabel()) ? String.valueOf(each.getIndex()) : SQLUtil.getOriginalValue(each.getColumnLabel(), databaseType);
                orderByPlaceholder.getColumnLabels().add(columnLabel);
                orderByPlaceholder.getOrderDirections().add(each.getOrderDirection());
            }
            sqlBuilder.appendPlaceholder(orderByPlaceholder);
        }
        appendRest(sqlBuilder, count, getStopIndex(orderByToken));
    }
    
    private void appendAggregationDistinctPlaceholder(final SQLBuilder sqlBuilder, final AggregationDistinctToken distinctToken, final int count, final boolean isRewrite) {
        if (!isRewrite) {
            sqlBuilder.appendLiterals(originalSQL.substring(distinctToken.getStartIndex(), distinctToken.getStopIndex() + 1)); 
        } else {
            sqlBuilder.appendPlaceholder(new AggregationDistinctPlaceholder(distinctToken.getColumnName().toLowerCase(), distinctToken.getAlias()));
        }
        appendRest(sqlBuilder, count, getStopIndex(distinctToken));
    }
    
    private void appendEncryptColumnPlaceholder(final SQLBuilder sqlBuilder, final EncryptColumnToken encryptColumnToken, final int count) {
        Optional<Condition> encryptCondition = ((AbstractSQLStatement) sqlStatement).getEncryptCondition(encryptColumnToken);
        Preconditions.checkArgument(!encryptColumnToken.isInWhere() || encryptCondition.isPresent(), "Can not find encrypt condition");
        ShardingPlaceholder result = encryptColumnToken.isInWhere() 
                ? getEncryptColumnPlaceholderFromConditions(encryptColumnToken, encryptCondition.get()) : getEncryptColumnPlaceholderFromUpdateItem(encryptColumnToken);
        sqlBuilder.appendPlaceholder(result);
        appendRest(sqlBuilder, count, getStopIndex(encryptColumnToken));
    }
    
    private EncryptWhereColumnPlaceholder getEncryptColumnPlaceholderFromConditions(final EncryptColumnToken encryptColumnToken, final Condition encryptCondition) {
        ColumnNode columnNode = new ColumnNode(encryptColumnToken.getColumn().getTableName(), encryptColumnToken.getColumn().getName());
        List<Comparable<?>> encryptColumnValues = encryptValues(columnNode, encryptCondition.getConditionValues(parameters));
        encryptParameters(encryptCondition.getPositionIndexMap(), encryptColumnValues);
        Optional<String> assistedColumnName = shardingRule.getShardingEncryptorEngine().getAssistedQueryColumn(columnNode.getTableName(), columnNode.getColumnName());
        return new EncryptWhereColumnPlaceholder(assistedColumnName.isPresent() ? assistedColumnName.get() : columnNode.getColumnName(),
                getPositionValues(encryptCondition.getPositionValueMap().keySet(), encryptColumnValues), encryptCondition.getPositionIndexMap().keySet(), encryptCondition.getOperator());
    }
    
    private List<Comparable<?>> encryptValues(final ColumnNode columnNode, final List<Comparable<?>> columnValues) {
        ShardingEncryptorEngine shardingEncryptorEngine = shardingRule.getShardingEncryptorEngine();
        return shardingEncryptorEngine.getAssistedQueryColumn(columnNode.getTableName(), columnNode.getColumnName()).isPresent() 
                ? shardingEncryptorEngine.getEncryptAssistedColumnValues(columnNode, columnValues) : shardingEncryptorEngine.getEncryptColumnValues(columnNode, columnValues);
    }
    
    private void encryptParameters(final Map<Integer, Integer> positionIndexes, final List<Comparable<?>> encryptColumnValues) {
        if (!positionIndexes.isEmpty()) {
            for (Entry<Integer, Integer> entry : positionIndexes.entrySet()) {
                parameters.set(entry.getValue(), encryptColumnValues.get(entry.getKey()));
            }
        }
    }
    
    private Map<Integer, Comparable<?>> getPositionValues(final Collection<Integer> valuePositions, final List<Comparable<?>> encryptColumnValues) {
        Map<Integer, Comparable<?>> result = new LinkedHashMap<>();
        for (int each : valuePositions) {
            result.put(each, encryptColumnValues.get(each));
        }
        return result;
    }
    
    private EncryptUpdateItemColumnPlaceholder getEncryptColumnPlaceholderFromUpdateItem(final EncryptColumnToken encryptColumnToken) {
        ColumnNode columnNode = new ColumnNode(encryptColumnToken.getColumn().getTableName(), encryptColumnToken.getColumn().getName());
        ShardingEncryptorEngine shardingEncryptorEngine = shardingRule.getShardingEncryptorEngine();
        Comparable<?> originalColumnValue = ((UpdateStatement) sqlStatement).getColumnValue(encryptColumnToken.getColumn(), parameters);
        List<Comparable<?>> encryptColumnValues = shardingEncryptorEngine.getEncryptColumnValues(columnNode, Collections.<Comparable<?>>singletonList(originalColumnValue));
        encryptParameters(getPositionIndexesFromUpdateItem(encryptColumnToken), encryptColumnValues);
        Optional<String> assistedColumnName = shardingEncryptorEngine.getAssistedQueryColumn(columnNode.getTableName(), columnNode.getColumnName());
        if (!assistedColumnName.isPresent()) {
            return getEncryptUpdateItemColumnPlaceholder(encryptColumnToken, encryptColumnValues);
        }
        List<Comparable<?>> encryptAssistedColumnValues = shardingEncryptorEngine.getEncryptAssistedColumnValues(columnNode, Collections.<Comparable<?>>singletonList(originalColumnValue));
        appendIndexAndParameters(encryptColumnToken, encryptAssistedColumnValues);
        return getEncryptUpdateItemColumnPlaceholder(encryptColumnToken, encryptColumnValues, encryptAssistedColumnValues);
    }
    
    private Map<Integer, Integer> getPositionIndexesFromUpdateItem(final EncryptColumnToken encryptColumnToken) {
        SQLExpression result = ((UpdateStatement) sqlStatement).getAssignments().get(encryptColumnToken.getColumn());
        if (result instanceof SQLParameterMarkerExpression) {
            return Collections.singletonMap(0, ((SQLParameterMarkerExpression) result).getIndex());
        }
        return new LinkedHashMap<>();
    }
    
    private void appendIndexAndParameters(final EncryptColumnToken encryptColumnToken, final List<Comparable<?>> encryptAssistedColumnValues) {
        if (encryptAssistedColumnValues.isEmpty()) {
            return;
        }
        if (!isUsingParameter(encryptColumnToken)) {
            return;
        }
        appendedIndexAndParameters.put(getPositionIndexesFromUpdateItem(encryptColumnToken).values().iterator().next() + 1, encryptAssistedColumnValues.get(0));
    }
    
    private EncryptUpdateItemColumnPlaceholder getEncryptUpdateItemColumnPlaceholder(final EncryptColumnToken encryptColumnToken, final List<Comparable<?>> encryptColumnValues) {
        if (isUsingParameter(encryptColumnToken)) {
            return new EncryptUpdateItemColumnPlaceholder(encryptColumnToken.getColumn().getName());
        }
        return new EncryptUpdateItemColumnPlaceholder(encryptColumnToken.getColumn().getName(), encryptColumnValues.get(0));
    }
    
    private EncryptUpdateItemColumnPlaceholder getEncryptUpdateItemColumnPlaceholder(final EncryptColumnToken encryptColumnToken,
                                                                                     final List<Comparable<?>> encryptColumnValues, final List<Comparable<?>> encryptAssistedColumnValues) {
        String assistedColumnName = shardingRule.getShardingEncryptorEngine().getAssistedQueryColumn(encryptColumnToken.getColumn().getTableName(), encryptColumnToken.getColumn().getName()).get();
        if (isUsingParameter(encryptColumnToken)) {
            return new EncryptUpdateItemColumnPlaceholder(encryptColumnToken.getColumn().getName(), assistedColumnName);
        }
        return new EncryptUpdateItemColumnPlaceholder(encryptColumnToken.getColumn().getName(), encryptColumnValues.get(0), assistedColumnName, encryptAssistedColumnValues.get(0));
    }
    
    private boolean isUsingParameter(final EncryptColumnToken encryptColumnToken) {
        return ((UpdateStatement) sqlStatement).isSQLParameterMarkerExpression(encryptColumnToken.getColumn());
    }
    
    private int getStopIndex(final SQLToken sqlToken) {
        return sqlToken instanceof Substitutable ? ((Substitutable) sqlToken).getStopIndex() + 1 : sqlToken.getStartIndex();
    }
    
    private void appendRest(final SQLBuilder sqlBuilder, final int count, final int startIndex) {
        int stopPosition = sqlStatement.getSQLTokens().size() - 1 == count ? originalSQL.length() : sqlStatement.getSQLTokens().get(count + 1).getStartIndex();
        sqlBuilder.appendLiterals(originalSQL.substring(startIndex > originalSQL.length() ? originalSQL.length() : startIndex, stopPosition));
    }
    
    /**
     * Generate SQL string.
     * 
     * @param routingUnit routing unit
     * @param sqlBuilder SQL builder
     * @return SQL unit
     */
    public SQLUnit generateSQL(final RoutingUnit routingUnit, final SQLBuilder sqlBuilder) {
        return sqlBuilder.toSQL(routingUnit, getTableTokens(routingUnit));
    }
   
    private Map<String, String> getTableTokens(final RoutingUnit routingUnit) {
        Map<String, String> result = new HashMap<>();
        for (TableUnit each : routingUnit.getTableUnits()) {
            String logicTableName = each.getLogicTableName().toLowerCase();
            result.put(logicTableName, each.getActualTableName());
            Optional<BindingTableRule> bindingTableRule = shardingRule.findBindingTableRule(logicTableName);
            if (bindingTableRule.isPresent()) {
                result.putAll(getBindingTableTokens(routingUnit.getMasterSlaveLogicDataSourceName(), each, bindingTableRule.get()));
            }
        }
        return result;
    }
    
    private Map<String, String> getBindingTableTokens(final String dataSourceName, final TableUnit tableUnit, final BindingTableRule bindingTableRule) {
        Map<String, String> result = new HashMap<>();
        for (String each : sqlStatement.getTables().getTableNames()) {
            String tableName = each.toLowerCase();
            if (!tableName.equals(tableUnit.getLogicTableName().toLowerCase()) && bindingTableRule.hasLogicTable(tableName)) {
                result.put(tableName, bindingTableRule.getBindingActualTable(dataSourceName, tableName, tableUnit.getActualTableName()));
            }
        }
        return result;
    }
    
    private void reviseParameters() {
        for (Entry<Integer, Object> entry : appendedIndexAndParameters.entrySet()) {
            parameters.add(entry.getKey(), entry.getValue());
        }
    }
}
