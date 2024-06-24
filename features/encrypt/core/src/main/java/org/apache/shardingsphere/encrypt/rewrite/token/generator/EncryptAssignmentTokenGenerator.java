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

import com.google.common.base.Preconditions;
import lombok.Setter;
import org.apache.shardingsphere.encrypt.rewrite.aware.DatabaseNameAware;
import org.apache.shardingsphere.encrypt.rewrite.aware.EncryptRuleAware;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptAssignmentToken;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptLiteralAssignmentToken;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptParameterAssignmentToken;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.TableAvailable;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.UpdateStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Assignment generator for encrypt.
 */
@Setter
public final class EncryptAssignmentTokenGenerator implements CollectionSQLTokenGenerator<SQLStatementContext>, EncryptRuleAware, DatabaseNameAware {
    
    private EncryptRule encryptRule;
    
    private String databaseName;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof UpdateStatementContext || sqlStatementContext instanceof InsertStatementContext
                && (((InsertStatementContext) sqlStatementContext).getSqlStatement()).getSetAssignment().isPresent();
    }
    
    @Override
    public Collection<SQLToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        String tableName = ((TableAvailable) sqlStatementContext).getTablesContext().getSimpleTables().iterator().next().getTableName().getIdentifier().getValue();
        Optional<EncryptTable> encryptTable = encryptRule.findEncryptTable(tableName);
        if (!encryptTable.isPresent()) {
            return Collections.emptyList();
        }
        Collection<SQLToken> result = new LinkedList<>();
        String schemaName = ((TableAvailable) sqlStatementContext).getTablesContext().getSchemaName()
                .orElseGet(() -> new DatabaseTypeRegistry(sqlStatementContext.getDatabaseType()).getDefaultSchemaName(databaseName));
        for (ColumnAssignmentSegment each : getSetAssignmentSegment(sqlStatementContext.getSqlStatement()).getAssignments()) {
            String columnName = each.getColumns().get(0).getIdentifier().getValue();
            if (encryptTable.get().isEncryptColumn(columnName)) {
                generateSQLToken(schemaName, encryptTable.get().getTable(), encryptTable.get().getEncryptColumn(columnName), each).ifPresent(result::add);
            }
        }
        return result;
    }
    
    private SetAssignmentSegment getSetAssignmentSegment(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof InsertStatement) {
            Optional<SetAssignmentSegment> result = ((InsertStatement) sqlStatement).getSetAssignment();
            Preconditions.checkState(result.isPresent());
            return result.get();
        }
        return ((UpdateStatement) sqlStatement).getSetAssignment();
    }
    
    private Optional<EncryptAssignmentToken> generateSQLToken(final String schemaName, final String tableName, final EncryptColumn encryptColumn, final ColumnAssignmentSegment segment) {
        if (segment.getValue() instanceof ParameterMarkerExpressionSegment) {
            return Optional.of(generateParameterSQLToken(encryptColumn, segment));
        }
        if (segment.getValue() instanceof LiteralExpressionSegment) {
            return Optional.of(generateLiteralSQLToken(schemaName, tableName, encryptColumn, segment));
        }
        return Optional.empty();
    }
    
    private EncryptAssignmentToken generateParameterSQLToken(final EncryptColumn encryptColumn, final ColumnAssignmentSegment segment) {
        EncryptParameterAssignmentToken result = new EncryptParameterAssignmentToken(segment.getColumns().get(0).getStartIndex(), segment.getStopIndex());
        result.addColumnName(encryptColumn.getCipher().getName());
        encryptColumn.getAssistedQuery().ifPresent(optional -> result.addColumnName(optional.getName()));
        encryptColumn.getLikeQuery().ifPresent(optional -> result.addColumnName(optional.getName()));
        return result;
    }
    
    private EncryptAssignmentToken generateLiteralSQLToken(final String schemaName, final String tableName, final EncryptColumn encryptColumn, final ColumnAssignmentSegment segment) {
        EncryptLiteralAssignmentToken result = new EncryptLiteralAssignmentToken(segment.getColumns().get(0).getStartIndex(), segment.getStopIndex());
        addCipherAssignment(schemaName, tableName, encryptColumn, segment, result);
        addAssistedQueryAssignment(schemaName, tableName, encryptColumn, segment, result);
        addLikeAssignment(schemaName, tableName, encryptColumn, segment, result);
        return result;
    }
    
    private void addCipherAssignment(final String schemaName, final String tableName,
                                     final EncryptColumn encryptColumn, final ColumnAssignmentSegment segment, final EncryptLiteralAssignmentToken token) {
        Object originalValue = ((LiteralExpressionSegment) segment.getValue()).getLiterals();
        Object cipherValue = encryptColumn.getCipher().encrypt(databaseName, schemaName, tableName, encryptColumn.getName(), Collections.singletonList(originalValue)).iterator().next();
        token.addAssignment(encryptColumn.getCipher().getName(), cipherValue);
    }
    
    private void addAssistedQueryAssignment(final String schemaName, final String tableName, final EncryptColumn encryptColumn,
                                            final ColumnAssignmentSegment segment, final EncryptLiteralAssignmentToken token) {
        Object originalValue = ((LiteralExpressionSegment) segment.getValue()).getLiterals();
        if (encryptColumn.getAssistedQuery().isPresent()) {
            Object assistedQueryValue = encryptColumn.getAssistedQuery().get().encrypt(
                    databaseName, schemaName, tableName, encryptColumn.getName(), Collections.singletonList(originalValue)).iterator().next();
            token.addAssignment(encryptColumn.getAssistedQuery().get().getName(), assistedQueryValue);
        }
    }
    
    private void addLikeAssignment(final String schemaName, final String tableName,
                                   final EncryptColumn encryptColumn, final ColumnAssignmentSegment segment, final EncryptLiteralAssignmentToken token) {
        Object originalValue = ((LiteralExpressionSegment) segment.getValue()).getLiterals();
        if (encryptColumn.getLikeQuery().isPresent()) {
            Object assistedQueryValue = encryptColumn.getLikeQuery().get().encrypt(databaseName, schemaName,
                    tableName, segment.getColumns().get(0).getIdentifier().getValue(), Collections.singletonList(originalValue)).iterator().next();
            token.addAssignment(encryptColumn.getLikeQuery().get().getName(), assistedQueryValue);
        }
    }
}
