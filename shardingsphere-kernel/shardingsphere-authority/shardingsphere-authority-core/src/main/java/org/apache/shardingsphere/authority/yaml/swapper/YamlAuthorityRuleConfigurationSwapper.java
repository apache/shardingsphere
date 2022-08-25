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

package org.apache.shardingsphere.authority.yaml.swapper;

import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.authority.constant.AuthorityOrder;
import org.apache.shardingsphere.authority.rule.builder.DefaultAuthorityRuleConfigurationBuilder;
import org.apache.shardingsphere.authority.yaml.config.YamlAuthorityRuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.metadata.user.yaml.config.YamlUsersConfigurationConverter;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.algorithm.YamlAlgorithmConfigurationSwapper;

import java.util.Collection;

/**
 * YAML Authority rule configuration swapper.
 */
public final class YamlAuthorityRuleConfigurationSwapper implements YamlRuleConfigurationSwapper<YamlAuthorityRuleConfiguration, AuthorityRuleConfiguration> {
    
    private final YamlAlgorithmConfigurationSwapper algorithmSwapper = new YamlAlgorithmConfigurationSwapper();
    
    @Override
    public YamlAuthorityRuleConfiguration swapToYamlConfiguration(final AuthorityRuleConfiguration data) {
        YamlAuthorityRuleConfiguration result = new YamlAuthorityRuleConfiguration();
        result.setProvider(algorithmSwapper.swapToYamlConfiguration(data.getProvider()));
        result.setUsers(YamlUsersConfigurationConverter.convertYamlUserConfigurations(data.getUsers()));
        return result;
    }
    
    @Override
    public AuthorityRuleConfiguration swapToObject(final YamlAuthorityRuleConfiguration yamlConfig) {
        Collection<ShardingSphereUser> users = YamlUsersConfigurationConverter.convertShardingSphereUser(yamlConfig.getUsers());
        AlgorithmConfiguration provider = algorithmSwapper.swapToObject(yamlConfig.getProvider());
        if (null == provider) {
            provider = new DefaultAuthorityRuleConfigurationBuilder().build().getProvider();
        }
        return new AuthorityRuleConfiguration(users, provider);
    }
    
    @Override
    public Class<AuthorityRuleConfiguration> getTypeClass() {
        return AuthorityRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "AUTHORITY";
    }
    
    @Override
    public int getOrder() {
        return AuthorityOrder.ORDER;
    }
}
