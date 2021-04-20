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

package org.apache.shardingsphere.authority.rule;

import org.apache.shardingsphere.authority.api.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.authority.model.ShardingSpherePrivileges;
import org.apache.shardingsphere.authority.spi.AuthorityProvideAlgorithm;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.rule.level.KernelRule;
import org.apache.shardingsphere.infra.rule.scope.GlobalRule;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Authority rule.
 */
public final class AuthorityRule implements KernelRule, GlobalRule {
    
    static {
        ShardingSphereServiceLoader.register(AuthorityProvideAlgorithm.class);
    }
    
    private final AuthorityProvideAlgorithm provider;
    
    public AuthorityRule(final AuthorityRuleConfiguration config, final Map<String, ShardingSphereMetaData> mataDataMap, final Collection<ShardingSphereUser> users) {
        provider = ShardingSphereAlgorithmFactory.createAlgorithm(config.getProvider(), AuthorityProvideAlgorithm.class);
        provider.init(mataDataMap, users);
    }
    
    /**
     * Find Privileges.
     *
     * @param grantee grantee
     * @return found privileges
     */
    public Optional<ShardingSpherePrivileges> findPrivileges(final Grantee grantee) {
        return provider.findPrivileges(grantee);
    }
    
    /**
     * Refresh authority.
     *
     * @param mataDataMap mata data map
     * @param users users
     */
    public void refresh(final Map<String, ShardingSphereMetaData> mataDataMap, final Collection<ShardingSphereUser> users) {
        provider.refresh(mataDataMap, users);
    }
}
