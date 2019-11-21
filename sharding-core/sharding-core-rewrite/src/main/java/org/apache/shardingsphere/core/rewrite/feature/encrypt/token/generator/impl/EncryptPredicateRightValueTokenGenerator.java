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

package org.apache.shardingsphere.core.rewrite.feature.encrypt.token.generator.impl;

import com.google.common.base.Optional;
import lombok.Setter;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.sql.parser.sql.statement.generic.WhereSegmentAvailable;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.core.rewrite.feature.encrypt.EncryptCondition;
import org.apache.shardingsphere.core.rewrite.feature.encrypt.EncryptConditionEngine;
import org.apache.shardingsphere.core.rewrite.feature.encrypt.token.generator.BaseEncryptSQLTokenGenerator;
import org.apache.shardingsphere.core.rewrite.feature.encrypt.aware.QueryWithCipherColumnAware;
import org.apache.shardingsphere.core.rewrite.feature.encrypt.token.pojo.EncryptPredicateRightValueToken;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.aware.ParametersAware;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.aware.TableMetasAware;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Predicate right value token generator for encrypt.
 *
 * @author panjuan
 * @author zhangliang
 */
@Setter
public final class EncryptPredicateRightValueTokenGenerator extends BaseEncryptSQLTokenGenerator implements CollectionSQLTokenGenerator, TableMetasAware, ParametersAware, QueryWithCipherColumnAware {
    
    private TableMetas tableMetas;
    
    private List<Object> parameters;
    
    private boolean queryWithCipherColumn;
    
    @Override
    protected boolean isGenerateSQLTokenForEncrypt(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext.getSqlStatement() instanceof WhereSegmentAvailable && ((WhereSegmentAvailable) sqlStatementContext.getSqlStatement()).getWhere().isPresent();
    }
    
    @Override
    public Collection<EncryptPredicateRightValueToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        List<EncryptCondition> encryptConditions = new EncryptConditionEngine(getEncryptRule(), tableMetas).createEncryptConditions(sqlStatementContext);
        return encryptConditions.isEmpty() ? Collections.<EncryptPredicateRightValueToken>emptyList() : generateSQLTokens(encryptConditions);
    }
    
    private Collection<EncryptPredicateRightValueToken> generateSQLTokens(final List<EncryptCondition> encryptConditions) {
        Collection<EncryptPredicateRightValueToken> result = new LinkedList<>();
        for (EncryptCondition each : encryptConditions) {
            result.add(generateSQLToken(each));
        }
        return result;
    }
    
    private EncryptPredicateRightValueToken generateSQLToken(final EncryptCondition encryptCondition) {
        List<Object> originalValues = encryptCondition.getValues(parameters);
        return queryWithCipherColumn ? generateSQLTokenForQueryWithCipherColumn(encryptCondition, originalValues) : generateSQLTokenForQueryWithoutCipherColumn(encryptCondition, originalValues);
    }
    
    private EncryptPredicateRightValueToken generateSQLTokenForQueryWithCipherColumn(final EncryptCondition encryptCondition, final List<Object> originalValues) {
        List<Object> encryptedValues = getEncryptedValues(encryptCondition, originalValues);
        return new EncryptPredicateRightValueToken(encryptCondition.getStartIndex(), encryptCondition.getStopIndex(),  
                getPositionValues(encryptCondition.getPositionValueMap().keySet(), encryptedValues), encryptCondition.getPositionIndexMap().keySet(), encryptCondition.getOperator());
    }
    
    private List<Object> getEncryptedValues(final EncryptCondition encryptCondition, final List<Object> originalValues) {
        Optional<String> assistedQueryColumn = getEncryptRule().findAssistedQueryColumn(encryptCondition.getTableName(), encryptCondition.getColumnName());
        return assistedQueryColumn.isPresent() 
                ? getEncryptRule().getEncryptAssistedQueryValues(encryptCondition.getTableName(), encryptCondition.getColumnName(), originalValues) 
                : getEncryptRule().getEncryptValues(encryptCondition.getTableName(), encryptCondition.getColumnName(), originalValues);
    }
    
    private EncryptPredicateRightValueToken generateSQLTokenForQueryWithoutCipherColumn(final EncryptCondition encryptCondition, final List<Object> originalValues) {
        return new EncryptPredicateRightValueToken(encryptCondition.getStartIndex(), encryptCondition.getStopIndex(),  
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
