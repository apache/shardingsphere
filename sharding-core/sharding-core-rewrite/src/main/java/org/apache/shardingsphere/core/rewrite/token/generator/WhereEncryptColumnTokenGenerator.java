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
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.DMLStatement;
import org.apache.shardingsphere.core.rewrite.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.token.pojo.EncryptColumnToken;
import org.apache.shardingsphere.core.rewrite.token.pojo.WhereEncryptColumnToken;
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
 * Where encrypt column token generator.
 *
 * @author panjuan
 */
public final class WhereEncryptColumnTokenGenerator implements CollectionSQLTokenGenerator<EncryptRule> {
    
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
        return createWhereEncryptColumnTokens(encryptRule, parameterBuilder);
    }
    
    private Collection<EncryptColumnToken> createWhereEncryptColumnTokens(final EncryptRule encryptRule, final ParameterBuilder parameterBuilder) {
        Collection<EncryptColumnToken> result = new LinkedList<>();
        if (dmlStatement.getEncryptConditions().getOrConditions().isEmpty()) {
            return result;
        }
        for (Condition each : dmlStatement.getEncryptConditions().getOrConditions().get(0).getConditions()) {
            this.column = new Column(each.getColumn().getName(), dmlStatement.getTables().getSingleTableName());
            this.startIndex = each.getPredicateSegment().getStartIndex();
            this.stopIndex = each.getPredicateSegment().getStopIndex();
            result.add(createEncryptColumnToken(encryptRule.getEncryptorEngine(), parameterBuilder));
        }
        return result;
    }
    
    private EncryptColumnToken createEncryptColumnToken(final ShardingEncryptorEngine encryptorEngine, final ParameterBuilder parameterBuilder) {
        Optional<Condition> encryptCondition = getEncryptCondition(dmlStatement.getEncryptConditions());
        Preconditions.checkArgument(encryptCondition.isPresent(), "Can not find encrypt condition");
        return getEncryptColumnTokenFromConditions(encryptorEngine, encryptCondition.get(), parameterBuilder);
    }
    
    private Optional<Condition> getEncryptCondition(final Conditions encryptConditions) {
        for (Condition each : encryptConditions.findConditions(column)) {
            if (isSameIndexes(each.getPredicateSegment())) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
    
    private boolean isSameIndexes(final PredicateSegment predicateSegment) {
        return predicateSegment.getStartIndex() == startIndex && predicateSegment.getStopIndex() == stopIndex;
    }
    
    private WhereEncryptColumnToken getEncryptColumnTokenFromConditions(
            final ShardingEncryptorEngine encryptorEngine, final Condition encryptCondition, final ParameterBuilder parameterBuilder) {
        ColumnNode columnNode = new ColumnNode(column.getTableName(), column.getName());
        List<Comparable<?>> encryptColumnValues = encryptValues(encryptorEngine, columnNode, encryptCondition.getConditionValues(parameterBuilder.getOriginalParameters()));
        encryptParameters(encryptCondition.getPositionIndexMap(), encryptColumnValues, parameterBuilder);
        Optional<String> assistedColumnName = encryptorEngine.getAssistedQueryColumn(columnNode.getTableName(), columnNode.getColumnName());
        return new WhereEncryptColumnToken(startIndex, stopIndex, assistedColumnName.isPresent() ? assistedColumnName.get() : columnNode.getColumnName(),
                getPositionValues(encryptCondition.getPositionValueMap().keySet(), encryptColumnValues), encryptCondition.getPositionIndexMap().keySet(), encryptCondition.getOperator());
    }
    
    private List<Comparable<?>> encryptValues(final ShardingEncryptorEngine encryptorEngine, final ColumnNode columnNode, final List<Comparable<?>> columnValues) {
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
}
