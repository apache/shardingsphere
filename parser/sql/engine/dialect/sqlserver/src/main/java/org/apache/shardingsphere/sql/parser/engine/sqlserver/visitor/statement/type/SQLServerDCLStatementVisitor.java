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

package org.apache.shardingsphere.sql.parser.engine.sqlserver.visitor.statement.type;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AlterLoginContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AlterRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AlterUserContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ClassPrivilegesContext;
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
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.GrantContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.IgnoredNameIdentifierContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.OnClassClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.OnClassTypeClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.OwnerContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.RevertContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.RevokeContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.SecurableContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.SetUserContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.UserNameContext;
import org.apache.shardingsphere.sql.parser.engine.sqlserver.visitor.statement.SQLServerStatementVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dcl.LoginSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dcl.UserSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.role.AlterRoleStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.role.CreateRoleStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.role.DropRoleStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.user.AlterUserStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.user.CreateUserStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.user.DropUserStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.StringLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.dcl.SQLServerGrantStatement;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.dcl.SQLServerRevertStatement;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.dcl.SQLServerRevokeStatement;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.dcl.login.SQLServerAlterLoginStatement;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.dcl.login.SQLServerCreateLoginStatement;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.dcl.login.SQLServerDropLoginStatement;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.dcl.user.SQLServerDenyUserStatement;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.dcl.user.SQLServerSetUserStatement;

import java.util.Collections;
import java.util.Optional;

/**
 * DCL statement visitor for SQLServer.
 */
public final class SQLServerDCLStatementVisitor extends SQLServerStatementVisitor implements DCLStatementVisitor {
    
    public SQLServerDCLStatementVisitor(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitGrant(final GrantContext ctx) {
        SQLServerGrantStatement result = new SQLServerGrantStatement(getDatabaseType());
        if (null != ctx.grantClassPrivilegesClause()) {
            findTableSegment(ctx.grantClassPrivilegesClause().onClassClause(), ctx.grantClassPrivilegesClause().classPrivileges()).ifPresent(optional -> result.getTables().add(optional));
            if (null != ctx.grantClassPrivilegesClause().classPrivileges().columnNames()) {
                for (ColumnNamesContext each : ctx.grantClassPrivilegesClause().classPrivileges().columnNames()) {
                    result.getColumns().addAll(((CollectionValue<ColumnSegment>) visit(each)).getValue());
                }
            }
        }
        if (null != ctx.grantClassTypePrivilegesClause()) {
            findTableSegment(ctx.grantClassTypePrivilegesClause().onClassTypeClause()).ifPresent(optional -> result.getTables().add(optional));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitRevoke(final RevokeContext ctx) {
        SQLServerRevokeStatement result = new SQLServerRevokeStatement(getDatabaseType());
        if (null != ctx.revokeClassPrivilegesClause()) {
            findTableSegment(ctx.revokeClassPrivilegesClause().onClassClause(), ctx.revokeClassPrivilegesClause().classPrivileges()).ifPresent(optional -> result.getTables().add(optional));
            if (null != ctx.revokeClassPrivilegesClause().classPrivileges().columnNames()) {
                for (ColumnNamesContext each : ctx.revokeClassPrivilegesClause().classPrivileges().columnNames()) {
                    result.getColumns().addAll(((CollectionValue<ColumnSegment>) visit(each)).getValue());
                }
            }
        }
        if (null != ctx.revokeClassTypePrivilegesClause()) {
            findTableSegment(ctx.revokeClassTypePrivilegesClause().onClassTypeClause()).ifPresent(optional -> result.getTables().add(optional));
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
    
    @Override
    public ASTNode visitCreateUser(final CreateUserContext ctx) {
        CreateUserStatement result = new CreateUserStatement(getDatabaseType());
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
        return 1 == identifierCount ? (IdentifierValue) visit(ctx.identifier(0)) : new IdentifierValue(ctx.getText());
    }
    
    @Override
    public ASTNode visitCreateUserWindowsPrincipalClause(final CreateUserWindowsPrincipalClauseContext ctx) {
        if (null != ctx.windowsPrincipal()) {
            return visit(ctx.windowsPrincipal().userName());
        }
        if (null != ctx.azureActiveDirectoryPrincipal()) {
            return visit(ctx.azureActiveDirectoryPrincipal().userName());
        }
        return visit(ctx.userName());
    }
    
    @Override
    public ASTNode visitCreateUserLoginWindowsPrincipalClause(final CreateUserLoginWindowsPrincipalClauseContext ctx) {
        return null == ctx.userName() ? (UserSegment) visit(ctx.windowsPrincipal(0)) : (UserSegment) visit(ctx.userName());
    }
    
    @Override
    public ASTNode visitAlterUser(final AlterUserContext ctx) {
        return new AlterUserStatement(getDatabaseType(), (UserSegment) visit(ctx.userName()));
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitDeny(final DenyContext ctx) {
        SQLServerDenyUserStatement result = new SQLServerDenyUserStatement(getDatabaseType());
        if (null != ctx.denyClassPrivilegesClause()) {
            findTableSegment(ctx.denyClassPrivilegesClause().onClassClause(), ctx.denyClassPrivilegesClause().classPrivileges()).ifPresent(result::setTable);
            if (null != ctx.denyClassPrivilegesClause().classPrivileges().columnNames()) {
                for (ColumnNamesContext each : ctx.denyClassPrivilegesClause().classPrivileges().columnNames()) {
                    result.getColumns().addAll(((CollectionValue<ColumnSegment>) visit(each)).getValue());
                }
            }
        }
        if (null != ctx.denyClassTypePrivilegesClause()) {
            findTableSegment(ctx.denyClassTypePrivilegesClause().onClassTypeClause()).ifPresent(result::setTable);
        }
        return result;
    }
    
    private Optional<SimpleTableSegment> findTableSegment(final OnClassClauseContext onClassClauseCtx, final ClassPrivilegesContext classPrivilegesCtx) {
        if (null == onClassClauseCtx) {
            return Optional.empty();
        }
        if (null != onClassClauseCtx.classItem() && null != onClassClauseCtx.classItem().OBJECT()
                || null != classPrivilegesCtx.privilegeType().get(0).objectPermission() || null != classPrivilegesCtx.privilegeType().get(0).PRIVILEGES()) {
            return Optional.of((SimpleTableSegment) visit(onClassClauseCtx.securable()));
        }
        return Optional.empty();
    }
    
    private Optional<SimpleTableSegment> findTableSegment(final OnClassTypeClauseContext ctx) {
        return null == ctx || null == ctx.classType() || null == ctx.classType().OBJECT() ? Optional.empty() : Optional.of((SimpleTableSegment) visit(ctx.securable()));
    }
    
    @Override
    public ASTNode visitDropUser(final DropUserContext ctx) {
        return new DropUserStatement(getDatabaseType(), Collections.singleton(((UserSegment) visit(ctx.userName())).getUser()));
    }
    
    @Override
    public ASTNode visitCreateRole(final CreateRoleContext ctx) {
        return new CreateRoleStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterRole(final AlterRoleContext ctx) {
        return new AlterRoleStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropRole(final DropRoleContext ctx) {
        return new DropRoleStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCreateLogin(final CreateLoginContext ctx) {
        return new SQLServerCreateLoginStatement(getDatabaseType(),
                null == ctx.ignoredNameIdentifier()
                        ? null
                        : new LoginSegment(
                                ctx.ignoredNameIdentifier().getStart().getStartIndex(), ctx.ignoredNameIdentifier().getStop().getStopIndex(), (IdentifierValue) visit(ctx.ignoredNameIdentifier())));
    }
    
    @Override
    public ASTNode visitAlterLogin(final AlterLoginContext ctx) {
        return new SQLServerAlterLoginStatement(getDatabaseType(), null == ctx.ignoredNameIdentifier()
                ? null
                : new LoginSegment(
                        ctx.ignoredNameIdentifier().getStart().getStartIndex(), ctx.ignoredNameIdentifier().getStop().getStopIndex(), (IdentifierValue) visit(ctx.ignoredNameIdentifier())));
    }
    
    @Override
    public ASTNode visitDropLogin(final DropLoginContext ctx) {
        return new SQLServerDropLoginStatement(getDatabaseType(), new LoginSegment(
                ctx.ignoredNameIdentifier().getStart().getStartIndex(), ctx.ignoredNameIdentifier().getStop().getStopIndex(), (IdentifierValue) visit(ctx.ignoredNameIdentifier())));
    }
    
    @Override
    public ASTNode visitSetUser(final SetUserContext ctx) {
        return new SQLServerSetUserStatement(getDatabaseType(), null == ctx.stringLiterals() ? null : getUserSegment(ctx));
    }
    
    private UserSegment getUserSegment(final SetUserContext ctx) {
        UserSegment result = new UserSegment();
        result.setUser(((StringLiteralValue) visit(ctx.stringLiterals())).getValue());
        result.setStartIndex(ctx.stringLiterals().start.getStartIndex());
        result.setStopIndex(ctx.stringLiterals().stop.getStopIndex());
        return result;
    }
    
    @Override
    public ASTNode visitRevert(final RevertContext ctx) {
        return new SQLServerRevertStatement(getDatabaseType());
    }
}
