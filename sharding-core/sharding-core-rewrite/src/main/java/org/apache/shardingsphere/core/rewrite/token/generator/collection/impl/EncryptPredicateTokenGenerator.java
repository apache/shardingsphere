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

package org.apache.shardingsphere.core.rewrite.token.generator.collection.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.Setter;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.optimize.statement.SQLStatementContext;
import org.apache.shardingsphere.core.rewrite.encrypt.EncryptCondition;
import org.apache.shardingsphere.core.rewrite.encrypt.EncryptConditionEngine;
import org.apache.shardingsphere.core.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.token.generator.EncryptRuleAware;
import org.apache.shardingsphere.core.rewrite.token.generator.ParameterBuilderAware;
import org.apache.shardingsphere.core.rewrite.token.generator.QueryWithCipherColumnAware;
import org.apache.shardingsphere.core.rewrite.token.generator.TableMetasAware;
import org.apache.shardingsphere.core.rewrite.token.generator.collection.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.pojo.EncryptPredicateToken;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Predicate token generator for encrypt.
 *
 * @author panjuan
 * @author zhangliang
 */
@Setter
public final class EncryptPredicateTokenGenerator implements CollectionSQLTokenGenerator, TableMetasAware, EncryptRuleAware, ParameterBuilderAware, QueryWithCipherColumnAware {
    
    private TableMetas tableMetas;
    
    private EncryptRule encryptRule;
    
    private ParameterBuilder parameterBuilder;
    
    private boolean queryWithCipherColumn;
    
    @Override
    public Collection<EncryptPredicateToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        List<EncryptCondition> encryptConditions = new EncryptConditionEngine(encryptRule, tableMetas).createEncryptConditions(sqlStatementContext);
        return encryptConditions.isEmpty() ? Collections.<EncryptPredicateToken>emptyList() : createSQLTokens(encryptConditions);
    }
    
    private Collection<EncryptPredicateToken> createSQLTokens(final List<EncryptCondition> encryptConditions) {
        Collection<EncryptPredicateToken> result = new LinkedList<>();
        for (EncryptCondition each : encryptConditions) {
            result.add(createSQLToken(each));
        }
        return result;
    }
    
    private EncryptPredicateToken createSQLToken(final EncryptCondition encryptCondition) {
        List<Object> originalValues = encryptCondition.getValues(parameterBuilder.getOriginalParameters());
        return queryWithCipherColumn ? createSQLTokenForQueryWithCipherColumn(encryptCondition, originalValues) : createSQLTokenForQueryWithoutCipherColumn(encryptCondition, originalValues);
    }
    
    private EncryptPredicateToken createSQLTokenForQueryWithCipherColumn(final EncryptCondition encryptCondition, final List<Object> originalValues) {
        String encryptedColumnName = encryptRule.findAssistedQueryColumn(encryptCondition.getTableName(), encryptCondition.getColumnName())
                .or(encryptRule.getCipherColumn(encryptCondition.getTableName(), encryptCondition.getColumnName()));
        List<Object> encryptedValues = getEncryptedValues(encryptCondition, originalValues);
        return new EncryptPredicateToken(encryptCondition.getStartIndex(), encryptCondition.getStopIndex(), encryptedColumnName,
                getPositionValues(encryptCondition.getPositionValueMap().keySet(), encryptedValues), encryptCondition.getPositionIndexMap().keySet(), encryptCondition.getOperator());
    }
    
    private List<Object> getEncryptedValues(final EncryptCondition encryptCondition, final List<Object> originalValues) {
        Optional<String> assistedQueryColumn = encryptRule.findAssistedQueryColumn(encryptCondition.getTableName(), encryptCondition.getColumnName());
        return assistedQueryColumn.isPresent() 
                ? encryptRule.getEncryptAssistedQueryValues(encryptCondition.getTableName(), encryptCondition.getColumnName(), originalValues) 
                : encryptRule.getEncryptValues(encryptCondition.getTableName(), encryptCondition.getColumnName(), originalValues);
    }
    
    private EncryptPredicateToken createSQLTokenForQueryWithoutCipherColumn(final EncryptCondition encryptCondition, final List<Object> originalValues) {
        Optional<String> plainColumn = encryptRule.findPlainColumn(encryptCondition.getTableName(), encryptCondition.getColumnName());
        Preconditions.checkState(plainColumn.isPresent(), "Plain column should be required.");
        return new EncryptPredicateToken(encryptCondition.getStartIndex(), encryptCondition.getStopIndex(), plainColumn.get(),
                getPositionValues(encryptCondition.getPositionValueMap().keySet(), originalValues), encryptCondition.getPositionIndexMap().keySet(), encryptCondition.getOperator());
    }
    
    private Map<Integer, Object> getPositionValues(final Collection<Integer> valuePositions, final List<Object> encryptValues) {
        Map<Integer, Object> result = new LinkedHashMap<>();
        for (int each : valuePositions) {
            result.put(each, encryptValues.get(each));
        }
        return result;
    }
}
