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
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ColumnNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CreateLoginContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CreateRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CreateUserContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CreateUserLoginWindowsPrincipalClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CreateUserWindowsPrincipalClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DenyClassPrivilegesClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DenyClassTypePrivilegesClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DenyContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DropLoginContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DropRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DropUserContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.GrantClassPrivilegesClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.GrantClassTypePrivilegesClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.GrantContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.IgnoredNameIdentifierContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.OwnerContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.RevertContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.RevokeClassPrivilegesClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.RevokeClassTypePrivilegesClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.RevokeContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.SecurableContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.SetUserContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.UserNameContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dcl.LoginSegment;
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
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dcl.SQLServerRevertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dcl.SQLServerRevokeStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dcl.SQLServerSetUserStatement;

import java.util.Collection;
import java.util.LinkedList;
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
            if (null != ctx.grantClassPrivilegesClause().classPrivileges().columnNames()) {
                for (ColumnNamesContext each : ctx.grantClassPrivilegesClause().classPrivileges().columnNames()) {
                    result.getColumns().addAll(((CollectionValue<ColumnSegment>) visit(each)).getValue());
                }
            }
        }
        if (null != ctx.grantClassTypePrivilegesClause()) {
            for (SimpleTableSegment each : getTableFromGrantTypePrivilegeClause(ctx.grantClassTypePrivilegesClause())) {
                result.getTables().add(each);
            }
        }
        return result;
    }
    
    private Collection<SimpleTableSegment> getTableFromGrantPrivilegeClause(final GrantClassPrivilegesClauseContext ctx) {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        if (null != ctx.onClassClause()) {
            if (null != ctx.onClassClause().classItem() && null != ctx.onClassClause().classItem().OBJECT()) {
                result.add((SimpleTableSegment) visit(ctx.onClassClause().securable()));
            } else if (null != ctx.classPrivileges().privilegeType().get(0).objectPermission()) {
                result.add((SimpleTableSegment) visit(ctx.onClassClause().securable()));
            } else if (null != ctx.classPrivileges().privilegeType().get(0).PRIVILEGES()) {
                result.add((SimpleTableSegment) visit(ctx.onClassClause().securable()));
            }
        }
        return result;
    }
    
    private Collection<SimpleTableSegment> getTableFromGrantTypePrivilegeClause(final GrantClassTypePrivilegesClauseContext ctx) {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        if (null != ctx.onClassTypeClause() && null != ctx.onClassTypeClause().classType() && null != ctx.onClassTypeClause().classType().OBJECT()) {
            result.add((SimpleTableSegment) visit(ctx.onClassTypeClause().securable()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitRevoke(final RevokeContext ctx) {
        SQLServerRevokeStatement result = new SQLServerRevokeStatement();
        if (null != ctx.revokeClassPrivilegesClause()) {
            for (SimpleTableSegment each : getTableFromRevokeClassPrivilegesClause(ctx.revokeClassPrivilegesClause())) {
                result.getTables().add(each);
            }
            if (null != ctx.revokeClassPrivilegesClause().classPrivileges().columnNames()) {
                for (ColumnNamesContext each : ctx.revokeClassPrivilegesClause().classPrivileges().columnNames()) {
                    result.getColumns().addAll(((CollectionValue<ColumnSegment>) visit(each)).getValue());
                }
            }
        }
        if (null != ctx.revokeClassTypePrivilegesClause()) {
            for (SimpleTableSegment each : getTableFromRevokeClassTypePrivilegesClause(ctx.revokeClassTypePrivilegesClause())) {
                result.getTables().add(each);
            }
        }
        return result;
    }
    
    private Collection<SimpleTableSegment> getTableFromRevokeClassPrivilegesClause(final RevokeClassPrivilegesClauseContext ctx) {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        if (null != ctx.onClassClause()) {
            if (null != ctx.onClassClause().classItem() && null != ctx.onClassClause().classItem().OBJECT()) {
                result.add((SimpleTableSegment) visit(ctx.onClassClause().securable()));
            } else if (null != ctx.classPrivileges().privilegeType().get(0).objectPermission()) {
                result.add((SimpleTableSegment) visit(ctx.onClassClause().securable()));
            } else if (null != ctx.classPrivileges().privilegeType().get(0).PRIVILEGES()) {
                result.add((SimpleTableSegment) visit(ctx.onClassClause().securable()));
            }
        }
        return result;
    }
    
    private Collection<SimpleTableSegment> getTableFromRevokeClassTypePrivilegesClause(final RevokeClassTypePrivilegesClauseContext ctx) {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        if (null != ctx.onClassTypeClause() && null != ctx.onClassTypeClause().classType() && null != ctx.onClassTypeClause().classType().OBJECT()) {
            result.add((SimpleTableSegment) visit(ctx.onClassTypeClause().securable()));
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
        return null != ctx.userName() ? (UserSegment) visit(ctx.userName()) : (UserSegment) visit(ctx.windowsPrincipal(0));
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
        if (null != ctx.denyClassPrivilegesClause()) {
            for (SimpleTableSegment each : getTableFromDenyClassPrivilegesClause(ctx.denyClassPrivilegesClause())) {
                result.setTable(each);
            }
            if (null != ctx.denyClassPrivilegesClause().classPrivileges().columnNames()) {
                for (ColumnNamesContext each : ctx.denyClassPrivilegesClause().classPrivileges().columnNames()) {
                    result.getColumns().addAll(((CollectionValue<ColumnSegment>) visit(each)).getValue());
                }
            }
        }
        if (null != ctx.denyClassTypePrivilegesClause()) {
            for (SimpleTableSegment each : getTableFromDenyClassTypePrivilegesClause(ctx.denyClassTypePrivilegesClause())) {
                result.setTable(each);
            }
        }
        return result;
    }
    
    private Collection<SimpleTableSegment> getTableFromDenyClassPrivilegesClause(final DenyClassPrivilegesClauseContext ctx) {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        if (null != ctx.onClassClause()) {
            if (null != ctx.onClassClause().classItem() && null != ctx.onClassClause().classItem().OBJECT()) {
                result.add((SimpleTableSegment) visit(ctx.onClassClause().securable()));
            } else if (null != ctx.classPrivileges().privilegeType().get(0).objectPermission()) {
                result.add((SimpleTableSegment) visit(ctx.onClassClause().securable()));
            } else if (null != ctx.classPrivileges().privilegeType().get(0).PRIVILEGES()) {
                result.add((SimpleTableSegment) visit(ctx.onClassClause().securable()));
            }
        }
        return result;
    }
    
    private Collection<SimpleTableSegment> getTableFromDenyClassTypePrivilegesClause(final DenyClassTypePrivilegesClauseContext ctx) {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        if (null != ctx.onClassTypeClause() && null != ctx.onClassTypeClause().classType() && null != ctx.onClassTypeClause().classType().OBJECT()) {
            result.add((SimpleTableSegment) visit(ctx.onClassTypeClause().securable()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitDropUser(final DropUserContext ctx) {
        SQLServerDropUserStatement result = new SQLServerDropUserStatement();
        result.getUsers().add(((UserSegment) visit(ctx.userName())).getUser());
        return result;
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
        SQLServerCreateLoginStatement result = new SQLServerCreateLoginStatement();
        if (null != ctx.ignoredNameIdentifier()) {
            LoginSegment loginSegment = new LoginSegment(
                    ctx.ignoredNameIdentifier().getStart().getStartIndex(), ctx.ignoredNameIdentifier().getStop().getStopIndex(), (IdentifierValue) visit(ctx.ignoredNameIdentifier()));
            result.setLoginSegment(loginSegment);
        }
        return result;
    }
    
    @Override
    public ASTNode visitAlterLogin(final AlterLoginContext ctx) {
        SQLServerAlterLoginStatement result = new SQLServerAlterLoginStatement();
        if (null != ctx.ignoredNameIdentifier()) {
            LoginSegment loginSegment = new LoginSegment(
                    ctx.ignoredNameIdentifier().getStart().getStartIndex(), ctx.ignoredNameIdentifier().getStop().getStopIndex(), (IdentifierValue) visit(ctx.ignoredNameIdentifier()));
            result.setLoginSegment(loginSegment);
        }
        return result;
    }
    
    @Override
    public ASTNode visitDropLogin(final DropLoginContext ctx) {
        SQLServerDropLoginStatement result = new SQLServerDropLoginStatement();
        LoginSegment loginSegment = new LoginSegment(
                ctx.ignoredNameIdentifier().getStart().getStartIndex(), ctx.ignoredNameIdentifier().getStop().getStopIndex(), (IdentifierValue) visit(ctx.ignoredNameIdentifier()));
        result.setLoginSegment(loginSegment);
        return result;
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
    
    @Override
    public ASTNode visitRevert(final RevertContext ctx) {
        return new SQLServerRevertStatement();
    }
}
