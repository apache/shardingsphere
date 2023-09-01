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
import org.apache.shardingsphere.authority.exception.UnauthorizedOperationException;
import org.apache.shardingsphere.authority.model.PrivilegeType;
import org.apache.shardingsphere.authority.model.ShardingSpherePrivileges;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Collections;
import java.util.Optional;

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
     * Check privileges.
     *
     * @param databaseName database name
     * @param sqlStatement SQL statement
     */
    public void checkPrivileges(final String databaseName, final SQLStatement sqlStatement) {
        if (null == grantee) {
            return;
        }
        Optional<ShardingSpherePrivileges> privileges = rule.findPrivileges(grantee);
        ShardingSpherePreconditions.checkState(null == databaseName || privileges.filter(optional -> optional.hasPrivileges(databaseName)).isPresent(),
                () -> new UnknownDatabaseException(databaseName));
        PrivilegeType privilegeType = PrivilegeTypeMapper.getPrivilegeType(sqlStatement);
        ShardingSpherePreconditions.checkState(privileges.isPresent() && privileges.get().hasPrivileges(Collections.singleton(privilegeType)),
                () -> new UnauthorizedOperationException(null == privilegeType ? "" : privilegeType.name()));
    }
}
