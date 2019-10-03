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

package org.apache.shardingsphere.core.rewrite.sql.token.generator.optional.impl;

import com.google.common.base.Optional;
import lombok.Setter;
import org.apache.shardingsphere.core.optimize.statement.SQLStatementContext;
import org.apache.shardingsphere.core.optimize.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.core.rewrite.sql.token.pojo.impl.InsertQueryAndPlainNamesToken;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.EncryptRuleAware;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.optional.OptionalSQLTokenGenerator;
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
@Setter
public final class InsertQueryAndPlainNamesTokenGenerator implements OptionalSQLTokenGenerator, EncryptRuleAware {
    
    private EncryptRule encryptRule;
    
    @Override
    public Optional<InsertQueryAndPlainNamesToken> generateSQLToken(final SQLStatementContext sqlStatementContext) {
        if (!isNeedToGenerateSQLToken(sqlStatementContext)) {
            return Optional.absent();
        }
        return createInsertAssistedColumnsToken(sqlStatementContext);
    }
    
    private boolean isNeedToGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        Optional<InsertColumnsSegment> insertColumnsSegment = sqlStatementContext.getSqlStatement().findSQLSegment(InsertColumnsSegment.class);
        return sqlStatementContext instanceof InsertSQLStatementContext && insertColumnsSegment.isPresent();
    }
    
    private Optional<InsertQueryAndPlainNamesToken> createInsertAssistedColumnsToken(final SQLStatementContext sqlStatementContext) {
        Optional<InsertColumnsSegment> insertColumnsSegment = sqlStatementContext.getSqlStatement().findSQLSegment(InsertColumnsSegment.class);
        if (!insertColumnsSegment.isPresent()) {
            return Optional.absent();
        }
        String tableName = sqlStatementContext.getTablesContext().getSingleTableName();
        return encryptRule.getAssistedQueryAndPlainColumns(tableName).isEmpty() ? Optional.<InsertQueryAndPlainNamesToken>absent()
                : Optional.of(new InsertQueryAndPlainNamesToken(
                        insertColumnsSegment.get().getStopIndex(), getEncryptDerivedColumnNames(tableName), insertColumnsSegment.get().getColumns().isEmpty()));
    }
    
    private List<String> getEncryptDerivedColumnNames(final String tableName) {
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
