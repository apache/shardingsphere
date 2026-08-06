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
import org.apache.shardingsphere.distsql.handler.util.DatabaseNameUtils;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.QueryableRALStatement;
import org.apache.shardingsphere.distsql.statement.type.rql.RQLStatement;
import org.apache.shardingsphere.distsql.statement.type.rul.RULStatement;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.FromDatabaseSQLStatementAttribute;

import java.util.Optional;

/**
 * Authority checker for DistSQL statements.
 *
 * <h2>Why this class exists</h2>
 * <p>The central dispatcher ({@code ProxyBackendHandlerFactory}) routes DistSQL
 * to {@code DistSQLProxyBackendHandlerFactory} before it reaches
 * {@code checkSQLExecution()}, which is where {@link AuthoritySQLExecutionChecker}
 * evaluates the authenticated user's privileges for regular SQL. DistSQL therefore
 * bypassed all authority checks entirely.</p>
 *
 * <p>This class is invoked explicitly in the DistSQL branch of
 * {@code ProxyBackendHandlerFactory.newInstance()} to close that gap.</p>
 *
 * <h2>Authorization model</h2>
 * <p>DistSQL statements fall into three categories by their impact on the proxy:</p>
 *
 * <ul>
 *   <li><b>Write DistSQL</b> (RDL and updatable RAL) — mutates global proxy
 *       configuration: registers/unregisters storage units, creates/drops sharding
 *       rules, etc. These operations have no meaningful per-database scope.
 *       Only {@code admin: true} users may execute them.</li>
 *
 *   <li><b>Database-scoped read DistSQL</b> (RQL, queryable RAL, RUL that carry a
 *       {@link FromDatabaseSQLStatementAttribute}) — reads proxy metadata for a
 *       specific database. The target database is resolved exactly as the DistSQL
 *       execute engines resolve it: via {@link DatabaseNameUtils#getDatabaseName},
 *       which honors an explicit {@code FROM <database>} clause on the statement
 *       over the session's current database. Authorization is evaluated against
 *       that resolved target, not against the session's current database, so a
 *       user permitted on one database cannot read another database's data by
 *       switching the {@code FROM} target while staying {@code USE}'d into a
 *       database they are permitted on.</li>
 *
 *   <li><b>Global read DistSQL</b> (RQL, queryable RAL, RUL that carry <em>no</em>
 *       {@link FromDatabaseSQLStatementAttribute}, e.g. {@code ExportMetaDataStatement})
 *       — has no per-database scope at all; it reads or exports cluster-wide state.
 *       These statements have no database to authorize against, so they require
 *       {@code admin: true} unconditionally, the same as write DistSQL.</li>
 * </ul>
 *
 * <h2>Null-grantee / no-AuthorityRule semantics</h2>
 * <p>A {@code null} grantee indicates an unauthenticated or internally-originated
 * connection. The absence of an {@code AuthorityRule} means the instance is
 * running with all-permitted access. Both cases allow the statement through,
 * consistent with how {@link AuthorityChecker#isAuthorized(String)} behaves.</p>
 */
public final class AuthorityDistSQLExecutionChecker {
    
    /**
     * Check whether the grantee is allowed to execute the given DistSQL statement.
     *
     * @param sqlStatement     the DistSQL statement to be authorized
     * @param grantee          the authenticated user identity; {@code null} for internal connections
     * @param metaData         cluster metadata used to resolve the {@link AuthorityRule}
     * @param usedDatabaseName the logical database currently selected by the connection; may be {@code null}
     * @throws AccessDeniedException when the grantee lacks the required privileges
     */
    public void check(final SQLStatement sqlStatement, final Grantee grantee,
                      final ShardingSphereMetaData metaData, final String usedDatabaseName) {
        // Null grantee: unauthenticated or internal — no authority configured; allow through.
        if (null == grantee) {
            return;
        }
        Optional<AuthorityRule> authorityRule = metaData.getGlobalRuleMetaData().findSingleRule(AuthorityRule.class);
        // No AuthorityRule: instance runs with all-permitted access; allow through.
        if (!authorityRule.isPresent()) {
            return;
        }
        AuthorityChecker checker = new AuthorityChecker(authorityRule.get(), grantee);
        // Admin users may execute any DistSQL unconditionally.
        if (checker.isAdmin()) {
            return;
        }
        if (isReadOnlyDistSQL(sqlStatement) && isDatabaseScoped(sqlStatement)) {
            // Database-scoped read DistSQL: resolve the actual execution-target database
            // the same way the DistSQL execute engines resolve it. DatabaseNameUtils honors
            // an explicit FROM <database> clause on the statement over the session's current
            // database, falling back to the session's current database only when the
            // statement carries no explicit FROM target. Authorizing against this resolved
            // value - rather than blindly against usedDatabaseName - prevents a user permitted
            // on app_db from reading other_db via e.g. `SHOW STORAGE UNITS FROM other_db`
            // while staying USE'd into app_db.
            String targetDatabaseName = DatabaseNameUtils.getDatabaseName(sqlStatement, usedDatabaseName);
            if (null != targetDatabaseName && checker.isAuthorized(targetDatabaseName)) {
                return;
            }
        }
        // Write DistSQL without admin, global read DistSQL without admin, or database-scoped
        // read DistSQL without admin or privilege on the resolved target database: deny.
        ShardingSpherePreconditions.checkState(false,
                () -> new AccessDeniedException(grantee.getUsername(), grantee.getHostname(), true));
    }
    
    /**
     * Returns {@code true} for read-only DistSQL statement types.
     *
     * <p>RQL, queryable RAL, and RUL are read-only; all other DistSQL (RDL, updatable RAL)
     * is treated as write and requires {@code admin: true} unconditionally.</p>
     *
     * @param sqlStatement DistSQL statement to classify
     * @return whether the statement is read-only
     */
    private boolean isReadOnlyDistSQL(final SQLStatement sqlStatement) {
        return sqlStatement instanceof RQLStatement
                || sqlStatement instanceof QueryableRALStatement
                || sqlStatement instanceof RULStatement;
    }
    
    /**
     * Returns {@code true} when the statement carries a {@link FromDatabaseSQLStatementAttribute},
     * meaning it has a per-database execution scope that {@link DatabaseNameUtils} can resolve.
     *
     * <p>Statements without this attribute (e.g. {@code ExportMetaDataStatement}, which exports
     * cluster-wide metadata) have no database to authorize against and must not be granted on
     * the strength of any single database privilege - see {@link #check} for the global-statement
     * handling that falls through to the admin-only deny path in that case.</p>
     *
     * @param sqlStatement DistSQL statement to inspect
     * @return whether the statement has a resolvable per-database scope
     */
    private boolean isDatabaseScoped(final SQLStatement sqlStatement) {
        return sqlStatement.getAttributes().findAttribute(FromDatabaseSQLStatementAttribute.class).isPresent();
    }
}
