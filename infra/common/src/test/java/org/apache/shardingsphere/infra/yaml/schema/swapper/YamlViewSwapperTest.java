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

import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.infra.util.file.SystemResourceFileUtils;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlShardingSphereView;
import org.junit.jupiter.api.Test;

import static org.apache.shardingsphere.test.infra.framework.matcher.ShardingSphereAssertionMatchers.deepEqual;
import static org.hamcrest.MatcherAssert.assertThat;

class YamlViewSwapperTest {
    
    private static final String YAML_FILE = "yaml/schema/view.yaml";
    
    private final YamlViewSwapper swapper = new YamlViewSwapper();
    
    @Test
    void assertSwapToYamlConfiguration() {
        ShardingSphereView view = createShardingSphereView();
        YamlShardingSphereView actual = swapper.swapToYamlConfiguration(view);
        YamlShardingSphereView expected = YamlEngine.unmarshal(SystemResourceFileUtils.readFile(YAML_FILE), YamlShardingSphereView.class);
        assertThat(actual, deepEqual(expected));
    }
    
    @Test
    void assertSwapToObject() {
        ShardingSphereView actual = swapper.swapToObject(YamlEngine.unmarshal(SystemResourceFileUtils.readFile(YAML_FILE), YamlShardingSphereView.class));
        ShardingSphereView expected = createShardingSphereView();
        assertThat(actual, deepEqual(expected));
    }
    
    private ShardingSphereView createShardingSphereView() {
        return new ShardingSphereView("foo_view", "SELECT 1");
    }
}
