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
import org.apache.shardingsphere.core.optimize.statement.WhereOptimizedStatement;
import org.apache.shardingsphere.core.optimize.statement.encrypt.condition.EncryptCondition;
import org.apache.shardingsphere.core.optimize.statement.encrypt.condition.EncryptConditions;
import org.apache.shardingsphere.core.parse.sql.context.Column;
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
    
    @Override
    public Collection<EncryptColumnToken> generateSQLTokens(final OptimizedStatement optimizedStatement, final ParameterBuilder parameterBuilder, final EncryptRule encryptRule) {
        return optimizedStatement.getSQLStatement() instanceof DMLStatement
                ? createWhereEncryptColumnTokens(optimizedStatement, parameterBuilder, encryptRule) : Collections.<EncryptColumnToken>emptyList();
    }
    
    private Collection<EncryptColumnToken> createWhereEncryptColumnTokens(final OptimizedStatement optimizedStatement, final ParameterBuilder parameterBuilder, final EncryptRule encryptRule) {
        Collection<EncryptColumnToken> result = new LinkedList<>();
        if (!(optimizedStatement instanceof WhereOptimizedStatement) || ((WhereOptimizedStatement) optimizedStatement).getEncryptConditions().getConditions().isEmpty()) {
            return result;
        }
        for (EncryptCondition each : ((WhereOptimizedStatement) optimizedStatement).getEncryptConditions().getConditions()) {
            column = new Column(each.getColumnName(), optimizedStatement.getSQLStatement().getTables().getSingleTableName());
            startIndex = each.getStartIndex();
            stopIndex = each.getStopIndex();
            result.add(createEncryptColumnToken(optimizedStatement, parameterBuilder, encryptRule.getEncryptorEngine()));
        }
        return result;
    }
    
    private EncryptColumnToken createEncryptColumnToken(final OptimizedStatement optimizedStatement, final ParameterBuilder parameterBuilder, final ShardingEncryptorEngine encryptorEngine) {
        Optional<EncryptCondition> encryptCondition = getEncryptCondition(((WhereOptimizedStatement) optimizedStatement).getEncryptConditions());
        Preconditions.checkArgument(encryptCondition.isPresent(), "Can not find encrypt condition");
        return getEncryptColumnTokenFromConditions(encryptorEngine, encryptCondition.get(), parameterBuilder);
    }
    
    private Optional<EncryptCondition> getEncryptCondition(final EncryptConditions encryptConditions) {
        for (EncryptCondition each : encryptConditions.getConditions()) {
            if (each.getColumnName().equalsIgnoreCase(column.getName()) && each.getTableName().equalsIgnoreCase(column.getTableName()) && each.isSameIndex(startIndex, stopIndex)) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
    
    private WhereEncryptColumnToken getEncryptColumnTokenFromConditions(
            final ShardingEncryptorEngine encryptorEngine, final EncryptCondition encryptCondition, final ParameterBuilder parameterBuilder) {
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
