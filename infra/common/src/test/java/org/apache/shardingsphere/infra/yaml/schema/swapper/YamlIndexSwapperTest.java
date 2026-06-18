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

import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlShardingSphereIndex;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlIndexSwapperTest {
    
    private final YamlIndexSwapper swapper = new YamlIndexSwapper();
    
    @Test
    void assertSwapToYamlConfiguration() {
        ShardingSphereIndex index = new ShardingSphereIndex("idx_user", Arrays.asList("id", "name"), true);
        YamlShardingSphereIndex actual = swapper.swapToYamlConfiguration(index);
        assertThat(actual.getName(), is("idx_user"));
        assertThat(actual.getColumns(), contains("id", "name"));
        assertTrue(actual.isUnique());
    }
    
    @Test
    void assertSwapToObject() {
        YamlShardingSphereIndex yamlIndex = new YamlShardingSphereIndex();
        yamlIndex.setName("idx_order");
        yamlIndex.getColumns().addAll(Arrays.asList("order_id", "user_id"));
        yamlIndex.setUnique(false);
        ShardingSphereIndex actual = swapper.swapToObject(yamlIndex);
        assertThat(actual.getName(), is("idx_order"));
        assertThat(actual.getColumns(), contains("order_id", "user_id"));
        assertFalse(actual.isUnique());
    }
}
