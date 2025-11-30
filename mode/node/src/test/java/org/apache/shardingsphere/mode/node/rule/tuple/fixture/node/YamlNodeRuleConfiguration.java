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

package org.apache.shardingsphere.mode.node.rule.tuple.fixture.node;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.mode.node.rule.tuple.annotation.RuleNodeTupleEntity;
import org.apache.shardingsphere.mode.node.rule.tuple.annotation.RuleNodeTupleField;
import org.apache.shardingsphere.mode.node.rule.tuple.annotation.RuleNodeTupleField.Type;
import org.apache.shardingsphere.mode.node.rule.tuple.annotation.RuleNodeTupleKeyListNameGenerator;
import org.apache.shardingsphere.mode.node.rule.tuple.fixture.leaf.YamlLeafRuleConfiguration;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

@RuleNodeTupleEntity("node")
@Getter
@Setter
public final class YamlNodeRuleConfiguration implements YamlRuleConfiguration {
    
    @RuleNodeTupleField(type = Type.OTHER)
    private Map<String, YamlLeafRuleConfiguration> mapValue = new HashMap<>();
    
    @RuleNodeTupleField(type = Type.OTHER)
    private Collection<YamlLeafRuleConfiguration> collectionValue = new LinkedList<>();
    
    @RuleNodeTupleField(type = Type.OTHER)
    private String stringValue = "";
    
    @RuleNodeTupleField(type = Type.OTHER)
    private Boolean booleanValue;
    
    @RuleNodeTupleField(type = Type.OTHER)
    private Integer integerValue;
    
    @RuleNodeTupleField(type = Type.OTHER)
    private Long longValue;
    
    @RuleNodeTupleField(type = Type.OTHER)
    private YamlNodeRuleConfigurationEnum enumValue;
    
    @RuleNodeTupleField(type = Type.OTHER)
    private YamlLeafRuleConfiguration leaf;
    
    @RuleNodeTupleField(type = Type.OTHER)
    @RuleNodeTupleKeyListNameGenerator(RuleNodeTupleKeyListNameGeneratorFixture.class)
    private Collection<String> gens = new LinkedList<>();
    
    @RuleNodeTupleField(type = Type.OTHER)
    @RuleNodeTupleKeyListNameGenerator(RuleNodeTupleKeyListNameGeneratorFixture.class)
    private String gen;
    
    @Override
    public Class<? extends RuleConfiguration> getRuleConfigurationType() {
        return RuleConfiguration.class;
    }
}
