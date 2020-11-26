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

package org.apache.shardingsphere.governance.core.yaml.swapper;

import lombok.SneakyThrows;
import org.apache.shardingsphere.governance.core.yaml.config.schema.YamlSchema;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class SchemaYamlSwapperTest {
    
    private static final String YAML = "yaml/schema.yaml";
    
    private static final String YAML_WITHOUT_TABLE = "yaml/schema-without-table.yaml";
    
    @Test
    public void assertSwapToYamlSchema() {
        ShardingSphereSchema schema = new SchemaYamlSwapper().swapToObject(YamlEngine.unmarshal(readYAML(YAML), YamlSchema.class));
        YamlSchema yamlSchema = new SchemaYamlSwapper().swapToYamlConfiguration(schema);
        assertNotNull(yamlSchema);
        assertThat(yamlSchema.getTables().keySet(), is(Collections.singleton("t_order")));
        assertThat(yamlSchema.getTables().get("t_order").getIndexes().keySet(), is(Collections.singleton("primary")));
        assertThat(yamlSchema.getTables().get("t_order").getColumns().keySet(), is(Collections.singleton("id")));
    }
    
    @Test
    public void assertSwapToShardingSphereSchema() {
        YamlSchema yamlSchema = YamlEngine.unmarshal(readYAML(YAML), YamlSchema.class);
        ShardingSphereSchema schema = new SchemaYamlSwapper().swapToObject(yamlSchema);
        assertThat(schema.getAllTableNames(), is(Collections.singleton("t_order")));
        assertThat(schema.get("t_order").getIndexes().keySet(), is(Collections.singleton("primary")));
        assertThat(schema.getAllColumnNames("t_order").size(), is(1));
        assertThat(schema.get("t_order").getColumns().keySet(), is(Collections.singleton("id")));
    }
    
    @Test
    public void assertSwapToYamlSchemaWithoutTable() {
        ShardingSphereSchema schema = new SchemaYamlSwapper().swapToObject(YamlEngine.unmarshal(readYAML(YAML_WITHOUT_TABLE), YamlSchema.class));
        YamlSchema yamlSchema = new SchemaYamlSwapper().swapToYamlConfiguration(schema);
        assertNotNull(yamlSchema);
        assertThat(yamlSchema.getTables().size(), is(0));
    }
    
    @Test
    public void assertSwapToShardingSphereSchemaWithoutTable() {
        YamlSchema yamlSchema = YamlEngine.unmarshal(readYAML(YAML_WITHOUT_TABLE), YamlSchema.class);
        ShardingSphereSchema schema = new SchemaYamlSwapper().swapToObject(yamlSchema);
        assertThat(schema.getAllTableNames().size(), is(0));
    }
    
    @SneakyThrows({URISyntaxException.class, IOException.class})
    private String readYAML(final String yamlFile) {
        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource(yamlFile).toURI()))
                .stream().filter(each -> !each.startsWith("#")).map(each -> each + System.lineSeparator()).collect(Collectors.joining());
    }
}
