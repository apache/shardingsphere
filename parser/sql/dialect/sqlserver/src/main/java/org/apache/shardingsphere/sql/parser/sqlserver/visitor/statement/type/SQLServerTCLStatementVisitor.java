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

package org.apache.shardingsphere.sql.parser.sqlserver.visitor.statement.type;

import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.TCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.BeginDistributedTransactionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.BeginTransactionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CommitContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CommitWorkContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.IsolationLevelContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.RollbackContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.RollbackWorkContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.SavepointContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.SetImplicitTransactionsContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.SetTransactionContext;
import org.apache.shardingsphere.sql.parser.sqlserver.visitor.statement.SQLServerStatementVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionIsolationLevel;
import org.apache.shardingsphere.sql.parser.statement.core.statement.tcl.BeginTransactionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.tcl.RollbackStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.tcl.SavepointStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.tcl.SetAutoCommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.tcl.SetTransactionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.tcl.xa.XABeginStatement;

/**
 * TCL statement visitor for SQLServer.
 */
public final class SQLServerTCLStatementVisitor extends SQLServerStatementVisitor implements TCLStatementVisitor {
    
    @Override
    public ASTNode visitSetTransaction(final SetTransactionContext ctx) {
        return new SetTransactionStatement(null, getTransactionIsolationLevel(ctx.isolationLevel()), null);
    }
    
    private TransactionIsolationLevel getTransactionIsolationLevel(final IsolationLevelContext ctx) {
        if (null != ctx.UNCOMMITTED()) {
            return TransactionIsolationLevel.READ_UNCOMMITTED;
        }
        if (null != ctx.COMMITTED()) {
            return TransactionIsolationLevel.READ_COMMITTED;
        }
        if (null != ctx.REPEATABLE()) {
            return TransactionIsolationLevel.REPEATABLE_READ;
        }
        if (null != ctx.SNAPSHOT()) {
            return TransactionIsolationLevel.SNAPSHOT;
        }
        return TransactionIsolationLevel.SERIALIZABLE;
    }
    
    @Override
    public ASTNode visitSetImplicitTransactions(final SetImplicitTransactionsContext ctx) {
        return new SetAutoCommitStatement("ON".equalsIgnoreCase(ctx.implicitTransactionsValue().getText()));
    }
    
    @Override
    public ASTNode visitBeginTransaction(final BeginTransactionContext ctx) {
        return new BeginTransactionStatement();
    }
    
    @Override
    public ASTNode visitCommit(final CommitContext ctx) {
        return new CommitStatement();
    }
    
    @Override
    public ASTNode visitCommitWork(final CommitWorkContext ctx) {
        return new CommitStatement();
    }
    
    @Override
    public ASTNode visitRollback(final RollbackContext ctx) {
        return new RollbackStatement();
    }
    
    @Override
    public ASTNode visitRollbackWork(final RollbackWorkContext ctx) {
        return new RollbackStatement();
    }
    
    @Override
    public ASTNode visitSavepoint(final SavepointContext ctx) {
        return new SavepointStatement(null);
    }
    
    @Override
    public ASTNode visitBeginDistributedTransaction(final BeginDistributedTransactionContext ctx) {
        String xid = null;
        if (null != ctx.transactionName()) {
            xid = ctx.transactionName().getText();
        } else if (null != ctx.transactionVariableName()) {
            xid = ctx.transactionVariableName().getText();
        }
        return new XABeginStatement(xid);
    }
}
