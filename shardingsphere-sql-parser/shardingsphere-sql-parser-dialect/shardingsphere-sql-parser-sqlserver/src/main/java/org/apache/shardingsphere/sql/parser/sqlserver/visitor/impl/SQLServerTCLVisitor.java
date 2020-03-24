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

package org.apache.shardingsphere.sql.parser.sqlserver.visitor.impl;

import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.TCLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.BeginTransactionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CommitContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.RollbackContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.SavepointContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.SetImplicitTransactionsContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.SetTransactionContext;
import org.apache.shardingsphere.sql.parser.sql.statement.tcl.BeginTransactionStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.tcl.RollbackStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.tcl.SavepointStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.tcl.SetAutoCommitStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.tcl.SetTransactionStatement;
import org.apache.shardingsphere.sql.parser.sqlserver.visitor.SQLServerVisitor;

/**
 * TCL visitor for SQLServer.
 */
public final class SQLServerTCLVisitor extends SQLServerVisitor implements TCLVisitor {
    
    @Override
    public ASTNode visitSetTransaction(final SetTransactionContext ctx) {
        return new SetTransactionStatement();
    }
    
    @Override
    public ASTNode visitSetImplicitTransactions(final SetImplicitTransactionsContext ctx) {
        SetAutoCommitStatement result = new SetAutoCommitStatement();
        result.setAutoCommit("ON".equalsIgnoreCase(ctx.implicitTransactionsValue().getText()));
        return result;
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
    public ASTNode visitRollback(final RollbackContext ctx) {
        return new RollbackStatement();
    }
    
    @Override
    public ASTNode visitSavepoint(final SavepointContext ctx) {
        return new SavepointStatement();
    }
}
