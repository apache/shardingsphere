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

package org.apache.shardingsphere.sql.parser.postgresql.visitor.impl;

import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.TCLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.BeginTransactionContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CommitContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.RollbackContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.SavepointContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.SetTransactionContext;
import org.apache.shardingsphere.sql.parser.postgresql.visitor.PostgreSQLVisitor;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.tcl.PostgreSQLBeginTransactionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.tcl.PostgreSQLCommitStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.tcl.PostgreSQLRollbackStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.tcl.PostgreSQLSavepointStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.tcl.PostgreSQLSetTransactionStatement;

/**
 * TCL visitor for PostgreSQL.
 */
public final class PostgreSQLTCLVisitor extends PostgreSQLVisitor implements TCLVisitor {
    
    @Override
    public ASTNode visitSetTransaction(final SetTransactionContext ctx) {
        return new PostgreSQLSetTransactionStatement();
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
    public ASTNode visitSavepoint(final SavepointContext ctx) {
        return new PostgreSQLSavepointStatement();
    }
}
