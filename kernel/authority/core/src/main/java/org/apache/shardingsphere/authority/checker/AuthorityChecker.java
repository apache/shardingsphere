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

package org.apache.shardingsphere.authority.checker;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.authority.model.PrivilegeType;
import org.apache.shardingsphere.authority.model.ShardingSpherePrivileges;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.executor.check.exception.SQLCheckException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowDatabasesStatement;

import java.util.Collections;
import java.util.Optional;
import java.util.function.BiPredicate;

/**
 * Authority checker.
 */
@RequiredArgsConstructor
public final class AuthorityChecker {
    
    private final AuthorityRule rule;
    
    private final Grantee grantee;
    
    /**
     * Check database authority.
     * 
     * @param databaseName database name
     * @return authorized or not
     */
    public boolean isAuthorized(final String databaseName) {
        return null == grantee || rule.findPrivileges(grantee).map(optional -> optional.hasPrivileges(databaseName)).orElse(false);
    }
    
    /**
     * Check authority with cipher.
     * 
     * @param validator validator
     * @param cipher cipher
     * @return authorized or not
     */
    public boolean isAuthorized(final BiPredicate<Object, Object> validator, final Object cipher) {
        return rule.findUser(grantee).filter(optional -> validator.test(optional, cipher)).isPresent();
    }
    
    /**
     * Check SQL authority.
     *
     * @param sqlStatementContext SQL statement context
     * @param database current database
     */
    public void isAuthorized(final SQLStatementContext<?> sqlStatementContext, final ShardingSphereDatabase database) {
        if (null == grantee) {
            return;
        }
        Optional<ShardingSpherePrivileges> privileges = rule.findPrivileges(grantee);
        ShardingSpherePreconditions.checkState(privileges.isPresent(), () -> new SQLCheckException(String.format("Access denied for user '%s'@'%s'", grantee.getUsername(), grantee.getHostname())));
        ShardingSpherePreconditions.checkState(null == database || privileges.filter(optional -> optional.hasPrivileges(database.getName())).isPresent(),
                () -> new SQLCheckException(String.format("Unknown database '%s'", null == database ? null : database.getName())));
        PrivilegeType privilegeType = getPrivilege(sqlStatementContext.getSqlStatement());
        boolean hasPrivileges = privileges.get().hasPrivileges(Collections.singleton(privilegeType));
        ShardingSpherePreconditions.checkState(hasPrivileges, () -> new SQLCheckException(String.format("Access denied for operation %s", null == privilegeType ? "" : privilegeType.name())));
    }
    
    private PrivilegeType getPrivilege(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof MySQLShowDatabasesStatement) {
            return PrivilegeType.SHOW_DB;
        }
        if (sqlStatement instanceof DMLStatement) {
            return getDMLPrivilege(sqlStatement);
        }
        if (sqlStatement instanceof DDLStatement) {
            return getDDLPrivilege(sqlStatement);
        }
        // TODO add more Privilege and SQL statement mapping
        return null;
    }
    
    private PrivilegeType getDMLPrivilege(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof SelectStatement) {
            return PrivilegeType.SELECT;
        }
        if (sqlStatement instanceof InsertStatement) {
            return PrivilegeType.INSERT;
        }
        if (sqlStatement instanceof UpdateStatement) {
            return PrivilegeType.UPDATE;
        }
        if (sqlStatement instanceof DeleteStatement) {
            return PrivilegeType.DELETE;
        }
        return null;
    }
    
    private PrivilegeType getDDLPrivilege(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof AlterDatabaseStatement) {
            return PrivilegeType.ALTER_ANY_DATABASE;
        }
        if (sqlStatement instanceof AlterTableStatement) {
            return PrivilegeType.ALTER;
        }
        if (sqlStatement instanceof CreateDatabaseStatement) {
            return PrivilegeType.CREATE_DATABASE;
        }
        if (sqlStatement instanceof CreateTableStatement) {
            return PrivilegeType.CREATE_TABLE;
        }
        if (sqlStatement instanceof CreateFunctionStatement) {
            return PrivilegeType.CREATE_FUNCTION;
        }
        if (sqlStatement instanceof DropTableStatement || sqlStatement instanceof DropDatabaseStatement) {
            return PrivilegeType.DROP;
        }
        if (sqlStatement instanceof TruncateStatement) {
            return PrivilegeType.TRUNCATE;
        }
        return null;
    }
}
