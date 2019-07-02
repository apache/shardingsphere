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

package org.apache.shardingsphere.core.rewrite.token.generator;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.apache.shardingsphere.core.optimize.statement.OptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.context.condition.Column;
import org.apache.shardingsphere.core.parse.sql.context.condition.Condition;
import org.apache.shardingsphere.core.parse.sql.context.condition.Conditions;
import org.apache.shardingsphere.core.parse.sql.segment.SQLSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.SetAssignmentsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.DMLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.UpdateStatement;
import org.apache.shardingsphere.core.rewrite.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.builder.SQLBuilder;
import org.apache.shardingsphere.core.rewrite.placeholder.ShardingPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.UpdateEncryptAssistedItemPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.UpdateEncryptItemPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.WhereEncryptColumnPlaceholder;
import org.apache.shardingsphere.core.rewrite.token.pojo.EncryptColumnToken;
import org.apache.shardingsphere.core.rule.ColumnNode;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Encrypt column token generator.
 *
 * @author panjuan
 */
public final class EncryptColumnTokenGenerator implements CollectionSQLTokenGenerator<EncryptRule> {
    
    @Override
    public Collection<EncryptColumnToken> generateSQLTokens(final OptimizedStatement optimizedStatement, final List<Object> parameters, final EncryptRule encryptRule) {
        if (!(optimizedStatement.getSQLStatement() instanceof DMLStatement)) {
            return Collections.emptyList();
        }
        Collection<EncryptColumnToken> result = new LinkedList<>();
        for (SQLSegment each : optimizedStatement.getSQLStatement().getSQLSegments()) {
            if (each instanceof SetAssignmentsSegment) {
                result.addAll(createFromUpdateSetAssignment(optimizedStatement.getSQLStatement(), encryptRule, (SetAssignmentsSegment) each));
            }
        }
        result.addAll(createFromWhereCondition((DMLStatement) optimizedStatement.getSQLStatement()));
        return result;
    }
    
    private Collection<EncryptColumnToken> createFromUpdateSetAssignment(final SQLStatement sqlStatement, final EncryptRule encryptRule, final SetAssignmentsSegment segment) {
        Collection<EncryptColumnToken> result = new LinkedList<>();
        if (sqlStatement instanceof InsertStatement) {
            return result;
        }
        for (AssignmentSegment each : segment.getAssignments()) {
            Column column = new Column(each.getColumn().getName(), sqlStatement.getTables().getSingleTableName());
            if (encryptRule.getEncryptorEngine().getShardingEncryptor(column.getTableName(), column.getName()).isPresent()) {
                result.add(new EncryptColumnToken(each.getColumn().getStartIndex(), each.getStopIndex(), column, false));
            }
        }
        return result;
    }
    
    private Collection<EncryptColumnToken> createFromWhereCondition(final DMLStatement dmlStatement) {
        Collection<EncryptColumnToken> result = new LinkedList<>();
        if (dmlStatement.getEncryptConditions().getOrConditions().isEmpty()) {
            return result;
        }
        for (Condition each : dmlStatement.getEncryptConditions().getOrConditions().get(0).getConditions()) {
            result.add(new EncryptColumnToken(each.getPredicateSegment().getStartIndex(), each.getPredicateSegment().getStopIndex(), each.getColumn(), true));
        }
        return result;
    }
    
    private void appendEncryptColumnPlaceholder(final DMLStatement dmlStatement, final EncryptColumnToken encryptColumnToken, final ParameterBuilder parameterBuilder) {
        Optional<Condition> encryptCondition = getEncryptCondition(dmlStatement.getEncryptConditions(), encryptColumnToken);
        Preconditions.checkArgument(!encryptColumnToken.isInWhere() || encryptCondition.isPresent(), "Can not find encrypt condition");
        ShardingPlaceholder result = encryptColumnToken.isInWhere() ? getEncryptColumnPlaceholderFromConditions(encryptColumnToken, encryptCondition.get(), parameterBuilder)
                : getEncryptColumnPlaceholderFromUpdateItem(encryptColumnToken, parameterBuilder);
        sqlBuilder.appendPlaceholder(result);
    }
    
    private Optional<Condition> getEncryptCondition(final Conditions encryptConditions, final EncryptColumnToken encryptColumnToken) {
        for (Condition each : encryptConditions.findConditions(encryptColumnToken.getColumn())) {
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
        Comparable<?> originalColumnValue = ((UpdateStatement) dmlStatement).getColumnValue(encryptColumnToken.getColumn(), parameterBuilder.getOriginalParameters());
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
        ExpressionSegment result = ((UpdateStatement) dmlStatement).getAssignments().get(encryptColumnToken.getColumn());
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
    
    private boolean isUsingParameter(final DMLStatement dmlStatement, final Column column) {
        return ((UpdateStatement) dmlStatement).getAssignments().get(column) instanceof ParameterMarkerExpressionSegment;
    }
}
