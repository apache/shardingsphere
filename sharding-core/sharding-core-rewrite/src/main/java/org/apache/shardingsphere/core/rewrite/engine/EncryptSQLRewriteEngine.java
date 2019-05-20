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

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.optimize.result.OptimizeResult;
import org.apache.shardingsphere.core.optimize.result.insert.InsertOptimizeResult;
import org.apache.shardingsphere.core.optimize.result.insert.InsertOptimizeResultUnit;
import org.apache.shardingsphere.core.parse.sql.context.condition.Condition;
import org.apache.shardingsphere.core.parse.sql.context.expression.SQLExpression;
import org.apache.shardingsphere.core.parse.sql.context.expression.SQLParameterMarkerExpression;
import org.apache.shardingsphere.core.parse.sql.statement.AbstractSQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.UpdateStatement;
import org.apache.shardingsphere.core.parse.sql.token.SQLToken;
import org.apache.shardingsphere.core.parse.sql.token.Substitutable;
import org.apache.shardingsphere.core.parse.sql.token.impl.EncryptColumnToken;
import org.apache.shardingsphere.core.parse.sql.token.impl.InsertColumnsToken;
import org.apache.shardingsphere.core.parse.sql.token.impl.InsertSetAddItemsToken;
import org.apache.shardingsphere.core.parse.sql.token.impl.InsertSetEncryptValueToken;
import org.apache.shardingsphere.core.parse.sql.token.impl.InsertValuesToken;
import org.apache.shardingsphere.core.parse.sql.token.impl.RemoveToken;
import org.apache.shardingsphere.core.parse.sql.token.impl.SelectItemsToken;
import org.apache.shardingsphere.core.parse.util.SQLUtil;
import org.apache.shardingsphere.core.rewrite.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.SQLBuilder;
import org.apache.shardingsphere.core.rewrite.placeholder.InsertColumnsPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.InsertSetAddItemsPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.InsertSetEncryptValuePlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.InsertValuePlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.InsertValuesPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.SelectItemsPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.ShardingPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.UpdateEncryptAssistedItemPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.UpdateEncryptItemPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.WhereEncryptColumnPlaceholder;
import org.apache.shardingsphere.core.route.SQLUnit;
import org.apache.shardingsphere.core.route.type.RoutingUnit;
import org.apache.shardingsphere.core.rule.ColumnNode;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.strategy.encrypt.ShardingEncryptorEngine;
import org.apache.shardingsphere.spi.encrypt.ShardingEncryptor;
import org.apache.shardingsphere.spi.encrypt.ShardingQueryAssistedEncryptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Encrypt SQL rewrite engine.
 * 
 * <p>Rewrite logic SQL to actual SQL, should rewrite table name and optimize something.</p>
 *
 * @author panjuan
 */
public final class EncryptSQLRewriteEngine implements SQLRewriteEngine {
    
    private final EncryptRule encryptRule;
    
    private final String originalSQL;
    
    private final DatabaseType databaseType;
    
    private final SQLStatement sqlStatement;
    
    private final List<SQLToken> sqlTokens;
    
    private final InsertOptimizeResult insertOptimizeResult;
    
    private final ParameterBuilder parametersBuilder;
    
    public EncryptSQLRewriteEngine(final EncryptRule encryptRule, 
                                   final String originalSQL, final DatabaseType databaseType, final SQLStatement sqlStatement, final List<Object> parameters, final OptimizeResult optimizeResult) {
        this.encryptRule = encryptRule;
        this.originalSQL = originalSQL;
        this.databaseType = databaseType;
        this.sqlStatement = sqlStatement;
        sqlTokens = sqlStatement.getSQLTokens();
        this.insertOptimizeResult = getInsertOptimizeResult(optimizeResult);
        this.parametersBuilder = new ParameterBuilder(parameters, insertOptimizeResult);
    }
    
    private InsertOptimizeResult getInsertOptimizeResult(final OptimizeResult optimizeResult) {
        if (null == optimizeResult) {
            return null;
        }
        Optional<InsertOptimizeResult> insertOptimizeResult = optimizeResult.getInsertOptimizeResult();
        if (!insertOptimizeResult.isPresent()) {
            return null;
        }
        for (InsertOptimizeResultUnit each : insertOptimizeResult.get().getUnits()) {
            encryptInsertOptimizeResultUnit(insertOptimizeResult.get().getColumnNames(), each);
        }
        return insertOptimizeResult.get();
    }
    
    private void encryptInsertOptimizeResultUnit(final Collection<String> columnNames, final InsertOptimizeResultUnit unit) {
        for (String each : columnNames) {
            Optional<ShardingEncryptor> shardingEncryptor = encryptRule.getEncryptorEngine().getShardingEncryptor(sqlStatement.getTables().getSingleTableName(), each);
            if (shardingEncryptor.isPresent()) {
                encryptInsertOptimizeResult(unit, each, shardingEncryptor.get());
            }
        }
    }
    
    private void encryptInsertOptimizeResult(final InsertOptimizeResultUnit unit, final String columnName, final ShardingEncryptor shardingEncryptor) {
        if (shardingEncryptor instanceof ShardingQueryAssistedEncryptor) {
            String assistedColumnName = encryptRule.getEncryptorEngine().getAssistedQueryColumn(sqlStatement.getTables().getSingleTableName(), columnName).get();
            unit.setColumnValue(
                    assistedColumnName, ((ShardingQueryAssistedEncryptor) shardingEncryptor).queryAssistedEncrypt(unit.getColumnValue(columnName).toString()));
        }
        unit.setColumnValue(columnName, shardingEncryptor.encrypt(unit.getColumnValue(columnName)));
    }
    
    @Override
    public SQLBuilder rewrite() {
        SQLBuilder result = new SQLBuilder(parametersBuilder);
        if (sqlTokens.isEmpty()) {
            return appendOriginalLiterals(result);
        }
        appendTokensAndPlaceholders(result);
        return result;
    }
    
    private SQLBuilder appendOriginalLiterals(final SQLBuilder sqlBuilder) {
        sqlBuilder.appendLiterals(originalSQL);
        return sqlBuilder;
    }
    
    private void appendTokensAndPlaceholders(final SQLBuilder sqlBuilder) {
        int count = 0;
        sqlBuilder.appendLiterals(originalSQL.substring(0, sqlTokens.get(0).getStartIndex()));
        for (SQLToken each : sqlTokens) {
            if (each instanceof SelectItemsToken) {
                appendSelectItemsPlaceholder(sqlBuilder, (SelectItemsToken) each, count);
            } else if (each instanceof InsertColumnsToken) {
                appendInsertColumnsPlaceholder(sqlBuilder, (InsertColumnsToken) each, count);
            } else if (each instanceof InsertValuesToken) {
                appendInsertValuesPlaceholder(sqlBuilder, (InsertValuesToken) each, count, insertOptimizeResult);
            } else if (each instanceof InsertSetEncryptValueToken) {
                appendInsertSetEncryptValuePlaceholder(sqlBuilder, (InsertSetEncryptValueToken) each, count, insertOptimizeResult);
            } else if (each instanceof InsertSetAddItemsToken) {
                appendInsertSetAddItemsPlaceholder(sqlBuilder, (InsertSetAddItemsToken) each, count, insertOptimizeResult);
            } else if (each instanceof EncryptColumnToken) {
                appendEncryptColumnPlaceholder(sqlBuilder, (EncryptColumnToken) each, count);
            } else if (each instanceof RemoveToken) {
                appendRest(sqlBuilder, count, getStopIndex(each));
            }
            count++;
        }
    }
    
    private void appendSelectItemsPlaceholder(final SQLBuilder sqlBuilder, final SelectItemsToken selectItemsToken, final int count) {
        if (sqlStatement instanceof InsertStatement) {
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
    
    private void appendInsertColumnsPlaceholder(final SQLBuilder sqlBuilder, final InsertColumnsToken insertColumnsToken, final int count) {
        InsertColumnsPlaceholder columnsPlaceholder = new InsertColumnsPlaceholder(insertColumnsToken.isPartColumns());
        columnsPlaceholder.getColumns().addAll(insertColumnsToken.getColumns());
        sqlBuilder.appendPlaceholder(columnsPlaceholder);
        appendRest(sqlBuilder, count, getStopIndex(insertColumnsToken));
    }
    
    private void appendInsertValuesPlaceholder(final SQLBuilder sqlBuilder, final InsertValuesToken insertValuesToken, final int count, final InsertOptimizeResult insertOptimizeResult) {
        List<InsertValuePlaceholder> insertValues = new LinkedList<>();
        for (InsertOptimizeResultUnit each : insertOptimizeResult.getUnits()) {
            insertValues.add(new InsertValuePlaceholder(new ArrayList<>(each.getColumnNames()), Arrays.asList(each.getValues()), each.getDataNodes()));
        }
        sqlBuilder.appendPlaceholder(new InsertValuesPlaceholder(insertValues));
        appendRest(sqlBuilder, count, getStopIndex(insertValuesToken));
    }
    
    private void appendInsertSetEncryptValuePlaceholder(final SQLBuilder sqlBuilder,
                                                       final InsertSetEncryptValueToken insertSetEncryptValueToken, final int count, final InsertOptimizeResult insertOptimizeResult) {
        sqlBuilder.appendPlaceholder(new InsertSetEncryptValuePlaceholder(insertOptimizeResult.getUnits().get(0).getColumnSQLExpression(insertSetEncryptValueToken.getColumnName())));
        appendRest(sqlBuilder, count, getStopIndex(insertSetEncryptValueToken));
    }
    
    private void appendInsertSetAddItemsPlaceholder(final SQLBuilder sqlBuilder,
                                                    final InsertSetAddItemsToken insertSetAddItemsToken, final int count, final InsertOptimizeResult insertOptimizeResult) {
        List<SQLExpression> columnValues = new LinkedList<>();
        for (String each : insertSetAddItemsToken.getColumnNames()) {
            columnValues.add(insertOptimizeResult.getUnits().get(0).getColumnSQLExpression(each));
        }
        sqlBuilder.appendPlaceholder(new InsertSetAddItemsPlaceholder(new LinkedList<>(insertSetAddItemsToken.getColumnNames()), columnValues));
        appendRest(sqlBuilder, count, getStopIndex(insertSetAddItemsToken));
    }
    
    private void appendEncryptColumnPlaceholder(final SQLBuilder sqlBuilder, final EncryptColumnToken encryptColumnToken, final int count) {
        Optional<Condition> encryptCondition = ((AbstractSQLStatement) sqlStatement).getEncryptCondition(encryptColumnToken);
        Preconditions.checkArgument(!encryptColumnToken.isInWhere() || encryptCondition.isPresent(), "Can not find encrypt condition");
        ShardingPlaceholder result = encryptColumnToken.isInWhere()
                ? getEncryptColumnPlaceholderFromConditions(encryptColumnToken, encryptCondition.get()) : getEncryptColumnPlaceholderFromUpdateItem(encryptColumnToken);
        sqlBuilder.appendPlaceholder(result);
        appendRest(sqlBuilder, count, getStopIndex(encryptColumnToken));
    }
    
    private WhereEncryptColumnPlaceholder getEncryptColumnPlaceholderFromConditions(final EncryptColumnToken encryptColumnToken, final Condition encryptCondition) {
        ColumnNode columnNode = new ColumnNode(encryptColumnToken.getColumn().getTableName(), encryptColumnToken.getColumn().getName());
        List<Comparable<?>> encryptColumnValues = encryptValues(columnNode, encryptCondition.getConditionValues(parametersBuilder.getOriginalParameters()));
        encryptParameters(encryptCondition.getPositionIndexMap(), encryptColumnValues);
        Optional<String> assistedColumnName = encryptRule.getEncryptorEngine().getAssistedQueryColumn(columnNode.getTableName(), columnNode.getColumnName());
        return new WhereEncryptColumnPlaceholder(assistedColumnName.isPresent() ? assistedColumnName.get() : columnNode.getColumnName(),
                getPositionValues(encryptCondition.getPositionValueMap().keySet(), encryptColumnValues), encryptCondition.getPositionIndexMap().keySet(), encryptCondition.getOperator());
    }
    
    private List<Comparable<?>> encryptValues(final ColumnNode columnNode, final List<Comparable<?>> columnValues) {
        ShardingEncryptorEngine encryptorEngine = encryptRule.getEncryptorEngine();
        return encryptorEngine.getAssistedQueryColumn(columnNode.getTableName(), columnNode.getColumnName()).isPresent()
                ? encryptorEngine.getEncryptAssistedColumnValues(columnNode, columnValues) : encryptorEngine.getEncryptColumnValues(columnNode, columnValues);
    }
    
    private void encryptParameters(final Map<Integer, Integer> positionIndexes, final List<Comparable<?>> encryptColumnValues) {
        if (!positionIndexes.isEmpty()) {
            for (Entry<Integer, Integer> entry : positionIndexes.entrySet()) {
                parametersBuilder.getOriginalParameters().set(entry.getValue(), encryptColumnValues.get(entry.getKey()));
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
    
    private ShardingPlaceholder getEncryptColumnPlaceholderFromUpdateItem(final EncryptColumnToken encryptColumnToken) {
        ColumnNode columnNode = new ColumnNode(encryptColumnToken.getColumn().getTableName(), encryptColumnToken.getColumn().getName());
        ShardingEncryptorEngine encryptorEngine = encryptRule.getEncryptorEngine();
        Comparable<?> originalColumnValue = ((UpdateStatement) sqlStatement).getColumnValue(encryptColumnToken.getColumn(), parametersBuilder.getOriginalParameters());
        List<Comparable<?>> encryptColumnValues = encryptorEngine.getEncryptColumnValues(columnNode, Collections.<Comparable<?>>singletonList(originalColumnValue));
        encryptParameters(getPositionIndexesFromUpdateItem(encryptColumnToken), encryptColumnValues);
        Optional<String> assistedColumnName = encryptorEngine.getAssistedQueryColumn(columnNode.getTableName(), columnNode.getColumnName());
        if (!assistedColumnName.isPresent()) {
            return getUpdateEncryptItemPlaceholder(encryptColumnToken, encryptColumnValues);
        }
        List<Comparable<?>> encryptAssistedColumnValues = encryptorEngine.getEncryptAssistedColumnValues(columnNode, Collections.<Comparable<?>>singletonList(originalColumnValue));
        parametersBuilder.getAssistedIndexAndParametersForUpdate().putAll(getIndexAndParameters(encryptColumnToken, encryptAssistedColumnValues));
        return getUpdateEncryptAssistedItemPlaceholder(encryptColumnToken, encryptColumnValues, encryptAssistedColumnValues);
    }
    
    private Map<Integer, Integer> getPositionIndexesFromUpdateItem(final EncryptColumnToken encryptColumnToken) {
        SQLExpression result = ((UpdateStatement) sqlStatement).getAssignments().get(encryptColumnToken.getColumn());
        if (result instanceof SQLParameterMarkerExpression) {
            return Collections.singletonMap(0, ((SQLParameterMarkerExpression) result).getIndex());
        }
        return new LinkedHashMap<>();
    }
    
    private Map<Integer, Object> getIndexAndParameters(final EncryptColumnToken encryptColumnToken, final List<Comparable<?>> encryptAssistedColumnValues) {
        if (encryptAssistedColumnValues.isEmpty()) {
            return Collections.emptyMap();
        }
        if (!isUsingParameter(encryptColumnToken)) {
            return Collections.emptyMap();
        }
        return Collections.singletonMap(getPositionIndexesFromUpdateItem(encryptColumnToken).values().iterator().next() + 1, (Object) encryptAssistedColumnValues.get(0));
    }
    
    private UpdateEncryptItemPlaceholder getUpdateEncryptItemPlaceholder(final EncryptColumnToken encryptColumnToken, final List<Comparable<?>> encryptColumnValues) {
        if (isUsingParameter(encryptColumnToken)) {
            return new UpdateEncryptItemPlaceholder(encryptColumnToken.getColumn().getTableName(), encryptColumnToken.getColumn().getName());
        }
        return new UpdateEncryptItemPlaceholder(encryptColumnToken.getColumn().getName(), encryptColumnValues.get(0));
    }
    
    private UpdateEncryptAssistedItemPlaceholder getUpdateEncryptAssistedItemPlaceholder(final EncryptColumnToken encryptColumnToken,
                                                                                         final List<Comparable<?>> encryptColumnValues, final List<Comparable<?>> encryptAssistedColumnValues) {
        String assistedColumnName = encryptRule.getEncryptorEngine().getAssistedQueryColumn(encryptColumnToken.getColumn().getTableName(), encryptColumnToken.getColumn().getName()).get();
        if (isUsingParameter(encryptColumnToken)) {
            return new UpdateEncryptAssistedItemPlaceholder(encryptColumnToken.getColumn().getName(), assistedColumnName);
        }
        return new UpdateEncryptAssistedItemPlaceholder(encryptColumnToken.getColumn().getName(), encryptColumnValues.get(0), assistedColumnName, encryptAssistedColumnValues.get(0));
    }
    
    private boolean isUsingParameter(final EncryptColumnToken encryptColumnToken) {
        return ((UpdateStatement) sqlStatement).isSQLParameterMarkerExpression(encryptColumnToken.getColumn());
    }
    
    private int getStopIndex(final SQLToken sqlToken) {
        return sqlToken instanceof Substitutable ? ((Substitutable) sqlToken).getStopIndex() + 1 : sqlToken.getStartIndex();
    }
    
    private void appendRest(final SQLBuilder sqlBuilder, final int count, final int startIndex) {
        int stopPosition = sqlTokens.size() - 1 == count ? originalSQL.length() : sqlTokens.get(count + 1).getStartIndex();
        sqlBuilder.appendLiterals(originalSQL.substring(startIndex, stopPosition));
    }
    
    @Override
    public SQLUnit generateSQL(final RoutingUnit routingUnit, final SQLBuilder sqlBuilder) {
        return sqlBuilder.toSQL();
    }
}
