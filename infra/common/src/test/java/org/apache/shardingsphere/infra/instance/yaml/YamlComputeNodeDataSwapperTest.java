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

package org.apache.shardingsphere.infra.instance.yaml;

import org.apache.shardingsphere.infra.instance.ComputeNodeData;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class YamlComputeNodeDataSwapperTest {
    
    private final YamlComputeNodeDataSwapper swapper = new YamlComputeNodeDataSwapper();
    
    @Test
    void assertSwapToYamlConfiguration() {
        ComputeNodeData computeNodeData = new ComputeNodeData("foo_db", "foo_attr", "1.0.0");
        YamlComputeNodeData actual = swapper.swapToYamlConfiguration(computeNodeData);
        assertThat(actual.getDatabaseName(), is("foo_db"));
        assertThat(actual.getAttribute(), is("foo_attr"));
        assertThat(actual.getVersion(), is("1.0.0"));
    }
    
    @Test
    void assertSwapToObject() {
        YamlComputeNodeData yamlConfig = new YamlComputeNodeData();
        yamlConfig.setDatabaseName("foo_db");
        yamlConfig.setAttribute("foo_attr");
        yamlConfig.setVersion("1.0.0");
        ComputeNodeData actual = swapper.swapToObject(yamlConfig);
        assertThat(actual.getDatabaseName(), is("foo_db"));
        assertThat(actual.getAttribute(), is("foo_attr"));
        assertThat(actual.getVersion(), is("1.0.0"));
    }
}
