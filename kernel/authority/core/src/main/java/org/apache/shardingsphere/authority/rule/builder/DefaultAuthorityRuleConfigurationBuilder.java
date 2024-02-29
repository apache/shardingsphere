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

package org.apache.shardingsphere.authority.rule.builder;

import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.authority.constant.AuthorityOrder;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.rule.builder.global.DefaultGlobalRuleConfigurationBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Properties;

/**
 * Default authority rule configuration builder.
 */
public final class DefaultAuthorityRuleConfigurationBuilder implements DefaultGlobalRuleConfigurationBuilder<AuthorityRuleConfiguration, AuthorityRuleBuilder> {
    
    @Override
    public AuthorityRuleConfiguration build() {
        Collection<ShardingSphereUser> defaultUsers = new LinkedHashSet<>(Collections.singleton(new ShardingSphereUser(DefaultUser.USERNAME, DefaultUser.PASSWORD, DefaultUser.HOSTNAME, "", true)));
        AlgorithmConfiguration authorityProviderConfig = new AlgorithmConfiguration("ALL_PERMITTED", new Properties());
        return new AuthorityRuleConfiguration(defaultUsers, authorityProviderConfig, new LinkedHashMap<>(), null);
    }
    
    @Override
    public int getOrder() {
        return AuthorityOrder.ORDER;
    }
    
    @Override
    public Class<AuthorityRuleBuilder> getTypeClass() {
        return AuthorityRuleBuilder.class;
    }
}
