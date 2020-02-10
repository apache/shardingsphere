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

package org.apache.shardingsphere.encrypt.rewrite.token.generator.impl;

import com.google.common.base.Optional;
import lombok.Setter;
import org.apache.shardingsphere.encrypt.rewrite.aware.QueryWithCipherColumnAware;
import org.apache.shardingsphere.encrypt.rewrite.condition.EncryptCondition;
import org.apache.shardingsphere.encrypt.rewrite.condition.EncryptConditionEngine;
import org.apache.shardingsphere.encrypt.rewrite.condition.impl.EncryptInCondition;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.BaseEncryptSQLTokenGenerator;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptPredicateEqualRightValueToken;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptPredicateInRightValueToken;
import org.apache.shardingsphere.sql.parser.relation.metadata.RelationMetas;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.generic.WhereSegmentAvailable;
import org.apache.shardingsphere.underlying.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.underlying.rewrite.sql.token.generator.aware.ParametersAware;
import org.apache.shardingsphere.underlying.rewrite.sql.token.generator.aware.RelationMetasAware;
import org.apache.shardingsphere.underlying.rewrite.sql.token.pojo.SQLToken;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Predicate right value token generator for encrypt.
 *
 * @author panjuan
 * @author zhangliang
 */
@Setter
public final class EncryptPredicateRightValueTokenGenerator extends BaseEncryptSQLTokenGenerator implements CollectionSQLTokenGenerator, 
        RelationMetasAware, ParametersAware, QueryWithCipherColumnAware {
    
    private RelationMetas relationMetas;
    
    private List<Object> parameters;
    
    private boolean queryWithCipherColumn;
    
    @Override
    protected boolean isGenerateSQLTokenForEncrypt(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext.getSqlStatement() instanceof WhereSegmentAvailable && ((WhereSegmentAvailable) sqlStatementContext.getSqlStatement()).getWhere().isPresent();
    }
    
    @Override
    public Collection<SQLToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        List<EncryptCondition> encryptConditions = new EncryptConditionEngine(getEncryptRule(), relationMetas).createEncryptConditions(sqlStatementContext);
        return encryptConditions.isEmpty() ? Collections.<SQLToken>emptyList() : generateSQLTokens(encryptConditions);
    }
    
    private Collection<SQLToken> generateSQLTokens(final List<EncryptCondition> encryptConditions) {
        Collection<SQLToken> result = new LinkedHashSet<>();
        for (EncryptCondition each : encryptConditions) {
            result.add(generateSQLToken(each));
        }
        return result;
    }
    
    private SQLToken generateSQLToken(final EncryptCondition encryptCondition) {
        List<Object> originalValues = encryptCondition.getValues(parameters);
        int startIndex = encryptCondition instanceof EncryptInCondition ? encryptCondition.getStartIndex() - 1 : encryptCondition.getStartIndex();
        return queryWithCipherColumn ? generateSQLTokenForQueryWithCipherColumn(encryptCondition, originalValues, startIndex)
                : generateSQLTokenForQueryWithoutCipherColumn(encryptCondition, originalValues, startIndex);
    }
    
    private SQLToken generateSQLTokenForQueryWithCipherColumn(final EncryptCondition encryptCondition, final List<Object> originalValues, final int startIndex) {
        int stopIndex = encryptCondition.getStopIndex();
        Map<Integer, Object> indexValues = getPositionValues(encryptCondition.getPositionValueMap().keySet(), getEncryptedValues(encryptCondition, originalValues));
        Collection<Integer> parameterMarkerIndexes = encryptCondition.getPositionIndexMap().keySet();
        return encryptCondition instanceof EncryptInCondition
                ? new EncryptPredicateInRightValueToken(startIndex, stopIndex, indexValues, parameterMarkerIndexes)
                : new EncryptPredicateEqualRightValueToken(startIndex, stopIndex, indexValues, parameterMarkerIndexes);
    }
    
    private List<Object> getEncryptedValues(final EncryptCondition encryptCondition, final List<Object> originalValues) {
        Optional<String> assistedQueryColumn = getEncryptRule().findAssistedQueryColumn(encryptCondition.getTableName(), encryptCondition.getColumnName());
        return assistedQueryColumn.isPresent() 
                ? getEncryptRule().getEncryptAssistedQueryValues(encryptCondition.getTableName(), encryptCondition.getColumnName(), originalValues) 
                : getEncryptRule().getEncryptValues(encryptCondition.getTableName(), encryptCondition.getColumnName(), originalValues);
    }
    
    private SQLToken generateSQLTokenForQueryWithoutCipherColumn(final EncryptCondition encryptCondition, final List<Object> originalValues, final int startIndex) {
        int stopIndex = encryptCondition.getStopIndex();
        Map<Integer, Object> indexValues = getPositionValues(encryptCondition.getPositionValueMap().keySet(), originalValues);
        Collection<Integer> parameterMarkerIndexes = encryptCondition.getPositionIndexMap().keySet();
        return encryptCondition instanceof EncryptInCondition
                ? new EncryptPredicateInRightValueToken(startIndex, stopIndex, indexValues, parameterMarkerIndexes)
                : new EncryptPredicateEqualRightValueToken(startIndex, stopIndex, indexValues, parameterMarkerIndexes);
    }
    
    private Map<Integer, Object> getPositionValues(final Collection<Integer> valuePositions, final List<Object> encryptValues) {
        Map<Integer, Object> result = new LinkedHashMap<>();
        for (int each : valuePositions) {
            result.put(each, encryptValues.get(each));
        }
        return result;
    }
}
