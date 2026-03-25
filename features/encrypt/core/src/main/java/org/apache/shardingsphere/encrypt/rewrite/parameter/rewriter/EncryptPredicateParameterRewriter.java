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

package org.apache.shardingsphere.encrypt.rewrite.parameter.rewriter;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.encrypt.exception.metadata.MissingMatchedEncryptQueryAlgorithmException;
import org.apache.shardingsphere.encrypt.rewrite.condition.EncryptCondition;
import org.apache.shardingsphere.encrypt.rewrite.condition.EncryptConditionValues;
import org.apache.shardingsphere.encrypt.rewrite.condition.impl.EncryptBinaryCondition;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.binder.context.available.WhereContextAvailable;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.StandardParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewriter;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Predicate parameter rewriter for encrypt.
 */
@RequiredArgsConstructor
public final class EncryptPredicateParameterRewriter implements ParameterRewriter {
    
    private final EncryptRule rule;
    
    private final Collection<EncryptCondition> encryptConditions;
    
    @Override
    public boolean isNeedRewrite(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof WhereContextAvailable && !encryptConditions.isEmpty();
    }
    
    @Override
    public void rewrite(final ParameterBuilder paramBuilder, final SQLStatementContext sqlStatementContext, final List<Object> params) {
        for (EncryptCondition each : encryptConditions) {
            encryptParameters(paramBuilder, each.getPositionIndexMap(), getEncryptedValues(each, new EncryptConditionValues(each).get(params)));
        }
    }
    
    private List<Object> getEncryptedValues(final EncryptCondition encryptCondition, final List<Object> originalValues) {
        ColumnSegmentBoundInfo columnBoundInfo = encryptCondition.getColumnSegment().getColumnBoundInfo();
        String databaseName = columnBoundInfo.getOriginalDatabase().getValue();
        String schemaName = columnBoundInfo.getOriginalSchema().getValue();
        String tableName = columnBoundInfo.getOriginalTable().getValue();
        String columnName = columnBoundInfo.getOriginalColumn().getValue();
        EncryptTable encryptTable = rule.getEncryptTable(tableName);
        EncryptColumn encryptColumn = encryptTable.getEncryptColumn(columnName);
        if (encryptCondition instanceof EncryptBinaryCondition && containsLikeOperator((EncryptBinaryCondition) encryptCondition)) {
            return getEncryptedLikeValues(databaseName, schemaName, originalValues, encryptColumn, tableName, columnName);
        }
        return encryptColumn.getAssistedQuery().isPresent()
                ? encryptColumn.getAssistedQuery().get().encrypt(databaseName, schemaName, tableName, columnName, originalValues)
                : encryptColumn.getCipher().encrypt(databaseName, schemaName, tableName, columnName, originalValues);
    }
    
    private List<Object> getEncryptedLikeValues(final String databaseName, final String schemaName, final List<Object> originalValues,
                                                final EncryptColumn encryptColumn, final String tableName, final String columnName) {
        ShardingSpherePreconditions.checkState(encryptColumn.getLikeQuery().isPresent() || encryptColumn.getCipher().getEncryptor().getMetaData().isSupportLike(),
                () -> new MissingMatchedEncryptQueryAlgorithmException(tableName, columnName, "LIKE"));
        return encryptColumn.getLikeQuery()
                .map(likeQueryColumnItem -> likeQueryColumnItem.encrypt(databaseName, schemaName, tableName, columnName, originalValues))
                .orElseGet(() -> encryptColumn.getCipher().encrypt(databaseName, schemaName, tableName, columnName, originalValues));
    }
    
    private boolean containsLikeOperator(final EncryptBinaryCondition encryptCondition) {
        return "LIKE".equalsIgnoreCase(encryptCondition.getOperator()) || "NOT LIKE".equalsIgnoreCase(encryptCondition.getOperator());
    }
    
    private void encryptParameters(final ParameterBuilder paramBuilder, final Map<Integer, Integer> positionIndexes, final List<Object> encryptValues) {
        if (!positionIndexes.isEmpty()) {
            for (Entry<Integer, Integer> entry : positionIndexes.entrySet()) {
                ((StandardParameterBuilder) paramBuilder).addReplacedParameters(entry.getValue(), encryptValues.get(entry.getKey()));
            }
        }
    }
}
