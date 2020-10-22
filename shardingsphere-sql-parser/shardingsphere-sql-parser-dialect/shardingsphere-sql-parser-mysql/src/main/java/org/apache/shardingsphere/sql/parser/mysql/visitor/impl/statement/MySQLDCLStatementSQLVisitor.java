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

package org.apache.shardingsphere.sql.parser.mysql.visitor.impl.statement;

import org.apache.shardingsphere.sql.parser.api.visitor.impl.statement.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.impl.statement.impl.DCLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterUserContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateUserContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropUserContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.GrantContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.PrivilegeClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RenameUserContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RevokeContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SetDefaultRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SetPasswordContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SetRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableNameContext;
import org.apache.shardingsphere.sql.parser.mysql.visitor.impl.statement.MySQLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dcl.MySQLAlterUserStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dcl.MySQLCreateRoleStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dcl.MySQLCreateUserStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dcl.MySQLDropRoleStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dcl.MySQLDropUserStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dcl.MySQLGrantStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dcl.MySQLRenameUserStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dcl.MySQLRevokeStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dcl.MySQLSetDefaultRoleStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dcl.MySQLSetPasswordStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dcl.MySQLSetRoleStatement;

import java.util.Optional;

/**
 * DCL Statement SQL visitor for MySQL.
 */
public final class MySQLDCLStatementSQLVisitor extends MySQLStatementSQLVisitor implements DCLStatementSQLVisitor {
    
    @Override
    public ASTNode visitGrant(final GrantContext ctx) {
        MySQLGrantStatement result = new MySQLGrantStatement();
        Optional<SimpleTableSegment> tableSegmentOptional = null == ctx.privilegeClause() ? Optional.empty() : getTableFromPrivilegeClause(ctx.privilegeClause());
        tableSegmentOptional.ifPresent(tableSegment -> result.getTables().add(tableSegment));
        return result;
    }
    
    @Override
    public ASTNode visitRevoke(final RevokeContext ctx) {
        MySQLRevokeStatement result = new MySQLRevokeStatement();
        Optional<SimpleTableSegment> tableSegmentOptional = null == ctx.privilegeClause() ? Optional.empty() : getTableFromPrivilegeClause(ctx.privilegeClause());
        tableSegmentOptional.ifPresent(tableSegment -> result.getTables().add(tableSegment));
        return result;
    }
    
    private Optional<SimpleTableSegment> getTableFromPrivilegeClause(final PrivilegeClauseContext ctx) {
        if (null != ctx.onObjectClause()) {
            TableNameContext tableName = ctx.onObjectClause().privilegeLevel().tableName();
            if (null != tableName) {
                return Optional.of((SimpleTableSegment) visitTableName(tableName));
            }
        }
        return Optional.empty();
    }
    
    @Override
    public ASTNode visitCreateUser(final CreateUserContext ctx) {
        return new MySQLCreateUserStatement();
    }
    
    @Override
    public ASTNode visitDropUser(final DropUserContext ctx) {
        return new MySQLDropUserStatement();
    }
    
    @Override
    public ASTNode visitAlterUser(final AlterUserContext ctx) {
        return new MySQLAlterUserStatement();
    }
    
    @Override
    public ASTNode visitRenameUser(final RenameUserContext ctx) {
        return new MySQLRenameUserStatement();
    }
    
    @Override
    public ASTNode visitCreateRole(final CreateRoleContext ctx) {
        return new MySQLCreateRoleStatement();
    }
    
    @Override
    public ASTNode visitDropRole(final DropRoleContext ctx) {
        return new MySQLDropRoleStatement();
    }
    
    @Override
    public ASTNode visitSetDefaultRole(final SetDefaultRoleContext ctx) {
        return new MySQLSetDefaultRoleStatement();
    }
    
    @Override
    public ASTNode visitSetRole(final SetRoleContext ctx) {
        return new MySQLSetRoleStatement();
    }
    
    @Override
    public ASTNode visitSetPassword(final SetPasswordContext ctx) {
        return new MySQLSetPasswordStatement();
    }
}
