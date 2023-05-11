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

package org.apache.shardingsphere.sql.parser.postgresql.visitor.statement.type;

import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.TCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AbortContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.BeginTransactionContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CommitContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CommitPreparedContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.EndContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.LockContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.PrepareTransactionContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.RelationExprContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ReleaseSavepointContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.RollbackContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.RollbackPreparedContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.RollbackToSavepointContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.SavepointContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.SetConstraintsContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.SetTransactionContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.StartTransactionContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.TransactionModeItemContext;
import org.apache.shardingsphere.sql.parser.postgresql.visitor.statement.PostgreSQLStatementVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.enums.TransactionAccessType;
import org.apache.shardingsphere.sql.parser.sql.common.enums.TransactionIsolationLevel;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.tcl.PostgreSQLBeginTransactionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.tcl.PostgreSQLCommitPreparedStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.tcl.PostgreSQLCommitStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.tcl.PostgreSQLLockStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.tcl.PostgreSQLPrepareTransactionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.tcl.PostgreSQLReleaseSavepointStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.tcl.PostgreSQLRollbackPreparedStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.tcl.PostgreSQLRollbackStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.tcl.PostgreSQLSavepointStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.tcl.PostgreSQLSetConstraintsStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.tcl.PostgreSQLSetTransactionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.tcl.PostgreSQLStartTransactionStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * TCL statement visitor for PostgreSQL.
 */
public final class PostgreSQLTCLStatementVisitor extends PostgreSQLStatementVisitor implements TCLStatementVisitor {
    
    @Override
    public ASTNode visitSetTransaction(final SetTransactionContext ctx) {
        PostgreSQLSetTransactionStatement result = new PostgreSQLSetTransactionStatement();
        if (null != ctx.transactionModeList()) {
            ctx.transactionModeList().transactionModeItem().forEach(each -> {
                result.setAccessMode(getTransactionAccessType(each));
                result.setIsolationLevel(getTransactionIsolationLevel(each));
            });
        }
        return result;
    }
    
    private TransactionAccessType getTransactionAccessType(final TransactionModeItemContext modeItemContext) {
        if (null != modeItemContext.ONLY()) {
            return TransactionAccessType.READ_ONLY;
        }
        if (null != modeItemContext.WRITE()) {
            return TransactionAccessType.READ_WRITE;
        }
        return null;
    }
    
    private TransactionIsolationLevel getTransactionIsolationLevel(final TransactionModeItemContext modeItemContext) {
        if (null == modeItemContext.isoLevel()) {
            return null;
        }
        if (null != modeItemContext.isoLevel().UNCOMMITTED()) {
            return TransactionIsolationLevel.READ_UNCOMMITTED;
        }
        if (null != modeItemContext.isoLevel().COMMITTED()) {
            return TransactionIsolationLevel.READ_COMMITTED;
        }
        if (null != modeItemContext.isoLevel().REPEATABLE()) {
            return TransactionIsolationLevel.REPEATABLE_READ;
        }
        if (null != modeItemContext.isoLevel().SERIALIZABLE()) {
            return TransactionIsolationLevel.SERIALIZABLE;
        }
        return null;
    }
    
    @Override
    public ASTNode visitBeginTransaction(final BeginTransactionContext ctx) {
        return new PostgreSQLBeginTransactionStatement();
    }
    
    @Override
    public ASTNode visitCommit(final CommitContext ctx) {
        return new PostgreSQLCommitStatement();
    }
    
    @Override
    public ASTNode visitRollback(final RollbackContext ctx) {
        return new PostgreSQLRollbackStatement();
    }
    
    @Override
    public ASTNode visitAbort(final AbortContext ctx) {
        return new PostgreSQLRollbackStatement();
    }
    
    @Override
    public ASTNode visitSavepoint(final SavepointContext ctx) {
        String savepointName = ctx.colId().getText();
        PostgreSQLSavepointStatement result = new PostgreSQLSavepointStatement();
        result.setSavepointName(savepointName);
        return result;
    }
    
    @Override
    public ASTNode visitRollbackToSavepoint(final RollbackToSavepointContext ctx) {
        PostgreSQLRollbackStatement result = new PostgreSQLRollbackStatement();
        result.setSavepointName(ctx.colId().getText());
        return result;
    }
    
    @Override
    public ASTNode visitReleaseSavepoint(final ReleaseSavepointContext ctx) {
        String savepointName = ctx.colId().getText();
        PostgreSQLReleaseSavepointStatement result = new PostgreSQLReleaseSavepointStatement();
        result.setSavepointName(savepointName);
        return result;
    }
    
    @Override
    public ASTNode visitStartTransaction(final StartTransactionContext ctx) {
        return new PostgreSQLStartTransactionStatement();
    }
    
    @Override
    public ASTNode visitEnd(final EndContext ctx) {
        return new PostgreSQLCommitStatement();
    }
    
    @Override
    public ASTNode visitSetConstraints(final SetConstraintsContext ctx) {
        return new PostgreSQLSetConstraintsStatement();
    }
    
    @Override
    public ASTNode visitCommitPrepared(final CommitPreparedContext ctx) {
        return new PostgreSQLCommitPreparedStatement();
    }
    
    @Override
    public ASTNode visitRollbackPrepared(final RollbackPreparedContext ctx) {
        return new PostgreSQLRollbackPreparedStatement();
    }
    
    @Override
    public ASTNode visitLock(final LockContext ctx) {
        PostgreSQLLockStatement result = new PostgreSQLLockStatement();
        if (null != ctx.relationExprList()) {
            result.getTables().addAll(getLockTables(ctx.relationExprList().relationExpr()));
        }
        return result;
    }
    
    private Collection<SimpleTableSegment> getLockTables(final List<RelationExprContext> relationExprContexts) {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        for (RelationExprContext each : relationExprContexts) {
            SimpleTableSegment tableSegment = (SimpleTableSegment) visit(each.qualifiedName());
            result.add(tableSegment);
        }
        return result;
    }
    
    @Override
    public ASTNode visitPrepareTransaction(final PrepareTransactionContext ctx) {
        return new PostgreSQLPrepareTransactionStatement();
    }
}
