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

package org.apache.shardingsphere.sql.parser.engine.oracle.visitor.statement.type;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.TCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CommitContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.LockContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.RollbackContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SavepointContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SetConstraintsContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SetTransactionContext;
import org.apache.shardingsphere.sql.parser.engine.oracle.visitor.statement.OracleStatementVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.lcl.LockStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.RollbackStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SavepointStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SetConstraintsStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SetTransactionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collections;

/**
 * TCL statement visitor for Oracle.
 */
public final class OracleTCLStatementVisitor extends OracleStatementVisitor implements TCLStatementVisitor {
    
    public OracleTCLStatementVisitor(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    @Override
    public ASTNode visitSetTransaction(final SetTransactionContext ctx) {
        return new SetTransactionStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCommit(final CommitContext ctx) {
        return new CommitStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitRollback(final RollbackContext ctx) {
        return null == ctx.savepointClause().savepointName() ? new RollbackStatement(getDatabaseType())
                : new RollbackStatement(getDatabaseType(), ((IdentifierValue) visit(ctx.savepointClause().savepointName())).getValue());
    }
    
    @Override
    public ASTNode visitSavepoint(final SavepointContext ctx) {
        return new SavepointStatement(getDatabaseType(), ((IdentifierValue) visit(ctx.savepointName())).getValue());
    }
    
    @Override
    public ASTNode visitSetConstraints(final SetConstraintsContext ctx) {
        return new SetConstraintsStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitLock(final LockContext ctx) {
        return new LockStatement(getDatabaseType(), Collections.emptyList());
    }
}
