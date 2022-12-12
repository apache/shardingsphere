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

package org.apache.shardingsphere.sql.parser.sqlserver.visitor.statement.impl;

import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.operation.SQLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.type.TCLSQLVisitor;
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
import org.apache.shardingsphere.sql.parser.sql.common.enums.TransactionIsolationLevel;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.tcl.SQLServerBeginDistributedTransactionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.tcl.SQLServerBeginTransactionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.tcl.SQLServerCommitStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.tcl.SQLServerRollbackStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.tcl.SQLServerSavepointStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.tcl.SQLServerSetAutoCommitStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.tcl.SQLServerSetTransactionStatement;

import java.util.Properties;

/**
 * TCL Statement SQL visitor for SQLServer.
 */
@NoArgsConstructor
public final class SQLServerTCLStatementSQLVisitor extends SQLServerStatementSQLVisitor implements TCLSQLVisitor, SQLStatementVisitor {
    
    public SQLServerTCLStatementSQLVisitor(final Properties props) {
        super(props);
    }
    
    @Override
    public ASTNode visitSetTransaction(final SetTransactionContext ctx) {
        SQLServerSetTransactionStatement result = new SQLServerSetTransactionStatement();
        result.setIsolationLevel(getTransactionIsolationLevel(ctx.isolationLevel()));
        return result;
    }
    
    private TransactionIsolationLevel getTransactionIsolationLevel(final IsolationLevelContext ctx) {
        TransactionIsolationLevel result;
        if (null != ctx.UNCOMMITTED()) {
            result = TransactionIsolationLevel.READ_UNCOMMITTED;
        } else if (null != ctx.COMMITTED()) {
            result = TransactionIsolationLevel.READ_COMMITTED;
        } else if (null != ctx.REPEATABLE()) {
            result = TransactionIsolationLevel.REPEATABLE_READ;
        } else if (null != ctx.SNAPSHOT()) {
            result = TransactionIsolationLevel.SNAPSHOT;
        } else {
            result = TransactionIsolationLevel.SERIALIZABLE;
        }
        return result;
    }
    
    @Override
    public ASTNode visitSetImplicitTransactions(final SetImplicitTransactionsContext ctx) {
        SQLServerSetAutoCommitStatement result = new SQLServerSetAutoCommitStatement();
        result.setAutoCommit("ON".equalsIgnoreCase(ctx.implicitTransactionsValue().getText()));
        return result;
    }
    
    @Override
    public ASTNode visitBeginTransaction(final BeginTransactionContext ctx) {
        return new SQLServerBeginTransactionStatement();
    }
    
    @Override
    public ASTNode visitBeginDistributedTransaction(final BeginDistributedTransactionContext ctx) {
        return new SQLServerBeginDistributedTransactionStatement();
    }
    
    @Override
    public ASTNode visitCommit(final CommitContext ctx) {
        return new SQLServerCommitStatement();
    }
    
    @Override
    public ASTNode visitCommitWork(final CommitWorkContext ctx) {
        return new SQLServerCommitStatement();
    }
    
    @Override
    public ASTNode visitRollback(final RollbackContext ctx) {
        return new SQLServerRollbackStatement();
    }
    
    @Override
    public ASTNode visitRollbackWork(final RollbackWorkContext ctx) {
        return new SQLServerRollbackStatement();
    }
    
    @Override
    public ASTNode visitSavepoint(final SavepointContext ctx) {
        return new SQLServerSavepointStatement();
    }
}
