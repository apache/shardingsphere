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

import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlGlobalRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.mode.node.rule.tuple.RuleNodeTuple;
import org.apache.shardingsphere.mode.node.rule.tuple.YamlRuleNodeTupleSwapperEngine;
import org.apache.shardingsphere.mode.node.rule.tuple.annotation.RuleNodeTupleEntity;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class YamlRuleNodeTupleSwapperEngineIT {
    
    private final File yamlFile;
    
    private final YamlRuleNodeTupleSwapperEngine engine;
    
    private final String databaseName;
    
    protected YamlRuleNodeTupleSwapperEngineIT(final String yamlFileName) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(yamlFileName);
        assertNotNull(url);
        yamlFile = new File(url.getFile());
        engine = new YamlRuleNodeTupleSwapperEngine();
        databaseName = "foo_db";
    }
    
    @Test
    void assertSwapToTuples() throws IOException {
        YamlRuleConfiguration yamlRuleConfig = loadYamlRuleConfiguration();
        List<RuleNodeTuple> tuples = yamlRuleConfig instanceof YamlGlobalRuleConfiguration
                ? Collections.singletonList(engine.swapToTuple((YamlGlobalRuleConfiguration) yamlRuleConfig))
                : new ArrayList<>(engine.swapToTuples(databaseName, yamlRuleConfig));
        assertRuleNodeTuples(tuples, yamlRuleConfig);
    }
    
    private YamlRuleConfiguration loadYamlRuleConfiguration() throws IOException {
        YamlRootConfiguration yamlRootConfig = YamlEngine.unmarshal(yamlFile, YamlRootConfiguration.class);
        assertThat(yamlRootConfig.getRules().size(), is(1));
        return yamlRootConfig.getRules().iterator().next();
    }
    
    protected abstract void assertRuleNodeTuples(List<RuleNodeTuple> actualTuples, YamlRuleConfiguration expectedYamlRuleConfig);
    
    protected void assertRuleNodeTuple(final RuleNodeTuple actual, final String expectedKey, final Object expectedValue) {
        assertThat(actual.getPath(), containsString(expectedKey));
        assertThat(actual.getContent(), is(isSimpleObject(expectedValue) ? expectedValue : YamlEngine.marshal(expectedValue)));
    }
    
    private boolean isSimpleObject(final Object expectedValue) {
        return expectedValue instanceof Boolean || expectedValue instanceof Integer || expectedValue instanceof Long || expectedValue instanceof String;
    }
    
    @Test
    void assertSwapToYamlRuleConfiguration() throws IOException {
        String actualYamlContent = getActualYamlContent();
        String expectedYamlContent = getExpectedYamlContent();
        if (!sameYamlContext(actualYamlContent, expectedYamlContent)) {
            assertThat(actualYamlContent, is(expectedYamlContent));
        }
    }
    
    private String getActualYamlContent() throws IOException {
        YamlRuleConfiguration yamlRuleConfig = loadYamlRuleConfiguration();
        RuleNodeTupleEntity entity = yamlRuleConfig.getClass().getAnnotation(RuleNodeTupleEntity.class);
        String ruleType = entity.value();
        YamlRuleConfiguration actualYamlRuleConfig = entity.leaf()
                ? engine.swapToYamlGlobalRuleConfiguration(ruleType, engine.swapToTuple((YamlGlobalRuleConfiguration) yamlRuleConfig).getContent())
                : engine.swapToYamlDatabaseRuleConfiguration(databaseName, ruleType, engine.swapToTuples(databaseName, yamlRuleConfig));
        YamlRootConfiguration yamlRootConfig = new YamlRootConfiguration();
        yamlRootConfig.setRules(Collections.singletonList(actualYamlRuleConfig));
        return YamlEngine.marshal(yamlRootConfig);
    }
    
    private String getExpectedYamlContent() throws IOException {
        String content = Files.readAllLines(yamlFile.toPath()).stream()
                .filter(each -> !each.contains("#") && !each.isEmpty()).collect(Collectors.joining(System.lineSeparator())) + System.lineSeparator();
        return YamlEngine.marshal(YamlEngine.unmarshal(content, Map.class));
    }
    
    private boolean sameYamlContext(final String actualYamlContext, final String expectedYamlContext) {
        char[] actualArray = actualYamlContext.toCharArray();
        char[] expectedArray = expectedYamlContext.toCharArray();
        Arrays.sort(actualArray);
        Arrays.sort(expectedArray);
        return Arrays.equals(actualArray, expectedArray);
    }
}
