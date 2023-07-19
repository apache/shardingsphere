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

package org.apache.shardingsphere.encrypt.rewrite.token.generator;

import lombok.Setter;
import org.apache.shardingsphere.encrypt.exception.syntax.UnsupportedEncryptSQLException;
import org.apache.shardingsphere.encrypt.rewrite.aware.DatabaseNameAware;
import org.apache.shardingsphere.encrypt.rewrite.aware.EncryptConditionsAware;
import org.apache.shardingsphere.encrypt.rewrite.aware.EncryptRuleAware;
import org.apache.shardingsphere.encrypt.rewrite.condition.EncryptCondition;
import org.apache.shardingsphere.encrypt.rewrite.condition.impl.EncryptBinaryCondition;
import org.apache.shardingsphere.encrypt.rewrite.condition.impl.EncryptInCondition;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptPredicateEqualRightValueToken;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptPredicateFunctionRightValueToken;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptPredicateInRightValueToken;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.column.item.LikeQueryColumnItem;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.WhereAvailable;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.ParametersAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.FunctionSegment;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Predicate right value token generator for encrypt.
 */
@Setter
public final class EncryptPredicateRightValueTokenGenerator implements CollectionSQLTokenGenerator<SQLStatementContext>, ParametersAware, EncryptConditionsAware, EncryptRuleAware, DatabaseNameAware {
    
    private List<Object> parameters;
    
    private Collection<EncryptCondition> encryptConditions;
    
    private EncryptRule encryptRule;
    
    private String databaseName;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof WhereAvailable && !((WhereAvailable) sqlStatementContext).getWhereSegments().isEmpty();
    }
    
    @Override
    public Collection<SQLToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        Collection<SQLToken> result = new LinkedHashSet<>();
        String schemaName = sqlStatementContext.getTablesContext().getSchemaName().orElseGet(() -> DatabaseTypeEngine.getDefaultSchemaName(sqlStatementContext.getDatabaseType(), databaseName));
        for (EncryptCondition each : encryptConditions) {
            encryptRule.findEncryptTable(each.getTableName()).ifPresent(optional -> result.add(generateSQLToken(schemaName, optional, each)));
        }
        return result;
    }
    
    private SQLToken generateSQLToken(final String schemaName, final EncryptTable encryptTable, final EncryptCondition encryptCondition) {
        List<Object> originalValues = encryptCondition.getValues(parameters);
        int startIndex = encryptCondition.getStartIndex();
        return generateSQLToken(schemaName, encryptTable, encryptCondition, originalValues, startIndex);
    }
    
    private SQLToken generateSQLToken(final String schemaName, final EncryptTable encryptTable, final EncryptCondition encryptCondition, final List<Object> originalValues, final int startIndex) {
        int stopIndex = encryptCondition.getStopIndex();
        Map<Integer, Object> indexValues = getPositionValues(encryptCondition.getPositionValueMap().keySet(), getEncryptedValues(schemaName, encryptTable, encryptCondition, originalValues));
        Collection<Integer> parameterMarkerIndexes = encryptCondition.getPositionIndexMap().keySet();
        if (encryptCondition instanceof EncryptBinaryCondition && ((EncryptBinaryCondition) encryptCondition).getExpressionSegment() instanceof FunctionSegment) {
            return new EncryptPredicateFunctionRightValueToken(startIndex, stopIndex,
                    ((FunctionSegment) ((EncryptBinaryCondition) encryptCondition).getExpressionSegment()).getFunctionName(), indexValues, parameterMarkerIndexes);
        }
        return encryptCondition instanceof EncryptInCondition
                ? new EncryptPredicateInRightValueToken(startIndex, stopIndex, indexValues, parameterMarkerIndexes)
                : new EncryptPredicateEqualRightValueToken(startIndex, stopIndex, indexValues, parameterMarkerIndexes);
    }
    
    private List<Object> getEncryptedValues(final String schemaName, final EncryptTable encryptTable, final EncryptCondition encryptCondition, final List<Object> originalValues) {
        EncryptColumn encryptColumn = encryptTable.getEncryptColumn(encryptCondition.getColumnName());
        if (encryptCondition instanceof EncryptBinaryCondition && "LIKE".equalsIgnoreCase(((EncryptBinaryCondition) encryptCondition).getOperator())) {
            Optional<LikeQueryColumnItem> likeQueryColumnItem = encryptColumn.getLikeQuery();
            ShardingSpherePreconditions.checkState(likeQueryColumnItem.isPresent(), () -> new UnsupportedEncryptSQLException("LIKE"));
            return likeQueryColumnItem.get().encrypt(databaseName, schemaName, encryptCondition.getTableName(), encryptCondition.getColumnName(), originalValues);
        }
        return encryptColumn.getAssistedQuery()
                .map(optional -> optional.encrypt(databaseName, schemaName, encryptCondition.getTableName(), encryptCondition.getColumnName(), originalValues))
                .orElseGet(() -> encryptColumn.getCipher().encrypt(databaseName, schemaName, encryptCondition.getTableName(), encryptCondition.getColumnName(), originalValues));
    }
    
    private Map<Integer, Object> getPositionValues(final Collection<Integer> valuePositions, final List<Object> encryptValues) {
        Map<Integer, Object> result = new LinkedHashMap<>();
        for (int each : valuePositions) {
            result.put(each, encryptValues.get(each));
        }
        return result;
    }
}
