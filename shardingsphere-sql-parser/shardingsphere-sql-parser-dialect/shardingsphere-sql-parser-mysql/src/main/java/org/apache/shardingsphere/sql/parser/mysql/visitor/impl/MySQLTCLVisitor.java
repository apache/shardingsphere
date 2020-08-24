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

package org.apache.shardingsphere.sql.parser.mysql.visitor.impl;

import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.TCLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TransactionCharacteristicContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.Scope_Context;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AutoCommitValueContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.BeginTransactionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CommitContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RollbackContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SavepointContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SetAutoCommitContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SetTransactionContext;
import org.apache.shardingsphere.sql.parser.mysql.visitor.MySQLVisitor;
import org.apache.shardingsphere.sql.parser.sql.segment.tcl.AutoCommitSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.tcl.BeginTransactionStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.tcl.RollbackStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.tcl.SavepointStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.tcl.SetAutoCommitStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.tcl.SetTransactionStatement;

/**
 * TCL visitor for MySQL.
 */
public final class MySQLTCLVisitor extends MySQLVisitor implements TCLVisitor {
    
    @Override
    public ASTNode visitSetTransaction(final SetTransactionContext ctx) {
        SetTransactionStatement result = new SetTransactionStatement();
        if (null != ctx.scope_()) {
            Scope_Context scopeContext = ctx.scope_();
            if (null != scopeContext.GLOBAL()) {
                result.setScope(scopeContext.GLOBAL().getText());
            } else if (null != scopeContext.SESSION()) {
                result.setScope(scopeContext.SESSION().getText());
            }
        }
        if (null != ctx.transactionCharacteristic()) {
            for (TransactionCharacteristicContext each : ctx.transactionCharacteristic()) {
                if (null != each.level_()) {
                    result.setIsolationLevel(each.level_().getText());
                }
                if (null != each.accessMode_()) {
                    result.setAccessMode(each.accessMode_().getText());
                }
            }
        }
        return new SetTransactionStatement();
    }
    
    @Override
    public ASTNode visitSetAutoCommit(final SetAutoCommitContext ctx) {
        SetAutoCommitStatement result = new SetAutoCommitStatement();
        result.setAutoCommit(((AutoCommitSegment) visit(ctx.autoCommitValue())).isAutoCommit());
        return result;
    }
    
    @Override
    public ASTNode visitAutoCommitValue(final AutoCommitValueContext ctx) {
        boolean autoCommit = "1".equals(ctx.getText()) || "ON".equals(ctx.getText());
        return new AutoCommitSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), autoCommit);
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
