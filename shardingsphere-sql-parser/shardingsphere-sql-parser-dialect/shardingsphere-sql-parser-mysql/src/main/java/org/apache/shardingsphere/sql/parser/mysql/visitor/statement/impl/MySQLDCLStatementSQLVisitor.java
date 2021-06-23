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

import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.operation.SQLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.type.DCLSQLVisitor;
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
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.GrantLevelGlobalContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.GrantLevelSchemaGlobalContext;
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
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.String_Context;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TlsOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.UserNameContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.ACLTypeEnum;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.GrantLevelSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.PrivilegeTypeEnum;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.NumberLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.StringLiteralValue;
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
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.segment.ACLAttributeEnum;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.segment.MySQLPrivilegeSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.segment.MySQLRoleOrPrivilegeSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.segment.PasswordOrLockOptionSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.segment.SSLTypeEnum;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.segment.TLSOptionSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.segment.UserResourceSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.segment.UserResourceSpecifiedLimitEnum;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.segment.UserSegment;

import java.util.Properties;

/**
 * DCL Statement SQL visitor for MySQL.
 */
@NoArgsConstructor
public final class MySQLDCLStatementSQLVisitor extends MySQLStatementSQLVisitor implements DCLSQLVisitor, SQLStatementVisitor {
    
    public MySQLDCLStatementSQLVisitor(final Properties props) {
        super(props);
    }
    
    @Override
    public ASTNode visitGrantRoleOrPrivilegeTo(final GrantRoleOrPrivilegeToContext ctx) {
        MySQLGrantStatement result = new MySQLGrantStatement();
        fillRoleOrPrivileges(result, ctx.roleOrPrivileges());
        for (UserNameContext each : ctx.userList().userName()) {
            result.getUsers().add((UserSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitGrantRoleOrPrivilegeOnTo(final GrantRoleOrPrivilegeOnToContext ctx) {
        MySQLGrantStatement result = new MySQLGrantStatement();
        if (null != ctx.roleOrPrivileges()) {
            fillRoleOrPrivileges(result, ctx.roleOrPrivileges());
        } else {
            result.setAllPrivileges(true);
        }
        result.setLevel(generateGrantLevel(ctx.grantIdentifier()));
        for (UserNameContext each : ctx.userList().userName()) {
            result.getUsers().add((UserSegment) visit(each));
        }
        if (null != ctx.aclType()) {
            switch (ctx.aclType().getText().toLowerCase()) {
                case "table":
                    result.setAclType(ACLTypeEnum.TABLE);
                    break;
                case "function":
                    result.setAclType(ACLTypeEnum.FUNCTION);
                    break;
                case "procedure":
                    result.setAclType(ACLTypeEnum.PROCEDURE);
                    break;
                default:
                    result.setAclType(ACLTypeEnum.TABLE);
                    break;
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitGrantProxy(final GrantProxyContext ctx) {
        MySQLGrantStatement result = new MySQLGrantStatement();
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.PROXY().getSymbol().getStartIndex(), ctx.PROXY().getSymbol().getStopIndex(), PrivilegeTypeEnum.GRANT_ACL);
        result.getRoleOrPrivileges().add(new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege));
        for (UserNameContext each : ctx.userList().userName()) {
            result.getUsers().add((UserSegment) visit(each));
        }
        return result;
    }
    
    private void fillRoleOrPrivileges(final MySQLGrantStatement statement, final RoleOrPrivilegesContext ctx) {
        for (RoleOrPrivilegeContext each : ctx.roleOrPrivilege()) {
            statement.getRoleOrPrivileges().add((MySQLRoleOrPrivilegeSegment) visit(each));
        }
    }
    
    private void fillRoleOrPrivileges(final MySQLRevokeStatement statement, final RoleOrPrivilegesContext ctx) {
        for (RoleOrPrivilegeContext each : ctx.roleOrPrivilege()) {
            statement.getRoleOrPrivileges().add((MySQLRoleOrPrivilegeSegment) visit(each));
        }
    }
    
    private GrantLevelSegment generateGrantLevel(final GrantIdentifierContext ctx) {
        if (ctx instanceof GrantLevelGlobalContext) {
            return new GrantLevelSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "*", "*");
    
        } else if (ctx instanceof GrantLevelSchemaGlobalContext) {
            String schemaName = new IdentifierValue(((GrantLevelSchemaGlobalContext) ctx).schemaName().getText()).getValue();
            return new GrantLevelSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), schemaName, "*");
        } else {
            String schemaName = null;
            String tableName;
            if (null != ((GrantLevelTableContext) ctx).tableName().owner()) {
                schemaName = new IdentifierValue(((GrantLevelTableContext) ctx).tableName().owner().getText()).getValue();
            }
            tableName = new IdentifierValue(((GrantLevelTableContext) ctx).tableName().name().getText()).getValue();
            return new GrantLevelSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), schemaName, tableName);
        }
    }
    
    @Override
    public ASTNode visitRoleOrDynamicPrivilege(final RoleOrDynamicPrivilegeContext ctx) {
        String role = new IdentifierValue(ctx.roleIdentifierOrText().getText()).getValue();
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), role, null, null);
    }
    
    @Override
    public ASTNode visitRoleAtHost(final RoleAtHostContext ctx) {
        String role = new IdentifierValue(ctx.roleIdentifierOrText().getText()).getValue();
        String host = new IdentifierValue(ctx.textOrIdentifier().getText()).getValue();
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), role, host, null);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeSelect(final StaticPrivilegeSelectContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.SELECT_ACL);
        if (null != ctx.columnNames()) {
            for (ColumnNameContext each : ctx.columnNames().columnName()) {
                privilege.getColumns().add(new IdentifierValue(each.getText()).getValue());
            }
        }
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeInsert(final StaticPrivilegeInsertContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.INSERT_ACL);
        if (null != ctx.columnNames()) {
            for (ColumnNameContext each : ctx.columnNames().columnName()) {
                privilege.getColumns().add(new IdentifierValue(each.getText()).getValue());
            }
        }
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeUpdate(final StaticPrivilegeUpdateContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.UPDATE_ACL);
        if (null != ctx.columnNames()) {
            for (ColumnNameContext each : ctx.columnNames().columnName()) {
                privilege.getColumns().add(new IdentifierValue(each.getText()).getValue());
            }
        }
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeReferences(final StaticPrivilegeReferencesContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.REFERENCES_ACL);
        if (null != ctx.columnNames()) {
            for (ColumnNameContext each : ctx.columnNames().columnName()) {
                privilege.getColumns().add(new IdentifierValue(each.getText()).getValue());
            }
        }
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeDelete(final StaticPrivilegeDeleteContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.DELETE_ACL);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeUsage(final StaticPrivilegeUsageContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.USAGE_ACL);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeIndex(final StaticPrivilegeIndexContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.INDEX_ACL);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeAlter(final StaticPrivilegeAlterContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.ALTER_ACL);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeCreate(final StaticPrivilegeCreateContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.CREATE_ACL);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeDrop(final StaticPrivilegeDropContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.DROP_ACL);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeExecute(final StaticPrivilegeExecuteContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.EXECUTE_ACL);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeReload(final StaticPrivilegeReloadContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.RELOAD_ACL);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeShutdown(final StaticPrivilegeShutdownContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.SHUTDOWN_ACL);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeProcess(final StaticPrivilegeProcessContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.PROCESS_ACL);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeFile(final StaticPrivilegeFileContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.FILE_ACL);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeGrant(final StaticPrivilegeGrantContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.GRANT_ACL);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeShowDatabases(final StaticPrivilegeShowDatabasesContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.SHOW_DB_ACL);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeSuper(final StaticPrivilegeSuperContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.SUPER_ACL);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeCreateTemporaryTables(final StaticPrivilegeCreateTemporaryTablesContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.CREATE_TMP_ACL);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeLockTables(final StaticPrivilegeLockTablesContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.LOCK_TABLES_ACL);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeReplicationSlave(final StaticPrivilegeReplicationSlaveContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.REPL_SLAVE_ACL);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeReplicationClient(final StaticPrivilegeReplicationClientContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.REPL_CLIENT_ACL);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeCreateView(final StaticPrivilegeCreateViewContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.CREATE_VIEW_ACL);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeShowView(final StaticPrivilegeShowViewContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.SHOW_VIEW_ACL);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeCreateRoutine(final StaticPrivilegeCreateRoutineContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.CREATE_PROC_ACL);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeAlterRoutine(final StaticPrivilegeAlterRoutineContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.ALTER_PROC_ACL);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeCreateUser(final StaticPrivilegeCreateUserContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.CREATE_USER_ACL);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeEvent(final StaticPrivilegeEventContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.EVENT_ACL);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeTrigger(final StaticPrivilegeTriggerContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.TRIGGER_ACL);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeCreateTablespace(final StaticPrivilegeCreateTablespaceContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.CREATE_TABLESPACE_ACL);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeCreateRole(final StaticPrivilegeCreateRoleContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.CREATE_ROLE_ACL);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitStaticPrivilegeDropRole(final StaticPrivilegeDropRoleContext ctx) {
        MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.DROP_ROLE_ACL);
        return new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege);
    }
    
    @Override
    public ASTNode visitRevokeFrom(final RevokeFromContext ctx) {
        MySQLRevokeStatement result = new MySQLRevokeStatement();
        if (null != ctx.roleOrPrivileges()) {
            fillRoleOrPrivileges(result, ctx.roleOrPrivileges());
        } else if (null != ctx.ALL()) {
            result.setAllPrivileges(true);
        }
        for (UserNameContext each : ctx.userList().userName()) {
            result.getFromUsers().add((UserSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitRevokeOnFrom(final RevokeOnFromContext ctx) {
        MySQLRevokeStatement result = new MySQLRevokeStatement();
        if (null != ctx.roleOrPrivileges()) {
            fillRoleOrPrivileges(result, ctx.roleOrPrivileges());
        } else if (null != ctx.ALL()) {
            result.setAllPrivileges(true);
        } else if (null != ctx.PROXY()) {
            MySQLPrivilegeSegment privilege = new MySQLPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), PrivilegeTypeEnum.DROP_ROLE_ACL);
            result.getRoleOrPrivileges().add(new MySQLRoleOrPrivilegeSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), null, null, privilege));
            result.setOnUser((UserSegment) visit(ctx.userName()));
        }
        if (null != ctx.grantIdentifier()) {
            result.setLevel(generateGrantLevel(ctx.grantIdentifier()));
        }
        for (UserNameContext each : ctx.userList().userName()) {
            result.getFromUsers().add((UserSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitCreateUser(final CreateUserContext ctx) {
        MySQLCreateUserStatement result = new MySQLCreateUserStatement();
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
        UserSegment result = (UserSegment) visit(ctx.userName());
        result.setStartIndex(ctx.start.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        return result;
    }
    
    @Override
    public ASTNode visitCreateUserEntryIdentifiedBy(final CreateUserEntryIdentifiedByContext ctx) {
        UserSegment result = (UserSegment) visit(ctx.userName());
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
    public ASTNode visitString_(final String_Context ctx) {
        return new StringLiteralValue(ctx.getText());
    }
    
    @Override
    public ASTNode visitCreateUserEntryIdentifiedWith(final CreateUserEntryIdentifiedWithContext ctx) {
        UserSegment result = (UserSegment) visit(ctx.userName());
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
            if (null != ctx.UNBOUNDED()) {
                segment.setUpdatePasswordLockTime(true);
                segment.setPasswordLockTime(-1);
            } else {
                segment.setUpdatePasswordLockTime(true);
                segment.setPasswordLockTime(new NumberLiteralValue(ctx.NUMBER_().getText()).getValue().intValue());
            }
        }
    }
    
    private void fillAccountLock(final PasswordOrLockOptionSegment segment, final AccountLockPasswordExpireOptionContext ctx) {
        if (null != ctx.LOCK()) {
            segment.setUpdateAccountLockedColumn(true);
            segment.setAccountLocked(true);
        } else {
            segment.setUpdateAccountLockedColumn(true);
            segment.setAccountLocked(false);
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
        if (null != ctx.DEFAULT()) {
            segment.setPasswordHistoryLength(0);
            segment.setUpdatePasswordHistory(true);
            segment.setUseDefaultPasswordHistory(true);
        } else {
            segment.setPasswordHistoryLength(new NumberLiteralValue(ctx.NUMBER_().getText()).getValue().intValue());
            segment.setUpdatePasswordHistory(true);
            segment.setUseDefaultPasswordHistory(false);
        }
    }
    
    private void fillPasswordReuse(final PasswordOrLockOptionSegment segment, final AccountLockPasswordExpireOptionContext ctx) {
        if (null != ctx.DEFAULT()) {
            segment.setPasswordReuseInterval(0);
            segment.setUpdatePasswordReuseInterval(true);
            segment.setUseDefaultPasswordReuseInterval(true);
        } else {
            segment.setPasswordReuseInterval(new NumberLiteralValue(ctx.NUMBER_().getText()).getValue().intValue());
            segment.setUpdatePasswordReuseInterval(true);
            segment.setUseDefaultPasswordReuseInterval(false);
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
    public ASTNode visitUserName(final UserNameContext ctx) {
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
