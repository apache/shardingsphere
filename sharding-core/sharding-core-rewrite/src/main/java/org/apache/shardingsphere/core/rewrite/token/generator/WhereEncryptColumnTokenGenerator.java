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
import org.apache.shardingsphere.core.optimize.api.statement.ConditionOptimizedStatement;
import org.apache.shardingsphere.core.optimize.api.statement.OptimizedStatement;
import org.apache.shardingsphere.core.optimize.encrypt.segment.condition.EncryptCondition;
import org.apache.shardingsphere.core.rewrite.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.token.pojo.EncryptColumnToken;
import org.apache.shardingsphere.core.rewrite.token.pojo.WhereEncryptColumnToken;
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
    
    private ParameterBuilder parameterBuilder;
    
    private EncryptRule encryptRule;
    
    private boolean isQueryWithCipherColumn;
    
    @Override
    public Collection<EncryptColumnToken> generateSQLTokens(
            final OptimizedStatement optimizedStatement, final ParameterBuilder parameterBuilder, final EncryptRule encryptRule, final boolean isQueryWithCipherColumn) {
        initParameters(parameterBuilder, encryptRule, isQueryWithCipherColumn);
        return optimizedStatement instanceof ConditionOptimizedStatement
                ? createWhereEncryptColumnTokens((ConditionOptimizedStatement) optimizedStatement) : Collections.<EncryptColumnToken>emptyList();
    }
    
    private void initParameters(final ParameterBuilder parameterBuilder, final EncryptRule encryptRule, final boolean isQueryWithCipherColumn) {
        this.parameterBuilder = parameterBuilder;
        this.encryptRule = encryptRule;
        this.isQueryWithCipherColumn = isQueryWithCipherColumn;
    }
    
    private Collection<EncryptColumnToken> createWhereEncryptColumnTokens(final ConditionOptimizedStatement optimizedStatement) {
        Collection<EncryptColumnToken> result = new LinkedList<>();
        for (EncryptCondition each : optimizedStatement.getEncryptConditions().getConditions()) {
            result.add(createWhereEncryptColumnToken(each));
        }
        return result;
    }
    
    private WhereEncryptColumnToken createWhereEncryptColumnToken(final EncryptCondition encryptCondition) {
        List<Object> originalColumnValues = encryptCondition.getValues(parameterBuilder.getOriginalParameters());
        if (isQueryWithCipherColumn) {
            return createWhereEncryptColumnToken(encryptCondition, originalColumnValues);
        }
        return new WhereEncryptColumnToken(encryptCondition.getStartIndex(), encryptCondition.getStopIndex(), getPlainColumn(encryptCondition, encryptRule),
                getPositionValues(encryptCondition.getPositionValueMap().keySet(), originalColumnValues), encryptCondition.getPositionIndexMap().keySet(), encryptCondition.getOperator());
    }
    
    private WhereEncryptColumnToken createWhereEncryptColumnToken(final EncryptCondition encryptCondition, final List<Object> originalValues) {
        String encryptedColumnName = getEncryptedColumnName(encryptCondition);
        List<Object> encryptedColumnValues = getEncryptedColumnValues(encryptCondition, originalValues);
        encryptParameters(encryptCondition.getPositionIndexMap(), encryptedColumnValues, parameterBuilder);
        return new WhereEncryptColumnToken(encryptCondition.getStartIndex(), encryptCondition.getStopIndex(), encryptedColumnName,
                getPositionValues(encryptCondition.getPositionValueMap().keySet(), encryptedColumnValues), encryptCondition.getPositionIndexMap().keySet(), encryptCondition.getOperator());
    }
    
    private String getEncryptedColumnName(final EncryptCondition encryptCondition) {
        Optional<String> assistedQueryColumn = encryptRule.getEncryptEngine().getAssistedQueryColumn(encryptCondition.getTableName(), encryptCondition.getColumnName());
        return assistedQueryColumn.isPresent() 
                ? assistedQueryColumn.get() : encryptRule.getEncryptEngine().getCipherColumn(encryptCondition.getTableName(), encryptCondition.getColumnName());
    }
    
    private List<Object> getEncryptedColumnValues(final EncryptCondition encryptCondition, final List<Object> originalValues) {
        Optional<String> assistedQueryColumn = encryptRule.getEncryptEngine().getAssistedQueryColumn(encryptCondition.getTableName(), encryptCondition.getColumnName());
        return assistedQueryColumn.isPresent() 
                ? encryptRule.getEncryptEngine().getEncryptAssistedColumnValues(encryptCondition.getTableName(), encryptCondition.getColumnName(), originalValues) 
                : encryptRule.getEncryptEngine().getEncryptColumnValues(encryptCondition.getTableName(), encryptCondition.getColumnName(), originalValues);
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
    
    private String getPlainColumn(final EncryptCondition encryptCondition, final EncryptRule encryptRule) {
        Optional<String> result = encryptRule.getEncryptEngine().getPlainColumn(encryptCondition.getTableName(), encryptCondition.getColumnName());
        if (result.isPresent()) {
            return result.get();
        }
        throw new ShardingException("Plain column is required.");
    }
}
