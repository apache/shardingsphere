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

package org.apache.shardingsphere.infra.yaml.schema.swapper;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlShardingSphereView;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.apache.shardingsphere.test.matcher.ShardingSphereAssertionMatchers.deepEqual;
import static org.hamcrest.MatcherAssert.assertThat;

class YamlViewSwapperTest {
    
    private static final String YAML_FILE = "yaml/schema/view.yaml";
    
    private final YamlViewSwapper swapper = new YamlViewSwapper();
    
    @Test
    void assertSwapToYamlConfiguration() {
        ShardingSphereView view = createShardingSphereView();
        YamlShardingSphereView actual = swapper.swapToYamlConfiguration(view);
        YamlShardingSphereView expected = unmarshal(YAML_FILE);
        assertThat(actual, deepEqual(expected));
    }
    
    @Test
    void assertSwapToObject() {
        ShardingSphereView actual = swapper.swapToObject(unmarshal(YAML_FILE));
        ShardingSphereView expected = createShardingSphereView();
        assertThat(actual, deepEqual(expected));
    }
    
    @SneakyThrows({URISyntaxException.class, IOException.class})
    private YamlShardingSphereView unmarshal(final String yamlFile) {
        String yamlContent = Files.readAllLines(Paths.get(ClassLoader.getSystemResource(yamlFile).toURI())).stream().collect(Collectors.joining(System.lineSeparator()));
        return YamlEngine.unmarshal(yamlContent, YamlShardingSphereView.class);
    }
    
    private ShardingSphereView createShardingSphereView() {
        return new ShardingSphereView("foo_view", "SELECT 1");
    }
}
