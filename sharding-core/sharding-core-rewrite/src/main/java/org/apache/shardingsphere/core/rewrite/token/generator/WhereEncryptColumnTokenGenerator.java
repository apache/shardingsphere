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
import org.apache.shardingsphere.core.optimize.api.statement.ConditionOptimizedStatement;
import org.apache.shardingsphere.core.optimize.api.statement.OptimizedStatement;
import org.apache.shardingsphere.core.optimize.encrypt.segment.condition.EncryptCondition;
import org.apache.shardingsphere.core.rewrite.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.token.pojo.EncryptColumnToken;
import org.apache.shardingsphere.core.rewrite.token.pojo.WhereEncryptColumnToken;
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
 * Where encrypt column token generator.
 *
 * @author panjuan
 */
public final class WhereEncryptColumnTokenGenerator implements CollectionSQLTokenGenerator<EncryptRule> {
    
    @Override
    public Collection<EncryptColumnToken> generateSQLTokens(
            final OptimizedStatement optimizedStatement, final ParameterBuilder parameterBuilder, final EncryptRule encryptRule, final boolean isQueryWithCipherColumn) {
        return optimizedStatement instanceof ConditionOptimizedStatement
                ? createEncryptColumnToken((ConditionOptimizedStatement) optimizedStatement, parameterBuilder, encryptRule) : Collections.<EncryptColumnToken>emptyList();
    }
    
    private Collection<EncryptColumnToken> createEncryptColumnToken(final ConditionOptimizedStatement optimizedStatement, final ParameterBuilder parameterBuilder, final EncryptRule encryptRule) {
        Collection<EncryptColumnToken> result = new LinkedList<>();
        for (EncryptCondition each : optimizedStatement.getEncryptConditions().getConditions()) {
            result.add(createWhereEncryptColumnToken(each, parameterBuilder, encryptRule));
        }
        return result;
    }
    
    private WhereEncryptColumnToken createWhereEncryptColumnToken(final EncryptCondition encryptCondition, final ParameterBuilder parameterBuilder, final EncryptRule encryptRule) {
        ColumnNode columnNode = new ColumnNode(encryptCondition.getTableName(), encryptCondition.getColumnName());
        List<Object> encryptColumnValues = encryptValues(columnNode, encryptCondition.getValues(parameterBuilder.getOriginalParameters()), encryptRule);
        encryptParameters(encryptCondition.getPositionIndexMap(), encryptColumnValues, parameterBuilder);
        Optional<String> assistedQueryColumnName = encryptRule.getEncryptEngine().getAssistedQueryColumn(encryptCondition.getTableName(), encryptCondition.getColumnName());
        return new WhereEncryptColumnToken(encryptCondition.getStartIndex(), encryptCondition.getStopIndex(), assistedQueryColumnName.or(encryptCondition.getColumnName()),
                getPositionValues(encryptCondition.getPositionValueMap().keySet(), encryptColumnValues), encryptCondition.getPositionIndexMap().keySet(), encryptCondition.getOperator());
    }
    
    private List<Object> encryptValues(final ColumnNode columnNode, final List<Object> columnValues, final EncryptRule encryptRule) {
        return encryptRule.getEncryptEngine().getAssistedQueryColumn(columnNode.getTableName(), columnNode.getColumnName()).isPresent()
                ? encryptRule.getEncryptEngine().getEncryptAssistedColumnValues(columnNode, columnValues) : encryptRule.getEncryptEngine().getEncryptColumnValues(columnNode, columnValues);
    }
    
    private void encryptParameters(final Map<Integer, Integer> positionIndexes, final List<Object> encryptColumnValues, final ParameterBuilder parameterBuilder) {
        if (!positionIndexes.isEmpty()) {
            for (Entry<Integer, Integer> entry : positionIndexes.entrySet()) {
                parameterBuilder.getOriginalParameters().set(entry.getValue(), encryptColumnValues.get(entry.getKey()));
            }
        }
    }
    
    private Map<Integer, Object> getPositionValues(final Collection<Integer> valuePositions, final List<Object> encryptColumnValues) {
        Map<Integer, Object> result = new LinkedHashMap<>();
        for (int each : valuePositions) {
            result.put(each, encryptColumnValues.get(each));
        }
        return result;
    }
}
