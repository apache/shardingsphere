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
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereConstraint;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlShardingSphereSchema;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.apache.shardingsphere.test.matcher.ShardingSphereAssertionMatchers.deepEqual;
import static org.hamcrest.MatcherAssert.assertThat;

class YamlSchemaSwapperTest {
    
    private static final String YAML_FILE = "yaml/schema/schema.yaml";
    
    private static final String EMPTY_YAML_FILE = "yaml/schema/empty-schema.yaml";
    
    private final YamlSchemaSwapper swapper = new YamlSchemaSwapper();
    
    @Test
    void assertSwapToYamlConfiguration() {
        ShardingSphereSchema schema = createShardingSphereSchema();
        YamlShardingSphereSchema actual = swapper.swapToYamlConfiguration(schema);
        YamlShardingSphereSchema expected = unmarshal(YAML_FILE);
        assertThat(actual, deepEqual(expected));
    }
    
    @Test
    void assertSwapToObject() {
        ShardingSphereSchema actual = swapper.swapToObject(unmarshal(YAML_FILE));
        ShardingSphereSchema expected = createShardingSphereSchema();
        assertThat(actual, deepEqual(expected));
    }
    
    @Test
    void assertSwapToObjectWithEmptySchema() {
        ShardingSphereSchema actual = swapper.swapToObject(unmarshal(EMPTY_YAML_FILE));
        ShardingSphereSchema expected = new ShardingSphereSchema("foo_schema");
        assertThat(actual, deepEqual(expected));
    }
    
    @SneakyThrows({URISyntaxException.class, IOException.class})
    private YamlShardingSphereSchema unmarshal(final String yamlFile) {
        String yamlContent = Files.readAllLines(Paths.get(ClassLoader.getSystemResource(yamlFile).toURI())).stream().collect(Collectors.joining(System.lineSeparator()));
        return YamlEngine.unmarshal(yamlContent, YamlShardingSphereSchema.class);
    }
    
    private ShardingSphereSchema createShardingSphereSchema() {
        ShardingSphereTable table = new ShardingSphereTable(null,
                Collections.singleton(new ShardingSphereColumn("foo_col", 0, true, false, false, true, false, false)),
                Collections.singleton(new ShardingSphereIndex("PRIMARY")),
                Collections.singleton(new ShardingSphereConstraint("foo_constraint", "foo_tbl")), null);
        ShardingSphereView view = new ShardingSphereView("foo_view", "SELECT 1");
        return new ShardingSphereSchema("foo_schema", Collections.singletonMap("foo_tbl", table), Collections.singletonMap("foo_view", view));
    }
}
