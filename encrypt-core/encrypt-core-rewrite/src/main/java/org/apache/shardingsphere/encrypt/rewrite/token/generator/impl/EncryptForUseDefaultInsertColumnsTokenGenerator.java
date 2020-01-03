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
import com.google.common.base.Preconditions;
import lombok.Setter;
import org.apache.shardingsphere.core.strategy.encrypt.EncryptTable;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.BaseEncryptSQLTokenGenerator;
import org.apache.shardingsphere.underlying.rewrite.sql.token.generator.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.underlying.rewrite.sql.token.generator.aware.PreviousSQLTokensAware;
import org.apache.shardingsphere.underlying.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.underlying.rewrite.sql.token.pojo.generic.UseDefaultInsertColumnsToken;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Use default insert columns token generator for encrypt.
 *
 * @author panjuan
 * @author zhangliang
 */
@Setter
public final class EncryptForUseDefaultInsertColumnsTokenGenerator extends BaseEncryptSQLTokenGenerator implements OptionalSQLTokenGenerator, PreviousSQLTokensAware {
    
    private List<SQLToken> previousSQLTokens;
    
    @Override
    protected boolean isGenerateSQLTokenForEncrypt(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof InsertSQLStatementContext && ((InsertStatement) sqlStatementContext.getSqlStatement()).useDefaultColumns();
    }
    
    @Override
    public UseDefaultInsertColumnsToken generateSQLToken(final SQLStatementContext sqlStatementContext) {
        String tableName = sqlStatementContext.getTablesContext().getSingleTableName();
        Optional<UseDefaultInsertColumnsToken> previousSQLToken = findInsertColumnsToken();
        if (previousSQLToken.isPresent()) {
            processPreviousSQLToken(previousSQLToken.get(), (InsertSQLStatementContext) sqlStatementContext, tableName);
            return previousSQLToken.get();
        }
        return generateNewSQLToken((InsertSQLStatementContext) sqlStatementContext, tableName);
    }
    
    private Optional<UseDefaultInsertColumnsToken> findInsertColumnsToken() {
        for (SQLToken each : previousSQLTokens) {
            if (each instanceof UseDefaultInsertColumnsToken) {
                return Optional.of((UseDefaultInsertColumnsToken) each);
            }
        }
        return Optional.absent();
    }
    
    private void processPreviousSQLToken(final UseDefaultInsertColumnsToken previousSQLToken, final InsertSQLStatementContext sqlStatementContext, final String tableName) {
        Optional<EncryptTable> encryptTable = getEncryptRule().findEncryptTable(tableName);
        Preconditions.checkState(encryptTable.isPresent());
        List<String> columnNames = getColumnNames(sqlStatementContext, encryptTable.get(), previousSQLToken.getColumns());
        previousSQLToken.getColumns().clear();
        previousSQLToken.getColumns().addAll(columnNames);
    }
    
    private UseDefaultInsertColumnsToken generateNewSQLToken(final InsertSQLStatementContext sqlStatementContext, final String tableName) {
        Optional<InsertColumnsSegment> insertColumnsSegment = sqlStatementContext.getSqlStatement().findSQLSegment(InsertColumnsSegment.class);
        Preconditions.checkState(insertColumnsSegment.isPresent());
        Optional<EncryptTable> encryptTable = getEncryptRule().findEncryptTable(tableName);
        Preconditions.checkState(encryptTable.isPresent());
        return new UseDefaultInsertColumnsToken(insertColumnsSegment.get().getStopIndex(), getColumnNames(sqlStatementContext, encryptTable.get(), sqlStatementContext.getColumnNames()));
    }
    
    private List<String> getColumnNames(final InsertSQLStatementContext sqlStatementContext, final EncryptTable encryptTable, final List<String> currentColumnNames) {
        List<String> result = new LinkedList<>(currentColumnNames);
        Iterator<String> descendingColumnNames = sqlStatementContext.getDescendingColumnNames();
        while (descendingColumnNames.hasNext()) {
            String columnName = descendingColumnNames.next();
            if (encryptTable.findShardingEncryptor(columnName).isPresent()) {
                int columnIndex = result.indexOf(columnName);
                addPlainColumn(result, encryptTable, columnName, columnIndex);
                addAssistedQueryColumn(result, encryptTable, columnName, columnIndex);
                setCipherColumn(result, encryptTable, columnName, columnIndex);
            }
        }
        return result;
    }
    
    private void addPlainColumn(final List<String> columnNames, final EncryptTable encryptTable, final String columnName, final int columnIndex) {
        Optional<String> plainColumn = encryptTable.findPlainColumn(columnName);
        if (plainColumn.isPresent()) {
            columnNames.add(columnIndex + 1, plainColumn.get());
        }
    }
    
    private void addAssistedQueryColumn(final List<String> columnNames, final EncryptTable encryptTable, final String columnName, final int columnIndex) {
        Optional<String> assistedQueryColumn = encryptTable.findAssistedQueryColumn(columnName);
        if (assistedQueryColumn.isPresent()) {
            columnNames.add(columnIndex + 1, assistedQueryColumn.get());
        }
    }
    
    private void setCipherColumn(final List<String> columnNames, final EncryptTable encryptTable, final String columnName, final int columnIndex) {
        columnNames.set(columnIndex, encryptTable.getCipherColumn(columnName));
    }
}
