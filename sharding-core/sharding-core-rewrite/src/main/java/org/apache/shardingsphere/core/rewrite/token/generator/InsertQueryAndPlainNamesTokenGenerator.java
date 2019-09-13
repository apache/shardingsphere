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
import org.apache.shardingsphere.core.optimize.api.statement.InsertOptimizedStatement;
import org.apache.shardingsphere.core.optimize.api.statement.OptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.core.rewrite.builder.parameter.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.token.pojo.InsertQueryAndPlainNamesToken;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.strategy.encrypt.EncryptTable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Insert assisted and plain names token generator.
 *
 * @author panjuan
 */
public final class InsertQueryAndPlainNamesTokenGenerator implements OptionalSQLTokenGenerator<EncryptRule> {
    
    @Override
    public Optional<InsertQueryAndPlainNamesToken> generateSQLToken(
            final OptimizedStatement optimizedStatement, final ParameterBuilder parameterBuilder, final EncryptRule encryptRule, final boolean isQueryWithCipherColumn) {
        if (!isNeedToGenerateSQLToken(optimizedStatement)) {
            return Optional.absent();
        }
        return createInsertAssistedColumnsToken(optimizedStatement, encryptRule);
    }
    
    private boolean isNeedToGenerateSQLToken(final OptimizedStatement optimizedStatement) {
        Optional<InsertColumnsSegment> insertColumnsSegment = optimizedStatement.getSQLStatement().findSQLSegment(InsertColumnsSegment.class);
        return optimizedStatement instanceof InsertOptimizedStatement && insertColumnsSegment.isPresent();
    }
    
    private Optional<InsertQueryAndPlainNamesToken> createInsertAssistedColumnsToken(final OptimizedStatement optimizedStatement, final EncryptRule encryptRule) {
        Optional<InsertColumnsSegment> insertColumnsSegment = optimizedStatement.getSQLStatement().findSQLSegment(InsertColumnsSegment.class);
        if (!insertColumnsSegment.isPresent()) {
            return Optional.absent();
        }
        String tableName = optimizedStatement.getTables().getSingleTableName();
        return encryptRule.getAssistedQueryAndPlainColumns(tableName).isEmpty() ? Optional.<InsertQueryAndPlainNamesToken>absent()
                : Optional.of(new InsertQueryAndPlainNamesToken(
                        insertColumnsSegment.get().getStopIndex(), getEncryptDerivedColumnNames(tableName, encryptRule), insertColumnsSegment.get().getColumns().isEmpty()));
    }
    
    private List<String> getEncryptDerivedColumnNames(final String tableName, final EncryptRule encryptRule) {
        Optional<EncryptTable> encryptTable = encryptRule.findEncryptTable(tableName);
        if (!encryptTable.isPresent()) {
            return Collections.emptyList();
        }
        List<String> result = new LinkedList<>();
        for (String each : encryptTable.get().getLogicColumns()) {
            Optional<String> assistedQueryColumn = encryptRule.findAssistedQueryColumn(tableName, each);
            if (assistedQueryColumn.isPresent()) {
                result.add(assistedQueryColumn.get());
            }
            Optional<String> plainColumn = encryptRule.findPlainColumn(tableName, each);
            if (plainColumn.isPresent()) {
                result.add(plainColumn.get());
            }
        }
        return result;
    }
}
