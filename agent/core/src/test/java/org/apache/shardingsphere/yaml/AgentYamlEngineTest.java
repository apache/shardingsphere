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

package org.apache.shardingsphere.yaml;

import org.apache.shardingsphere.agent.core.advisor.config.yaml.entity.YamlAdvisorsConfiguration;
import org.apache.shardingsphere.agent.core.plugin.config.yaml.entity.YamlAgentConfiguration;
import org.apache.shardingsphere.agent.core.yaml.AgentYamlEngine;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AgentYamlEngineTest {
    
    @Test
    void assertUnmarshalYamlAgentConfiguration() {
        InputStream inputStream = getClass().getResourceAsStream("/conf/agent.yaml");
        YamlAgentConfiguration actual = AgentYamlEngine.unmarshalYamlAgentConfiguration(inputStream);
        
        assertNotNull(actual);
    }
    
    @Test
    void assertUnmarshalYamlAdvisorsConfiguration() {
        InputStream inputStream = getClass().getResourceAsStream("/META-INF/conf/advisors.yaml");
        YamlAdvisorsConfiguration actual = AgentYamlEngine.unmarshalYamlAdvisorsConfiguration(inputStream);
        
        assertNotNull(actual);
    }
}
