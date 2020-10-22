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

package org.apache.shardingsphere.sql.parser.sqlserver.visitor.impl.statement;

import org.apache.shardingsphere.sql.parser.api.visitor.impl.statement.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.impl.statement.impl.DCLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AlterLoginContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AlterRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AlterUserContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ClassPrivilegesClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ClassTypePrivilegesClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CreateLoginContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CreateRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CreateUserContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DenyContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DropLoginContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DropRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DropUserContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.GrantContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.RevokeContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dcl.SQLServerAlterLoginStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dcl.SQLServerAlterRoleStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dcl.SQLServerAlterUserStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dcl.SQLServerCreateLoginStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dcl.SQLServerCreateRoleStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dcl.SQLServerCreateUserStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dcl.SQLServerDenyUserStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dcl.SQLServerDropLoginStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dcl.SQLServerDropRoleStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dcl.SQLServerDropUserStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dcl.SQLServerGrantStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dcl.SQLServerRevokeStatement;
import org.apache.shardingsphere.sql.parser.sqlserver.visitor.impl.statement.SQLServerStatementSQLVisitor;

import java.util.Collection;
import java.util.Collections;

/**
 * DCL Statement SQL visitor for SQLServer.
 */
public final class SQLServerDCLStatementSQLVisitor extends SQLServerStatementSQLVisitor implements DCLStatementSQLVisitor {
    
    @Override
    public ASTNode visitGrant(final GrantContext ctx) {
        SQLServerGrantStatement result = new SQLServerGrantStatement();
        if (null != ctx.classPrivilegesClause()) {
            for (SimpleTableSegment each : getTableFromPrivilegeClause(ctx.classPrivilegesClause())) {
                result.getTables().add(each);
            }
        }
        if (null != ctx.classTypePrivilegesClause()) {
            for (SimpleTableSegment each : getTableFromPrivilegeClause(ctx.classTypePrivilegesClause())) {
                result.getTables().add(each);
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitRevoke(final RevokeContext ctx) {
        SQLServerRevokeStatement result = new SQLServerRevokeStatement();
        if (null != ctx.classPrivilegesClause()) {
            for (SimpleTableSegment each : getTableFromPrivilegeClause(ctx.classPrivilegesClause())) {
                result.getTables().add(each);
            }
        }
        if (null != ctx.classTypePrivilegesClause()) {
            for (SimpleTableSegment each : getTableFromPrivilegeClause(ctx.classTypePrivilegesClause())) {
                result.getTables().add(each);
            }
        }
        return result;
    }
    
    private Collection<SimpleTableSegment> getTableFromPrivilegeClause(final ClassPrivilegesClauseContext ctx) {
        return null == ctx.onClassClause().tableName() ? Collections.emptyList() : Collections.singletonList((SimpleTableSegment) visit(ctx.onClassClause().tableName()));
    }
    
    private Collection<SimpleTableSegment> getTableFromPrivilegeClause(final ClassTypePrivilegesClauseContext ctx) {
        return null == ctx.onClassTypeClause().tableName() ? Collections.emptyList() : Collections.singletonList((SimpleTableSegment) visit(ctx.onClassTypeClause().tableName()));
    }
    
    @Override
    public ASTNode visitCreateUser(final CreateUserContext ctx) {
        return new SQLServerCreateUserStatement();
    }
    
    @Override
    public ASTNode visitAlterUser(final AlterUserContext ctx) {
        return new SQLServerAlterUserStatement();
    }
    
    @Override
    public ASTNode visitDeny(final DenyContext ctx) {
        SQLServerDenyUserStatement result = new SQLServerDenyUserStatement();
        if (null != ctx.classPrivilegesClause()) {
            for (SimpleTableSegment each : getTableFromPrivilegeClause(ctx.classPrivilegesClause())) {
                result.setTable(each);
            }
        }
        if (null != ctx.classTypePrivilegesClause()) {
            for (SimpleTableSegment each : getTableFromPrivilegeClause(ctx.classTypePrivilegesClause())) {
                result.setTable(each);
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitDropUser(final DropUserContext ctx) {
        return new SQLServerDropUserStatement();
    }
    
    @Override
    public ASTNode visitCreateRole(final CreateRoleContext ctx) {
        return new SQLServerCreateRoleStatement();
    }
    
    @Override
    public ASTNode visitAlterRole(final AlterRoleContext ctx) {
        return new SQLServerAlterRoleStatement();
    }
    
    @Override
    public ASTNode visitDropRole(final DropRoleContext ctx) {
        return new SQLServerDropRoleStatement();
    }
    
    @Override
    public ASTNode visitCreateLogin(final CreateLoginContext ctx) {
        return new SQLServerCreateLoginStatement();
    }
    
    @Override
    public ASTNode visitAlterLogin(final AlterLoginContext ctx) {
        return new SQLServerAlterLoginStatement();
    }
    
    @Override
    public ASTNode visitDropLogin(final DropLoginContext ctx) {
        return new SQLServerDropLoginStatement();
    }
}
