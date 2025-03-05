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
import org.apache.shardingsphere.encrypt.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.rule.YamlEncryptTableRuleConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.mode.node.rule.tuple.annotation.RuleNodeTupleEntity;
import org.apache.shardingsphere.mode.node.rule.tuple.annotation.RuleNodeTupleField;
import org.apache.shardingsphere.mode.node.rule.tuple.annotation.RuleNodeTupleField.Type;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Encrypt rule configuration for YAML.
 */
@RuleNodeTupleEntity("encrypt")
@Getter
@Setter
public final class YamlEncryptRuleConfiguration implements YamlRuleConfiguration {
    
    @RuleNodeTupleField(type = Type.TABLE)
    private Map<String, YamlEncryptTableRuleConfiguration> tables = new LinkedHashMap<>();
    
    @RuleNodeTupleField(type = Type.ALGORITHM)
    private Map<String, YamlAlgorithmConfiguration> encryptors = new LinkedHashMap<>();
    
    @Override
    public Class<EncryptRuleConfiguration> getRuleConfigurationType() {
        return EncryptRuleConfiguration.class;
    }
}
