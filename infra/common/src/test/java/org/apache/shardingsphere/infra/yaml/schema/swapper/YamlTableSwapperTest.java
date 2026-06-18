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

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.TableType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereConstraint;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.util.file.SystemResourceFileUtils;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlShardingSphereColumn;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlShardingSphereConstraint;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlShardingSphereIndex;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlShardingSphereTable;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.Map;

import static org.apache.shardingsphere.test.infra.framework.matcher.ShardingSphereAssertionMatchers.deepEqual;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

class YamlTableSwapperTest {
    
    private static final String YAML_FILE = "yaml/schema/table.yaml";
    
    private static final String EMPTY_YAML_FILE = "yaml/schema/empty-table.yaml";
    
    private final YamlTableSwapper swapper = new YamlTableSwapper();
    
    @Test
    void assertSwapToYamlConfiguration() {
        ShardingSphereTable table = createShardingSphereTable();
        YamlShardingSphereTable actual = swapper.swapToYamlConfiguration(table);
        YamlShardingSphereTable expected = YamlEngine.unmarshal(SystemResourceFileUtils.readFile(YAML_FILE), YamlShardingSphereTable.class);
        assertThat(actual, deepEqual(expected));
    }
    
    @Test
    void assertSwapToObject() {
        ShardingSphereTable actual = swapper.swapToObject(YamlEngine.unmarshal(SystemResourceFileUtils.readFile(YAML_FILE), YamlShardingSphereTable.class));
        ShardingSphereTable expected = createShardingSphereTable();
        assertThat(actual, deepEqual(expected));
    }
    
    @Test
    void assertSwapToObjectWithEmptySchema() {
        ShardingSphereTable actual = swapper.swapToObject(YamlEngine.unmarshal(SystemResourceFileUtils.readFile(EMPTY_YAML_FILE), YamlShardingSphereTable.class));
        ShardingSphereTable expected = new ShardingSphereTable("", Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), TableType.TABLE);
        assertThat(actual, deepEqual(expected));
    }
    
    @Test
    void assertSwapToYamlConfigurationWithCaseSensitiveKeys() {
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl",
                Arrays.asList(new ShardingSphereColumn("Foo_Col", 0, true, false, false, true, false, false),
                        new ShardingSphereColumn("foo_col", 1, false, false, false, true, false, false)),
                Arrays.asList(new ShardingSphereIndex("Foo_Idx", Collections.emptyList(), false), new ShardingSphereIndex("foo_idx", Collections.emptyList(), false)),
                Arrays.asList(new ShardingSphereConstraint("Foo_Constraint", "foo_tbl"), new ShardingSphereConstraint("foo_constraint", "foo_tbl")), TableType.TABLE);
        YamlShardingSphereTable actual = swapper.swapToYamlConfiguration(table);
        assertThat(actual.getColumns().size(), is(2));
        assertThat(actual.getColumns(), hasKey("Foo_Col"));
        assertThat(actual.getColumns(), hasKey("foo_col"));
        assertThat(actual.getIndexes().size(), is(2));
        assertThat(actual.getIndexes(), hasKey("Foo_Idx"));
        assertThat(actual.getIndexes(), hasKey("foo_idx"));
        assertThat(actual.getConstraints().size(), is(2));
        assertThat(actual.getConstraints(), hasKey("Foo_Constraint"));
        assertThat(actual.getConstraints(), hasKey("foo_constraint"));
        assertThat(actual.getColumns(), not(hasKey("FOO_COL")));
    }
    
    @Test
    void assertSwapToObjectWithCaseSensitiveYamlKeys() {
        YamlShardingSphereTable yamlTable = new YamlShardingSphereTable();
        yamlTable.setName("foo_tbl");
        yamlTable.setColumns(createYamlColumns());
        yamlTable.setIndexes(createYamlIndexes());
        yamlTable.setConstraints(createYamlConstraints());
        yamlTable.setType(TableType.TABLE);
        ShardingSphereTable actual = swapper.swapToObject(yamlTable);
        assertThat(actual.getAllColumns().size(), is(2));
        assertThat(actual.getAllIndexes().size(), is(2));
        assertThat(actual.getAllConstraints().size(), is(2));
    }
    
    private ShardingSphereTable createShardingSphereTable() {
        return new ShardingSphereTable("foo_tbl",
                Collections.singleton(new ShardingSphereColumn("foo_col", 0, true, false, false, true, false, false)),
                Collections.singleton(new ShardingSphereIndex("PRIMARY", Collections.emptyList(), false)),
                Collections.singleton(new ShardingSphereConstraint("foo_constraint", "foo_tbl")), null);
    }
    
    private Map<String, YamlShardingSphereColumn> createYamlColumns() {
        Map<String, YamlShardingSphereColumn> result = new LinkedHashMap<>(2, 1F);
        result.put("Foo_Col", createYamlColumn("Foo_Col", 0, true));
        result.put("foo_col", createYamlColumn("foo_col", 1, false));
        return result;
    }
    
    private YamlShardingSphereColumn createYamlColumn(final String name, final int dataType, final boolean primaryKey) {
        YamlShardingSphereColumn result = new YamlShardingSphereColumn();
        result.setName(name);
        result.setDataType(dataType);
        result.setPrimaryKey(primaryKey);
        result.setGenerated(false);
        result.setCaseSensitive(false);
        result.setVisible(true);
        result.setUnsigned(false);
        result.setNullable(false);
        return result;
    }
    
    private Map<String, YamlShardingSphereIndex> createYamlIndexes() {
        Map<String, YamlShardingSphereIndex> result = new LinkedHashMap<>(2, 1F);
        result.put("Foo_Idx", createYamlIndex("Foo_Idx"));
        result.put("foo_idx", createYamlIndex("foo_idx"));
        return result;
    }
    
    private YamlShardingSphereIndex createYamlIndex(final String name) {
        YamlShardingSphereIndex result = new YamlShardingSphereIndex();
        result.setName(name);
        result.setColumns(Collections.emptyList());
        result.setUnique(false);
        return result;
    }
    
    private Map<String, YamlShardingSphereConstraint> createYamlConstraints() {
        Map<String, YamlShardingSphereConstraint> result = new LinkedHashMap<>(2, 1F);
        result.put("Foo_Constraint", createYamlConstraint("Foo_Constraint"));
        result.put("foo_constraint", createYamlConstraint("foo_constraint"));
        return result;
    }
    
    private YamlShardingSphereConstraint createYamlConstraint(final String name) {
        YamlShardingSphereConstraint result = new YamlShardingSphereConstraint();
        result.setName(name);
        result.setReferencedTableName("foo_tbl");
        return result;
    }
}
