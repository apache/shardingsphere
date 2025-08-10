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

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.apache.shardingsphere.test.infra.framework.matcher.ShardingSphereAssertionMatchers.deepEqual;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class YamlRuleConfigurationIT {
    
    private final String yamlFile;
    
    private final RuleConfiguration expectedRuleConfig;
    
    @SuppressWarnings("rawtypes")
    private final YamlRuleConfigurationSwapper swapper;
    
    protected YamlRuleConfigurationIT(final String yamlFile, final RuleConfiguration expectedRuleConfig) {
        this.yamlFile = yamlFile;
        this.expectedRuleConfig = expectedRuleConfig;
        swapper = OrderedSPILoader.getServices(YamlRuleConfigurationSwapper.class, Collections.singleton(expectedRuleConfig)).get(expectedRuleConfig);
    }
    
    @Test
    void assertUnmarshal() throws IOException, URISyntaxException {
        URL url = Thread.currentThread().getContextClassLoader().getResource(yamlFile);
        assertNotNull(url);
        YamlRootConfiguration yamlRootConfigFromFile = YamlEngine.unmarshal(new File(url.getFile()), YamlRootConfiguration.class);
        YamlRootConfiguration yamlRootConfigFromBytes = YamlEngine.unmarshal(
                Files.readAllLines(Paths.get(url.toURI())).stream().collect(Collectors.joining(System.lineSeparator())).getBytes(), YamlRootConfiguration.class);
        assertThat(yamlRootConfigFromFile, deepEqual(yamlRootConfigFromBytes));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertSwapToYamlConfiguration() throws IOException {
        URL url = Thread.currentThread().getContextClassLoader().getResource(yamlFile);
        assertNotNull(url);
        YamlRootConfiguration actual = YamlEngine.unmarshal(new File(url.getFile()), YamlRootConfiguration.class);
        assertThat(actual.getRules().size(), is(1));
        YamlRuleConfiguration actualYAMLRuleConfig = actual.getRules().iterator().next();
        YamlRuleConfiguration expectedYAMLRuleConfig = (YamlRuleConfiguration) swapper.swapToYamlConfiguration(expectedRuleConfig);
        if (!assertYamlConfiguration(actualYAMLRuleConfig)) {
            assertThat(actualYAMLRuleConfig, deepEqual(expectedYAMLRuleConfig));
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertSwapToObject() throws IOException {
        URL url = Thread.currentThread().getContextClassLoader().getResource(yamlFile);
        assertNotNull(url);
        YamlRootConfiguration actual = YamlEngine.unmarshal(new File(url.getFile()), YamlRootConfiguration.class);
        RuleConfiguration actualRuleConfig = (RuleConfiguration) swapper.swapToObject(actual.getRules().iterator().next());
        assertThat(actualRuleConfig, deepEqual(expectedRuleConfig));
    }
    
    // TODO should remove the method when yaml rule swapper fixed by map's key
    protected boolean assertYamlConfiguration(final YamlRuleConfiguration actual) {
        return false;
    }
}
