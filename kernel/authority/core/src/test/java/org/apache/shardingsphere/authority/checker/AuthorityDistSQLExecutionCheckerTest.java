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

import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.database.exception.core.exception.connection.AccessDeniedException;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.QueryableRALStatement;
import org.apache.shardingsphere.distsql.statement.type.rdl.RDLStatement;
import org.apache.shardingsphere.distsql.statement.type.rql.RQLStatement;
import org.apache.shardingsphere.distsql.statement.type.rul.RULStatement;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.authority.model.ShardingSpherePrivileges;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link AuthorityDistSQLExecutionChecker}.
 *
 * <p>Coverage matrix (grantee × statement-type × privilege-state):</p>
 * <ul>
 *   <li>Null grantee → always allow (no authentication configured)</li>
 *   <li>No AuthorityRule present → always allow (all-permitted instance)</li>
 *   <li>Admin user → allow any DistSQL (read and write)</li>
 *   <li>Non-admin user + write DistSQL (RDL) → deny regardless of db privilege</li>
 *   <li>Non-admin user + read DistSQL (RQL/queryable RAL/RUL) + has db privilege → allow</li>
 *   <li>Non-admin user + read DistSQL + no db privilege → deny</li>
 *   <li>Non-admin user + read DistSQL + null usedDatabaseName → deny</li>
 * </ul>
 */
class AuthorityDistSQLExecutionCheckerTest {
    
    private static final String DATABASE_NAME = "app_db";
    
    private AuthorityDistSQLExecutionChecker checker;
    
    private ShardingSphereMetaData metaData;
    
    private AuthorityRule authorityRule;
    
    @BeforeEach
    void setUp() {
        checker = new AuthorityDistSQLExecutionChecker();
        authorityRule = mock(AuthorityRule.class);
        metaData = mock(ShardingSphereMetaData.class);
        RuleMetaData globalRuleMetaData = mock(RuleMetaData.class);
        when(metaData.getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        when(globalRuleMetaData.findSingleRule(AuthorityRule.class)).thenReturn(Optional.of(authorityRule));
    }
    
    // -------------------------------------------------------------------------
    // Null grantee — unauthenticated / internal connection
    // -------------------------------------------------------------------------
    
    @Test
    void assertNullGranteeAllowsWriteDistSQL() {
        // A null grantee means no authentication is configured; all statements pass through.
        assertDoesNotThrow(() -> checker.check(mock(RDLStatement.class), null, metaData, DATABASE_NAME));
    }
    
    @Test
    void assertNullGranteeAllowsReadDistSQL() {
        assertDoesNotThrow(() -> checker.check(mock(RQLStatement.class), null, metaData, null));
    }
    
    // -------------------------------------------------------------------------
    // No AuthorityRule present — all-permitted instance
    // -------------------------------------------------------------------------
    
    @Test
    void assertNoAuthorityRuleAllowsAnyDistSQL() {
        ShardingSphereMetaData noRuleMetaData = mock(ShardingSphereMetaData.class);
        RuleMetaData globalRuleMetaData = mock(RuleMetaData.class);
        when(noRuleMetaData.getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        when(globalRuleMetaData.findSingleRule(AuthorityRule.class)).thenReturn(Optional.empty());
        
        Grantee grantee = new Grantee("app_user", "%");
        assertDoesNotThrow(() -> checker.check(mock(RDLStatement.class), grantee, noRuleMetaData, DATABASE_NAME));
    }
    
    // -------------------------------------------------------------------------
    // Admin user — allow everything
    // -------------------------------------------------------------------------
    
    @Test
    void assertAdminUserAllowsWriteDistSQL() {
        Grantee grantee = new Grantee("root", "%");
        ShardingSphereUser adminUser = new ShardingSphereUser("root", "root", "%", "", true);
        when(authorityRule.findUser(grantee)).thenReturn(Optional.of(adminUser));
        
        assertDoesNotThrow(() -> checker.check(mock(RDLStatement.class), grantee, metaData, DATABASE_NAME));
    }
    
    @Test
    void assertAdminUserAllowsReadDistSQL() {
        Grantee grantee = new Grantee("root", "%");
        ShardingSphereUser adminUser = new ShardingSphereUser("root", "root", "%", "", true);
        when(authorityRule.findUser(grantee)).thenReturn(Optional.of(adminUser));
        
        assertDoesNotThrow(() -> checker.check(mock(RQLStatement.class), grantee, metaData, DATABASE_NAME));
        assertDoesNotThrow(() -> checker.check(mock(QueryableRALStatement.class), grantee, metaData, DATABASE_NAME));
        assertDoesNotThrow(() -> checker.check(mock(RULStatement.class), grantee, metaData, DATABASE_NAME));
    }
    
    @Test
    void assertAdminUserAllowsWriteDistSQLEvenWithNullDatabase() {
        Grantee grantee = new Grantee("root", "%");
        ShardingSphereUser adminUser = new ShardingSphereUser("root", "root", "%", "", true);
        when(authorityRule.findUser(grantee)).thenReturn(Optional.of(adminUser));
        
        // Admin operations often run without a USE database context.
        assertDoesNotThrow(() -> checker.check(mock(RDLStatement.class), grantee, metaData, null));
    }
    
    // -------------------------------------------------------------------------
    // Non-admin user + write DistSQL → always deny
    // This reproduces the four exploits from the vulnerability report:
    //   REGISTER STORAGE UNIT, UNREGISTER STORAGE UNIT, EXPORT DATABASE CONFIGURATION
    //   all go through RDL or updatable RAL paths.
    // -------------------------------------------------------------------------
    
    @Test
    void assertNonAdminUserDeniedWriteDistSQL() {
        Grantee grantee = new Grantee("app_user", "%");
        ShardingSphereUser nonAdminUser = new ShardingSphereUser("app_user", "app123", "%", "", false);
        when(authorityRule.findUser(grantee)).thenReturn(Optional.of(nonAdminUser));
        
        // Even with database privilege, write DistSQL requires admin.
        ShardingSpherePrivileges privileges = mock(ShardingSpherePrivileges.class);
        when(privileges.hasPrivileges(DATABASE_NAME)).thenReturn(true);
        when(authorityRule.findPrivileges(grantee)).thenReturn(Optional.of(privileges));
        
        assertThrows(AccessDeniedException.class,
                () -> checker.check(mock(RDLStatement.class), grantee, metaData, DATABASE_NAME));
    }
    
    // -------------------------------------------------------------------------
    // Non-admin user + read DistSQL
    // -------------------------------------------------------------------------
    
    @Test
    void assertNonAdminUserWithDatabasePrivilegeAllowsReadDistSQL() {
        // This is the intended behavior: a DATABASE_PERMITTED user scoped to app_db
        // may run RQL/queryable RAL/RUL against their own database.
        Grantee grantee = new Grantee("app_user", "%");
        ShardingSphereUser nonAdminUser = new ShardingSphereUser("app_user", "app123", "%", "", false);
        when(authorityRule.findUser(grantee)).thenReturn(Optional.of(nonAdminUser));
        
        ShardingSpherePrivileges privileges = mock(ShardingSpherePrivileges.class);
        when(privileges.hasPrivileges(DATABASE_NAME)).thenReturn(true);
        when(authorityRule.findPrivileges(grantee)).thenReturn(Optional.of(privileges));
        
        assertDoesNotThrow(() -> checker.check(mock(RQLStatement.class), grantee, metaData, DATABASE_NAME));
        assertDoesNotThrow(() -> checker.check(mock(QueryableRALStatement.class), grantee, metaData, DATABASE_NAME));
        assertDoesNotThrow(() -> checker.check(mock(RULStatement.class), grantee, metaData, DATABASE_NAME));
    }
    
    @Test
    void assertNonAdminUserWithoutDatabasePrivilegeDeniedReadDistSQL() {
        // Reproduces the pre-fix behavior: app_user has no privilege on other_db.
        Grantee grantee = new Grantee("app_user", "%");
        ShardingSphereUser nonAdminUser = new ShardingSphereUser("app_user", "app123", "%", "", false);
        when(authorityRule.findUser(grantee)).thenReturn(Optional.of(nonAdminUser));
        
        ShardingSpherePrivileges privileges = mock(ShardingSpherePrivileges.class);
        when(privileges.hasPrivileges("other_db")).thenReturn(false);
        when(authorityRule.findPrivileges(grantee)).thenReturn(Optional.of(privileges));
        
        assertThrows(AccessDeniedException.class,
                () -> checker.check(mock(RQLStatement.class), grantee, metaData, "other_db"));
    }
    
    @Test
    void assertNonAdminUserWithNullDatabaseDeniedReadDistSQL() {
        // When no database is in scope, there is no meaningful privilege to evaluate.
        // Deny to prevent credential exposure via SHOW STORAGE UNITS etc.
        Grantee grantee = new Grantee("app_user", "%");
        ShardingSphereUser nonAdminUser = new ShardingSphereUser("app_user", "app123", "%", "", false);
        when(authorityRule.findUser(grantee)).thenReturn(Optional.of(nonAdminUser));
        
        assertThrows(AccessDeniedException.class,
                () -> checker.check(mock(RQLStatement.class), grantee, metaData, null));
    }
    
    @Test
    void assertNonAdminUserWithEmptyPrivilegeDeniedReadDistSQL() {
        // findPrivileges returns empty: user exists but has no privilege entry at all.
        Grantee grantee = new Grantee("app_user", "%");
        ShardingSphereUser nonAdminUser = new ShardingSphereUser("app_user", "app123", "%", "", false);
        when(authorityRule.findUser(grantee)).thenReturn(Optional.of(nonAdminUser));
        when(authorityRule.findPrivileges(grantee)).thenReturn(Optional.empty());
        
        assertThrows(AccessDeniedException.class,
                () -> checker.check(mock(RQLStatement.class), grantee, metaData, DATABASE_NAME));
    }
    
    // -------------------------------------------------------------------------
    // AccessDeniedException carries the correct grantee identity
    // -------------------------------------------------------------------------
    
    @Test
    void assertAccessDeniedExceptionContainsGranteeIdentity() {
        Grantee grantee = new Grantee("app_user", "192.168.1.10");
        ShardingSphereUser nonAdminUser = new ShardingSphereUser("app_user", "app123", "192.168.1.10", "", false);
        when(authorityRule.findUser(grantee)).thenReturn(Optional.of(nonAdminUser));
        
        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> checker.check(mock(RDLStatement.class), grantee, metaData, DATABASE_NAME));
        
        org.junit.jupiter.api.Assertions.assertEquals("app_user", ex.getUsername());
        org.junit.jupiter.api.Assertions.assertEquals("192.168.1.10", ex.getHostname());
        org.junit.jupiter.api.Assertions.assertTrue(ex.isUsingPassword());
    }
}
