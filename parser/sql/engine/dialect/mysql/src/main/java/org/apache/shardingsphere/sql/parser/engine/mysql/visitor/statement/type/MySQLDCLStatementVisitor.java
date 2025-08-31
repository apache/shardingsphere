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

package org.apache.shardingsphere.sql.parser.engine.mysql.visitor.statement.type;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AccountLockPasswordExpireOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AccountLockPasswordExpireOptionsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterUserContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ColumnNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ConnectOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ConnectOptionsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateUserContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateUserEntryContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateUserEntryIdentifiedByContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateUserEntryIdentifiedWithContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateUserEntryNoOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropUserContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.GrantIdentifierContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.GrantLevelDatabaseGlobalContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.GrantLevelGlobalContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.GrantLevelTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.GrantProxyContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.GrantRoleOrPrivilegeOnToContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.GrantRoleOrPrivilegeToContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RenameUserContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RequireClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RevokeFromContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RevokeOnFromContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RoleAtHostContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RoleNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RoleOrDynamicPrivilegeContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RoleOrPrivilegeContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RoleOrPrivilegesContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SetDefaultRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SetPasswordContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SetRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StaticPrivilegeAlterContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StaticPrivilegeAlterRoutineContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StaticPrivilegeCreateContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StaticPrivilegeCreateRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StaticPrivilegeCreateRoutineContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StaticPrivilegeCreateTablespaceContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StaticPrivilegeCreateTemporaryTablesContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StaticPrivilegeCreateUserContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StaticPrivilegeCreateViewContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StaticPrivilegeDeleteContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StaticPrivilegeDropContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StaticPrivilegeDropRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StaticPrivilegeEventContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StaticPrivilegeExecuteContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StaticPrivilegeFileContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StaticPrivilegeGrantContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StaticPrivilegeIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StaticPrivilegeInsertContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StaticPrivilegeLockTablesContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StaticPrivilegeProcessContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StaticPrivilegeReferencesContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StaticPrivilegeReloadContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StaticPrivilegeReplicationClientContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StaticPrivilegeReplicationSlaveContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StaticPrivilegeSelectContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StaticPrivilegeShowDatabasesContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StaticPrivilegeShowViewContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StaticPrivilegeShutdownContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StaticPrivilegeSuperContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StaticPrivilegeTriggerContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StaticPrivilegeUpdateContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StaticPrivilegeUsageContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TlsOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.UsernameContext;
import org.apache.shardingsphere.sql.parser.engine.mysql.visitor.statement.MySQLStatementVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.enums.ACLAttributeType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.SSLType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.UserResourceSpecifiedLimitType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dcl.PasswordOrLockOptionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dcl.PrivilegeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dcl.RoleOrPrivilegeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dcl.TLSOptionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dcl.UserResourceSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dcl.UserSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.GrantLevelSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.role.CreateRoleStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.role.DropRoleStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.role.SetRoleStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.user.AlterUserStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.user.DropUserStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.NumberLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.StringLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.mysql.dcl.MySQLGrantStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dcl.MySQLRevokeStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dcl.role.MySQLSetDefaultRoleStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dcl.user.MySQLCreateUserStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dcl.user.MySQLRenameUserStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dcl.user.MySQLSetPasswordStatement;

import java.util.stream.Collectors;

/**
 * DCL statement visitor for MySQL.
 */
public final class MySQLDCLStatementVisitor extends MySQLStatementVisitor implements DCLStatementVisitor {
    
    public MySQLDCLStatementVisitor(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    @Override
    public ASTNode visitGrantRoleOrPrivilegeTo(final GrantRoleOrPrivilegeToContext ctx) {
        MySQLGrantStatement result = new MySQLGrantStatement(getDatabaseType());
        fillRoleOrPrivileges(result, ctx.roleOrPrivileges());
        for (UsernameContext each : ctx.userList().username()) {
            result.getUsers().add((UserSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitGrantRoleOrPrivilegeOnTo(final GrantRoleOrPrivilegeOnToContext ctx) {
        MySQLGrantStatement result = new MySQLGrantStatement(getDatabaseType());
        if (null == ctx.roleOrPrivileges()) {
            result.setAllPrivileges(true);
        } else {
            fillRoleOrPrivileges(result, ctx.roleOrPrivileges());
        }
        result.setLevel(generateGrantLevel(ctx.grantIdentifier()));
        for (UsernameContext each : ctx.userList().username()) {
            result.getUsers().add((UserSegment) visit(each));
        }
        if (null != ctx.aclType()) {
            result.setAclObject(ctx.aclType().getText().toUpperCase());
        }
        return result;
    }
    
    @Override
    public ASTNode visitGrantProxy(final GrantProxyContext ctx) {
        MySQLGrantStatement result = new MySQLGrantStatement(getDatabaseType());
        PrivilegeSegment privilege = new PrivilegeSegment(ctx.PROXY().getSymbol().getStartIndex(), ctx.PROXY().getSymbol().getStopIndex(), "GRANT");
        result.getRoleOrPrivileges().add(new RoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege));
        for (UsernameContext each : ctx.userList().username()) {
            result.getUsers().add((UserSegment) visit(each));
        }
        return result;
    }
    
    private void fillRoleOrPrivileges(final MySQLGrantStatement statement, final RoleOrPrivilegesContext ctx) {
        for (RoleOrPrivilegeContext each : ctx.roleOrPrivilege()) {
            statement.getRoleOrPrivileges().add((RoleOrPrivilegeSegment) visit(each));
        }
    }
    
    private void fillRoleOrPrivileges(final MySQLRevokeStatement statement, final RoleOrPrivilegesContext ctx) {
        for (RoleOrPrivilegeContext each : ctx.roleOrPrivilege()) {
            statement.getRoleOrPrivileges().add((RoleOrPrivilegeSegment) visit(each));
        }
    }
    
    private GrantLevelSegment generateGrantLevel(final GrantIdentifierContext ctx) {
        if (ctx instanceof GrantLevelGlobalContext) {
            return new GrantLevelSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "*", "*");
        }
        if (ctx instanceof GrantLevelDatabaseGlobalContext) {
            String databaseName = new IdentifierValue(((GrantLevelDatabaseGlobalContext) ctx).databaseName().getText()).getValue();
            return new GrantLevelSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), databaseName, "*");
        }
        String databaseName = null;
        String tableName;
        if (null != ((GrantLevelTableContext) ctx).tableName().owner()) {
            databaseName = new IdentifierValue(((GrantLevelTableContext) ctx).tableName().owner().getText()).getValue();
        }
        tableName = new IdentifierValue(((GrantLevelTableContext) ctx).tableName().name().getText()).getValue();
        return new GrantLevelSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), databaseName, tableName);
    }
    
    @Override
    public ASTNode visitRoleOrDynamicPrivilege(final RoleOrDynamicPrivilegeContext ctx) {
        String role = new IdentifierValue(ctx.roleIdentifierOrText().getText()).getValue();
        return new RoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), role, null, null);
    }
    
    @Override
    public ASTNode visitRoleAtHost(final RoleAtHostContext ctx) {
        String role = new IdentifierValue(ctx.roleIdentifierOrText().getText()).getValue();
        String host = new IdentifierValue(ctx.textOrIdentifier().getText()).getValue();
        return new RoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), role, host, null);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeSelect(final StaticPrivilegeSelectContext ctx) {
        PrivilegeSegment privilege = new PrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "SELECT");
        if (null != ctx.columnNames()) {
            for (ColumnNameContext each : ctx.columnNames().columnName()) {
                privilege.getColumns().add(new IdentifierValue(each.getText()).getValue());
            }
        }
        return new RoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeInsert(final StaticPrivilegeInsertContext ctx) {
        PrivilegeSegment privilege = new PrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "INSERT");
        if (null != ctx.columnNames()) {
            for (ColumnNameContext each : ctx.columnNames().columnName()) {
                privilege.getColumns().add(new IdentifierValue(each.getText()).getValue());
            }
        }
        return new RoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeUpdate(final StaticPrivilegeUpdateContext ctx) {
        PrivilegeSegment privilege = new PrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "UPDATE");
        if (null != ctx.columnNames()) {
            for (ColumnNameContext each : ctx.columnNames().columnName()) {
                privilege.getColumns().add(new IdentifierValue(each.getText()).getValue());
            }
        }
        return new RoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeReferences(final StaticPrivilegeReferencesContext ctx) {
        PrivilegeSegment privilege = new PrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "REFERENCES");
        if (null != ctx.columnNames()) {
            for (ColumnNameContext each : ctx.columnNames().columnName()) {
                privilege.getColumns().add(new IdentifierValue(each.getText()).getValue());
            }
        }
        return new RoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeDelete(final StaticPrivilegeDeleteContext ctx) {
        PrivilegeSegment privilege = new PrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "DELETE");
        return new RoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeUsage(final StaticPrivilegeUsageContext ctx) {
        PrivilegeSegment privilege = new PrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "USAGE");
        return new RoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeIndex(final StaticPrivilegeIndexContext ctx) {
        PrivilegeSegment privilege = new PrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "INDEX");
        return new RoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeAlter(final StaticPrivilegeAlterContext ctx) {
        PrivilegeSegment privilege = new PrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "ALTER");
        return new RoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeCreate(final StaticPrivilegeCreateContext ctx) {
        PrivilegeSegment privilege = new PrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "CREATE");
        return new RoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeDrop(final StaticPrivilegeDropContext ctx) {
        PrivilegeSegment privilege = new PrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "DROP");
        return new RoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeExecute(final StaticPrivilegeExecuteContext ctx) {
        PrivilegeSegment privilege = new PrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "EXECUTE");
        return new RoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeReload(final StaticPrivilegeReloadContext ctx) {
        PrivilegeSegment privilege = new PrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "RELOAD");
        return new RoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeShutdown(final StaticPrivilegeShutdownContext ctx) {
        PrivilegeSegment privilege = new PrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "SHUTDOWN");
        return new RoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeProcess(final StaticPrivilegeProcessContext ctx) {
        PrivilegeSegment privilege = new PrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "PROCESS");
        return new RoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeFile(final StaticPrivilegeFileContext ctx) {
        PrivilegeSegment privilege = new PrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "FILE");
        return new RoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeGrant(final StaticPrivilegeGrantContext ctx) {
        PrivilegeSegment privilege = new PrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "GRANT");
        return new RoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeShowDatabases(final StaticPrivilegeShowDatabasesContext ctx) {
        PrivilegeSegment privilege = new PrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "SHOW_DB");
        return new RoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeSuper(final StaticPrivilegeSuperContext ctx) {
        PrivilegeSegment privilege = new PrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "SUPER");
        return new RoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeCreateTemporaryTables(final StaticPrivilegeCreateTemporaryTablesContext ctx) {
        PrivilegeSegment privilege = new PrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "CREATE_TMP");
        return new RoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeLockTables(final StaticPrivilegeLockTablesContext ctx) {
        PrivilegeSegment privilege = new PrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "LOCK_TABLES");
        return new RoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeReplicationSlave(final StaticPrivilegeReplicationSlaveContext ctx) {
        PrivilegeSegment privilege = new PrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "REPL_SLAVE");
        return new RoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeReplicationClient(final StaticPrivilegeReplicationClientContext ctx) {
        PrivilegeSegment privilege = new PrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "REPL_CLIENT");
        return new RoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeCreateView(final StaticPrivilegeCreateViewContext ctx) {
        PrivilegeSegment privilege = new PrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "CREATE_VIEW");
        return new RoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeShowView(final StaticPrivilegeShowViewContext ctx) {
        PrivilegeSegment privilege = new PrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "SHOW_VIEW");
        return new RoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeCreateRoutine(final StaticPrivilegeCreateRoutineContext ctx) {
        PrivilegeSegment privilege = new PrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "CREATE_PROC");
        return new RoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeAlterRoutine(final StaticPrivilegeAlterRoutineContext ctx) {
        PrivilegeSegment privilege = new PrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "ALTER_PROC");
        return new RoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeCreateUser(final StaticPrivilegeCreateUserContext ctx) {
        PrivilegeSegment privilege = new PrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "CREATE_USER");
        return new RoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeEvent(final StaticPrivilegeEventContext ctx) {
        PrivilegeSegment privilege = new PrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "EVENT");
        return new RoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeTrigger(final StaticPrivilegeTriggerContext ctx) {
        PrivilegeSegment privilege = new PrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "TRIGGER");
        return new RoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeCreateTablespace(final StaticPrivilegeCreateTablespaceContext ctx) {
        PrivilegeSegment privilege = new PrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "CREATE_TABLESPACE");
        return new RoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeCreateRole(final StaticPrivilegeCreateRoleContext ctx) {
        PrivilegeSegment privilege = new PrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "CREATE_ROLE");
        return new RoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeDropRole(final StaticPrivilegeDropRoleContext ctx) {
        PrivilegeSegment privilege = new PrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "DROP_ROLE");
        return new RoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitRevokeFrom(final RevokeFromContext ctx) {
        MySQLRevokeStatement result = new MySQLRevokeStatement(getDatabaseType());
        if (null != ctx.roleOrPrivileges()) {
            fillRoleOrPrivileges(result, ctx.roleOrPrivileges());
        } else if (null != ctx.ALL()) {
            result.setAllPrivileges(true);
        }
        for (UsernameContext each : ctx.userList().username()) {
            result.getFromUsers().add((UserSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitRevokeOnFrom(final RevokeOnFromContext ctx) {
        MySQLRevokeStatement result = new MySQLRevokeStatement(getDatabaseType());
        if (null != ctx.roleOrPrivileges()) {
            fillRoleOrPrivileges(result, ctx.roleOrPrivileges());
        } else if (null != ctx.ALL()) {
            result.setAllPrivileges(true);
        } else if (null != ctx.PROXY()) {
            PrivilegeSegment privilege = new PrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "DROP_ROLE");
            result.getRoleOrPrivileges().add(new RoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege));
            result.setOnUser((UserSegment) visit(ctx.username()));
        }
        if (null != ctx.grantIdentifier()) {
            result.setLevel(generateGrantLevel(ctx.grantIdentifier()));
        }
        for (UsernameContext each : ctx.userList().username()) {
            result.getFromUsers().add((UserSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitCreateUser(final CreateUserContext ctx) {
        MySQLCreateUserStatement result = new MySQLCreateUserStatement(getDatabaseType());
        for (CreateUserEntryContext each : ctx.createUserList().createUserEntry()) {
            result.getUsers().add((UserSegment) visit(each));
        }
        if (null != ctx.defaultRoleClause()) {
            for (RoleNameContext each : ctx.defaultRoleClause().roleName()) {
                result.getDefaultRoles().add(each.getText());
            }
        }
        if (null != ctx.requireClause()) {
            result.setTlsOptionSegment((TLSOptionSegment) visit(ctx.requireClause()));
        }
        if (null != ctx.connectOptions()) {
            result.setUserResource((UserResourceSegment) visit(ctx.connectOptions()));
        }
        if (null != ctx.accountLockPasswordExpireOptions()) {
            result.setPasswordOrLockOption((PasswordOrLockOptionSegment) visit(ctx.accountLockPasswordExpireOptions()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitRequireClause(final RequireClauseContext ctx) {
        TLSOptionSegment result = new TLSOptionSegment();
        if (null != ctx.NONE()) {
            result.setType(SSLType.NONE);
        } else if (null != ctx.X509()) {
            result.setType(SSLType.X509);
        } else if (null != ctx.SSL()) {
            result.setType(SSLType.ANY);
        } else {
            result.setType(SSLType.SPECIFIED);
            for (TlsOptionContext each : ctx.tlsOption()) {
                if (null != each.SUBJECT()) {
                    result.setX509Subject(each.string_().getText());
                } else if (null != each.ISSUER()) {
                    result.setX509Issuer(each.string_().getText());
                } else if (null != each.CIPHER()) {
                    result.setX509Cipher(each.string_().getText());
                }
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitConnectOptions(final ConnectOptionsContext ctx) {
        UserResourceSegment result = new UserResourceSegment();
        result.setStartIndex(ctx.start.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        for (ConnectOptionContext each : ctx.connectOption()) {
            if (null != each.MAX_QUERIES_PER_HOUR()) {
                result.setSpecifiedLimits(UserResourceSpecifiedLimitType.QUERIES_PER_HOUR);
                result.setQuestions(new NumberLiteralValue(each.NUMBER_().getText()).getValue().intValue());
            }
            if (null != each.MAX_UPDATES_PER_HOUR()) {
                result.setSpecifiedLimits(UserResourceSpecifiedLimitType.UPDATES_PER_HOUR);
                result.setUpdates(new NumberLiteralValue(each.NUMBER_().getText()).getValue().intValue());
            }
            if (null != each.MAX_CONNECTIONS_PER_HOUR()) {
                result.setSpecifiedLimits(UserResourceSpecifiedLimitType.CONNECTIONS_PER_HOUR);
                result.setConnPerHour(new NumberLiteralValue(each.NUMBER_().getText()).getValue().intValue());
            }
            if (null != each.MAX_USER_CONNECTIONS()) {
                result.setSpecifiedLimits(UserResourceSpecifiedLimitType.USER_CONNECTIONS);
                result.setUserConn(new NumberLiteralValue(each.NUMBER_().getText()).getValue().intValue());
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitCreateUserEntryNoOption(final CreateUserEntryNoOptionContext ctx) {
        UserSegment result = (UserSegment) visit(ctx.username());
        result.setStartIndex(ctx.start.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        return result;
    }
    
    @Override
    public ASTNode visitCreateUserEntryIdentifiedBy(final CreateUserEntryIdentifiedByContext ctx) {
        UserSegment result = (UserSegment) visit(ctx.username());
        result.setStartIndex(ctx.start.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        if (null != ctx.string_()) {
            result.setAuth(((StringLiteralValue) visit(ctx.string_())).getValue());
            result.setHasPasswordGenerator(false);
            result.setUsesIdentifiedByClause(true);
            result.setDiscardOldPassword(false);
        } else {
            result.setHasPasswordGenerator(true);
            result.setUsesIdentifiedByClause(true);
            result.setDiscardOldPassword(false);
            result.setUsesIdentifiedWithClause(false);
        }
        result.setRetainCurrentPassword(false);
        return result;
    }
    
    @Override
    public ASTNode visitCreateUserEntryIdentifiedWith(final CreateUserEntryIdentifiedWithContext ctx) {
        UserSegment result = (UserSegment) visit(ctx.username());
        result.setStartIndex(ctx.start.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        if (null != ctx.textOrIdentifier()) {
            result.setPlugin(ctx.textOrIdentifier().getText());
            result.setHasPasswordGenerator(false);
            result.setUsesIdentifiedByClause(true);
            result.setDiscardOldPassword(false);
            result.setRetainCurrentPassword(false);
        } else if (null != ctx.AS()) {
            result.setPlugin(ctx.textOrIdentifier().getText());
            result.setAuth(((StringLiteralValue) visit(ctx.string_())).getValue());
            result.setHasPasswordGenerator(false);
            result.setUsesIdentifiedByClause(true);
            result.setDiscardOldPassword(false);
            result.setRetainCurrentPassword(false);
        } else if (null != ctx.BY() && null != ctx.string_()) {
            result.setPlugin(ctx.textOrIdentifier().getText());
            result.setAuth(((StringLiteralValue) visit(ctx.string_())).getValue());
            result.setHasPasswordGenerator(false);
            result.setUsesIdentifiedByClause(true);
            result.setUsesIdentifiedWithClause(true);
            result.setDiscardOldPassword(false);
            result.setRetainCurrentPassword(false);
        } else {
            result.setPlugin(ctx.textOrIdentifier().getText());
            result.setAuth(((StringLiteralValue) visit(ctx.string_())).getValue());
            result.setHasPasswordGenerator(true);
            result.setUsesIdentifiedByClause(true);
            result.setUsesIdentifiedWithClause(true);
            result.setDiscardOldPassword(false);
            result.setRetainCurrentPassword(false);
        }
        return result;
    }
    
    @Override
    public ASTNode visitAccountLockPasswordExpireOptions(final AccountLockPasswordExpireOptionsContext ctx) {
        PasswordOrLockOptionSegment result = new PasswordOrLockOptionSegment();
        for (AccountLockPasswordExpireOptionContext each : ctx.accountLockPasswordExpireOption()) {
            fillAccountLockPasswordExpireOption(result, each);
        }
        return result;
    }
    
    private void fillAccountLockPasswordExpireOption(final PasswordOrLockOptionSegment segment, final AccountLockPasswordExpireOptionContext ctx) {
        if (null != ctx.ACCOUNT()) {
            fillAccountLock(segment, ctx);
        } else if (null != ctx.PASSWORD() && null != ctx.EXPIRE()) {
            fillPasswordExpire(segment, ctx);
        } else if (null != ctx.PASSWORD() && null != ctx.HISTORY()) {
            fillPasswordHistory(segment, ctx);
        } else if (null != ctx.PASSWORD() && null != ctx.REUSE()) {
            fillPasswordReuse(segment, ctx);
        } else if (null != ctx.PASSWORD() && null != ctx.REQUIRE()) {
            fillPasswordRequire(segment, ctx);
        } else if (null != ctx.FAILED_LOGIN_ATTEMPTS()) {
            segment.setUpdateFailedLoginAttempts(true);
            segment.setFailedLoginAttempts(new NumberLiteralValue(ctx.NUMBER_().getText()).getValue().intValue());
        } else {
            if (null == ctx.UNBOUNDED()) {
                segment.setUpdatePasswordLockTime(true);
                segment.setPasswordLockTime(new NumberLiteralValue(ctx.NUMBER_().getText()).getValue().intValue());
            } else {
                segment.setUpdatePasswordLockTime(true);
                segment.setPasswordLockTime(-1);
            }
        }
    }
    
    private void fillAccountLock(final PasswordOrLockOptionSegment segment, final AccountLockPasswordExpireOptionContext ctx) {
        if (null == ctx.LOCK()) {
            segment.setUpdateAccountLockedColumn(true);
            segment.setAccountLocked(false);
        } else {
            segment.setUpdateAccountLockedColumn(true);
            segment.setAccountLocked(true);
        }
    }
    
    private void fillPasswordExpire(final PasswordOrLockOptionSegment segment, final AccountLockPasswordExpireOptionContext ctx) {
        if (null != ctx.INTERVAL()) {
            segment.setExpireAfterDays(new NumberLiteralValue(ctx.NUMBER_().getText()).getValue().intValue());
            segment.setUpdatePasswordExpiredColumn(false);
            segment.setUpdatePasswordExpiredFields(true);
            segment.setUseDefaultPasswordLifeTime(false);
        } else if (null != ctx.NEVER()) {
            segment.setExpireAfterDays(0);
            segment.setUpdatePasswordExpiredColumn(false);
            segment.setUpdatePasswordExpiredFields(true);
            segment.setUseDefaultPasswordLifeTime(false);
        } else if (null != ctx.DEFAULT()) {
            segment.setExpireAfterDays(0);
            segment.setUpdatePasswordExpiredColumn(false);
            segment.setUpdatePasswordExpiredFields(true);
            segment.setUseDefaultPasswordLifeTime(true);
        } else {
            segment.setExpireAfterDays(0);
            segment.setUpdatePasswordExpiredColumn(true);
            segment.setUpdatePasswordExpiredFields(true);
            segment.setUseDefaultPasswordLifeTime(true);
        }
    }
    
    private void fillPasswordHistory(final PasswordOrLockOptionSegment segment, final AccountLockPasswordExpireOptionContext ctx) {
        if (null == ctx.DEFAULT()) {
            segment.setPasswordHistoryLength(new NumberLiteralValue(ctx.NUMBER_().getText()).getValue().intValue());
            segment.setUpdatePasswordHistory(true);
            segment.setUseDefaultPasswordHistory(false);
        } else {
            segment.setPasswordHistoryLength(0);
            segment.setUpdatePasswordHistory(true);
            segment.setUseDefaultPasswordHistory(true);
        }
    }
    
    private void fillPasswordReuse(final PasswordOrLockOptionSegment segment, final AccountLockPasswordExpireOptionContext ctx) {
        if (null == ctx.DEFAULT()) {
            segment.setPasswordReuseInterval(new NumberLiteralValue(ctx.NUMBER_().getText()).getValue().intValue());
            segment.setUpdatePasswordReuseInterval(true);
            segment.setUseDefaultPasswordReuseInterval(false);
        } else {
            segment.setPasswordReuseInterval(0);
            segment.setUpdatePasswordReuseInterval(true);
            segment.setUseDefaultPasswordReuseInterval(true);
        }
    }
    
    private void fillPasswordRequire(final PasswordOrLockOptionSegment segment, final AccountLockPasswordExpireOptionContext ctx) {
        if (null != ctx.DEFAULT()) {
            segment.setUpdatePasswordRequireCurrent(ACLAttributeType.DEFAULT);
        } else if (null != ctx.OPTIONAL()) {
            segment.setUpdatePasswordRequireCurrent(ACLAttributeType.NO);
        } else {
            segment.setUpdatePasswordRequireCurrent(ACLAttributeType.YES);
        }
    }
    
    @Override
    public ASTNode visitUsername(final UsernameContext ctx) {
        UserSegment result = new UserSegment();
        if (null != ctx.userIdentifierOrText()) {
            result.setUser(new IdentifierValue(ctx.userIdentifierOrText().textOrIdentifier(0).getText()).getValue());
            if (null != ctx.userIdentifierOrText().AT_()) {
                result.setHost(new IdentifierValue(ctx.userIdentifierOrText().textOrIdentifier(1).getText()).getValue());
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitDropUser(final DropUserContext ctx) {
        return new DropUserStatement(getDatabaseType(), ctx.username().stream().map(UsernameContext::getText).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitAlterUser(final AlterUserContext ctx) {
        return new AlterUserStatement(getDatabaseType(), null);
    }
    
    @Override
    public ASTNode visitRenameUser(final RenameUserContext ctx) {
        return new MySQLRenameUserStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCreateRole(final CreateRoleContext ctx) {
        return new CreateRoleStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropRole(final DropRoleContext ctx) {
        return new DropRoleStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitSetDefaultRole(final SetDefaultRoleContext ctx) {
        return new MySQLSetDefaultRoleStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitSetRole(final SetRoleContext ctx) {
        return new SetRoleStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitSetPassword(final SetPasswordContext ctx) {
        return new MySQLSetPasswordStatement(getDatabaseType());
    }
}
