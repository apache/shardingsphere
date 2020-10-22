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

package org.apache.shardingsphere.sql.parser.mysql.visitor.statement.impl;

import org.apache.shardingsphere.sql.parser.api.visitor.statement.impl.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.impl.TCLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AutoCommitValueContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.BeginTransactionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CommitContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RollbackContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SavepointContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ScopeContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SetAutoCommitContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SetTransactionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TransactionCharacteristicContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.tcl.AutoCommitSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.tcl.MySQLBeginTransactionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.tcl.MySQLCommitStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.tcl.MySQLRollbackStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.tcl.MySQLSavepointStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.tcl.MySQLSetAutoCommitStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.tcl.MySQLSetTransactionStatement;

/**
 * TCL Statement SQL visitor for MySQL.
 */
public final class MySQLTCLStatementSQLVisitor extends MySQLStatementSQLVisitor implements TCLStatementSQLVisitor {
    
    @Override
    public ASTNode visitSetTransaction(final SetTransactionContext ctx) {
        MySQLSetTransactionStatement result = new MySQLSetTransactionStatement();
        if (null != ctx.scope()) {
            ScopeContext scopeContext = ctx.scope();
            if (null != scopeContext.GLOBAL()) {
                result.setScope(scopeContext.GLOBAL().getText());
            } else if (null != scopeContext.SESSION()) {
                result.setScope(scopeContext.SESSION().getText());
            }
        }
        if (null != ctx.transactionCharacteristic()) {
            for (TransactionCharacteristicContext each : ctx.transactionCharacteristic()) {
                if (null != each.level()) {
                    result.setIsolationLevel(each.level().getText());
                }
                if (null != each.accessMode()) {
                    result.setAccessMode(each.accessMode().getText());
                }
            }
        }
        return new MySQLSetTransactionStatement();
    }
    
    @Override
    public ASTNode visitSetAutoCommit(final SetAutoCommitContext ctx) {
        MySQLSetAutoCommitStatement result = new MySQLSetAutoCommitStatement();
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
        return new MySQLBeginTransactionStatement();
    }
    
    @Override
    public ASTNode visitCommit(final CommitContext ctx) {
        return new MySQLCommitStatement();
    }
    
    @Override
    public ASTNode visitRollback(final RollbackContext ctx) {
        return new MySQLRollbackStatement();
    }
    
    @Override
    public ASTNode visitSavepoint(final SavepointContext ctx) {
        return new MySQLSavepointStatement();
    }
}
