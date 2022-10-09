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

package org.apache.shardingsphere.sql.parser.mysql.visitor.statement.impl;

import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.Token;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.operation.SQLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.type.TCLSQLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.BeginTransactionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CommitContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.LockContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RollbackContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SavepointContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SetAutoCommitContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SetTransactionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableLockContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.UnlockContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.XaContext;
import org.apache.shardingsphere.sql.parser.sql.common.constant.OperationScope;
import org.apache.shardingsphere.sql.parser.sql.common.constant.TransactionAccessType;
import org.apache.shardingsphere.sql.parser.sql.common.constant.TransactionIsolationLevel;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.tcl.AutoCommitSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.tcl.MySQLBeginTransactionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.tcl.MySQLCommitStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.tcl.MySQLLockStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.tcl.MySQLRollbackStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.tcl.MySQLSavepointStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.tcl.MySQLSetAutoCommitStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.tcl.MySQLSetTransactionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.tcl.MySQLUnlockStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.tcl.MySQLXAStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * TCL Statement SQL visitor for MySQL.
 */
@NoArgsConstructor
public final class MySQLTCLStatementSQLVisitor extends MySQLStatementSQLVisitor implements TCLSQLVisitor, SQLStatementVisitor {
    
    public MySQLTCLStatementSQLVisitor(final Properties props) {
        super(props);
    }
    
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
    public ASTNode visitXa(final XaContext ctx) {
        MySQLXAStatement result = new MySQLXAStatement();
        result.setOp(ctx.getChild(1).getText().toUpperCase());
        if (null != ctx.xid()) {
            result.setXid(ctx.xid().getText());
        }
        return result;
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
