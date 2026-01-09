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

package org.apache.shardingsphere.encrypt.rewrite.token.generator.predicate;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.encrypt.exception.metadata.MissingMatchedEncryptQueryAlgorithmException;
import org.apache.shardingsphere.encrypt.rewrite.condition.EncryptCondition;
import org.apache.shardingsphere.encrypt.rewrite.condition.EncryptConditionValues;
import org.apache.shardingsphere.encrypt.rewrite.condition.impl.EncryptBinaryCondition;
import org.apache.shardingsphere.encrypt.rewrite.condition.impl.EncryptInCondition;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptPredicateEqualRightValueToken;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptPredicateFunctionRightValueToken;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptPredicateInRightValueToken;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.column.item.LikeQueryColumnItem;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.available.WhereContextAvailable;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.aware.ParametersAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Predicate value token generator for encrypt.
 */
@HighFrequencyInvocation
@RequiredArgsConstructor
@Setter
public final class EncryptPredicateValueTokenGenerator implements CollectionSQLTokenGenerator<SQLStatementContext>, ParametersAware {
    
    private final EncryptRule rule;
    
    private final ShardingSphereDatabase database;
    
    private final Collection<EncryptCondition> encryptConditions;
    
    private List<Object> parameters;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof WhereContextAvailable;
    }
    
    @Override
    public Collection<SQLToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        Collection<SQLToken> result = new LinkedHashSet<>(encryptConditions.size(), 1F);
        String schemaName = sqlStatementContext.getTablesContext().getSchemaName()
                .orElseGet(() -> new DatabaseTypeRegistry(sqlStatementContext.getSqlStatement().getDatabaseType()).getDefaultSchemaName(database.getName()));
        for (EncryptCondition each : encryptConditions) {
            Optional<EncryptTable> encryptTable = rule.findEncryptTable(each.getTableName());
            encryptTable.flatMap(optional -> generateSQLToken(schemaName, optional, each)).ifPresent(result::add);
        }
        return result;
    }
    
    private Optional<SQLToken> generateSQLToken(final String schemaName, final EncryptTable encryptTable, final EncryptCondition encryptCondition) {
        int startIndex = encryptCondition.getStartIndex();
        int stopIndex = encryptCondition.getStopIndex();
        Map<Integer, Object> indexValues = getPositionValues(
                encryptCondition.getPositionValueMap().keySet(), getEncryptedValues(schemaName, encryptTable, encryptCondition, new EncryptConditionValues(encryptCondition).get(parameters)));
        Collection<Integer> parameterMarkerIndexes = encryptCondition.getPositionIndexMap().keySet();
        if (encryptCondition instanceof EncryptBinaryCondition && ((EncryptBinaryCondition) encryptCondition).getExpressionSegment() instanceof FunctionSegment) {
            FunctionSegment functionSegment = (FunctionSegment) ((EncryptBinaryCondition) encryptCondition).getExpressionSegment();
            return Optional.of(new EncryptPredicateFunctionRightValueToken(startIndex, stopIndex, functionSegment.getFunctionName(), functionSegment.getParameters(), indexValues,
                    parameterMarkerIndexes));
        }
        
        return encryptCondition instanceof EncryptInCondition
                ? Optional.of(new EncryptPredicateInRightValueToken(startIndex, stopIndex, indexValues, parameterMarkerIndexes))
                : Optional.of(new EncryptPredicateEqualRightValueToken(startIndex, stopIndex, indexValues, parameterMarkerIndexes));
    }
    
    private List<Object> getEncryptedValues(final String schemaName, final EncryptTable encryptTable, final EncryptCondition encryptCondition, final List<Object> originalValues) {
        String columnName = encryptCondition.getColumnSegment().getColumnBoundInfo().getOriginalColumn().getValue();
        EncryptColumn encryptColumn = encryptTable.getEncryptColumn(columnName);
        if (encryptCondition instanceof EncryptBinaryCondition && ("LIKE".equalsIgnoreCase(((EncryptBinaryCondition) encryptCondition).getOperator())
                || "NOT LIKE".equalsIgnoreCase(((EncryptBinaryCondition) encryptCondition).getOperator()))) {
            LikeQueryColumnItem likeQueryColumnItem = encryptColumn.getLikeQuery()
                    .orElseThrow(() -> new MissingMatchedEncryptQueryAlgorithmException(encryptTable.getTable(), columnName, "LIKE"));
            return likeQueryColumnItem.encrypt(database.getName(), schemaName, encryptCondition.getTableName(), columnName, originalValues);
        }
        String tableName = encryptCondition.getColumnSegment().getColumnBoundInfo().getOriginalTable().getValue();
        return encryptColumn.getAssistedQuery()
                .map(optional -> optional.encrypt(database.getName(), schemaName, tableName, encryptCondition.getColumnSegment().getIdentifier().getValue(), originalValues))
                .orElseGet(() -> encryptColumn.getCipher().encrypt(database.getName(), schemaName, tableName, encryptCondition.getColumnSegment().getIdentifier().getValue(), originalValues));
    }
    
    private Map<Integer, Object> getPositionValues(final Collection<Integer> valuePositions, final List<Object> encryptValues) {
        Map<Integer, Object> result = new LinkedHashMap<>(valuePositions.size(), 1F);
        for (int each : valuePositions) {
            result.put(each, encryptValues.get(each));
        }
        return result;
    }
}
