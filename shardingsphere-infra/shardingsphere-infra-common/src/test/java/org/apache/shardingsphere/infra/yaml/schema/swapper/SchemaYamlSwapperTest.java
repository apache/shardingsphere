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
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlColumnMetaData;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlSchema;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlTableMetaData;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class SchemaYamlSwapperTest {
    
    private static final String YAML = "yaml/schema/schema.yaml";
    
    private static final String YAML_WITHOUT_TABLE = "yaml/schema/schema-without-table.yaml";
    
    @Test
    public void assertSwapToYamlSchema() {
        ShardingSphereSchema schema = new SchemaYamlSwapper().swapToObject(YamlEngine.unmarshal(readYAML(YAML), YamlSchema.class));
        YamlSchema yamlSchema = new SchemaYamlSwapper().swapToYamlConfiguration(schema);
        assertNotNull(yamlSchema);
        assertThat(yamlSchema.getTables().keySet(), is(Collections.singleton("t_order")));
        YamlTableMetaData yamlTableMetaData = yamlSchema.getTables().get("t_order");
        assertThat(yamlTableMetaData.getIndexes().keySet(), is(Collections.singleton("primary")));
        assertColumn(yamlTableMetaData.getColumns());
    }
    
    private void assertColumn(final Map<String, YamlColumnMetaData> columns) {
        assertThat(columns.size(), is(2));
        YamlColumnMetaData idColumn = columns.get("id");
        assertThat(idColumn.getName(), is("id"));
        assertThat(idColumn.isCaseSensitive(), is(false));
        assertThat(idColumn.getDataType(), is(0));
        assertThat(idColumn.isGenerated(), is(false));
        assertThat(idColumn.isPrimaryKey(), is(true));
        YamlColumnMetaData nameColumn = columns.get("name");
        assertThat(nameColumn.getName(), is("name"));
        assertThat(nameColumn.isCaseSensitive(), is(true));
        assertThat(nameColumn.getDataType(), is(10));
        assertThat(nameColumn.isGenerated(), is(true));
        assertThat(nameColumn.isPrimaryKey(), is(false));
    }
    
    @Test
    public void assertSwapToShardingSphereSchema() {
        YamlSchema yamlSchema = YamlEngine.unmarshal(readYAML(YAML), YamlSchema.class);
        ShardingSphereSchema schema = new SchemaYamlSwapper().swapToObject(yamlSchema);
        assertThat(schema.getAllTableNames(), is(Collections.singleton("t_order")));
        assertThat(schema.get("t_order").getIndexes().keySet(), is(Collections.singleton("primary")));
        assertThat(schema.getAllColumnNames("t_order").size(), is(2));
        assertTrue(schema.containsColumn("t_order", "id"));
        assertTrue(schema.containsColumn("t_order", "name"));
    }
    
    @Test
    public void assertSwapToYamlSchemaWithoutTable() {
        ShardingSphereSchema schema = new SchemaYamlSwapper().swapToObject(YamlEngine.unmarshal(readYAML(YAML_WITHOUT_TABLE), YamlSchema.class));
        assertTrue(new SchemaYamlSwapper().swapToYamlConfiguration(schema).getTables().isEmpty());
    }
    
    @Test
    public void assertSwapToShardingSphereSchemaWithoutTable() {
        YamlSchema yamlSchema = YamlEngine.unmarshal(readYAML(YAML_WITHOUT_TABLE), YamlSchema.class);
        assertTrue(new SchemaYamlSwapper().swapToObject(yamlSchema).getAllTableNames().isEmpty());
    }
    
    @SneakyThrows({URISyntaxException.class, IOException.class})
    private String readYAML(final String yamlFile) {
        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource(yamlFile).toURI())).stream().map(each -> each + System.lineSeparator()).collect(Collectors.joining());
    }
}
