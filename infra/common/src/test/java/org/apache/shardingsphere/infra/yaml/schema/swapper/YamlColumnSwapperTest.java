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

import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlShardingSphereColumn;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlColumnSwapperTest {
    
    private final YamlColumnSwapper swapper = new YamlColumnSwapper();
    
    @Test
    void assertSwapToYamlConfiguration() {
        ShardingSphereColumn column = new ShardingSphereColumn("id", 1, true, false, true, true, false, false);
        YamlShardingSphereColumn actual = swapper.swapToYamlConfiguration(column);
        assertThat(actual.getName(), is("id"));
        assertThat(actual.getDataType(), is(1));
        assertTrue(actual.isPrimaryKey());
        assertFalse(actual.isGenerated());
        assertTrue(actual.isCaseSensitive());
        assertTrue(actual.isVisible());
        assertFalse(actual.isUnsigned());
        assertFalse(actual.isNullable());
    }
    
    @Test
    void assertSwapToObject() {
        YamlShardingSphereColumn yamlColumn = new YamlShardingSphereColumn();
        yamlColumn.setName("id");
        yamlColumn.setDataType(2);
        yamlColumn.setPrimaryKey(false);
        yamlColumn.setGenerated(true);
        yamlColumn.setCaseSensitive(false);
        yamlColumn.setVisible(false);
        yamlColumn.setUnsigned(true);
        yamlColumn.setNullable(true);
        ShardingSphereColumn actual = swapper.swapToObject(yamlColumn);
        assertThat(actual.getName(), is("id"));
        assertThat(actual.getDataType(), is(2));
        assertFalse(actual.isPrimaryKey());
        assertTrue(actual.isGenerated());
        assertFalse(actual.isCaseSensitive());
        assertFalse(actual.isVisible());
        assertTrue(actual.isUnsigned());
        assertTrue(actual.isNullable());
    }
}
