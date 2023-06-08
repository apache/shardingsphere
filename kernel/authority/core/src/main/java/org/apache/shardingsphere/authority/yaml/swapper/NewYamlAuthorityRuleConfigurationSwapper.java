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
import org.apache.shardingsphere.authority.converter.YamlUsersConfigurationConverter;
import org.apache.shardingsphere.authority.metadata.converter.AuthorityNodeConverter;
import org.apache.shardingsphere.authority.rule.builder.DefaultAuthorityRuleConfigurationBuilder;
import org.apache.shardingsphere.authority.yaml.config.YamlAuthorityRuleConfiguration;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.datanode.YamlDataNode;
import org.apache.shardingsphere.infra.yaml.config.swapper.algorithm.YamlAlgorithmConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.NewYamlRuleConfigurationSwapper;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * TODO Rename YamlAuthorityRuleConfigurationSwapper when metadata structure adjustment completed. #25485
 * New YAML Authority rule configuration swapper.
 */
public final class NewYamlAuthorityRuleConfigurationSwapper implements NewYamlRuleConfigurationSwapper<AuthorityRuleConfiguration> {
    
    private final YamlAlgorithmConfigurationSwapper algorithmSwapper = new YamlAlgorithmConfigurationSwapper();
    
    @Override
    public Collection<YamlDataNode> swapToDataNodes(final AuthorityRuleConfiguration data) {
        return Collections.singletonList(new YamlDataNode(AuthorityNodeConverter.getRootNode(), YamlEngine.marshal(swapToYamlConfiguration(data))));
        
    }
    
    private YamlAuthorityRuleConfiguration swapToYamlConfiguration(final AuthorityRuleConfiguration data) {
        YamlAuthorityRuleConfiguration result = new YamlAuthorityRuleConfiguration();
        result.setPrivilege(algorithmSwapper.swapToYamlConfiguration(data.getAuthorityProvider()));
        result.setUsers(YamlUsersConfigurationConverter.convertToYamlUserConfiguration(data.getUsers()));
        result.setDefaultAuthenticator(data.getDefaultAuthenticator());
        if (!data.getAuthenticators().isEmpty()) {
            data.getAuthenticators().forEach((key, value) -> result.getAuthenticators().put(key, algorithmSwapper.swapToYamlConfiguration(value)));
        }
        return result;
    }
    
    @Override
    public AuthorityRuleConfiguration swapToObject(final Collection<YamlDataNode> dataNodes) {
        for (YamlDataNode each : dataNodes) {
            Optional<String> version = AuthorityNodeConverter.getVersion(each.getKey());
            if (!version.isPresent()) {
                continue;
            }
            return YamlEngine.unmarshal(each.getValue(), AuthorityRuleConfiguration.class);
        }
        return new AuthorityRuleConfiguration(Collections.emptyList(), new DefaultAuthorityRuleConfigurationBuilder().build().getAuthorityProvider(), "");
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
