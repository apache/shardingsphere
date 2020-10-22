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

package org.apache.shardingsphere.sql.parser.sqlserver.visitor.impl.statement;

import org.apache.shardingsphere.sql.parser.api.visitor.impl.statement.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.impl.statement.impl.TCLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.BeginTransactionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CommitContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.RollbackContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.SavepointContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.SetImplicitTransactionsContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.SetTransactionContext;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.tcl.SQLServerBeginTransactionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.tcl.SQLServerCommitStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.tcl.SQLServerRollbackStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.tcl.SQLServerSavepointStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.tcl.SQLServerSetAutoCommitStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.tcl.SQLServerSetTransactionStatement;

/**
 * TCL Statement SQL visitor for SQLServer.
 */
public final class SQLServerTCLStatementSQLVisitor extends SQLServerStatementSQLVisitor implements TCLStatementSQLVisitor {
    
    @Override
    public ASTNode visitSetTransaction(final SetTransactionContext ctx) {
        return new SQLServerSetTransactionStatement();
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
    public ASTNode visitCommit(final CommitContext ctx) {
        return new SQLServerCommitStatement();
    }
    
    @Override
    public ASTNode visitRollback(final RollbackContext ctx) {
        return new SQLServerRollbackStatement();
    }
    
    @Override
    public ASTNode visitSavepoint(final SavepointContext ctx) {
        return new SQLServerSavepointStatement();
    }
}
