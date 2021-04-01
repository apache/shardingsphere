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

package org.apache.shardingsphere.infra.check.auth;

import org.apache.shardingsphere.infra.check.SQLCheckResult;
import org.apache.shardingsphere.infra.check.SQLChecker;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.auth.AuthenticationContext;
import org.apache.shardingsphere.infra.metadata.auth.model.privilege.PrivilegeType;
import org.apache.shardingsphere.infra.metadata.auth.model.privilege.ShardingSpherePrivilege;
import org.apache.shardingsphere.infra.metadata.auth.model.user.Grantee;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowDatabasesStatement;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Authentication SQL checker.
 */
public final class AuthenticationSQLChecker implements SQLChecker {
    
    private static final int ORDER = 0;
    
    private static final String CHECK_TYPE = "AUTHENTICATION";
    
    @Override
    public boolean check(final String schemaName, final Grantee grantee) {
        if (null == grantee) {
            return true;
        }
        return AuthenticationContext.getInstance().getAuthentication().findPrivilege(grantee).map(optional -> optional.hasPrivileges(schemaName)).orElse(false);
    }
    
    @Override
    public SQLCheckResult check(final SQLStatement sqlStatement, final List<Object> parameters, final ShardingSphereMetaData metaData, final Grantee grantee) {
        if (null == grantee) {
            return new SQLCheckResult(true, "");
        }
        Optional<ShardingSpherePrivilege> privilege = AuthenticationContext.getInstance().getAuthentication().findPrivilege(grantee);
        // TODO add error msg
        return privilege.map(optional -> new SQLCheckResult(optional.hasPrivileges(Collections.singletonList(getPrivilege(sqlStatement))), "")).orElseGet(() -> new SQLCheckResult(false, ""));
    }
    
    private PrivilegeType getPrivilege(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof MySQLShowDatabasesStatement) {
            return PrivilegeType.SHOW_DB;
        }
        // TODO add more Privilege and SQL statement mapping
        return null;
    }
    
    @Override
    public String getSQLCheckType() {
        return CHECK_TYPE;
    }
    
    @Override
    public int getOrder() {
        return ORDER;
    }
    
    @Override
    public Class getTypeClass() {
        return null;
    }
}
