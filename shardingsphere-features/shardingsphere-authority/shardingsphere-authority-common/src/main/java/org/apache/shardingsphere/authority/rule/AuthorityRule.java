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

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.authority.api.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.authority.spi.PrivilegeLoadAlgorithm;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.infra.metadata.auth.Authentication;
import org.apache.shardingsphere.infra.metadata.auth.AuthenticationContext;
import org.apache.shardingsphere.infra.metadata.auth.builtin.DefaultAuthentication;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;

/**
 * Authority rule.
 */
public final class AuthorityRule implements ShardingSphereRule {
    
    static {
        ShardingSphereServiceLoader.register(PrivilegeLoadAlgorithm.class);
    }
    
    private final PrivilegeLoadAlgorithm privilegeLoader;
    
    public AuthorityRule(final AuthorityRuleConfiguration config) {
        Preconditions.checkState(1 == config.getPrivilegeLoaders().size(), "Only support one privilege loader.");
        privilegeLoader = ShardingSphereAlgorithmFactory.createAlgorithm(config.getPrivilegeLoaders().values().iterator().next(), PrivilegeLoadAlgorithm.class);
        Authentication authentication = new DefaultAuthentication();
        // TODO pass correct parameters
        authentication.init(privilegeLoader.load(null, null, null));
        AuthenticationContext.getInstance().init(authentication);
    }
}
