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

package org.apache.shardingsphere.agent.core.config;

import com.google.common.base.Preconditions;
import java.io.File;
import java.io.IOException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.agent.core.common.AgentPathLocator;
import org.apache.shardingsphere.agent.core.yaml.YamlEngine;

/**
 * Agent configuration loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AgentConfigurationLoader {
    
    private static final String DEFAULT_CONFIG_PATH = "/conf/agent.yaml";
    
    private static final String SPECIFIED_CONFIG_PATH = "agent-path";
    
    /**
     * Load configuration of ShardingSphere-Agent.
     *
     * @return configuration of ShardingSphere-Agent
     * @throws IOException IO exception
     */
    public static AgentConfiguration load() throws IOException {
        String specifiedConfigPath = System.getProperty(SPECIFIED_CONFIG_PATH);
        File configFile = null == specifiedConfigPath ? new File(AgentPathLocator.findPath(), DEFAULT_CONFIG_PATH) : new File(specifiedConfigPath);
        return loadAgentConfiguration(configFile);
    }

    private static AgentConfiguration loadAgentConfiguration(final File yamlFile) throws IOException {
        AgentConfiguration result = YamlEngine.unmarshal(yamlFile, AgentConfiguration.class);
        Preconditions.checkNotNull(result, "Agent configuration file `%s` is invalid.", yamlFile.getName());
        return result;
    }
}
