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
import org.apache.shardingsphere.infra.util.yaml.datanode.RepositoryTuple;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.mode.spi.RepositoryTupleSwapper;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class RepositoryTupleSwapperIT {
    
    private final File yamlFile;
    
    @SuppressWarnings("rawtypes")
    private final RepositoryTupleSwapper swapper;
    
    @SuppressWarnings("rawtypes")
    public RepositoryTupleSwapperIT(final String yamlFileName, final RepositoryTupleSwapper swapper) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(yamlFileName);
        assertNotNull(url);
        yamlFile = new File(url.getFile());
        this.swapper = swapper;
    }
    
    @Test
    void assertSwapToRepositoryTuples() throws IOException {
        assertRepositoryTuples(getRepositoryTuples());
    }
    
    @SuppressWarnings("unchecked")
    private Collection<RepositoryTuple> getRepositoryTuples() throws IOException {
        YamlRootConfiguration yamlRootConfig = YamlEngine.unmarshal(yamlFile, YamlRootConfiguration.class);
        assertThat(yamlRootConfig.getRules().size(), is(1));
        return (Collection<RepositoryTuple>) swapper.swapToRepositoryTuples(yamlRootConfig.getRules().iterator().next());
    }
    
    protected abstract void assertRepositoryTuples(Collection<RepositoryTuple> actualRepositoryTuples);
    
    @Test
    void assertSwapToObject() throws IOException {
        assertThat(getActualYamlContent(), is(getExpectedYamlContent()));
    }
    
    @SuppressWarnings("unchecked")
    private String getActualYamlContent() throws IOException {
        Collection<RepositoryTuple> repositoryTuples = getRepositoryTuples().stream()
                .map(each -> new RepositoryTuple(String.format("/metadata/foo_db/rules/%s/%s/versions/0", swapper.getRuleTypeName(), each.getKey()), each.getValue())).collect(Collectors.toList());
        Optional<YamlRuleConfiguration> actualYamlRuleConfig = swapper.swapToObject(repositoryTuples);
        assertTrue(actualYamlRuleConfig.isPresent());
        YamlRootConfiguration yamlRootConfig = new YamlRootConfiguration();
        yamlRootConfig.setRules(Collections.singletonList(actualYamlRuleConfig.get()));
        return YamlEngine.marshal(yamlRootConfig);
    }
    
    private String getExpectedYamlContent() throws IOException {
        return Files.readAllLines(yamlFile.toPath()).stream().filter(each -> !each.contains("#") && !each.isEmpty()).collect(Collectors.joining(System.lineSeparator())) + System.lineSeparator();
    }
}
