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
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.EncryptRuleAware;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.PreviousSQLTokensAware;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.optional.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.core.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.core.rewrite.sql.token.pojo.impl.InsertColumnsToken;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.strategy.encrypt.EncryptTable;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Encrypt for insert columns token generator.
 *
 * @author panjuan
 * @author zhangliang
 */
@Setter
public final class EncryptForInsertColumnsTokenGenerator implements OptionalSQLTokenGenerator, EncryptRuleAware, PreviousSQLTokensAware {
    
    private EncryptRule encryptRule;
    
    private List<SQLToken> previousSQLTokens;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof InsertSQLStatementContext && ((InsertStatement) sqlStatementContext.getSqlStatement()).useDefaultColumns();
    }
    
    @Override
    public InsertColumnsToken generateSQLToken(final SQLStatementContext sqlStatementContext) {
        String tableName = sqlStatementContext.getTablesContext().getSingleTableName();
        Optional<InsertColumnsToken> previousSQLToken = findInsertColumnsToken();
        if (previousSQLToken.isPresent()) {
            processPreviousSQLToken(previousSQLToken.get(), tableName);
            return previousSQLToken.get();
        }
        return generateNewSQLToken((InsertSQLStatementContext) sqlStatementContext, tableName);
    }
    
    private Optional<InsertColumnsToken> findInsertColumnsToken() {
        for (SQLToken each : previousSQLTokens) {
            if (each instanceof InsertColumnsToken) {
                return Optional.of((InsertColumnsToken) each);
            }
        }
        return Optional.absent();
    }
    
    private void processPreviousSQLToken(final InsertColumnsToken previousSQLToken, final String tableName) {
        for (Entry<String, String> entry : encryptRule.getLogicAndCipherColumns(tableName).entrySet()) {
            int encryptLogicColumnIndex = previousSQLToken.getColumns().indexOf(entry.getKey());
            if (-1 != encryptLogicColumnIndex) {
                previousSQLToken.getColumns().set(encryptLogicColumnIndex, entry.getValue());
            }
        }
        Optional<EncryptTable> encryptTable = encryptRule.findEncryptTable(tableName);
        if (encryptTable.isPresent()) {
            previousSQLToken.getColumns().addAll(getEncryptDerivedColumnNames(encryptTable.get(), tableName));
        }
    }
    
    private InsertColumnsToken generateNewSQLToken(final InsertSQLStatementContext sqlStatementContext, final String tableName) {
        Optional<InsertColumnsSegment> insertColumnsSegment = sqlStatementContext.getSqlStatement().findSQLSegment(InsertColumnsSegment.class);
        Preconditions.checkState(insertColumnsSegment.isPresent());
        List<String> columnNames = new LinkedList<>();
        Map<String, String> logicAndCipherColumns = encryptRule.getLogicAndCipherColumns(tableName);
        for (String each : sqlStatementContext.getColumnNames()) {
            columnNames.add(logicAndCipherColumns.containsKey(each) ? logicAndCipherColumns.get(each) : each);
        }
        Optional<EncryptTable> encryptTable = encryptRule.findEncryptTable(tableName);
        if (encryptTable.isPresent()) {
            columnNames.addAll(getEncryptDerivedColumnNames(encryptTable.get(), tableName));
        }
        return new InsertColumnsToken(insertColumnsSegment.get().getStopIndex(), columnNames);
    }
    
    private List<String> getEncryptDerivedColumnNames(final EncryptTable encryptTable, final String tableName) {
        List<String> result = new LinkedList<>();
        for (String each : encryptTable.getLogicColumns()) {
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
