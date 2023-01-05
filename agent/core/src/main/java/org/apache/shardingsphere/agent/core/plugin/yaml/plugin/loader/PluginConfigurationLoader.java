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

package org.apache.shardingsphere.agent.core.plugin.yaml.plugin.loader;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.agent.api.PluginConfiguration;
import org.apache.shardingsphere.agent.core.plugin.yaml.plugin.entity.YamlAgentConfiguration;
import org.apache.shardingsphere.agent.core.plugin.yaml.plugin.swapper.YamlPluginsConfigurationSwapper;
import org.apache.shardingsphere.agent.core.plugin.yaml.path.AgentPathBuilder;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Plugin configuration loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PluginConfigurationLoader {
    
    private static final String CONFIG_PATH = "/conf/agent.yaml";
    
    /**
     * Load plugin configurations.
     *
     * @return plugin configurations
     * @throws IOException IO exception
     */
    public static Map<String, PluginConfiguration> load() throws IOException {
        File configFile = new File(AgentPathBuilder.getAgentPath(), CONFIG_PATH);
        return load(configFile).map(YamlPluginsConfigurationSwapper::swap).orElse(Collections.emptyMap());
    }
    
    private static Optional<YamlAgentConfiguration> load(final File yamlFile) throws IOException {
        try (
                FileInputStream fileInputStream = new FileInputStream(yamlFile);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream)) {
            YamlAgentConfiguration result = new Yaml().loadAs(inputStreamReader, YamlAgentConfiguration.class);
            return null == result ? Optional.empty() : Optional.of(result);
        }
    }
}
