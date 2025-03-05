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
import org.apache.shardingsphere.infra.config.rule.scope.DatabaseRuleConfiguration;
import org.apache.shardingsphere.mode.node.rule.tuple.fixture.leaf.YamlLeafRuleConfiguration;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

@Getter
@Setter
public final class NodeRuleConfiguration implements DatabaseRuleConfiguration {
    
    private Map<String, YamlLeafRuleConfiguration> mapValue = new HashMap<>();
    
    private Collection<YamlLeafRuleConfiguration> collectionValue = new LinkedList<>();
    
    private String stringValue = "";
    
    private Boolean booleanValue;
    
    private Integer integerValue;
    
    private Long longValue;
    
    private YamlNodeRuleConfigurationEnum enumValue;
    
    private YamlLeafRuleConfiguration leaf;
    
    private Collection<String> gens = new LinkedList<>();
    
    private String gen;
}
