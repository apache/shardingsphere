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

package org.apache.shardingsphere.agent.core.plugin.config.yaml.loader;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.agent.core.plugin.config.yaml.entity.YamlAgentConfiguration;
import org.apache.shardingsphere.agent.core.yaml.AgentYamlEngine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Plugin configuration loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlPluginConfigurationLoader {
    
    /**
     * Load plugin configurations.
     *
     * @param yamlFile yaml file
     * @return plugin configurations
     * @throws IOException IO exception
     */
    public static Optional<YamlAgentConfiguration> load(final File yamlFile) throws IOException {
        try (InputStream fileInputStream = Files.newInputStream(Paths.get(yamlFile.toURI()))) {
            YamlAgentConfiguration result = AgentYamlEngine.unmarshalYamlAgentConfiguration(fileInputStream);
            return null == result ? Optional.empty() : Optional.of(result);
        }
    }
}
