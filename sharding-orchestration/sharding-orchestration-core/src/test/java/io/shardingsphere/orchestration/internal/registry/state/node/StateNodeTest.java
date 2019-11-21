/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.orchestration.internal.registry.state.node;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class StateNodeTest {
    
    private StateNode stateNode = new StateNode("test");
    
    @Test
    public void assertGetInstancesNodeFullPath() {
        assertThat(stateNode.getInstancesNodeFullPath("testId"), is("/test/state/instances/testId"));
    }
    
    @Test
    public void assertGetDataSourcesNodeFullRootPath() {
        assertThat(stateNode.getDataSourcesNodeFullRootPath(), is("/test/state/datasources"));
    }
    
    @Test
    public void assertGetDataSourcesNodeFullPath() {
        assertThat(stateNode.getDataSourcesNodeFullPath("sharding_db"), is("/test/state/datasources/sharding_db"));
    }
    
    @Test
    public void assertGetOrchestrationShardingSchema() {
        assertThat(stateNode.getOrchestrationShardingSchema("/test/state/datasources/master_slave_db.slave_ds_0").getSchemaName(), is("master_slave_db"));
    }
}
