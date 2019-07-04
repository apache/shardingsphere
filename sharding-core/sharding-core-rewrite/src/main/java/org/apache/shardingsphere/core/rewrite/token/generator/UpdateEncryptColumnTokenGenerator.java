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
import org.apache.shardingsphere.core.optimize.statement.OptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.context.condition.Column;
import org.apache.shardingsphere.core.parse.sql.segment.SQLSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.SetAssignmentsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.DMLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.UpdateStatement;
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
    
    private DMLStatement dmlStatement;
    
    @Override
    public Collection<EncryptColumnToken> generateSQLTokens(final OptimizedStatement optimizedStatement, final ParameterBuilder parameterBuilder, final EncryptRule encryptRule) {
        if (!(optimizedStatement.getSQLStatement() instanceof DMLStatement)) {
            return Collections.emptyList();
        }
        this.dmlStatement = (DMLStatement) optimizedStatement.getSQLStatement();
        return createUpdateEncryptColumnTokens(optimizedStatement, parameterBuilder, encryptRule);
    }
    
    private Collection<EncryptColumnToken> createUpdateEncryptColumnTokens(final OptimizedStatement optimizedStatement, final ParameterBuilder parameterBuilder, final EncryptRule encryptRule) {
        Collection<EncryptColumnToken> result = new LinkedList<>();
        for (SQLSegment each : optimizedStatement.getSQLStatement().getSQLSegments()) {
            if (each instanceof SetAssignmentsSegment) {
                result.addAll(createUpdateEncryptColumnTokens(encryptRule, parameterBuilder, (SetAssignmentsSegment) each));
            }
        }
        return result;
    }
    
    private Collection<EncryptColumnToken> createUpdateEncryptColumnTokens(final EncryptRule encryptRule, final ParameterBuilder parameterBuilder, final SetAssignmentsSegment segment) {
        Collection<EncryptColumnToken> result = new LinkedList<>();
        if (dmlStatement instanceof InsertStatement) {
            return result;
        }
        for (AssignmentSegment each : segment.getAssignments()) {
            this.column = new Column(each.getColumn().getName(), dmlStatement.getTables().getSingleTableName());
            if (encryptRule.getEncryptorEngine().getShardingEncryptor(column.getTableName(), column.getName()).isPresent()) {
                this.startIndex = each.getColumn().getStartIndex();
                this.stopIndex = each.getStopIndex();
                result.add(createUpdateEncryptColumnToken(encryptRule.getEncryptorEngine(), parameterBuilder));
            }
        }
        return result;
    }
    
    private EncryptColumnToken createUpdateEncryptColumnToken(final ShardingEncryptorEngine encryptorEngine, final ParameterBuilder parameterBuilder) {
        ColumnNode columnNode = new ColumnNode(column.getTableName(), column.getName());
        Comparable<?> originalColumnValue = ((UpdateStatement) dmlStatement).getColumnValue(column, parameterBuilder.getOriginalParameters());
        List<Comparable<?>> encryptColumnValues = encryptorEngine.getEncryptColumnValues(columnNode, Collections.<Comparable<?>>singletonList(originalColumnValue));
        encryptParameters(getPositionIndexesFromUpdateItem(), encryptColumnValues, parameterBuilder);
        Optional<String> assistedColumnName = encryptorEngine.getAssistedQueryColumn(columnNode.getTableName(), columnNode.getColumnName());
        if (!assistedColumnName.isPresent()) {
            return createUpdateEncryptItemToken(encryptColumnValues);
        }
        List<Comparable<?>> encryptAssistedColumnValues = encryptorEngine.getEncryptAssistedColumnValues(columnNode, Collections.<Comparable<?>>singletonList(originalColumnValue));
        parameterBuilder.getAddedIndexAndParameters().putAll(getIndexAndParameters(encryptAssistedColumnValues));
        return createUpdateEncryptAssistedItemToken(encryptorEngine, encryptColumnValues, encryptAssistedColumnValues);
    }
    
    private Map<Integer, Integer> getPositionIndexesFromUpdateItem() {
        ExpressionSegment result = ((UpdateStatement) dmlStatement).getAssignments().get(column);
        return result instanceof ParameterMarkerExpressionSegment
                ? Collections.singletonMap(0, ((ParameterMarkerExpressionSegment) result).getParameterMarkerIndex()) : new LinkedHashMap<Integer, Integer>();
    }
    
    private void encryptParameters(final Map<Integer, Integer> positionIndexes, final List<Comparable<?>> encryptColumnValues, final ParameterBuilder parameterBuilder) {
        if (!positionIndexes.isEmpty()) {
            for (Entry<Integer, Integer> entry : positionIndexes.entrySet()) {
                parameterBuilder.getOriginalParameters().set(entry.getValue(), encryptColumnValues.get(entry.getKey()));
            }
        }
    }
    
    private Map<Integer, Object> getIndexAndParameters(final List<Comparable<?>> encryptAssistedColumnValues) {
        if (encryptAssistedColumnValues.isEmpty() || !isUsingParameter()) {
            return Collections.emptyMap();
        }
        return Collections.singletonMap(getPositionIndexesFromUpdateItem().values().iterator().next() + 1, (Object) encryptAssistedColumnValues.get(0));
    }
    
    private UpdateEncryptItemToken createUpdateEncryptItemToken(final List<Comparable<?>> encryptColumnValues) {
        return isUsingParameter() 
                ? new UpdateEncryptItemToken(startIndex, stopIndex, column.getName()) : new UpdateEncryptItemToken(startIndex, stopIndex, column.getName(), encryptColumnValues.get(0));
    }
    
    private UpdateEncryptAssistedItemToken createUpdateEncryptAssistedItemToken(
            final ShardingEncryptorEngine encryptorEngine, final List<Comparable<?>> encryptColumnValues, final List<Comparable<?>> encryptAssistedColumnValues) {
        String assistedColumnName = encryptorEngine.getAssistedQueryColumn(column.getTableName(), column.getName()).get();
        return isUsingParameter() ? new UpdateEncryptAssistedItemToken(startIndex, stopIndex, column.getName(), assistedColumnName) 
                : new UpdateEncryptAssistedItemToken(startIndex, stopIndex, column.getName(), encryptColumnValues.get(0), assistedColumnName, encryptAssistedColumnValues.get(0));
    }
    
    private boolean isUsingParameter() {
        return ((UpdateStatement) dmlStatement).getAssignments().get(column) instanceof ParameterMarkerExpressionSegment;
    }
}
