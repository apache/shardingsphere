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

package org.apache.shardingsphere.test.it.yaml;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RequiredArgsConstructor
public abstract class YamlRuleConfigurationIT {
    
    private final String yamlFile;
    
    @Test
    void assertUnmarshalWithYamlFile() throws IOException {
        URL url = Thread.currentThread().getContextClassLoader().getResource(yamlFile);
        assertNotNull(url);
        YamlRootConfiguration actual = YamlEngine.unmarshal(new File(url.getFile()), YamlRootConfiguration.class);
        assertThat(actual.getRules().size(), is(1));
        assertYamlRootConfiguration(actual);
    }
    
    @Test
    void assertUnmarshalWithYamlBytes() throws IOException, URISyntaxException {
        URL url = Thread.currentThread().getContextClassLoader().getResource(yamlFile);
        assertNotNull(url);
        StringBuilder yamlContent = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(url.toURI()))) {
            String line;
            while (null != (line = reader.readLine())) {
                yamlContent.append(line).append(System.lineSeparator());
            }
        }
        YamlRootConfiguration actual = YamlEngine.unmarshal(yamlContent.toString().getBytes(), YamlRootConfiguration.class);
        assertThat(actual.getRules().size(), is(1));
        assertYamlRootConfiguration(actual);
    }
    
    protected abstract void assertYamlRootConfiguration(YamlRootConfiguration actual);
}
