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

package org.apache.shardingsphere.authority.yaml.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlAlgorithmConfiguration;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Authority configuration for YAML.
 */
@Getter
@Setter
public final class YamlAuthorityConfiguration implements YamlRuleConfiguration {
    
    private Collection<YamlAuthorityUserConfiguration> users = new LinkedList<>();
    
    private YamlAlgorithmConfiguration privilege;
    
    private Map<String, YamlAlgorithmConfiguration> authenticators = new LinkedHashMap<>();
    
    private String defaultAuthenticator;
    
    @Override
    public Class<AuthorityRuleConfiguration> getRuleConfigurationType() {
        return AuthorityRuleConfiguration.class;
    }
    
    /**
     * Convert to yaml authority rule configuration.
     *
     * @return yaml authority rule configuration
     */
    public YamlAuthorityRuleConfiguration convertToYamlAuthorityRuleConfiguration() {
        YamlAuthorityRuleConfiguration result = new YamlAuthorityRuleConfiguration();
        result.setUsers(users.stream().map(Objects::toString).collect(Collectors.toCollection(LinkedList::new)));
        result.setProvider(privilege);
        return result;
    }
}
