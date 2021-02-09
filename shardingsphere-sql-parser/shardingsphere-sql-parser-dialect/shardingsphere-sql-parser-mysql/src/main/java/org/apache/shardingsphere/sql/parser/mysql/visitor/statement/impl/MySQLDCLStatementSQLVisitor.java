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
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterUserContext;
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
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.GrantContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.PrivilegeClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RenameUserContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RequireClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RevokeContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RoleNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SetDefaultRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SetPasswordContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SetRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TlsOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.UserNameContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.NumberLiteralValue;
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
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.segment.PasswordOrLockOptionSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.segment.SSLTypeEnum;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.segment.TLSOptionSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.segment.UserResourceSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.segment.UserResourceSpecifiedLimitEnum;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.segment.UserSegment;

import java.util.Optional;
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
            result.setAuth(ctx.string_().getText());
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
            result.setAuth(ctx.string_().getText());
            result.setHasPasswordGenerator(false);
            result.setUsesIdentifiedByClause(true);
            result.setDiscardOldPassword(false);
            result.setRetainCurrentPassword(false);
        } else if (null != ctx.BY() && null != ctx.string_()) {
            result.setPlugin(ctx.textOrIdentifier().getText());
            result.setAuth(ctx.string_().getText());
            result.setHasPasswordGenerator(false);
            result.setUsesIdentifiedByClause(true);
            result.setUsesIdentifiedWithClause(true);
            result.setDiscardOldPassword(false);
            result.setRetainCurrentPassword(false);
        } else {
            result.setPlugin(ctx.textOrIdentifier().getText());
            result.setAuth(ctx.string_().getText());
            result.setHasPasswordGenerator(true);
            result.setUsesIdentifiedByClause(true);
            result.setUsesIdentifiedWithClause(true);
            result.setDiscardOldPassword(false);
            result.setRetainCurrentPassword(false);
        }
        return result;
    }
    
    @Override
    public ASTNode visitAccountLockPasswordExpireOption(final AccountLockPasswordExpireOptionContext ctx) {
        PasswordOrLockOptionSegment result = new PasswordOrLockOptionSegment();
        if (null != ctx.ACCOUNT()) {
            if (null != ctx.LOCK()) {
                result.setUpdateAccountLockedColumn(true);
                result.setAccountLocked(true);
            } else {
                result.setUpdateAccountLockedColumn(true);
                result.setAccountLocked(false);
            }
        } else if (null != ctx.PASSWORD() && null != ctx.EXPIRE()) {
            if (null != ctx.INTERVAL()) {
                result.setExpireAfterDays(new NumberLiteralValue(ctx.NUMBER_().getText()).getValue().intValue());
                result.setUpdatePasswordExpiredColumn(false);
                result.setUpdatePasswordExpiredFields(true);
                result.setUseDefaultPasswordLifeTime(false);
            } else if (null != ctx.NEVER()) {
                result.setExpireAfterDays(0);
                result.setUpdatePasswordExpiredColumn(false);
                result.setUpdatePasswordExpiredFields(true);
                result.setUseDefaultPasswordLifeTime(false);
            } else if (null != ctx.DEFAULT()) {
                result.setExpireAfterDays(0);
                result.setUpdatePasswordExpiredColumn(false);
                result.setUpdatePasswordExpiredFields(true);
                result.setUseDefaultPasswordLifeTime(true);
            } else {
                result.setExpireAfterDays(0);
                result.setUpdatePasswordExpiredColumn(true);
                result.setUpdatePasswordExpiredFields(true);
                result.setUseDefaultPasswordLifeTime(true);
            }
        } else if (null != ctx.PASSWORD() && null != ctx.HISTORY()) {
            if (null != ctx.DEFAULT()) {
                result.setPasswordHistoryLength(0);
                result.setUpdatePasswordHistory(true);
                result.setUseDefaultPasswordHistory(true);
            } else {
                result.setPasswordHistoryLength(new NumberLiteralValue(ctx.NUMBER_().getText()).getValue().intValue());
                result.setUpdatePasswordHistory(true);
                result.setUseDefaultPasswordHistory(false);
            }
        } else if (null != ctx.PASSWORD() && null != ctx.REUSE()) {
            if (null != ctx.DEFAULT()) {
                result.setPasswordReuseInterval(0);
                result.setUpdatePasswordReuseInterval(true);
                result.setUseDefaultPasswordReuseInterval(true);
            } else {
                result.setPasswordReuseInterval(new NumberLiteralValue(ctx.NUMBER_().getText()).getValue().intValue());
                result.setUpdatePasswordReuseInterval(true);
                result.setUseDefaultPasswordReuseInterval(false);
            }
        } else if (null != ctx.PASSWORD() && null != ctx.REQUIRE()) {
            if (null != ctx.DEFAULT()) {
                result.setUpdatePasswordRequireCurrent(ACLAttributeEnum.DEFAULT);
            } else if (null != ctx.OPTIONAL()) {
                result.setUpdatePasswordRequireCurrent(ACLAttributeEnum.NO);
            } else {
                result.setUpdatePasswordRequireCurrent(ACLAttributeEnum.YES);
            }
        } else if (null != ctx.FAILED_LOGIN_ATTEMPTS()) {
            result.setUpdateFailedLoginAttempts(true);
            result.setFailedLoginAttempts(new NumberLiteralValue(ctx.NUMBER_().getText()).getValue().intValue());
        } else {
            if (null != ctx.UNBOUNDED()) {
                result.setUpdatePasswordLockTime(true);
                result.setPasswordLockTime(-1);
            } else {
                result.setUpdatePasswordLockTime(true);
                result.setPasswordLockTime(new NumberLiteralValue(ctx.NUMBER_().getText()).getValue().intValue());
            }
        }
        return result;
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
