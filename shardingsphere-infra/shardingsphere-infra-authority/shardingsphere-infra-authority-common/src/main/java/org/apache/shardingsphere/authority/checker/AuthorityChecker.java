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

import org.apache.shardingsphere.authority.constant.AuthorityOrder;
import org.apache.shardingsphere.authority.model.PrivilegeType;
import org.apache.shardingsphere.authority.model.ShardingSpherePrivileges;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.infra.executor.check.SQLCheckResult;
import org.apache.shardingsphere.infra.executor.check.SQLChecker;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowDatabasesStatement;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;

/**
 * Authority checker.
 */
public final class AuthorityChecker implements SQLChecker<AuthorityRule> {
    
    @Override
    public boolean check(final String schemaName, final Grantee grantee, final AuthorityRule authorityRule) {
        if (null == grantee) {
            return true;
        }
        return authorityRule.findPrivileges(grantee).map(optional -> optional.hasPrivileges(schemaName)).orElse(false);
    }
    
    @Override
    public SQLCheckResult check(final SQLStatement sqlStatement, final List<Object> parameters, final Grantee grantee, 
                                final String currentSchema, final Map<String, ShardingSphereMetaData> metaDataMap, final AuthorityRule authorityRule) {
        if (null == grantee) {
            return new SQLCheckResult(true, "");
        }
        Optional<ShardingSpherePrivileges> privileges = authorityRule.findPrivileges(grantee);
        // TODO add error msg
        return privileges.map(optional -> new SQLCheckResult(optional.hasPrivileges(Collections.singletonList(getPrivilege(sqlStatement))), "")).orElseGet(() -> new SQLCheckResult(false, ""));
    }
    
    @Override
    public boolean check(final Grantee grantee, final AuthorityRule authorityRule) {
        return authorityRule.findUser(grantee).isPresent();
    }
    
    @Override
    public boolean check(final Grantee grantee, final BiPredicate<Object, Object> validator, final Object cipher, final AuthorityRule authorityRule) {
        Optional<ShardingSphereUser> user = authorityRule.findUser(grantee);
        if (user.isPresent()) {
            return validator.test(user.get(), cipher);
        }
        return false;
    }
    
    private PrivilegeType getPrivilege(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof MySQLShowDatabasesStatement) {
            return PrivilegeType.SHOW_DB;
        }
        // TODO add more Privilege and SQL statement mapping
        return null;
    }
    
    @Override
    public int getOrder() {
        return AuthorityOrder.ORDER;
    }
    
    @Override
    public Class<AuthorityRule> getTypeClass() {
        return AuthorityRule.class;
    }
}
