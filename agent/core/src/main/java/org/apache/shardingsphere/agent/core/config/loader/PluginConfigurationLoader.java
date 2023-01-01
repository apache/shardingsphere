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

package org.apache.shardingsphere.agent.core.config.loader;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.agent.core.config.yaml.entity.YamlPluginsConfiguration;
import org.apache.shardingsphere.agent.core.config.yaml.swapper.YamlPluginsConfigurationSwapper;
import org.apache.shardingsphere.agent.core.path.AgentPathBuilder;
import org.apache.shardingsphere.agent.api.PluginConfiguration;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

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
        return YamlPluginsConfigurationSwapper.swap(load(configFile));
    }
    
    private static YamlPluginsConfiguration load(final File yamlFile) throws IOException {
        try (
                FileInputStream fileInputStream = new FileInputStream(yamlFile);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream)) {
            YamlPluginsConfiguration result = new Yaml().loadAs(inputStreamReader, YamlPluginsConfiguration.class);
            Preconditions.checkNotNull(result, "Agent configuration file `%s` is invalid.", yamlFile.getName());
            return result;
        }
    }
}
