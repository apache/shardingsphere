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

package org.apache.shardingsphere.sql.rewriter.encrypt.token.generator.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.assignment.SetAssignmentsSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.rewriter.encrypt.token.generator.BaseEncryptSQLTokenGenerator;
import org.apache.shardingsphere.sql.rewriter.encrypt.token.pojo.EncryptAssignmentToken;
import org.apache.shardingsphere.sql.rewriter.encrypt.token.pojo.EncryptLiteralAssignmentToken;
import org.apache.shardingsphere.sql.rewriter.encrypt.token.pojo.EncryptParameterAssignmentToken;
import org.apache.shardingsphere.sql.rewriter.sql.token.generator.CollectionSQLTokenGenerator;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Assignment generator for encrypt.
 *
 * @author panjuan
 */
public final class EncryptAssignmentTokenGenerator extends BaseEncryptSQLTokenGenerator implements CollectionSQLTokenGenerator {
    
    @Override
    protected boolean isGenerateSQLTokenForEncrypt(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext.getSqlStatement() instanceof UpdateStatement
                || sqlStatementContext instanceof InsertSQLStatementContext && sqlStatementContext.getSqlStatement().findSQLSegment(SetAssignmentsSegment.class).isPresent();
    }
    
    @Override
    public Collection<EncryptAssignmentToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        Collection<EncryptAssignmentToken> result = new LinkedList<>();
        String tableName = sqlStatementContext.getTablesContext().getSingleTableName();
        for (AssignmentSegment each : getSetAssignmentsSegment(sqlStatementContext.getSqlStatement()).getAssignments()) {
            if (getEncryptRule().findShardingEncryptor(tableName, each.getColumn().getName()).isPresent()) {
                Optional<EncryptAssignmentToken> sqlToken = generateSQLToken(tableName, each);
                if (sqlToken.isPresent()) {
                    result.add(sqlToken.get());
                }
            }
        }
        return result;
    }
    
    private SetAssignmentsSegment getSetAssignmentsSegment(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof InsertStatement) {
            Optional<SetAssignmentsSegment> result = ((InsertStatement) sqlStatement).getSetAssignment();
            Preconditions.checkState(result.isPresent());
            return result.get();
        }
        return ((UpdateStatement) sqlStatement).getSetAssignment();
    }
    
    private Optional<EncryptAssignmentToken> generateSQLToken(final String tableName, final AssignmentSegment assignmentSegment) {
        if (assignmentSegment.getValue() instanceof ParameterMarkerExpressionSegment) {
            return Optional.of(generateParameterSQLToken(tableName, assignmentSegment));
        }
        if (assignmentSegment.getValue() instanceof LiteralExpressionSegment) {
            return Optional.of(generateLiteralSQLToken(tableName, assignmentSegment));
        }
        return Optional.absent();
    }
    
    private EncryptAssignmentToken generateParameterSQLToken(final String tableName, final AssignmentSegment assignmentSegment) {
        EncryptParameterAssignmentToken result = new EncryptParameterAssignmentToken(assignmentSegment.getColumn().getStartIndex(), assignmentSegment.getStopIndex());
        String columnName = assignmentSegment.getColumn().getName();
        addCipherColumn(tableName, columnName, result);
        addAssistedQueryColumn(tableName, columnName, result);
        addPlainColumn(tableName, columnName, result);
        return result;
    }
    
    private void addCipherColumn(final String tableName, final String columnName, final EncryptParameterAssignmentToken token) {
        token.addColumnName(getEncryptRule().getCipherColumn(tableName, columnName));
    }
    
    private void addAssistedQueryColumn(final String tableName, final String columnName, final EncryptParameterAssignmentToken token) {
        Optional<String> assistedQueryColumn = getEncryptRule().findAssistedQueryColumn(tableName, columnName);
        if (assistedQueryColumn.isPresent()) {
            token.addColumnName(assistedQueryColumn.get());
        }
    }
    
    private void addPlainColumn(final String tableName, final String columnName, final EncryptParameterAssignmentToken token) {
        Optional<String> plainColumn = getEncryptRule().findPlainColumn(tableName, columnName);
        if (plainColumn.isPresent()) {
            token.addColumnName(plainColumn.get());
        }
    }
    
    private EncryptAssignmentToken generateLiteralSQLToken(final String tableName, final AssignmentSegment assignmentSegment) {
        EncryptLiteralAssignmentToken result = new EncryptLiteralAssignmentToken(assignmentSegment.getColumn().getStartIndex(), assignmentSegment.getStopIndex());
        addCipherAssignment(tableName, assignmentSegment, result);
        addAssistedQueryAssignment(tableName, assignmentSegment, result);
        addPlainAssignment(tableName, assignmentSegment, result);
        return result;
    }
    
    private void addCipherAssignment(final String tableName, final AssignmentSegment assignmentSegment, final EncryptLiteralAssignmentToken token) {
        Object originalValue = ((LiteralExpressionSegment) assignmentSegment.getValue()).getLiterals();
        Object cipherValue = getEncryptRule().getEncryptValues(tableName, assignmentSegment.getColumn().getName(), Collections.singletonList(originalValue)).iterator().next();
        token.addAssignment(getEncryptRule().getCipherColumn(tableName, assignmentSegment.getColumn().getName()), cipherValue);
    }
    
    private void addAssistedQueryAssignment(final String tableName, final AssignmentSegment assignmentSegment, final EncryptLiteralAssignmentToken token) {
        Object originalValue = ((LiteralExpressionSegment) assignmentSegment.getValue()).getLiterals();
        Optional<String> assistedQueryColumn = getEncryptRule().findAssistedQueryColumn(tableName, assignmentSegment.getColumn().getName());
        if (assistedQueryColumn.isPresent()) {
            Object assistedQueryValue = getEncryptRule().getEncryptAssistedQueryValues(tableName, assignmentSegment.getColumn().getName(), Collections.singletonList(originalValue)).iterator().next();
            token.addAssignment(assistedQueryColumn.get(), assistedQueryValue);
        }
    }
    
    private void addPlainAssignment(final String tableName, final AssignmentSegment assignmentSegment, final EncryptLiteralAssignmentToken token) {
        Object originalValue = ((LiteralExpressionSegment) assignmentSegment.getValue()).getLiterals();
        Optional<String> plainColumn = getEncryptRule().findPlainColumn(tableName, assignmentSegment.getColumn().getName());
        if (plainColumn.isPresent()) {
            token.addAssignment(plainColumn.get(), originalValue);
        }
    }
}
