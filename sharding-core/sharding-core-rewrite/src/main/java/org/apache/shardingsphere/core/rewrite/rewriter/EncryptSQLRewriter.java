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

package org.apache.shardingsphere.core.rewrite.rewriter;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.apache.shardingsphere.core.optimize.result.OptimizeResult;
import org.apache.shardingsphere.core.optimize.result.insert.InsertOptimizeResult;
import org.apache.shardingsphere.core.optimize.result.insert.InsertOptimizeResultUnit;
import org.apache.shardingsphere.core.parse.sql.context.condition.Condition;
import org.apache.shardingsphere.core.parse.sql.context.condition.ParseCondition;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.UpdateStatement;
import org.apache.shardingsphere.core.parse.sql.token.SQLToken;
import org.apache.shardingsphere.core.rewrite.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.builder.SQLBuilder;
import org.apache.shardingsphere.core.rewrite.placeholder.InsertAssistedColumnsPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.InsertSetAddItemsPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.InsertSetEncryptValuePlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.InsertValuePlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.InsertValuesPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.ShardingPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.UpdateEncryptAssistedItemPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.UpdateEncryptItemPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.WhereEncryptColumnPlaceholder;
import org.apache.shardingsphere.core.rewrite.token.pojo.EncryptColumnToken;
import org.apache.shardingsphere.core.rewrite.token.pojo.InsertAssistedColumnsToken;
import org.apache.shardingsphere.core.rewrite.token.pojo.InsertSetAddAssistedColumnsToken;
import org.apache.shardingsphere.core.rewrite.token.pojo.InsertSetEncryptValueToken;
import org.apache.shardingsphere.core.rewrite.token.pojo.InsertValuesToken;
import org.apache.shardingsphere.core.rule.ColumnNode;
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
 * SQL rewriter encrypt.
 * 
 * @author panjuan
 */
public final class EncryptSQLRewriter implements SQLRewriter {
    
    private final ShardingEncryptorEngine encryptorEngine;
    
    private final SQLStatement sqlStatement;
    
    private final InsertOptimizeResult insertOptimizeResult;
    
    public EncryptSQLRewriter(final ShardingEncryptorEngine encryptorEngine, final SQLStatement sqlStatement, final OptimizeResult optimizeResult) {
        this.encryptorEngine = encryptorEngine;
        this.sqlStatement = sqlStatement;
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
        for (InsertOptimizeResultUnit each : insertOptimizeResult.get().getUnits()) {
            encryptInsertOptimizeResultUnit(each, insertOptimizeResult.get().getColumnNames());
        }
        return insertOptimizeResult.get();
    }
    
    private void encryptInsertOptimizeResultUnit(final InsertOptimizeResultUnit unit, final Collection<String> columnNames) {
        for (String each : columnNames) {
            Optional<ShardingEncryptor> shardingEncryptor = encryptorEngine.getShardingEncryptor(sqlStatement.getTables().getSingleTableName(), each);
            if (shardingEncryptor.isPresent()) {
                encryptInsertOptimizeResult(unit, each, shardingEncryptor.get());
            }
        }
    }
    
    private void encryptInsertOptimizeResult(final InsertOptimizeResultUnit unit, final String columnName, final ShardingEncryptor shardingEncryptor) {
        if (shardingEncryptor instanceof ShardingQueryAssistedEncryptor) {
            Optional<String> assistedColumnName = encryptorEngine.getAssistedQueryColumn(sqlStatement.getTables().getSingleTableName(), columnName);
            Preconditions.checkArgument(assistedColumnName.isPresent(), "Can not find assisted query Column Name");
            unit.setColumnValue(assistedColumnName.get(), ((ShardingQueryAssistedEncryptor) shardingEncryptor).queryAssistedEncrypt(unit.getColumnValue(columnName).toString()));
        }
        unit.setColumnValue(columnName, shardingEncryptor.encrypt(unit.getColumnValue(columnName)));
    }
    
    @Override
    public void rewrite(final SQLBuilder sqlBuilder, final ParameterBuilder parameterBuilder, final SQLToken sqlToken) {
        parameterBuilder.setInsertParameterUnits(insertOptimizeResult);
        if (sqlToken instanceof InsertValuesToken) {
            appendInsertValuesPlaceholder(sqlBuilder, insertOptimizeResult);
        } else if (sqlToken instanceof InsertSetEncryptValueToken) {
            appendInsertSetEncryptValuePlaceholder(sqlBuilder, (InsertSetEncryptValueToken) sqlToken, insertOptimizeResult);
        } else if (sqlToken instanceof InsertSetAddAssistedColumnsToken) {
            appendInsertSetAddItemsPlaceholder(sqlBuilder, (InsertSetAddAssistedColumnsToken) sqlToken, insertOptimizeResult);
        } else if (sqlToken instanceof InsertAssistedColumnsToken) {
            appendInsertAssistedColumnsPlaceholder(sqlBuilder, (InsertAssistedColumnsToken) sqlToken);
        } else if (sqlToken instanceof EncryptColumnToken) {
            appendEncryptColumnPlaceholder(sqlBuilder, (EncryptColumnToken) sqlToken, parameterBuilder);
        }
    }
    
    private void appendInsertValuesPlaceholder(final SQLBuilder sqlBuilder, final InsertOptimizeResult insertOptimizeResult) {
        List<InsertValuePlaceholder> insertValues = new LinkedList<>();
        for (InsertOptimizeResultUnit each : insertOptimizeResult.getUnits()) {
            insertValues.add(new InsertValuePlaceholder(new ArrayList<>(each.getColumnNames()), Arrays.asList(each.getValues()), each.getDataNodes()));
        }
        sqlBuilder.appendPlaceholder(new InsertValuesPlaceholder(insertValues));
    }
    
    private void appendInsertSetEncryptValuePlaceholder(final SQLBuilder sqlBuilder, final InsertSetEncryptValueToken insertSetEncryptValueToken, final InsertOptimizeResult insertOptimizeResult) {
        sqlBuilder.appendPlaceholder(new InsertSetEncryptValuePlaceholder(insertOptimizeResult.getUnits().get(0).getColumnSQLExpression(insertSetEncryptValueToken.getColumnName())));
    }
    
    private void appendInsertSetAddItemsPlaceholder(
            final SQLBuilder sqlBuilder, final InsertSetAddAssistedColumnsToken insertSetAddAssistedColumnsToken, final InsertOptimizeResult insertOptimizeResult) {
        List<ExpressionSegment> columnValues = new LinkedList<>();
        for (String each : insertSetAddAssistedColumnsToken.getColumnNames()) {
            columnValues.add(insertOptimizeResult.getUnits().get(0).getColumnSQLExpression(each));
        }
        sqlBuilder.appendPlaceholder(new InsertSetAddItemsPlaceholder(new LinkedList<>(insertSetAddAssistedColumnsToken.getColumnNames()), columnValues));
    }
    
    private void appendInsertAssistedColumnsPlaceholder(final SQLBuilder sqlBuilder, final InsertAssistedColumnsToken insertAssistedColumnsToken) {
        sqlBuilder.appendPlaceholder(new InsertAssistedColumnsPlaceholder(insertAssistedColumnsToken.getColumns(), insertAssistedColumnsToken.isToAddCloseParenthesis()));
    }
    
    private void appendEncryptColumnPlaceholder(final SQLBuilder sqlBuilder, final EncryptColumnToken encryptColumnToken, final ParameterBuilder parameterBuilder) {
        Optional<Condition> encryptCondition = getEncryptCondition(sqlStatement.getEncryptCondition(), encryptColumnToken);
        Preconditions.checkArgument(!encryptColumnToken.isInWhere() || encryptCondition.isPresent(), "Can not find encrypt condition");
        ShardingPlaceholder result = encryptColumnToken.isInWhere() ? getEncryptColumnPlaceholderFromConditions(encryptColumnToken, encryptCondition.get(), parameterBuilder) 
                : getEncryptColumnPlaceholderFromUpdateItem(encryptColumnToken, parameterBuilder);
        sqlBuilder.appendPlaceholder(result);
    }
    
    private Optional<Condition> getEncryptCondition(final ParseCondition encryptCondition, final EncryptColumnToken encryptColumnToken) {
        for (Condition each : encryptCondition.findConditions(encryptColumnToken.getColumn())) {
            if (isSameIndexes(each.getPredicateSegment(), encryptColumnToken)) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
    
    private boolean isSameIndexes(final PredicateSegment predicateSegment, final EncryptColumnToken encryptColumnToken) {
        return predicateSegment.getStartIndex() == encryptColumnToken.getStartIndex() && predicateSegment.getStopIndex() == encryptColumnToken.getStopIndex();
    }
    
    private WhereEncryptColumnPlaceholder getEncryptColumnPlaceholderFromConditions(
            final EncryptColumnToken encryptColumnToken, final Condition encryptCondition, final ParameterBuilder parameterBuilder) {
        ColumnNode columnNode = new ColumnNode(encryptColumnToken.getColumn().getTableName(), encryptColumnToken.getColumn().getName());
        List<Comparable<?>> encryptColumnValues = encryptValues(columnNode, encryptCondition.getConditionValues(parameterBuilder.getOriginalParameters()));
        encryptParameters(encryptCondition.getPositionIndexMap(), encryptColumnValues, parameterBuilder);
        Optional<String> assistedColumnName = encryptorEngine.getAssistedQueryColumn(columnNode.getTableName(), columnNode.getColumnName());
        return new WhereEncryptColumnPlaceholder(assistedColumnName.isPresent() ? assistedColumnName.get() : columnNode.getColumnName(),
                getPositionValues(encryptCondition.getPositionValueMap().keySet(), encryptColumnValues), encryptCondition.getPositionIndexMap().keySet(), encryptCondition.getOperator());
    }
    
    private List<Comparable<?>> encryptValues(final ColumnNode columnNode, final List<Comparable<?>> columnValues) {
        return encryptorEngine.getAssistedQueryColumn(columnNode.getTableName(), columnNode.getColumnName()).isPresent()
                ? encryptorEngine.getEncryptAssistedColumnValues(columnNode, columnValues) : encryptorEngine.getEncryptColumnValues(columnNode, columnValues);
    }
    
    private void encryptParameters(final Map<Integer, Integer> positionIndexes, final List<Comparable<?>> encryptColumnValues, final ParameterBuilder parameterBuilder) {
        if (!positionIndexes.isEmpty()) {
            for (Entry<Integer, Integer> entry : positionIndexes.entrySet()) {
                parameterBuilder.getOriginalParameters().set(entry.getValue(), encryptColumnValues.get(entry.getKey()));
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
    
    private ShardingPlaceholder getEncryptColumnPlaceholderFromUpdateItem(final EncryptColumnToken encryptColumnToken, final ParameterBuilder parameterBuilder) {
        ColumnNode columnNode = new ColumnNode(encryptColumnToken.getColumn().getTableName(), encryptColumnToken.getColumn().getName());
        Comparable<?> originalColumnValue = ((UpdateStatement) sqlStatement).getColumnValue(encryptColumnToken.getColumn(), parameterBuilder.getOriginalParameters());
        List<Comparable<?>> encryptColumnValues = encryptorEngine.getEncryptColumnValues(columnNode, Collections.<Comparable<?>>singletonList(originalColumnValue));
        encryptParameters(getPositionIndexesFromUpdateItem(encryptColumnToken), encryptColumnValues, parameterBuilder);
        Optional<String> assistedColumnName = encryptorEngine.getAssistedQueryColumn(columnNode.getTableName(), columnNode.getColumnName());
        if (!assistedColumnName.isPresent()) {
            return getUpdateEncryptItemPlaceholder(encryptColumnToken, encryptColumnValues);
        }
        List<Comparable<?>> encryptAssistedColumnValues = encryptorEngine.getEncryptAssistedColumnValues(columnNode, Collections.<Comparable<?>>singletonList(originalColumnValue));
        parameterBuilder.getAddedIndexAndParameters().putAll(getIndexAndParameters(encryptColumnToken, encryptAssistedColumnValues));
        return getUpdateEncryptAssistedItemPlaceholder(encryptColumnToken, encryptColumnValues, encryptAssistedColumnValues);
    }
    
    private Map<Integer, Integer> getPositionIndexesFromUpdateItem(final EncryptColumnToken encryptColumnToken) {
        ExpressionSegment result = ((UpdateStatement) sqlStatement).getAssignments().get(encryptColumnToken.getColumn());
        return result instanceof ParameterMarkerExpressionSegment
                ? Collections.singletonMap(0, ((ParameterMarkerExpressionSegment) result).getParameterMarkerIndex()) : new LinkedHashMap<Integer, Integer>();
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
        String assistedColumnName = encryptorEngine.getAssistedQueryColumn(encryptColumnToken.getColumn().getTableName(), encryptColumnToken.getColumn().getName()).get();
        if (isUsingParameter(encryptColumnToken)) {
            return new UpdateEncryptAssistedItemPlaceholder(encryptColumnToken.getColumn().getName(), assistedColumnName);
        }
        return new UpdateEncryptAssistedItemPlaceholder(encryptColumnToken.getColumn().getName(), encryptColumnValues.get(0), assistedColumnName, encryptAssistedColumnValues.get(0));
    }
    
    private boolean isUsingParameter(final EncryptColumnToken encryptColumnToken) {
        return ((UpdateStatement) sqlStatement).getAssignments().get(encryptColumnToken.getColumn()) instanceof ParameterMarkerExpressionSegment;
    }
}
