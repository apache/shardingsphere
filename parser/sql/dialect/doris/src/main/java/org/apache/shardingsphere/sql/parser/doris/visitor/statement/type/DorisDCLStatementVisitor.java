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

package org.apache.shardingsphere.sql.parser.doris.visitor.statement.type;

import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.AccountLockPasswordExpireOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.AccountLockPasswordExpireOptionsContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.AlterUserContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ColumnNameContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ConnectOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ConnectOptionsContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.CreateRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.CreateUserContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.CreateUserEntryContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.CreateUserEntryIdentifiedByContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.CreateUserEntryIdentifiedWithContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.CreateUserEntryNoOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.DropRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.DropUserContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.GrantIdentifierContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.GrantLevelGlobalContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.GrantLevelSchemaGlobalContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.GrantLevelTableContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.GrantProxyContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.GrantRoleOrPrivilegeOnToContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.GrantRoleOrPrivilegeToContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.RenameUserContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.RequireClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.RevokeFromContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.RevokeOnFromContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.RoleAtHostContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.RoleNameContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.RoleOrDynamicPrivilegeContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.RoleOrPrivilegeContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.RoleOrPrivilegesContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.SetDefaultRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.SetPasswordContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.SetRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.StaticPrivilegeAlterContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.StaticPrivilegeAlterRoutineContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.StaticPrivilegeCreateContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.StaticPrivilegeCreateRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.StaticPrivilegeCreateRoutineContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.StaticPrivilegeCreateTablespaceContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.StaticPrivilegeCreateTemporaryTablesContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.StaticPrivilegeCreateUserContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.StaticPrivilegeCreateViewContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.StaticPrivilegeDeleteContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.StaticPrivilegeDropContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.StaticPrivilegeDropRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.StaticPrivilegeEventContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.StaticPrivilegeExecuteContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.StaticPrivilegeFileContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.StaticPrivilegeGrantContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.StaticPrivilegeIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.StaticPrivilegeInsertContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.StaticPrivilegeLockTablesContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.StaticPrivilegeProcessContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.StaticPrivilegeReferencesContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.StaticPrivilegeReloadContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.StaticPrivilegeReplicationClientContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.StaticPrivilegeReplicationSlaveContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.StaticPrivilegeSelectContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.StaticPrivilegeShowDatabasesContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.StaticPrivilegeShowViewContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.StaticPrivilegeShutdownContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.StaticPrivilegeSuperContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.StaticPrivilegeTriggerContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.StaticPrivilegeUpdateContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.StaticPrivilegeUsageContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.TlsOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.UsernameContext;
import org.apache.shardingsphere.sql.parser.doris.visitor.statement.DorisStatementVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dcl.UserSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.GrantLevelSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.NumberLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.StringLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.doris.dcl.DorisAlterUserStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.doris.dcl.DorisCreateRoleStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.doris.dcl.DorisCreateUserStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.doris.dcl.DorisDropRoleStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.doris.dcl.DorisDropUserStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.doris.dcl.DorisGrantStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.doris.dcl.DorisRenameUserStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.doris.dcl.DorisRevokeStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.doris.dcl.DorisSetDefaultRoleStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.doris.dcl.DorisSetPasswordStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.doris.dcl.DorisSetRoleStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.doris.segment.ACLAttributeEnum;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.doris.segment.DorisPrivilegeSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.doris.segment.DorisRoleOrPrivilegeSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.doris.segment.PasswordOrLockOptionSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.doris.segment.SSLTypeEnum;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.doris.segment.TLSOptionSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.doris.segment.UserResourceSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.doris.segment.UserResourceSpecifiedLimitEnum;

import java.util.stream.Collectors;

/**
 * DCL statement visitor for Doris.
 */
public final class DorisDCLStatementVisitor extends DorisStatementVisitor implements DCLStatementVisitor {
    
    @Override
    public ASTNode visitGrantRoleOrPrivilegeTo(final GrantRoleOrPrivilegeToContext ctx) {
        DorisGrantStatement result = new DorisGrantStatement();
        fillRoleOrPrivileges(result, ctx.roleOrPrivileges());
        for (UsernameContext each : ctx.userList().username()) {
            result.getUsers().add((UserSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitGrantRoleOrPrivilegeOnTo(final GrantRoleOrPrivilegeOnToContext ctx) {
        DorisGrantStatement result = new DorisGrantStatement();
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
        DorisGrantStatement result = new DorisGrantStatement();
        DorisPrivilegeSegment privilege = new DorisPrivilegeSegment(ctx.PROXY().getSymbol().getStartIndex(), ctx.PROXY().getSymbol().getStopIndex(), "GRANT");
        result.getRoleOrPrivileges().add(new DorisRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege));
        for (UsernameContext each : ctx.userList().username()) {
            result.getUsers().add((UserSegment) visit(each));
        }
        return result;
    }
    
    private void fillRoleOrPrivileges(final DorisGrantStatement statement, final RoleOrPrivilegesContext ctx) {
        for (RoleOrPrivilegeContext each : ctx.roleOrPrivilege()) {
            statement.getRoleOrPrivileges().add((DorisRoleOrPrivilegeSegment) visit(each));
        }
    }
    
    private void fillRoleOrPrivileges(final DorisRevokeStatement statement, final RoleOrPrivilegesContext ctx) {
        for (RoleOrPrivilegeContext each : ctx.roleOrPrivilege()) {
            statement.getRoleOrPrivileges().add((DorisRoleOrPrivilegeSegment) visit(each));
        }
    }
    
    private GrantLevelSegment generateGrantLevel(final GrantIdentifierContext ctx) {
        if (ctx instanceof GrantLevelGlobalContext) {
            return new GrantLevelSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "*", "*");
            
        }
        if (ctx instanceof GrantLevelSchemaGlobalContext) {
            String schemaName = new IdentifierValue(((GrantLevelSchemaGlobalContext) ctx).schemaName().getText()).getValue();
            return new GrantLevelSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), schemaName, "*");
        }
        String schemaName = null;
        String tableName;
        if (null != ((GrantLevelTableContext) ctx).tableName().owner()) {
            schemaName = new IdentifierValue(((GrantLevelTableContext) ctx).tableName().owner().getText()).getValue();
        }
        tableName = new IdentifierValue(((GrantLevelTableContext) ctx).tableName().name().getText()).getValue();
        return new GrantLevelSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), schemaName, tableName);
    }
    
    @Override
    public ASTNode visitRoleOrDynamicPrivilege(final RoleOrDynamicPrivilegeContext ctx) {
        String role = new IdentifierValue(ctx.roleIdentifierOrText().getText()).getValue();
        return new DorisRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), role, null, null);
    }
    
    @Override
    public ASTNode visitRoleAtHost(final RoleAtHostContext ctx) {
        String role = new IdentifierValue(ctx.roleIdentifierOrText().getText()).getValue();
        String host = new IdentifierValue(ctx.textOrIdentifier().getText()).getValue();
        return new DorisRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), role, host, null);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeSelect(final StaticPrivilegeSelectContext ctx) {
        DorisPrivilegeSegment privilege = new DorisPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "SELECT");
        if (null != ctx.columnNames()) {
            for (ColumnNameContext each : ctx.columnNames().columnName()) {
                privilege.getColumns().add(new IdentifierValue(each.getText()).getValue());
            }
        }
        return new DorisRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeInsert(final StaticPrivilegeInsertContext ctx) {
        DorisPrivilegeSegment privilege = new DorisPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "INSERT");
        if (null != ctx.columnNames()) {
            for (ColumnNameContext each : ctx.columnNames().columnName()) {
                privilege.getColumns().add(new IdentifierValue(each.getText()).getValue());
            }
        }
        return new DorisRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeUpdate(final StaticPrivilegeUpdateContext ctx) {
        DorisPrivilegeSegment privilege = new DorisPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "UPDATE");
        if (null != ctx.columnNames()) {
            for (ColumnNameContext each : ctx.columnNames().columnName()) {
                privilege.getColumns().add(new IdentifierValue(each.getText()).getValue());
            }
        }
        return new DorisRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeReferences(final StaticPrivilegeReferencesContext ctx) {
        DorisPrivilegeSegment privilege = new DorisPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "REFERENCES");
        if (null != ctx.columnNames()) {
            for (ColumnNameContext each : ctx.columnNames().columnName()) {
                privilege.getColumns().add(new IdentifierValue(each.getText()).getValue());
            }
        }
        return new DorisRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeDelete(final StaticPrivilegeDeleteContext ctx) {
        DorisPrivilegeSegment privilege = new DorisPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "DELETE");
        return new DorisRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeUsage(final StaticPrivilegeUsageContext ctx) {
        DorisPrivilegeSegment privilege = new DorisPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "USAGE");
        return new DorisRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeIndex(final StaticPrivilegeIndexContext ctx) {
        DorisPrivilegeSegment privilege = new DorisPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "INDEX");
        return new DorisRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeAlter(final StaticPrivilegeAlterContext ctx) {
        DorisPrivilegeSegment privilege = new DorisPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "ALTER");
        return new DorisRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeCreate(final StaticPrivilegeCreateContext ctx) {
        DorisPrivilegeSegment privilege = new DorisPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "CREATE");
        return new DorisRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeDrop(final StaticPrivilegeDropContext ctx) {
        DorisPrivilegeSegment privilege = new DorisPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "DROP");
        return new DorisRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeExecute(final StaticPrivilegeExecuteContext ctx) {
        DorisPrivilegeSegment privilege = new DorisPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "EXECUTE");
        return new DorisRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeReload(final StaticPrivilegeReloadContext ctx) {
        DorisPrivilegeSegment privilege = new DorisPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "RELOAD");
        return new DorisRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeShutdown(final StaticPrivilegeShutdownContext ctx) {
        DorisPrivilegeSegment privilege = new DorisPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "SHUTDOWN");
        return new DorisRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeProcess(final StaticPrivilegeProcessContext ctx) {
        DorisPrivilegeSegment privilege = new DorisPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "PROCESS");
        return new DorisRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeFile(final StaticPrivilegeFileContext ctx) {
        DorisPrivilegeSegment privilege = new DorisPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "FILE");
        return new DorisRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeGrant(final StaticPrivilegeGrantContext ctx) {
        DorisPrivilegeSegment privilege = new DorisPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "GRANT");
        return new DorisRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeShowDatabases(final StaticPrivilegeShowDatabasesContext ctx) {
        DorisPrivilegeSegment privilege = new DorisPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "SHOW_DB");
        return new DorisRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeSuper(final StaticPrivilegeSuperContext ctx) {
        DorisPrivilegeSegment privilege = new DorisPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "SUPER");
        return new DorisRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeCreateTemporaryTables(final StaticPrivilegeCreateTemporaryTablesContext ctx) {
        DorisPrivilegeSegment privilege = new DorisPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "CREATE_TMP");
        return new DorisRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeLockTables(final StaticPrivilegeLockTablesContext ctx) {
        DorisPrivilegeSegment privilege = new DorisPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "LOCK_TABLES");
        return new DorisRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeReplicationSlave(final StaticPrivilegeReplicationSlaveContext ctx) {
        DorisPrivilegeSegment privilege = new DorisPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "REPL_SLAVE");
        return new DorisRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeReplicationClient(final StaticPrivilegeReplicationClientContext ctx) {
        DorisPrivilegeSegment privilege = new DorisPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "REPL_CLIENT");
        return new DorisRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeCreateView(final StaticPrivilegeCreateViewContext ctx) {
        DorisPrivilegeSegment privilege = new DorisPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "CREATE_VIEW");
        return new DorisRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeShowView(final StaticPrivilegeShowViewContext ctx) {
        DorisPrivilegeSegment privilege = new DorisPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "SHOW_VIEW");
        return new DorisRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeCreateRoutine(final StaticPrivilegeCreateRoutineContext ctx) {
        DorisPrivilegeSegment privilege = new DorisPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "CREATE_PROC");
        return new DorisRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeAlterRoutine(final StaticPrivilegeAlterRoutineContext ctx) {
        DorisPrivilegeSegment privilege = new DorisPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "ALTER_PROC");
        return new DorisRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeCreateUser(final StaticPrivilegeCreateUserContext ctx) {
        DorisPrivilegeSegment privilege = new DorisPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "CREATE_USER");
        return new DorisRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeEvent(final StaticPrivilegeEventContext ctx) {
        DorisPrivilegeSegment privilege = new DorisPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "EVENT");
        return new DorisRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeTrigger(final StaticPrivilegeTriggerContext ctx) {
        DorisPrivilegeSegment privilege = new DorisPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "TRIGGER");
        return new DorisRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeCreateTablespace(final StaticPrivilegeCreateTablespaceContext ctx) {
        DorisPrivilegeSegment privilege = new DorisPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "CREATE_TABLESPACE");
        return new DorisRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeCreateRole(final StaticPrivilegeCreateRoleContext ctx) {
        DorisPrivilegeSegment privilege = new DorisPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "CREATE_ROLE");
        return new DorisRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeDropRole(final StaticPrivilegeDropRoleContext ctx) {
        DorisPrivilegeSegment privilege = new DorisPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "DROP_ROLE");
        return new DorisRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitRevokeFrom(final RevokeFromContext ctx) {
        DorisRevokeStatement result = new DorisRevokeStatement();
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
        DorisRevokeStatement result = new DorisRevokeStatement();
        if (null != ctx.roleOrPrivileges()) {
            fillRoleOrPrivileges(result, ctx.roleOrPrivileges());
        } else if (null != ctx.ALL()) {
            result.setAllPrivileges(true);
        } else if (null != ctx.PROXY()) {
            DorisPrivilegeSegment privilege = new DorisPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "DROP_ROLE");
            result.getRoleOrPrivileges().add(new DorisRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege));
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
        DorisCreateUserStatement result = new DorisCreateUserStatement();
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
            result.setType(SSLTypeEnum.SSL_TYPE_NONE);
        } else if (null != ctx.X509()) {
            result.setType(SSLTypeEnum.SSL_TYPE_X509);
        } else if (null != ctx.SSL()) {
            result.setType(SSLTypeEnum.SSL_TYPE_ANY);
        } else {
            result.setType(SSLTypeEnum.SSL_TYPE_SPECIFIED);
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
                result.setSpecifiedLimits(UserResourceSpecifiedLimitEnum.QUERIES_PER_HOUR);
                result.setQuestions(new NumberLiteralValue(each.NUMBER_().getText()).getValue().intValue());
            }
            if (null != each.MAX_UPDATES_PER_HOUR()) {
                result.setSpecifiedLimits(UserResourceSpecifiedLimitEnum.UPDATES_PER_HOUR);
                result.setUpdates(new NumberLiteralValue(each.NUMBER_().getText()).getValue().intValue());
            }
            if (null != each.MAX_CONNECTIONS_PER_HOUR()) {
                result.setSpecifiedLimits(UserResourceSpecifiedLimitEnum.CONNECTIONS_PER_HOUR);
                result.setConnPerHour(new NumberLiteralValue(each.NUMBER_().getText()).getValue().intValue());
            }
            if (null != each.MAX_USER_CONNECTIONS()) {
                result.setSpecifiedLimits(UserResourceSpecifiedLimitEnum.USER_CONNECTIONS);
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
            segment.setUpdatePasswordRequireCurrent(ACLAttributeEnum.DEFAULT);
        } else if (null != ctx.OPTIONAL()) {
            segment.setUpdatePasswordRequireCurrent(ACLAttributeEnum.NO);
        } else {
            segment.setUpdatePasswordRequireCurrent(ACLAttributeEnum.YES);
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
        DorisDropUserStatement result = new DorisDropUserStatement();
        result.getUsers().addAll(ctx.username().stream().map(UsernameContext::getText).collect(Collectors.toList()));
        return result;
    }
    
    @Override
    public ASTNode visitAlterUser(final AlterUserContext ctx) {
        return new DorisAlterUserStatement();
    }
    
    @Override
    public ASTNode visitRenameUser(final RenameUserContext ctx) {
        return new DorisRenameUserStatement();
    }
    
    @Override
    public ASTNode visitCreateRole(final CreateRoleContext ctx) {
        return new DorisCreateRoleStatement();
    }
    
    @Override
    public ASTNode visitDropRole(final DropRoleContext ctx) {
        return new DorisDropRoleStatement();
    }
    
    @Override
    public ASTNode visitSetDefaultRole(final SetDefaultRoleContext ctx) {
        return new DorisSetDefaultRoleStatement();
    }
    
    @Override
    public ASTNode visitSetRole(final SetRoleContext ctx) {
        return new DorisSetRoleStatement();
    }
    
    @Override
    public ASTNode visitSetPassword(final SetPasswordContext ctx) {
        return new DorisSetPasswordStatement();
    }
}
