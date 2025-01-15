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

package org.apache.shardingsphere.mode.node.tuple.fixture.node;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.mode.node.tuple.annotation.RepositoryTupleEntity;
import org.apache.shardingsphere.mode.node.tuple.annotation.RepositoryTupleField;
import org.apache.shardingsphere.mode.node.tuple.annotation.RepositoryTupleField.Type;
import org.apache.shardingsphere.mode.node.tuple.annotation.RepositoryTupleKeyListNameGenerator;
import org.apache.shardingsphere.mode.node.tuple.fixture.leaf.LeafYamlRuleConfiguration;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

@RepositoryTupleEntity(value = "node")
@Getter
@Setter
public final class NodeYamlRuleConfiguration implements YamlRuleConfiguration {
    
    @RepositoryTupleField(type = Type.OTHER)
    private Map<String, LeafYamlRuleConfiguration> mapValue = new HashMap<>();
    
    @RepositoryTupleField(type = Type.OTHER)
    private Collection<LeafYamlRuleConfiguration> collectionValue = new LinkedList<>();
    
    @RepositoryTupleField(type = Type.OTHER)
    private String stringValue = "";
    
    @RepositoryTupleField(type = Type.OTHER)
    private Boolean booleanValue;
    
    @RepositoryTupleField(type = Type.OTHER)
    private Integer integerValue;
    
    @RepositoryTupleField(type = Type.OTHER)
    private Long longValue;
    
    @RepositoryTupleField(type = Type.OTHER)
    private NodeYamlRuleConfigurationEnum enumValue;
    
    @RepositoryTupleField(type = Type.OTHER)
    private LeafYamlRuleConfiguration leaf;
    
    @RepositoryTupleField(type = Type.OTHER)
    @RepositoryTupleKeyListNameGenerator(RepositoryTupleKeyListNameGeneratorFixture.class)
    private Collection<String> gens = new LinkedList<>();
    
    @RepositoryTupleField(type = Type.OTHER)
    @RepositoryTupleKeyListNameGenerator(RepositoryTupleKeyListNameGeneratorFixture.class)
    private String gen;
    
    @Override
    public Class<? extends RuleConfiguration> getRuleConfigurationType() {
        return RuleConfiguration.class;
    }
}
