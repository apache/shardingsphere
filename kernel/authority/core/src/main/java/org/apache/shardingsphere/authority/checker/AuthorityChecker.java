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
     * @param database database name
     * @return authorized or not
     */
    @HighFrequencyInvocation
    public boolean isAuthorized(final String database) {
        return null == grantee || rule.findUser(grantee).map(ShardingSphereUser::isAdmin).orElse(false)
                || rule.findPrivileges(grantee).map(optional -> optional.hasPrivileges(database)).orElse(false);
    }
}
