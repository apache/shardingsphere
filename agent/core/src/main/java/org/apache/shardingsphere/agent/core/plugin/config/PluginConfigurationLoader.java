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

package org.apache.shardingsphere.agent.core.plugin.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.agent.api.PluginConfiguration;
import org.apache.shardingsphere.agent.core.plugin.config.yaml.loader.YamlPluginConfigurationLoader;
import org.apache.shardingsphere.agent.core.plugin.config.yaml.swapper.YamlPluginsConfigurationSwapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

/**
 * Plugin configuration loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PluginConfigurationLoader {
    
    /**
     * Load plugin configurations.
     *
     * @param agentRootPath agent root path
     * @return plugin configurations
     * @throws IOException IO exception
     */
    public static Map<String, PluginConfiguration> load(final File agentRootPath) throws IOException {
        return YamlPluginConfigurationLoader.load(new File(agentRootPath, Paths.get("conf", "agent.yaml").toString())).map(YamlPluginsConfigurationSwapper::swap).orElse(Collections.emptyMap());
    }
}
