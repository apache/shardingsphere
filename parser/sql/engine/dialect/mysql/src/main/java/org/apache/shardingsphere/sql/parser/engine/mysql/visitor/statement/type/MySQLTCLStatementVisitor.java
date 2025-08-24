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

package org.apache.shardingsphere.sql.parser.engine.mysql.visitor.statement.type;

import org.antlr.v4.runtime.Token;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.TCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.BeginTransactionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CommitContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.IsolationTypesContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.OptionTypeContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ReleaseSavepointContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RollbackContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SavepointContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SetAutoCommitContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SetTransactionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TransactionAccessModeContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.XaBeginContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.XaCommitContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.XaEndContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.XaPrepareContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.XaRecoveryContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.XaRollbackContext;
import org.apache.shardingsphere.sql.parser.engine.mysql.visitor.statement.MySQLStatementVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OperationScope;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionAccessType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionIsolationLevel;
import org.apache.shardingsphere.sql.parser.statement.core.segment.tcl.AutoCommitSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.BeginTransactionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.ReleaseSavepointStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.RollbackStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SavepointStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SetAutoCommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SetTransactionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.xa.XABeginStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.xa.XACommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.xa.XAEndStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.xa.XAPrepareStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.xa.XARecoveryStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.xa.XARollbackStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

/**
 * TCL statement visitor for MySQL.
 */
public final class MySQLTCLStatementVisitor extends MySQLStatementVisitor implements TCLStatementVisitor {
    
    public MySQLTCLStatementVisitor(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    @Override
    public ASTNode visitSetTransaction(final SetTransactionContext ctx) {
        return new SetTransactionStatement(getDatabaseType(), getOperationScope(ctx.optionType()),
                getTransactionIsolationLevel(null == ctx.transactionCharacteristics().isolationLevel() ? null : ctx.transactionCharacteristics().isolationLevel().isolationTypes()),
                getTransactionAccessType(ctx.transactionCharacteristics().transactionAccessMode()));
    }
    
    private OperationScope getOperationScope(final OptionTypeContext ctx) {
        if (null == ctx) {
            return null;
        }
        if (null != ctx.SESSION()) {
            return OperationScope.SESSION;
        }
        if (null != ctx.GLOBAL()) {
            return OperationScope.GLOBAL;
        }
        return null;
    }
    
    private TransactionIsolationLevel getTransactionIsolationLevel(final IsolationTypesContext ctx) {
        if (null == ctx) {
            return null;
        }
        if (null != ctx.SERIALIZABLE()) {
            return TransactionIsolationLevel.SERIALIZABLE;
        }
        if (null != ctx.COMMITTED()) {
            return TransactionIsolationLevel.READ_COMMITTED;
        }
        if (null != ctx.UNCOMMITTED()) {
            return TransactionIsolationLevel.READ_UNCOMMITTED;
        }
        if (null != ctx.REPEATABLE()) {
            return TransactionIsolationLevel.REPEATABLE_READ;
        }
        return null;
    }
    
    private TransactionAccessType getTransactionAccessType(final TransactionAccessModeContext ctx) {
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
    public ASTNode visitSetAutoCommit(final SetAutoCommitContext ctx) {
        return new SetAutoCommitStatement(getDatabaseType(), generateAutoCommitSegment(ctx.autoCommitValue).isAutoCommit());
    }
    
    private AutoCommitSegment generateAutoCommitSegment(final Token ctx) {
        boolean autoCommit = "1".equals(ctx.getText()) || "ON".equals(ctx.getText());
        return new AutoCommitSegment(ctx.getStartIndex(), ctx.getStopIndex(), autoCommit);
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
        return null == ctx.identifier() ? new RollbackStatement(getDatabaseType()) : new RollbackStatement(getDatabaseType(), ((IdentifierValue) visit(ctx.identifier())).getValue());
    }
    
    @Override
    public ASTNode visitSavepoint(final SavepointContext ctx) {
        return new SavepointStatement(getDatabaseType(), ((IdentifierValue) visit(ctx.identifier())).getValue());
    }
    
    @Override
    public ASTNode visitReleaseSavepoint(final ReleaseSavepointContext ctx) {
        return new ReleaseSavepointStatement(getDatabaseType(), ((IdentifierValue) visit(ctx.identifier())).getValue());
    }
    
    @Override
    public ASTNode visitXaBegin(final XaBeginContext ctx) {
        return new XABeginStatement(getDatabaseType(), ctx.xid().getText());
    }
    
    @Override
    public ASTNode visitXaPrepare(final XaPrepareContext ctx) {
        return new XAPrepareStatement(getDatabaseType(), ctx.xid().getText());
    }
    
    @Override
    public ASTNode visitXaCommit(final XaCommitContext ctx) {
        return new XACommitStatement(getDatabaseType(), ctx.xid().getText());
    }
    
    @Override
    public ASTNode visitXaRollback(final XaRollbackContext ctx) {
        return new XARollbackStatement(getDatabaseType(), ctx.xid().getText());
    }
    
    @Override
    public ASTNode visitXaEnd(final XaEndContext ctx) {
        return new XAEndStatement(getDatabaseType(), ctx.xid().getText());
    }
    
    @Override
    public ASTNode visitXaRecovery(final XaRecoveryContext ctx) {
        return new XARecoveryStatement(getDatabaseType());
    }
}
