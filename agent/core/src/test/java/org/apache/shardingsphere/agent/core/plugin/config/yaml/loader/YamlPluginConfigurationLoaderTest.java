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

import org.apache.shardingsphere.agent.core.plugin.config.yaml.entity.YamlAgentConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlPluginConfigurationLoaderTest {
    
    @Test
    void assertLoad(@TempDir final Path tempDir) throws IOException {
        Path yamlFile = tempDir.resolve("agent.yaml");
        String yamlContent = ""
                + "plugins:\n"
                + "  logging:\n"
                + "    FILE:\n"
                + "      host: localhost\n"
                + "      port: 3307\n"
                + "      password: root\n"
                + "      props:\n"
                + "        level: INFO\n";
        Files.write(yamlFile, yamlContent.getBytes(StandardCharsets.UTF_8));
        Optional<YamlAgentConfiguration> actual = YamlPluginConfigurationLoader.load(yamlFile.toFile());
        assertTrue(actual.isPresent());
        assertThat(actual.get().getPlugins().getLogging().get("FILE").getHost(), is("localhost"));
        assertThat(actual.get().getPlugins().getLogging().get("FILE").getPort(), is(3307));
        assertThat(actual.get().getPlugins().getLogging().get("FILE").getPassword(), is("root"));
        assertThat(actual.get().getPlugins().getLogging().get("FILE").getProps().get("level"), is("INFO"));
    }
    
    @Test
    void assertLoadEmptyYaml(@TempDir final Path tempDir) throws IOException {
        File yamlFile = tempDir.resolve("empty.yaml").toFile();
        Files.write(yamlFile.toPath(), new byte[0]);
        assertFalse(YamlPluginConfigurationLoader.load(yamlFile).isPresent());
    }
}
