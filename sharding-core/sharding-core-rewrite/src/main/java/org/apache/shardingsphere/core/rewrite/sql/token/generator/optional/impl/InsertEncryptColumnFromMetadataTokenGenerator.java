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
import com.google.common.base.Preconditions;
import lombok.Setter;
import org.apache.shardingsphere.core.optimize.statement.SQLStatementContext;
import org.apache.shardingsphere.core.optimize.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.PreviousSQLTokensAware;
import org.apache.shardingsphere.core.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.core.rewrite.sql.token.pojo.impl.InsertRegularNamesToken;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.EncryptRuleAware;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.optional.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Insert encrypt column from metadata token generator.
 *
 * @author panjuan
 */
@Setter
public final class InsertEncryptColumnFromMetadataTokenGenerator implements OptionalSQLTokenGenerator, EncryptRuleAware, PreviousSQLTokensAware {
    
    private EncryptRule encryptRule;
    
    private List<SQLToken> previousSQLTokens;
    
    @Override
    public Optional<InsertRegularNamesToken> generateSQLToken(final SQLStatementContext sqlStatementContext) {
        return isNeedToGenerateSQLToken(sqlStatementContext.getSqlStatement()) ? createInsertColumnsToken(sqlStatementContext) : Optional.<InsertRegularNamesToken>absent();
    }
    
    private boolean isNeedToGenerateSQLToken(final SQLStatement sqlStatement) {
        return sqlStatement instanceof InsertStatement && ((InsertStatement) sqlStatement).useDefaultColumns() && sqlStatement.findSQLSegment(InsertColumnsSegment.class).isPresent();
    }
    
    private Optional<InsertRegularNamesToken> createInsertColumnsToken(final SQLStatementContext sqlStatementContext) {
        String tableName = sqlStatementContext.getTablesContext().getSingleTableName();
        boolean hasMoreDerivedColumns = !encryptRule.getAssistedQueryAndPlainColumns(tableName).isEmpty();
        Optional<InsertRegularNamesToken> previousInsertRegularNamesToken = findInsertRegularNamesToken();
        if (previousInsertRegularNamesToken.isPresent()) {
            processPreviousSQLToken(previousInsertRegularNamesToken.get(), tableName, hasMoreDerivedColumns);
            return Optional.absent();
        }
        return Optional.of(createNewSQLToken((InsertSQLStatementContext) sqlStatementContext, tableName, hasMoreDerivedColumns));
    }
    
    private Optional<InsertRegularNamesToken> findInsertRegularNamesToken() {
        for (SQLToken each : previousSQLTokens) {
            if (each instanceof InsertRegularNamesToken) {
                return Optional.of((InsertRegularNamesToken) each);
            }
        }
        return Optional.absent();
    }
    
    private void processPreviousSQLToken(final InsertRegularNamesToken previousSQLToken, final String tableName, final boolean hasMoreDerivedColumns) {
        for (Entry<String, String> entry : encryptRule.getLogicAndCipherColumns(tableName).entrySet()) {
            int encryptLogicColumnIndex = previousSQLToken.getColumns().indexOf(entry.getKey());
            if (-1 != encryptLogicColumnIndex) {
                previousSQLToken.getColumns().set(encryptLogicColumnIndex, entry.getValue());
            }
        }
        previousSQLToken.setToAppendCloseParenthesis(!hasMoreDerivedColumns);
    }
    
    private InsertRegularNamesToken createNewSQLToken(final InsertSQLStatementContext sqlStatementContext, final String tableName, final boolean hasMoreDerivedColumns) {
        Optional<InsertColumnsSegment> insertColumnsSegment = sqlStatementContext.getSqlStatement().findSQLSegment(InsertColumnsSegment.class);
        Preconditions.checkState(insertColumnsSegment.isPresent());
        List<String> result = new LinkedList<>();
        Map<String, String> logicAndCipherColumns = encryptRule.getLogicAndCipherColumns(tableName);
        for (String each : sqlStatementContext.getColumnNames()) {
            result.add(logicAndCipherColumns.containsKey(each) ? logicAndCipherColumns.get(each) : each);
        }
        return new InsertRegularNamesToken(insertColumnsSegment.get().getStopIndex(), result, !hasMoreDerivedColumns);
    }
}
