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
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlShardingSphereTable;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.apache.shardingsphere.test.matcher.ShardingSphereAssertionMatchers.deepEqual;
import static org.hamcrest.MatcherAssert.assertThat;

class YamlTableSwapperTest {
    
    private static final String YAML_FILE = "yaml/schema/table.yaml";
    
    private static final String EMPTY_YAML_FILE = "yaml/schema/empty-table.yaml";
    
    private final YamlTableSwapper swapper = new YamlTableSwapper();
    
    @Test
    void assertSwapToYamlConfiguration() {
        ShardingSphereTable table = createShardingSphereTable();
        YamlShardingSphereTable actual = swapper.swapToYamlConfiguration(table);
        YamlShardingSphereTable expected = unmarshal(YAML_FILE);
        assertThat(actual, deepEqual(expected));
    }
    
    @Test
    void assertSwapToObject() {
        ShardingSphereTable actual = swapper.swapToObject(unmarshal(YAML_FILE));
        ShardingSphereTable expected = createShardingSphereTable();
        assertThat(actual, deepEqual(expected));
    }
    
    @Test
    void assertSwapToObjectWithEmptySchema() {
        ShardingSphereTable actual = swapper.swapToObject(unmarshal(EMPTY_YAML_FILE));
        ShardingSphereTable expected = new ShardingSphereTable();
        assertThat(actual, deepEqual(expected));
    }
    
    @SneakyThrows({URISyntaxException.class, IOException.class})
    private YamlShardingSphereTable unmarshal(final String yamlFile) {
        String yamlContent = Files.readAllLines(Paths.get(ClassLoader.getSystemResource(yamlFile).toURI())).stream().collect(Collectors.joining(System.lineSeparator()));
        return YamlEngine.unmarshal(yamlContent, YamlShardingSphereTable.class);
    }
    
    private ShardingSphereTable createShardingSphereTable() {
        return new ShardingSphereTable("foo_tbl",
                Collections.singleton(new ShardingSphereColumn("foo_col", 0, true, false, false, true, false, false)),
                Collections.singleton(new ShardingSphereIndex("PRIMARY")),
                Collections.singleton(new ShardingSphereConstraint("foo_constraint", "foo_tbl")), null);
    }
}
