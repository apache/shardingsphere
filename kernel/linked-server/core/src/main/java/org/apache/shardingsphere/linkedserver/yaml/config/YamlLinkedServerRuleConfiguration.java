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

package org.apache.shardingsphere.linkedserver.yaml.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.linkedserver.config.LinkedServerRuleConfiguration;
import org.apache.shardingsphere.linkedserver.yaml.config.rule.YamlLinkedServerConfiguration;
import org.apache.shardingsphere.mode.node.rule.tuple.annotation.RuleNodeTupleEntity;
import org.apache.shardingsphere.mode.node.rule.tuple.annotation.RuleNodeTupleField;
import org.apache.shardingsphere.mode.node.rule.tuple.annotation.RuleNodeTupleField.Type;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Linked server rule configuration for YAML.
 */
@RuleNodeTupleEntity("linked_server")
@Getter
@Setter
public final class YamlLinkedServerRuleConfiguration implements YamlRuleConfiguration {
    
    @RuleNodeTupleField(type = Type.TABLE)
    private Map<String, YamlLinkedServerConfiguration> servers = new LinkedHashMap<>();
    
    @Override
    public Class<LinkedServerRuleConfiguration> getRuleConfigurationType() {
        return LinkedServerRuleConfiguration.class;
    }
}
