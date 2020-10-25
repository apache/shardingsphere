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

package org.apache.shardingsphere.sql.parser.oracle.visitor.statement.impl;

import org.apache.shardingsphere.sql.parser.api.visitor.statement.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.impl.TCLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CommitContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.RollbackContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SavepointContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SetTransactionContext;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.tcl.OracleCommitStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.tcl.OracleRollbackStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.tcl.OracleSavepointStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.tcl.OracleSetTransactionStatement;

/**
 * TCL Statement SQL visitor for Oracle.
 */
public final class OracleTCLStatementSQLVisitor extends OracleStatementSQLVisitor implements TCLStatementSQLVisitor {
    
    @Override
    public ASTNode visitSetTransaction(final SetTransactionContext ctx) {
        return new OracleSetTransactionStatement();
    }
    
    @Override
    public ASTNode visitCommit(final CommitContext ctx) {
        return new OracleCommitStatement();
    }
    
    @Override
    public ASTNode visitRollback(final RollbackContext ctx) {
        return new OracleRollbackStatement();
    }
    
    @Override
    public ASTNode visitSavepoint(final SavepointContext ctx) {
        return new OracleSavepointStatement();
    }
}
