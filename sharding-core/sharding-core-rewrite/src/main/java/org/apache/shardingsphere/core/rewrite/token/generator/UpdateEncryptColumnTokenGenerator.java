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
import org.apache.shardingsphere.core.parse.sql.context.Column;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.SetAssignmentsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
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
    
    private Column column;
    
    private int startIndex;
    
    private int stopIndex;
    
    private SQLStatement sqlStatement;
    
    @Override
    public Collection<EncryptColumnToken> generateSQLTokens(final OptimizedStatement optimizedStatement, final ParameterBuilder parameterBuilder, final EncryptRule encryptRule) {
        if (!(optimizedStatement.getSQLStatement() instanceof UpdateStatement)) {
            return Collections.emptyList();
        }
        this.sqlStatement = optimizedStatement.getSQLStatement();
        return createUpdateEncryptColumnTokens(optimizedStatement, parameterBuilder, encryptRule);
    }
    
    private Collection<EncryptColumnToken> createUpdateEncryptColumnTokens(final OptimizedStatement optimizedStatement, final ParameterBuilder parameterBuilder, final EncryptRule encryptRule) {
        if (!(optimizedStatement.getSQLStatement() instanceof UpdateStatement)) {
            return Collections.emptyList();
        }
        return createUpdateEncryptColumnTokens(encryptRule, parameterBuilder, ((UpdateStatement) optimizedStatement.getSQLStatement()).getSetAssignment());
    }
    
    private Collection<EncryptColumnToken> createUpdateEncryptColumnTokens(final EncryptRule encryptRule, final ParameterBuilder parameterBuilder, final SetAssignmentsSegment segment) {
        Collection<EncryptColumnToken> result = new LinkedList<>();
        for (AssignmentSegment each : segment.getAssignments()) {
            this.column = new Column(each.getColumn().getName(), sqlStatement.getTables().getSingleTableName());
            if (encryptRule.getEncryptorEngine().getShardingEncryptor(column.getTableName(), column.getName()).isPresent()) {
                this.startIndex = each.getColumn().getStartIndex();
                this.stopIndex = each.getStopIndex();
                result.add(createUpdateEncryptColumnToken(encryptRule.getEncryptorEngine(), (BaseParameterBuilder) parameterBuilder));
            }
        }
        return result;
    }
    
    private EncryptColumnToken createUpdateEncryptColumnToken(final ShardingEncryptorEngine encryptorEngine, final BaseParameterBuilder baseParameterBuilder) {
        ColumnNode columnNode = new ColumnNode(column.getTableName(), column.getName());
        Object originalColumnValue = getColumnValue(findAssignment(column, (UpdateStatement) sqlStatement), baseParameterBuilder.getOriginalParameters());
        List<Object> encryptColumnValues = encryptorEngine.getEncryptColumnValues(columnNode, Collections.singletonList(originalColumnValue));
        encryptParameters(getPositionIndexesFromUpdateItem(), encryptColumnValues, baseParameterBuilder);
        Optional<String> assistedColumnName = encryptorEngine.getAssistedQueryColumn(columnNode.getTableName(), columnNode.getColumnName());
        if (!assistedColumnName.isPresent()) {
            return createUpdateEncryptItemToken(encryptColumnValues);
        }
        List<Object> encryptAssistedColumnValues = encryptorEngine.getEncryptAssistedColumnValues(columnNode, Collections.singletonList(originalColumnValue));
        baseParameterBuilder.getAddedIndexAndParameters().putAll(getIndexAndParameters(encryptAssistedColumnValues));
        return createUpdateEncryptAssistedItemToken(encryptorEngine, encryptColumnValues, encryptAssistedColumnValues);
    }
    
    private AssignmentSegment findAssignment(final Column column, final UpdateStatement updateStatement) {
        for (AssignmentSegment each : updateStatement.getSetAssignment().getAssignments()) {
            if (column.getName().equalsIgnoreCase(each.getColumn().getName())) {
                return each;
            }
        }
        throw new ShardingException("Cannot find column '%s'", column);
    }
    
    private Object getColumnValue(final AssignmentSegment assignmentSegment, final List<Object> parameters) {
        ExpressionSegment expressionSegment = assignmentSegment.getValue();
        if (expressionSegment instanceof ParameterMarkerExpressionSegment) {
            return parameters.get(((ParameterMarkerExpressionSegment) expressionSegment).getParameterMarkerIndex());
        }
        if (expressionSegment instanceof LiteralExpressionSegment) {
            return ((LiteralExpressionSegment) expressionSegment).getLiterals();
        }
        throw new ShardingException("Can not find column value by %s.", column);
    }
    
    private Map<Integer, Integer> getPositionIndexesFromUpdateItem() {
        ExpressionSegment result = findAssignment(column, (UpdateStatement) sqlStatement).getValue();
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
    
    private Map<Integer, Object> getIndexAndParameters(final List<Object> encryptAssistedColumnValues) {
        if (encryptAssistedColumnValues.isEmpty() || !isUsingParameter()) {
            return Collections.emptyMap();
        }
        return Collections.singletonMap(getPositionIndexesFromUpdateItem().values().iterator().next() + 1, encryptAssistedColumnValues.get(0));
    }
    
    private UpdateEncryptItemToken createUpdateEncryptItemToken(final List<Object> encryptColumnValues) {
        return isUsingParameter() 
                ? new UpdateEncryptItemToken(startIndex, stopIndex, column.getName()) : new UpdateEncryptItemToken(startIndex, stopIndex, column.getName(), encryptColumnValues.get(0));
    }
    
    private UpdateEncryptAssistedItemToken createUpdateEncryptAssistedItemToken(
            final ShardingEncryptorEngine encryptorEngine, final List<Object> encryptColumnValues, final List<Object> encryptAssistedColumnValues) {
        String assistedColumnName = encryptorEngine.getAssistedQueryColumn(column.getTableName(), column.getName()).get();
        return isUsingParameter() ? new UpdateEncryptAssistedItemToken(startIndex, stopIndex, column.getName(), assistedColumnName) 
                : new UpdateEncryptAssistedItemToken(startIndex, stopIndex, column.getName(), encryptColumnValues.get(0), assistedColumnName, encryptAssistedColumnValues.get(0));
    }
    
    private boolean isUsingParameter() {
        return findAssignment(column, (UpdateStatement) sqlStatement).getValue() instanceof ParameterMarkerExpressionSegment;
    }
}
