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
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;

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
     * <p>Returns {@code true} when any of the following hold:</p>
     * <ul>
     *   <li>The grantee is {@code null} (unauthenticated / internal connection).</li>
     *   <li>The grantee is configured with {@code admin: true}.</li>
     *   <li>The grantee holds privileges on the specified database under the
     *       configured privilege provider (e.g. {@code DATABASE_PERMITTED}).</li>
     * </ul>
     *
     * @param database database name
     * @return authorized or not
     */
    @HighFrequencyInvocation
    public boolean isAuthorized(final String database) {
        return null == grantee || isAdmin()
                || rule.findPrivileges(grantee).map(optional -> optional.hasPrivileges(database)).orElse(false);
    }
    
    /**
     * Check whether the grantee holds global admin privileges.
     *
     * <p>This is the query-time evaluation of the {@code admin} flag configured in
     * {@code global.yaml}. It is used by both the regular SQL authorization path
     * (via {@link #isAuthorized(String)}) and the DistSQL authorization path
     * (via {@link AuthorityDistSQLExecutionChecker}), which bypasses
     * {@code isAuthorized} because DistSQL statements have no per-database scope.</p>
     *
     * <p>Returns {@code true} when the grantee is {@code null} (no authentication
     * configured) or the matching user has {@code admin: true}.</p>
     *
     * @return whether the grantee has admin privileges
     */
    public boolean isAdmin() {
        return null == grantee || rule.findUser(grantee).map(ShardingSphereUser::isAdmin).orElse(false);
    }
}
