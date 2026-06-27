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

import org.apache.shardingsphere.authority.model.ShardingSpherePrivileges;
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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.FromDatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.FromDatabaseSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link AuthorityDistSQLExecutionChecker}.
 *
 * <p>Coverage matrix (grantee x statement-type x privilege-state x target-scope):</p>
 * <ul>
 *   <li>Null grantee -&gt; always allow (no authentication configured)</li>
 *   <li>No AuthorityRule present -&gt; always allow (all-permitted instance)</li>
 *   <li>Admin user -&gt; allow any DistSQL (write, database-scoped read, global read)</li>
 *   <li>Non-admin user + write DistSQL (RDL) -&gt; deny regardless of db privilege</li>
 *   <li>Non-admin user + database-scoped read DistSQL, no FROM clause, current db permitted -&gt; allow</li>
 *   <li>Non-admin user + database-scoped read DistSQL, explicit FROM target permitted -&gt; allow</li>
 *   <li>Non-admin user + database-scoped read DistSQL, explicit FROM targets a DIFFERENT,
 *       non-permitted database while USE'd into a permitted database -&gt; deny
 *       (this is the cross-database bypass regression test)</li>
 *   <li>Non-admin user + database-scoped read DistSQL + no db privilege -&gt; deny</li>
 *   <li>Non-admin user + database-scoped read DistSQL + null usedDatabaseName and no FROM -&gt; deny</li>
 *   <li>Non-admin user + global read DistSQL (no FromDatabaseSQLStatementAttribute at all,
 *       e.g. ExportMetaDataStatement) -&gt; always deny, even with full database privilege
 *       (this is the global-statement regression test)</li>
 * </ul>
 */
class AuthorityDistSQLExecutionCheckerTest {
    
    private static final String DATABASE_NAME = "app_db";
    
    private static final String OTHER_DATABASE_NAME = "other_db";
    
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
    
    /**
     * Builds a mocked database-scoped statement carrying a {@link FromDatabaseSQLStatementAttribute}.
     *
     * <p>The type bound is intentionally {@code SQLStatement}, not {@code RQLStatement}, because
     * this helper is reused for both {@code RQLStatement} and {@code RULStatement} mocks, which are
     * sibling interfaces under {@code SQLStatement} rather than one extending the other.</p>
     *
     * @param statementType        the DistSQL statement subtype to mock (RQL, QueryableRAL, or RUL)
     * @param explicitFromDatabase the database named in an explicit FROM clause; {@code null} when
     *                             the statement has no explicit FROM and should fall back to the
     *                             session's current database
     * @param <T> statement type
     * @return mocked statement with a stubbed database-scoped attribute set
     */
    private <T extends SQLStatement> T mockDatabaseScopedStatement(final Class<T> statementType, final String explicitFromDatabase) {
        T statement = mock(statementType);
        FromDatabaseSegment fromDatabaseSegment = null;
        if (null != explicitFromDatabase) {
            DatabaseSegment databaseSegment = mock(DatabaseSegment.class);
            IdentifierValue identifierValue = mock(IdentifierValue.class);
            when(identifierValue.getValue()).thenReturn(explicitFromDatabase);
            when(databaseSegment.getIdentifier()).thenReturn(identifierValue);
            fromDatabaseSegment = mock(FromDatabaseSegment.class);
            when(fromDatabaseSegment.getDatabase()).thenReturn(databaseSegment);
        }
        SQLStatementAttributes attributes = new SQLStatementAttributes(new FromDatabaseSQLStatementAttribute(fromDatabaseSegment));
        when(statement.getAttributes()).thenReturn(attributes);
        return statement;
    }
    
    /**
     * Builds a mocked global statement carrying no {@link FromDatabaseSQLStatementAttribute} at all,
     * matching the structure of statements such as {@code ExportMetaDataStatement}.
     *
     * @param statementType the DistSQL statement subtype to mock (typically QueryableRAL)
     * @param <T> statement type
     * @return mocked statement with an empty attribute set
     */
    private <T extends SQLStatement> T mockGlobalStatement(final Class<T> statementType) {
        T statement = mock(statementType);
        when(statement.getAttributes()).thenReturn(new SQLStatementAttributes());
        return statement;
    }
    
    // -------------------------------------------------------------------------
    // Null grantee - unauthenticated / internal connection
    // -------------------------------------------------------------------------
    
    @Test
    void assertNullGranteeAllowsWriteDistSQL() {
        // A null grantee means no authentication is configured; all statements pass through.
        assertDoesNotThrow(() -> checker.check(mock(RDLStatement.class), null, metaData, DATABASE_NAME));
    }
    
    @Test
    void assertNullGranteeAllowsReadDistSQL() {
        assertDoesNotThrow(() -> checker.check(mockGlobalStatement(QueryableRALStatement.class), null, metaData, null));
    }
    
    // -------------------------------------------------------------------------
    // No AuthorityRule present - all-permitted instance
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
    // Admin user - allow everything
    // -------------------------------------------------------------------------
    
    @Test
    void assertAdminUserAllowsWriteDistSQL() {
        Grantee grantee = new Grantee("root", "%");
        ShardingSphereUser adminUser = new ShardingSphereUser("root", "root", "%", "", true);
        when(authorityRule.findUser(grantee)).thenReturn(Optional.of(adminUser));
        
        assertDoesNotThrow(() -> checker.check(mock(RDLStatement.class), grantee, metaData, DATABASE_NAME));
    }
    
    @Test
    void assertAdminUserAllowsDatabaseScopedReadDistSQL() {
        Grantee grantee = new Grantee("root", "%");
        ShardingSphereUser adminUser = new ShardingSphereUser("root", "root", "%", "", true);
        when(authorityRule.findUser(grantee)).thenReturn(Optional.of(adminUser));
        
        assertDoesNotThrow(() -> checker.check(
                mockDatabaseScopedStatement(RQLStatement.class, null), grantee, metaData, DATABASE_NAME));
        assertDoesNotThrow(() -> checker.check(
                mockDatabaseScopedStatement(RULStatement.class, null), grantee, metaData, DATABASE_NAME));
    }
    
    @Test
    void assertAdminUserAllowsGlobalReadDistSQL() {
        Grantee grantee = new Grantee("root", "%");
        ShardingSphereUser adminUser = new ShardingSphereUser("root", "root", "%", "", true);
        when(authorityRule.findUser(grantee)).thenReturn(Optional.of(adminUser));
        
        assertDoesNotThrow(() -> checker.check(
                mockGlobalStatement(QueryableRALStatement.class), grantee, metaData, DATABASE_NAME));
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
    // Non-admin user + write DistSQL -> always deny
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
    // Non-admin user + database-scoped read DistSQL, no explicit FROM clause
    // -------------------------------------------------------------------------
    
    @Test
    void assertNonAdminUserWithDatabasePrivilegeAllowsReadDistSQLWithNoExplicitFrom() {
        // No explicit FROM target: resolution falls back to the session's current database,
        // which the user is permitted on.
        Grantee grantee = new Grantee("app_user", "%");
        ShardingSphereUser nonAdminUser = new ShardingSphereUser("app_user", "app123", "%", "", false);
        when(authorityRule.findUser(grantee)).thenReturn(Optional.of(nonAdminUser));
        
        ShardingSpherePrivileges privileges = mock(ShardingSpherePrivileges.class);
        when(privileges.hasPrivileges(DATABASE_NAME)).thenReturn(true);
        when(authorityRule.findPrivileges(grantee)).thenReturn(Optional.of(privileges));
        
        assertDoesNotThrow(() -> checker.check(
                mockDatabaseScopedStatement(RQLStatement.class, null), grantee, metaData, DATABASE_NAME));
        assertDoesNotThrow(() -> checker.check(
                mockDatabaseScopedStatement(RULStatement.class, null), grantee, metaData, DATABASE_NAME));
    }
    
    @Test
    void assertNonAdminUserWithDatabasePrivilegeAllowsReadDistSQLWithMatchingExplicitFrom() {
        // Explicit FROM target matches a database the user is permitted on.
        Grantee grantee = new Grantee("app_user", "%");
        ShardingSphereUser nonAdminUser = new ShardingSphereUser("app_user", "app123", "%", "", false);
        when(authorityRule.findUser(grantee)).thenReturn(Optional.of(nonAdminUser));
        
        ShardingSpherePrivileges privileges = mock(ShardingSpherePrivileges.class);
        when(privileges.hasPrivileges(DATABASE_NAME)).thenReturn(true);
        when(authorityRule.findPrivileges(grantee)).thenReturn(Optional.of(privileges));
        
        assertDoesNotThrow(() -> checker.check(
                mockDatabaseScopedStatement(RQLStatement.class, DATABASE_NAME), grantee, metaData, DATABASE_NAME));
    }
    
    /**
     * Regression test for the cross-database FROM bypass: a user permitted only on
     * {@code app_db} stays USE'd into app_db but issues a read DistSQL statement with
     * an explicit {@code FROM other_db} clause. The resolved execution target is
     * {@code other_db}, not the session's current database, so this must be denied even
     * though the session's current database is one the user is permitted on.
     */
    @Test
    void assertNonAdminUserDeniedReadDistSQLWhenExplicitFromTargetsDifferentNonPermittedDatabase() {
        Grantee grantee = new Grantee("app_user", "%");
        ShardingSphereUser nonAdminUser = new ShardingSphereUser("app_user", "app123", "%", "", false);
        when(authorityRule.findUser(grantee)).thenReturn(Optional.of(nonAdminUser));
        
        ShardingSpherePrivileges privileges = mock(ShardingSpherePrivileges.class);
        // Permitted on the session's current database...
        when(privileges.hasPrivileges(DATABASE_NAME)).thenReturn(true);
        // ...but NOT permitted on the database the statement actually targets via FROM.
        when(privileges.hasPrivileges(OTHER_DATABASE_NAME)).thenReturn(false);
        when(authorityRule.findPrivileges(grantee)).thenReturn(Optional.of(privileges));
        
        // usedDatabaseName is app_db (permitted), but the statement's FROM clause targets
        // other_db (not permitted). Must deny based on the resolved target, not the session db.
        assertThrows(AccessDeniedException.class,
                () -> checker.check(
                        mockDatabaseScopedStatement(RQLStatement.class, OTHER_DATABASE_NAME), grantee, metaData, DATABASE_NAME));
    }
    
    @Test
    void assertNonAdminUserWithoutDatabasePrivilegeDeniedReadDistSQL() {
        // Reproduces the pre-fix behavior: app_user has no privilege on other_db,
        // and the statement targets other_db both via explicit FROM and as the session database.
        Grantee grantee = new Grantee("app_user", "%");
        ShardingSphereUser nonAdminUser = new ShardingSphereUser("app_user", "app123", "%", "", false);
        when(authorityRule.findUser(grantee)).thenReturn(Optional.of(nonAdminUser));
        
        ShardingSpherePrivileges privileges = mock(ShardingSpherePrivileges.class);
        when(privileges.hasPrivileges(OTHER_DATABASE_NAME)).thenReturn(false);
        when(authorityRule.findPrivileges(grantee)).thenReturn(Optional.of(privileges));
        
        assertThrows(AccessDeniedException.class,
                () -> checker.check(
                        mockDatabaseScopedStatement(RQLStatement.class, null), grantee, metaData, OTHER_DATABASE_NAME));
    }
    
    @Test
    void assertNonAdminUserWithNullDatabaseAndNoExplicitFromDeniedReadDistSQL() {
        // When no database is in scope at all (no explicit FROM, no session database),
        // there is no meaningful privilege to evaluate. Deny to prevent credential
        // exposure via statements like SHOW STORAGE UNITS.
        Grantee grantee = new Grantee("app_user", "%");
        ShardingSphereUser nonAdminUser = new ShardingSphereUser("app_user", "app123", "%", "", false);
        when(authorityRule.findUser(grantee)).thenReturn(Optional.of(nonAdminUser));
        
        assertThrows(AccessDeniedException.class,
                () -> checker.check(
                        mockDatabaseScopedStatement(RQLStatement.class, null), grantee, metaData, null));
    }
    
    @Test
    void assertNonAdminUserWithEmptyPrivilegeDeniedReadDistSQL() {
        // findPrivileges returns empty: user exists but has no privilege entry at all.
        Grantee grantee = new Grantee("app_user", "%");
        ShardingSphereUser nonAdminUser = new ShardingSphereUser("app_user", "app123", "%", "", false);
        when(authorityRule.findUser(grantee)).thenReturn(Optional.of(nonAdminUser));
        when(authorityRule.findPrivileges(grantee)).thenReturn(Optional.empty());
        
        assertThrows(AccessDeniedException.class,
                () -> checker.check(
                        mockDatabaseScopedStatement(RQLStatement.class, null), grantee, metaData, DATABASE_NAME));
    }
    
    // -------------------------------------------------------------------------
    // Non-admin user + global read DistSQL (no FromDatabaseSQLStatementAttribute at all)
    // -------------------------------------------------------------------------
    
    /**
     * Regression test for global-effect read DistSQL such as {@code ExportMetaDataStatement}.
     * These statements carry no {@link FromDatabaseSQLStatementAttribute} and have no
     * per-database scope - they must require {@code admin: true} unconditionally, even when
     * the grantee holds full privileges on every database they would otherwise be checked
     * against. This must NOT be satisfiable by any database-level privilege.
     */
    @Test
    void assertNonAdminUserDeniedGlobalReadDistSQLEvenWithFullDatabasePrivilege() {
        Grantee grantee = new Grantee("app_user", "%");
        ShardingSphereUser nonAdminUser = new ShardingSphereUser("app_user", "app123", "%", "", false);
        when(authorityRule.findUser(grantee)).thenReturn(Optional.of(nonAdminUser));
        
        // Grant privilege on every database that could plausibly be checked - this must still
        // be insufficient, because a global statement has no database to authorize against.
        ShardingSpherePrivileges privileges = mock(ShardingSpherePrivileges.class);
        when(privileges.hasPrivileges(DATABASE_NAME)).thenReturn(true);
        when(authorityRule.findPrivileges(grantee)).thenReturn(Optional.of(privileges));
        
        assertThrows(AccessDeniedException.class,
                () -> checker.check(
                        mockGlobalStatement(QueryableRALStatement.class), grantee, metaData, DATABASE_NAME));
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
        assertTrue(ex.isUsingPassword());
    }
}
