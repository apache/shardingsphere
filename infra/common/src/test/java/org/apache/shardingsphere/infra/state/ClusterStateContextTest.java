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

package org.apache.shardingsphere.infra.state;

import org.apache.shardingsphere.infra.state.cluster.ClusterState;
import org.apache.shardingsphere.infra.state.cluster.ClusterStateContext;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClusterStateContextTest {
    
    private final ClusterStateContext clusterStateContext = new ClusterStateContext();
    
    @Test
    void assertSwitchStateWithUnavailable() {
        clusterStateContext.switchState(ClusterState.UNAVAILABLE);
        assertThat(clusterStateContext.getCurrentState(), is(ClusterState.UNAVAILABLE));
        clusterStateContext.switchState(ClusterState.OK);
    }
    
    @Test
    void assertSwitchStateWithReadOnly() {
        clusterStateContext.switchState(ClusterState.READ_ONLY);
        assertThat(clusterStateContext.getCurrentState(), is(ClusterState.READ_ONLY));
        clusterStateContext.switchState(ClusterState.OK);
    }
    
    @Test
    void assertSwitchStateWithMultiStateChange() {
        clusterStateContext.switchState(ClusterState.UNAVAILABLE);
        assertThrows(IllegalStateException.class, () -> clusterStateContext.switchState(ClusterState.READ_ONLY));
        clusterStateContext.switchState(ClusterState.OK);
        clusterStateContext.switchState(ClusterState.READ_ONLY);
        assertThat(clusterStateContext.getCurrentState(), is(ClusterState.READ_ONLY));
        clusterStateContext.switchState(ClusterState.OK);
    }
}
