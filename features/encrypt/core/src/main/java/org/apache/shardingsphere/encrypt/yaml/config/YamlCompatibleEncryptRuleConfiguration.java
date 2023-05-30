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

package org.apache.shardingsphere.encrypt.yaml.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.encrypt.api.config.CompatibleEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.rule.YamlCompatibleEncryptTableRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Encrypt rule configuration for YAML.
 * 
 * @deprecated Should use new api, compatible api will remove in next version. 
 */
@Getter
@Setter
@Deprecated
public final class YamlCompatibleEncryptRuleConfiguration implements YamlRuleConfiguration {
    
    private Map<String, YamlCompatibleEncryptTableRuleConfiguration> tables = new LinkedHashMap<>();
    
    private Map<String, YamlAlgorithmConfiguration> encryptors = new LinkedHashMap<>();
    
    @Override
    public Class<CompatibleEncryptRuleConfiguration> getRuleConfigurationType() {
        return CompatibleEncryptRuleConfiguration.class;
    }
}
