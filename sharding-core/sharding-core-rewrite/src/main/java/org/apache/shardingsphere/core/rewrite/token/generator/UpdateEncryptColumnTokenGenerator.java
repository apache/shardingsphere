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
import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.core.optimize.statement.OptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.UpdateStatement;
import org.apache.shardingsphere.core.rewrite.builder.BaseParameterBuilder;
import org.apache.shardingsphere.core.rewrite.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.token.pojo.EncryptColumnToken;
import org.apache.shardingsphere.core.rewrite.token.pojo.UpdateEncryptAssistedItemToken;
import org.apache.shardingsphere.core.rewrite.token.pojo.UpdateEncryptItemToken;
import org.apache.shardingsphere.core.rule.ColumnNode;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.strategy.encrypt.ShardingEncryptorEngine;

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
public final class UpdateEncryptColumnTokenGenerator implements CollectionSQLTokenGenerator<EncryptRule> {
    
    private int startIndex;
    
    private int stopIndex;
    
    @Override
    public Collection<EncryptColumnToken> generateSQLTokens(final OptimizedStatement optimizedStatement, final ParameterBuilder parameterBuilder, final EncryptRule encryptRule) {
        return optimizedStatement.getSQLStatement() instanceof UpdateStatement
                ? createUpdateEncryptColumnTokens(optimizedStatement, parameterBuilder, encryptRule) : Collections.<EncryptColumnToken>emptyList();
    }
    
    private Collection<EncryptColumnToken> createUpdateEncryptColumnTokens(final OptimizedStatement optimizedStatement, final ParameterBuilder parameterBuilder, final EncryptRule encryptRule) {
        Collection<EncryptColumnToken> result = new LinkedList<>();
        String tableName = optimizedStatement.getSQLStatement().getTables().getSingleTableName();
        for (AssignmentSegment each : ((UpdateStatement) optimizedStatement.getSQLStatement()).getSetAssignment().getAssignments()) {
            String columnName = each.getColumn().getName();
            ShardingEncryptorEngine encryptorEngine = encryptRule.getEncryptorEngine();
            if (encryptorEngine.getShardingEncryptor(tableName, columnName).isPresent()) {
                this.startIndex = each.getColumn().getStartIndex();
                this.stopIndex = each.getStopIndex();
                result.add(createUpdateEncryptColumnToken(encryptorEngine, optimizedStatement, (BaseParameterBuilder) parameterBuilder, columnName, tableName));
            }
        }
        return result;
    }
    
    private EncryptColumnToken createUpdateEncryptColumnToken(final ShardingEncryptorEngine encryptorEngine, final OptimizedStatement optimizedStatement, 
                                                              final BaseParameterBuilder baseParameterBuilder, final String columnName, final String tableName) {
        ColumnNode columnNode = new ColumnNode(tableName, columnName);
        Object originalColumnValue = getColumnValue(findAssignment(columnName, (UpdateStatement) optimizedStatement.getSQLStatement()), baseParameterBuilder.getOriginalParameters());
        List<Object> encryptColumnValues = encryptorEngine.getEncryptColumnValues(columnNode, Collections.singletonList(originalColumnValue));
        encryptParameters(getPositionIndexesFromUpdateItem(optimizedStatement, columnName), encryptColumnValues, baseParameterBuilder);
        Optional<String> assistedColumnName = encryptorEngine.getAssistedQueryColumn(columnNode.getTableName(), columnNode.getColumnName());
        if (!assistedColumnName.isPresent()) {
            return createUpdateEncryptItemToken(optimizedStatement, columnName, encryptColumnValues);
        }
        List<Object> encryptAssistedColumnValues = encryptorEngine.getEncryptAssistedColumnValues(columnNode, Collections.singletonList(originalColumnValue));
        baseParameterBuilder.getAddedIndexAndParameters().putAll(getIndexAndParameters(optimizedStatement, columnName, encryptAssistedColumnValues));
        return createUpdateEncryptAssistedItemToken(encryptorEngine, optimizedStatement, columnName, tableName, encryptColumnValues, encryptAssistedColumnValues);
    }
    
    private AssignmentSegment findAssignment(final String columnName, final UpdateStatement updateStatement) {
        for (AssignmentSegment each : updateStatement.getSetAssignment().getAssignments()) {
            if (columnName.equalsIgnoreCase(each.getColumn().getName())) {
                return each;
            }
        }
        throw new ShardingException("Cannot find column '%s'", columnName);
    }
    
    private Object getColumnValue(final AssignmentSegment assignmentSegment, final List<Object> parameters) {
        ExpressionSegment expressionSegment = assignmentSegment.getValue();
        if (expressionSegment instanceof ParameterMarkerExpressionSegment) {
            return parameters.get(((ParameterMarkerExpressionSegment) expressionSegment).getParameterMarkerIndex());
        }
        if (expressionSegment instanceof LiteralExpressionSegment) {
            return ((LiteralExpressionSegment) expressionSegment).getLiterals();
        }
        throw new ShardingException("Can not find column value by %s.", assignmentSegment.getColumn().getName());
    }
    
    private Map<Integer, Integer> getPositionIndexesFromUpdateItem(final OptimizedStatement optimizedStatement, final String columnName) {
        ExpressionSegment result = findAssignment(columnName, (UpdateStatement) optimizedStatement.getSQLStatement()).getValue();
        return result instanceof ParameterMarkerExpressionSegment
                ? Collections.singletonMap(0, ((ParameterMarkerExpressionSegment) result).getParameterMarkerIndex()) : new LinkedHashMap<Integer, Integer>();
    }
    
    private void encryptParameters(final Map<Integer, Integer> positionIndexes, final List<Object> encryptColumnValues, final BaseParameterBuilder baseParameterBuilder) {
        if (!positionIndexes.isEmpty()) {
            for (Entry<Integer, Integer> entry : positionIndexes.entrySet()) {
                baseParameterBuilder.getOriginalParameters().set(entry.getValue(), encryptColumnValues.get(entry.getKey()));
            }
        }
    }
    
    private Map<Integer, Object> getIndexAndParameters(final OptimizedStatement optimizedStatement, final String columnName, final List<Object> encryptAssistedColumnValues) {
        return encryptAssistedColumnValues.isEmpty() || !isUsingParameter(optimizedStatement, columnName)
                ? Collections.<Integer, Object>emptyMap()
                : Collections.singletonMap(getPositionIndexesFromUpdateItem(optimizedStatement, columnName).values().iterator().next() + 1, encryptAssistedColumnValues.get(0));
    }
    
    private UpdateEncryptItemToken createUpdateEncryptItemToken(final OptimizedStatement optimizedStatement, final String columnName, final List<Object> encryptColumnValues) {
        return isUsingParameter(optimizedStatement, columnName)
                ? new UpdateEncryptItemToken(startIndex, stopIndex, columnName) : new UpdateEncryptItemToken(startIndex, stopIndex, columnName, encryptColumnValues.get(0));
    }
    
    private UpdateEncryptAssistedItemToken createUpdateEncryptAssistedItemToken(final ShardingEncryptorEngine encryptorEngine, final OptimizedStatement optimizedStatement, final String columnName, 
                                                                                final String tableName, final List<Object> encryptColumnValues, final List<Object> encryptAssistedColumnValues) {
        String assistedColumnName = encryptorEngine.getAssistedQueryColumn(tableName, columnName).get();
        return isUsingParameter(optimizedStatement, columnName) ? new UpdateEncryptAssistedItemToken(startIndex, stopIndex, columnName, assistedColumnName) 
                : new UpdateEncryptAssistedItemToken(startIndex, stopIndex, columnName, encryptColumnValues.get(0), assistedColumnName, encryptAssistedColumnValues.get(0));
    }
    
    private boolean isUsingParameter(final OptimizedStatement optimizedStatement, final String columnName) {
        return findAssignment(columnName, (UpdateStatement) optimizedStatement.getSQLStatement()).getValue() instanceof ParameterMarkerExpressionSegment;
    }
}
