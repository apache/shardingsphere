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

package org.apache.shardingsphere.sql.parser.engine.postgresql.visitor.statement.type;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.TCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AbortContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.BeginTransactionContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CommitContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CommitPreparedContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.EndContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.IsoLevelContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.PrepareTransactionContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ReleaseSavepointContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.RollbackContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.RollbackPreparedContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.RollbackToSavepointContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.SavepointContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.SetConstraintsContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.SetTransactionContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.StartTransactionContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.TransactionModeItemContext;
import org.apache.shardingsphere.sql.parser.engine.postgresql.visitor.statement.PostgreSQLStatementVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionAccessType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionIsolationLevel;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.BeginTransactionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.ReleaseSavepointStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.RollbackStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SavepointStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SetConstraintsStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SetTransactionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.xa.XACommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.xa.XAPrepareStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.xa.XARollbackStatement;

/**
 * TCL statement visitor for PostgreSQL.
 */
public final class PostgreSQLTCLStatementVisitor extends PostgreSQLStatementVisitor implements TCLStatementVisitor {
    
    public PostgreSQLTCLStatementVisitor(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    @Override
    public ASTNode visitSetTransaction(final SetTransactionContext ctx) {
        SetTransactionStatement result = new SetTransactionStatement(getDatabaseType());
        if (null != ctx.transactionModeList()) {
            for (TransactionModeItemContext each : ctx.transactionModeList().transactionModeItem()) {
                result = new SetTransactionStatement(getDatabaseType(), null, getTransactionIsolationLevel(each.isoLevel()), getTransactionAccessType(each));
            }
        }
        return result;
    }
    
    private TransactionIsolationLevel getTransactionIsolationLevel(final IsoLevelContext ctx) {
        if (null == ctx) {
            return null;
        }
        if (null != ctx.UNCOMMITTED()) {
            return TransactionIsolationLevel.READ_UNCOMMITTED;
        }
        if (null != ctx.COMMITTED()) {
            return TransactionIsolationLevel.READ_COMMITTED;
        }
        if (null != ctx.REPEATABLE()) {
            return TransactionIsolationLevel.REPEATABLE_READ;
        }
        if (null != ctx.SERIALIZABLE()) {
            return TransactionIsolationLevel.SERIALIZABLE;
        }
        return null;
    }
    
    private TransactionAccessType getTransactionAccessType(final TransactionModeItemContext ctx) {
        if (null == ctx) {
            return null;
        }
        if (null != ctx.ONLY()) {
            return TransactionAccessType.READ_ONLY;
        }
        if (null != ctx.WRITE()) {
            return TransactionAccessType.READ_WRITE;
        }
        return null;
    }
    
    @Override
    public ASTNode visitBeginTransaction(final BeginTransactionContext ctx) {
        return new BeginTransactionStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCommit(final CommitContext ctx) {
        return new CommitStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitRollback(final RollbackContext ctx) {
        return new RollbackStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAbort(final AbortContext ctx) {
        return new RollbackStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitSavepoint(final SavepointContext ctx) {
        return new SavepointStatement(getDatabaseType(), ctx.colId().getText());
    }
    
    @Override
    public ASTNode visitRollbackToSavepoint(final RollbackToSavepointContext ctx) {
        return new RollbackStatement(getDatabaseType(), ctx.colId().getText());
    }
    
    @Override
    public ASTNode visitReleaseSavepoint(final ReleaseSavepointContext ctx) {
        return new ReleaseSavepointStatement(getDatabaseType(), ctx.colId().getText());
    }
    
    @Override
    public ASTNode visitStartTransaction(final StartTransactionContext ctx) {
        return new BeginTransactionStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitEnd(final EndContext ctx) {
        return new CommitStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitSetConstraints(final SetConstraintsContext ctx) {
        return new SetConstraintsStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitPrepareTransaction(final PrepareTransactionContext ctx) {
        return new XAPrepareStatement(getDatabaseType(), ctx.gid().getText());
    }
    
    @Override
    public ASTNode visitCommitPrepared(final CommitPreparedContext ctx) {
        return new XACommitStatement(getDatabaseType(), ctx.gid().getText());
    }
    
    @Override
    public ASTNode visitRollbackPrepared(final RollbackPreparedContext ctx) {
        return new XARollbackStatement(getDatabaseType(), ctx.gid().getText());
    }
}
