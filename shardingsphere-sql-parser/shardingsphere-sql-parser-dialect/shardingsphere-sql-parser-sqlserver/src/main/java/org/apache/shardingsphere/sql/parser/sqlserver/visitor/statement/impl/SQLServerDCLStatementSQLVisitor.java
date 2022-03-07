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

package org.apache.shardingsphere.sql.parser.sqlserver.visitor.statement.impl;

import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.operation.SQLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.type.DCLSQLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AlterLoginContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AlterRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AlterUserContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ClassPrivilegesClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ClassTypePrivilegesClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ColumnNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CreateLoginContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CreateRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CreateUserContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CreateUserLoginWindowsPrincipalClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CreateUserWindowsPrincipalClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DenyContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DropLoginContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DropRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DropUserContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.GrantClassPrivilegesClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.GrantClassTypePrivilegesClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.GrantContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.IgnoredNameIdentifierContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.OwnerContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.RevokeContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.SecurableContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.SetUserContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.UserNameContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.StringLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.segment.UserSegment;
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
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dcl.SQLServerSetUserStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

/**
 * DCL Statement SQL visitor for SQLServer.
 */
@NoArgsConstructor
public final class SQLServerDCLStatementSQLVisitor extends SQLServerStatementSQLVisitor implements DCLSQLVisitor, SQLStatementVisitor {
    
    public SQLServerDCLStatementSQLVisitor(final Properties props) {
        super(props);
    }
    
    @Override
    public ASTNode visitGrant(final GrantContext ctx) {
        SQLServerGrantStatement result = new SQLServerGrantStatement();
        if (null != ctx.grantClassPrivilegesClause()) {
            for (SimpleTableSegment each : getTableFromGrantPrivilegeClause(ctx.grantClassPrivilegesClause())) {
                result.getTables().add(each);
            }
            if (null != ctx.grantClassPrivilegesClause().grantClassPrivileges().columnNames()) {
                for (ColumnNamesContext each : ctx.grantClassPrivilegesClause().grantClassPrivileges().columnNames()) {
                    result.getColumns().addAll(((CollectionValue<ColumnSegment>) visit(each)).getValue());
                }
            }
        }
        if (null != ctx.grantClassTypePrivilegesClause()) {
            for (SimpleTableSegment each : getTableFromGrantPrivilegeClause(ctx.grantClassTypePrivilegesClause())) {
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
    
    private Collection<SimpleTableSegment> getTableFromGrantPrivilegeClause(final GrantClassPrivilegesClauseContext ctx) {
        Collection<SimpleTableSegment> result = new ArrayList<>();
        if (null != ctx.grantOnClassClause()) {
            if (null != ctx.grantOnClassClause().classItem() && null != ctx.grantOnClassClause().classItem().OBJECT()) {
                result = Collections.singletonList((SimpleTableSegment) visit(ctx.grantOnClassClause().securable()));
            }
            if (null != ctx.grantClassPrivileges().privilegeType().get(0).objectPermission()) {
                result = Collections.singletonList((SimpleTableSegment) visit(ctx.grantOnClassClause().securable()));
            }
            if (null != ctx.grantClassPrivileges().privilegeType().get(0).PRIVILEGES()) {
                result = Collections.singletonList((SimpleTableSegment) visit(ctx.grantOnClassClause().securable()));
            }
        }
        return result;
    }
    
    private Collection<SimpleTableSegment> getTableFromGrantPrivilegeClause(final GrantClassTypePrivilegesClauseContext ctx) {
        Collection<SimpleTableSegment> result = new ArrayList<>();
        if (null != ctx.grantOnClassTypeClause() && null != ctx.grantOnClassTypeClause().grantClassType() && null != ctx.grantOnClassTypeClause().grantClassType().OBJECT()) {
            result = Collections.singletonList((SimpleTableSegment) visit(ctx.grantOnClassTypeClause().securable()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitSecurable(final SecurableContext ctx) {
        SimpleTableSegment result = new SimpleTableSegment(new TableNameSegment(ctx.name().getStart().getStartIndex(), ctx.name().getStop().getStopIndex(), (IdentifierValue) visit(ctx.name())));
        OwnerContext owner = ctx.owner();
        if (null != owner) {
            result.setOwner(new OwnerSegment(owner.getStart().getStartIndex(), owner.getStop().getStopIndex(), (IdentifierValue) visit(owner.identifier())));
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
        SQLServerCreateUserStatement result = new SQLServerCreateUserStatement();
        if (null != ctx.createUserLoginClause()) {
            result.getUsers().add((UserSegment) visit(ctx.createUserLoginClause().userName()));
        } else if (null != ctx.createUserWindowsPrincipalClause()) {
            result.getUsers().add((UserSegment) visit(ctx.createUserWindowsPrincipalClause()));
        } else if (null != ctx.createUserLoginWindowsPrincipalClause()) {
            result.getUsers().add((UserSegment) visit(ctx.createUserLoginWindowsPrincipalClause()));
        } else if (null != ctx.createUserWithoutLoginClause()) {
            result.getUsers().add((UserSegment) visit(ctx.createUserWithoutLoginClause().userName()));
        } else if (null != ctx.createUserFromExternalProviderClause()) {
            result.getUsers().add((UserSegment) visit(ctx.createUserFromExternalProviderClause().userName()));
        } else if (null != ctx.createUserWithDefaultSchema()) {
            result.getUsers().add((UserSegment) visit(ctx.createUserWithDefaultSchema().userName()));
        } else if (null != ctx.createUserWithAzureActiveDirectoryPrincipalClause()) {
            result.getUsers().add((UserSegment) visit(ctx.createUserWithAzureActiveDirectoryPrincipalClause().azureActiveDirectoryPrincipal().userName()));
        } else {
            result.getUsers().add((UserSegment) visit(ctx.userName()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitUserName(final UserNameContext ctx) {
        UserSegment result = new UserSegment();
        String user = ((IdentifierValue) visit(ctx.ignoredNameIdentifier())).getValue();
        result.setUser(user);
        return result;
    }
    
    @Override
    public ASTNode visitIgnoredNameIdentifier(final IgnoredNameIdentifierContext ctx) {
        int identifierCount = ctx.identifier().size();
        IdentifierValue result = 1 == identifierCount ? (IdentifierValue) visit(ctx.identifier(0)) : new IdentifierValue(ctx.getText());
        return result;
    }
    
    @Override
    public ASTNode visitCreateUserWindowsPrincipalClause(final CreateUserWindowsPrincipalClauseContext ctx) {
        UserSegment result;
        if (null != ctx.windowsPrincipal()) {
            result = (UserSegment) visit(ctx.windowsPrincipal().userName());
        } else if (null != ctx.azureActiveDirectoryPrincipal()) {
            result = (UserSegment) visit(ctx.azureActiveDirectoryPrincipal().userName());
        } else {
            result = (UserSegment) visit(ctx.userName());
        }
        return result;
    }
    
    @Override
    public ASTNode visitCreateUserLoginWindowsPrincipalClause(final CreateUserLoginWindowsPrincipalClauseContext ctx) {
        UserSegment result;
        result = null != ctx.userName() ? (UserSegment) visit(ctx.userName()) : (UserSegment) visit(ctx.windowsPrincipal(0));
        return result;
    }
    
    @Override
    public ASTNode visitAlterUser(final AlterUserContext ctx) {
        SQLServerAlterUserStatement result = new SQLServerAlterUserStatement();
        result.setUser((UserSegment) visit(ctx.userName()));
        return result;
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
    
    @Override
    public ASTNode visitSetUser(final SetUserContext ctx) {
        SQLServerSetUserStatement result = new SQLServerSetUserStatement();
        if (null != ctx.stringLiterals()) {
            UserSegment userSegment = new UserSegment();
            userSegment.setUser(((StringLiteralValue) visit(ctx.stringLiterals())).getValue());
            userSegment.setStartIndex(ctx.stringLiterals().start.getStartIndex());
            userSegment.setStopIndex(ctx.stringLiterals().stop.getStopIndex());
            result.setUser(userSegment); 
        }
        return result;
    }
}
