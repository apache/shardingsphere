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

package org.apache.shardingsphere.sql.parser.engine.opengauss.visitor.statement.type;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.TCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AbortContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.BeginTransactionContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CommitContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CommitPreparedContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.EndContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ReleaseSavepointContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.RollbackContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.RollbackPreparedContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.RollbackToSavepointContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.SavepointContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.SetConstraintsContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.SetTransactionContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.StartTransactionContext;
import org.apache.shardingsphere.sql.parser.engine.opengauss.visitor.statement.OpenGaussStatementVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.BeginTransactionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.ReleaseSavepointStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.RollbackStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SavepointStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SetConstraintsStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SetTransactionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.xa.XACommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.xa.XARollbackStatement;

/**
 * TCL statement visitor for openGauss.
 */
public final class OpenGaussTCLStatementVisitor extends OpenGaussStatementVisitor implements TCLStatementVisitor {
    
    public OpenGaussTCLStatementVisitor(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    @Override
    public ASTNode visitSetTransaction(final SetTransactionContext ctx) {
        return new SetTransactionStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitBeginTransaction(final BeginTransactionContext ctx) {
        return new BeginTransactionStatement(getDatabaseType());
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
    public ASTNode visitAbort(final AbortContext ctx) {
        return new RollbackStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitSavepoint(final SavepointContext ctx) {
        return new SavepointStatement(getDatabaseType(), ctx.colId().getText());
    }
    
    @Override
    public ASTNode visitRollbackToSavepoint(final RollbackToSavepointContext ctx) {
        return new RollbackStatement(getDatabaseType(), ctx.colId().getText());
    }
    
    @Override
    public ASTNode visitReleaseSavepoint(final ReleaseSavepointContext ctx) {
        return new ReleaseSavepointStatement(getDatabaseType(), ctx.colId().getText());
    }
    
    @Override
    public ASTNode visitStartTransaction(final StartTransactionContext ctx) {
        return new BeginTransactionStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitEnd(final EndContext ctx) {
        return new CommitStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitSetConstraints(final SetConstraintsContext ctx) {
        return new SetConstraintsStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCommitPrepared(final CommitPreparedContext ctx) {
        return new XACommitStatement(getDatabaseType(), ctx.gid().getText());
    }
    
    @Override
    public ASTNode visitRollbackPrepared(final RollbackPreparedContext ctx) {
        return new XARollbackStatement(getDatabaseType(), ctx.gid().getText());
    }
}
