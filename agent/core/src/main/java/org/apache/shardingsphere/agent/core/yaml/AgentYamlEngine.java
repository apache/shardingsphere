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

package org.apache.shardingsphere.agent.core.yaml;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.agent.core.advisor.config.yaml.entity.YamlAdvisorsConfiguration;
import org.apache.shardingsphere.agent.core.plugin.config.yaml.entity.YamlAgentConfiguration;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;

/**
 * Agent YAML engine.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AgentYamlEngine {
    
    /**
     * Unmarshal YAML agent configuration YAML.
     *
     * @param inputStream input stream
     * @return YAML agent configuration
     */
    public static YamlAgentConfiguration unmarshalYamlAgentConfiguration(final InputStream inputStream) {
        return new Yaml(new AgentYamlConstructor(YamlAgentConfiguration.class, createLoaderOptions())).loadAs(inputStream, YamlAgentConfiguration.class);
    }
    
    /**
     * Unmarshal YAML advisors configuration YAML.
     *
     * @param inputStream input stream
     * @return YAML advisors configuration
     */
    public static YamlAdvisorsConfiguration unmarshalYamlAdvisorsConfiguration(final InputStream inputStream) {
        return new Yaml(new AgentYamlConstructor(YamlAdvisorsConfiguration.class, createLoaderOptions())).loadAs(inputStream, YamlAdvisorsConfiguration.class);
    }
    
    private static LoaderOptions createLoaderOptions() {
        LoaderOptions result = new LoaderOptions();
        result.setMaxAliasesForCollections(1000);
        result.setCodePointLimit(Integer.MAX_VALUE);
        return result;
    }
}
