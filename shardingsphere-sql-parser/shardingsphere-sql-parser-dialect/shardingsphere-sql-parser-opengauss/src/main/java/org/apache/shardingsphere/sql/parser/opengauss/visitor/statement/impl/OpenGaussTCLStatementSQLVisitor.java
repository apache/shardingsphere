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

package org.apache.shardingsphere.sql.parser.opengauss.visitor.statement.impl;

import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.operation.SQLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.type.TCLSQLVisitor;
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
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.tcl.OpenGaussBeginTransactionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.tcl.OpenGaussCommitPreparedStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.tcl.OpenGaussCommitStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.tcl.OpenGaussReleaseSavepointStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.tcl.OpenGaussRollbackPreparedStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.tcl.OpenGaussRollbackStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.tcl.OpenGaussSavepointStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.tcl.OpenGaussSetConstraintsStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.tcl.OpenGaussSetTransactionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.tcl.OpenGaussStartTransactionStatement;

import java.util.Properties;

/**
 * TCL Statement SQL visitor for openGauss.
 */
@NoArgsConstructor
public final class OpenGaussTCLStatementSQLVisitor extends OpenGaussStatementSQLVisitor implements TCLSQLVisitor, SQLStatementVisitor {
    
    public OpenGaussTCLStatementSQLVisitor(final Properties props) {
        super(props);
    }
    
    @Override
    public ASTNode visitSetTransaction(final SetTransactionContext ctx) {
        return new OpenGaussSetTransactionStatement();
    }
    
    @Override
    public ASTNode visitBeginTransaction(final BeginTransactionContext ctx) {
        return new OpenGaussBeginTransactionStatement();
    }
    
    @Override
    public ASTNode visitCommit(final CommitContext ctx) {
        return new OpenGaussCommitStatement();
    }
    
    @Override
    public ASTNode visitRollback(final RollbackContext ctx) {
        return new OpenGaussRollbackStatement();
    }
    
    @Override
    public ASTNode visitAbort(final AbortContext ctx) {
        return new OpenGaussRollbackStatement();
    }
    
    @Override
    public ASTNode visitSavepoint(final SavepointContext ctx) {
        String savepointName = ctx.colId().getText();
        OpenGaussSavepointStatement result = new OpenGaussSavepointStatement();
        result.setSavepointName(savepointName);
        return result;
    }
    
    @Override
    public ASTNode visitRollbackToSavepoint(final RollbackToSavepointContext ctx) {
        OpenGaussRollbackStatement result = new OpenGaussRollbackStatement();
        result.setSavepointName(ctx.colId().getText());
        return result;
    }
    
    @Override
    public ASTNode visitReleaseSavepoint(final ReleaseSavepointContext ctx) {
        String savepointName = ctx.colId().getText();
        OpenGaussReleaseSavepointStatement result = new OpenGaussReleaseSavepointStatement();
        result.setSavepointName(savepointName);
        return result;
    }
    
    @Override
    public ASTNode visitStartTransaction(final StartTransactionContext ctx) {
        return new OpenGaussStartTransactionStatement();
    }
    
    @Override
    public ASTNode visitEnd(final EndContext ctx) {
        return new OpenGaussCommitStatement();
    }
    
    @Override
    public ASTNode visitSetConstraints(final SetConstraintsContext ctx) {
        return new OpenGaussSetConstraintsStatement();
    }
    
    @Override
    public ASTNode visitCommitPrepared(final CommitPreparedContext ctx) {
        return new OpenGaussCommitPreparedStatement();
    }
    
    @Override
    public ASTNode visitRollbackPrepared(final RollbackPreparedContext ctx) {
        return new OpenGaussRollbackPreparedStatement();
    }
}
