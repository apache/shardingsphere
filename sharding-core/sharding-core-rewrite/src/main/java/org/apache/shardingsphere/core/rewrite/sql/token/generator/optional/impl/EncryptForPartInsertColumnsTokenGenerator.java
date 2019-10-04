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
import org.apache.shardingsphere.core.rewrite.sql.token.pojo.impl.PartInsertColumnsToken;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.strategy.encrypt.EncryptTable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Encrypt for part insert columns token generator.
 *
 * @author panjuan
 * @author zhangliang
 */
@Setter
public final class EncryptForPartInsertColumnsTokenGenerator implements OptionalSQLTokenGenerator, EncryptRuleAware, PreviousSQLTokensAware {
    
    private EncryptRule encryptRule;
    
    private List<SQLToken> previousSQLTokens;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof InsertSQLStatementContext && sqlStatementContext.getSqlStatement().findSQLSegment(InsertColumnsSegment.class).isPresent()
                && !((InsertStatement) sqlStatementContext.getSqlStatement()).useDefaultColumns()
                && !encryptRule.getAssistedQueryAndPlainColumns(sqlStatementContext.getTablesContext().getSingleTableName()).isEmpty();
    }
    
    @Override
    public PartInsertColumnsToken generateSQLToken(final SQLStatementContext sqlStatementContext) {
        Optional<InsertColumnsSegment> sqlSegment = sqlStatementContext.getSqlStatement().findSQLSegment(InsertColumnsSegment.class);
        Preconditions.checkState(sqlSegment.isPresent());
        String tableName = sqlStatementContext.getTablesContext().getSingleTableName();
        Optional<PartInsertColumnsToken> previousSQLToken = findPartInsertColumnsToken();
        if (previousSQLToken.isPresent()) {
            processPreviousSQLToken(previousSQLToken.get(), tableName);
            return previousSQLToken.get();
        }
        return generateNewSQLToken(sqlSegment.get(), tableName);
    }
    
    private Optional<PartInsertColumnsToken> findPartInsertColumnsToken() {
        for (SQLToken each : previousSQLTokens) {
            if (each instanceof PartInsertColumnsToken) {
                return Optional.of((PartInsertColumnsToken) each);
            }
        }
        return Optional.absent();
    }
    
    private void processPreviousSQLToken(final PartInsertColumnsToken previousSQLToken, final String tableName) {
        Optional<EncryptTable> encryptTable = encryptRule.findEncryptTable(tableName);
        if (encryptTable.isPresent()) {
            previousSQLToken.getColumns().addAll(getEncryptDerivedColumnNames(encryptTable.get(), tableName));
        }
    }
    
    private PartInsertColumnsToken generateNewSQLToken(final InsertColumnsSegment sqlSegment, final String tableName) {
        Optional<EncryptTable> encryptTable = encryptRule.findEncryptTable(tableName);
        List<String> columnNames = encryptTable.isPresent() ? getEncryptDerivedColumnNames(encryptTable.get(), tableName) : Collections.<String>emptyList();
        return new PartInsertColumnsToken(sqlSegment.getStopIndex(), columnNames);
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
