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

package org.apache.shardingsphere.cluster.state;

import com.google.common.collect.Sets;
import lombok.SneakyThrows;
import org.apache.shardingsphere.cluster.state.enums.NodeState;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.orchestration.core.registry.RegistryCenter;
import org.apache.shardingsphere.orchestration.core.registry.event.DisabledStateChangedEvent;
import org.apache.shardingsphere.orchestration.core.registry.schema.OrchestrationSchema;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ClusterStateInstanceTest {
    
    private static final ClusterStateInstance INSTANCE = ClusterStateInstance.getInstance();
    
    @Mock
    private RegistryCenter registryCenter;
    
    @SneakyThrows(ReflectiveOperationException.class)
    @Before
    public void setUp() {
        FieldSetter.setField(INSTANCE, ClusterStateInstance.class.getDeclaredField("registryCenter"), registryCenter);
    }
    
    @Test
    public void assertPersistInstanceState() {
        InstanceState instanceState = new InstanceState();
        instanceState.setState(NodeState.ONLINE);
        instanceState.setDataSources(buildDataSources());
        INSTANCE.persistInstanceState(instanceState);
        verify(registryCenter).persistInstanceData(YamlEngine.marshal(instanceState));
    }
    
    private Map<String, DataSourceState> buildDataSources() {
        DataSourceState dataSourceState = new DataSourceState();
        dataSourceState.setState(NodeState.ONLINE);
        dataSourceState.setLastConnect(System.currentTimeMillis());
        dataSourceState.setRetryCount(1);
        Map<String, DataSourceState> result = new HashMap<>();
        result.put("sharding_db.ds_0", dataSourceState);
        return result;
    }
    
    @Test
    public void assertLoadInstanceState() {
        when(registryCenter.loadInstanceData()).thenReturn(buildInstanceData());
        InstanceState instanceState = INSTANCE.loadInstanceState();
        assertNotNull(instanceState);
        assertThat(instanceState.getState(), is(NodeState.ONLINE));
        assertThat(instanceState.getDataSources().keySet(), is(Sets.newHashSet("sharding_db.ds_1")));
        assertThat(instanceState.getDataSources().get("sharding_db.ds_1").getState(), is(NodeState.ONLINE));
    }
    
    private String buildInstanceData() {
        return "state: ONLINE\n"
                + "dataSources:\n"
                + "    sharding_db.ds_1:\n"
                + "        state: ONLINE\n"
                + "        lastConnect: \n"
                + "        retryCount: 1";
    }
    
    @Test
    public void assertDataSourceStateChanged() {
        DisabledStateChangedEvent event = new DisabledStateChangedEvent(new OrchestrationSchema("sharding_db", "ds_1"), true);
        when(registryCenter.loadInstanceData()).thenReturn(buildInstanceData());
        INSTANCE.dataSourceStateChanged(event);
        InstanceState instanceState = YamlEngine.unmarshal(registryCenter.loadInstanceData(), InstanceState.class);
        instanceState.getDataSources().forEach((key, value) -> value.setState(NodeState.DISABLED));
        verify(registryCenter).persistInstanceData(YamlEngine.marshal(instanceState));
    }
}
