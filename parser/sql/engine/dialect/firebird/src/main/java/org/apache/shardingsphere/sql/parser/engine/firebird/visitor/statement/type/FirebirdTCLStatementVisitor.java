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

package org.apache.shardingsphere.sql.parser.engine.firebird.visitor.statement.type;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.TCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.CommitContext;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.RollbackContext;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.SavepointContext;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.SetTransactionContext;
import org.apache.shardingsphere.sql.parser.engine.firebird.visitor.statement.FirebirdStatementVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.RollbackStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SavepointStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SetTransactionStatement;

/**
 * TCL statement visitor for Firebird.
 */
public final class FirebirdTCLStatementVisitor extends FirebirdStatementVisitor implements TCLStatementVisitor {
    
    public FirebirdTCLStatementVisitor(final DatabaseType databaseType) {
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
        return new RollbackStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitSavepoint(final SavepointContext ctx) {
        return new SavepointStatement(getDatabaseType(), null);
    }
}
