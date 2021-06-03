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

import org.apache.shardingsphere.authority.api.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.authority.constant.AuthorityOrder;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.rule.builder.level.KernelRuleBuilder;
import org.apache.shardingsphere.infra.rule.builder.scope.GlobalRuleBuilder;

import java.util.Map;

/**
 * Authority rule builder.
 */
public final class AuthorityRuleBuilder implements KernelRuleBuilder, GlobalRuleBuilder<AuthorityRule, AuthorityRuleConfiguration> {
    
    @Override
    public AuthorityRule build(final AuthorityRuleConfiguration ruleConfig, final Map<String, ShardingSphereMetaData> mataDataMap) {
        return new AuthorityRule(ruleConfig, mataDataMap, ruleConfig.getUsers());
    }
    
    @Override
    public int getOrder() {
        return AuthorityOrder.ORDER;
    }
    
    @Override
    public Class<AuthorityRuleConfiguration> getTypeClass() {
        return AuthorityRuleConfiguration.class;
    }
}
