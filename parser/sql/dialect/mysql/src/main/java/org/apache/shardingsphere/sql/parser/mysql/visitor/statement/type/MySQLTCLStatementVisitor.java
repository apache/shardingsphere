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

package org.apache.shardingsphere.sql.parser.mysql.visitor.statement.type;

import org.antlr.v4.runtime.Token;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.TCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.BeginTransactionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CommitContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.LockContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ReleaseSavepointContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RollbackContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SavepointContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SetAutoCommitContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SetTransactionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableLockContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.UnlockContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.XaBeginContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.XaCommitContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.XaEndContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.XaPrepareContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.XaRecoveryContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.XaRollbackContext;
import org.apache.shardingsphere.sql.parser.mysql.visitor.statement.MySQLStatementVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.enums.OperationScope;
import org.apache.shardingsphere.sql.parser.sql.common.enums.TransactionAccessType;
import org.apache.shardingsphere.sql.parser.sql.common.enums.TransactionIsolationLevel;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.tcl.AutoCommitSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.xa.XABeginStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.xa.XACommitStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.xa.XAEndStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.xa.XAPrepareStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.xa.XARecoveryStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.xa.XARollbackStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.tcl.MySQLBeginTransactionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.tcl.MySQLCommitStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.tcl.MySQLLockStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.tcl.MySQLReleaseSavepointStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.tcl.MySQLRollbackStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.tcl.MySQLSavepointStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.tcl.MySQLSetAutoCommitStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.tcl.MySQLSetTransactionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.tcl.MySQLUnlockStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * TCL statement visitor for MySQL.
 */
public final class MySQLTCLStatementVisitor extends MySQLStatementVisitor implements TCLStatementVisitor {
    
    @Override
    public ASTNode visitSetTransaction(final SetTransactionContext ctx) {
        MySQLSetTransactionStatement result = new MySQLSetTransactionStatement();
        if (null != ctx.optionType()) {
            OperationScope scope = null;
            if (null != ctx.optionType().SESSION()) {
                scope = OperationScope.SESSION;
            } else if (null != ctx.optionType().GLOBAL()) {
                scope = OperationScope.GLOBAL;
            }
            result.setScope(scope);
        }
        if (null != ctx.transactionCharacteristics().isolationLevel()) {
            TransactionIsolationLevel isolationLevel = null;
            if (null != ctx.transactionCharacteristics().isolationLevel().isolationTypes().SERIALIZABLE()) {
                isolationLevel = TransactionIsolationLevel.SERIALIZABLE;
            } else if (null != ctx.transactionCharacteristics().isolationLevel().isolationTypes().COMMITTED()) {
                isolationLevel = TransactionIsolationLevel.READ_COMMITTED;
            } else if (null != ctx.transactionCharacteristics().isolationLevel().isolationTypes().UNCOMMITTED()) {
                isolationLevel = TransactionIsolationLevel.READ_UNCOMMITTED;
            } else if (null != ctx.transactionCharacteristics().isolationLevel().isolationTypes().REPEATABLE()) {
                isolationLevel = TransactionIsolationLevel.REPEATABLE_READ;
            }
            result.setIsolationLevel(isolationLevel);
        }
        if (null != ctx.transactionCharacteristics().transactionAccessMode()) {
            TransactionAccessType accessType = null;
            if (null != ctx.transactionCharacteristics().transactionAccessMode().ONLY()) {
                accessType = TransactionAccessType.READ_ONLY;
            } else if (null != ctx.transactionCharacteristics().transactionAccessMode().WRITE()) {
                accessType = TransactionAccessType.READ_WRITE;
            }
            result.setAccessMode(accessType);
        }
        return result;
    }
    
    @Override
    public ASTNode visitSetAutoCommit(final SetAutoCommitContext ctx) {
        MySQLSetAutoCommitStatement result = new MySQLSetAutoCommitStatement();
        result.setAutoCommit(generateAutoCommitSegment(ctx.autoCommitValue).isAutoCommit());
        return result;
    }
    
    private AutoCommitSegment generateAutoCommitSegment(final Token ctx) {
        boolean autoCommit = "1".equals(ctx.getText()) || "ON".equals(ctx.getText());
        return new AutoCommitSegment(ctx.getStartIndex(), ctx.getStopIndex(), autoCommit);
    }
    
    @Override
    public ASTNode visitBeginTransaction(final BeginTransactionContext ctx) {
        return new MySQLBeginTransactionStatement();
    }
    
    @Override
    public ASTNode visitCommit(final CommitContext ctx) {
        return new MySQLCommitStatement();
    }
    
    @Override
    public ASTNode visitRollback(final RollbackContext ctx) {
        MySQLRollbackStatement result = new MySQLRollbackStatement();
        if (null != ctx.identifier()) {
            result.setSavepointName(((IdentifierValue) visit(ctx.identifier())).getValue());
        }
        return result;
    }
    
    @Override
    public ASTNode visitSavepoint(final SavepointContext ctx) {
        MySQLSavepointStatement result = new MySQLSavepointStatement();
        result.setSavepointName(((IdentifierValue) visit(ctx.identifier())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitReleaseSavepoint(final ReleaseSavepointContext ctx) {
        MySQLReleaseSavepointStatement result = new MySQLReleaseSavepointStatement();
        result.setSavepointName(((IdentifierValue) visit(ctx.identifier())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitXaBegin(final XaBeginContext ctx) {
        return new XABeginStatement(ctx.xid().getText());
    }
    
    @Override
    public ASTNode visitXaPrepare(final XaPrepareContext ctx) {
        return new XAPrepareStatement(ctx.xid().getText());
    }
    
    @Override
    public ASTNode visitXaCommit(final XaCommitContext ctx) {
        return new XACommitStatement(ctx.xid().getText());
    }
    
    @Override
    public ASTNode visitXaRollback(final XaRollbackContext ctx) {
        return new XARollbackStatement(ctx.xid().getText());
    }
    
    @Override
    public ASTNode visitXaEnd(final XaEndContext ctx) {
        return new XAEndStatement(ctx.xid().getText());
    }
    
    @Override
    public ASTNode visitXaRecovery(final XaRecoveryContext ctx) {
        return new XARecoveryStatement();
    }
    
    @Override
    public ASTNode visitLock(final LockContext ctx) {
        MySQLLockStatement result = new MySQLLockStatement();
        if (null != ctx.tableLock()) {
            result.getTables().addAll(getLockTables(ctx.tableLock()));
        }
        return result;
    }
    
    private Collection<SimpleTableSegment> getLockTables(final List<TableLockContext> tableLockContexts) {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        for (TableLockContext each : tableLockContexts) {
            SimpleTableSegment simpleTableSegment = (SimpleTableSegment) visit(each.tableName());
            if (null != each.alias()) {
                simpleTableSegment.setAlias((AliasSegment) visit(each.alias()));
            }
            result.add(simpleTableSegment);
        }
        return result;
    }
    
    @Override
    public ASTNode visitUnlock(final UnlockContext ctx) {
        return new MySQLUnlockStatement();
    }
}
