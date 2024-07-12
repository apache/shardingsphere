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

package org.apache.shardingsphere.mode.manager.cluster.event.subscriber.dispatch;

import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.manager.cluster.ClusterContextManagerBuilder;
import org.apache.shardingsphere.mode.event.dispatch.state.compute.instance.InstanceOfflineEvent;
import org.apache.shardingsphere.mode.event.dispatch.state.compute.instance.InstanceOnlineEvent;
import org.apache.shardingsphere.mode.metadata.MetaDataContextsFactory;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ComputeNodeOnlineSubscriberTest {
    
    private ComputeNodeOnlineSubscriber subscriber;
    
    private ContextManager contextManager;
    
    @BeforeEach
    void setUp() throws SQLException {
        EventBusContext eventBusContext = new EventBusContext();
        contextManager = new ClusterContextManagerBuilder().build(createContextManagerBuilderParameter(), eventBusContext);
        contextManager.renewMetaDataContexts(MetaDataContextsFactory.create(contextManager.getPersistServiceFacade().getMetaDataPersistService(), mock(ShardingSphereMetaData.class)));
        subscriber = new ComputeNodeOnlineSubscriber(contextManager);
    }
    
    @Test
    void assertRenewInstanceOfflineEvent() {
        subscriber.renew(new InstanceOfflineEvent(contextManager.getComputeNodeInstanceContext().getInstance().getMetaData()));
        assertThat(((ProxyInstanceMetaData) contextManager.getComputeNodeInstanceContext().getInstance().getMetaData()).getPort(), is(3307));
    }
    
    @Test
    void assertRenewInstanceOnlineEvent() {
        InstanceMetaData instanceMetaData1 = new ProxyInstanceMetaData("foo_instance_3307", 3307);
        InstanceOnlineEvent instanceOnlineEvent1 = new InstanceOnlineEvent(instanceMetaData1);
        subscriber.renew(instanceOnlineEvent1);
        assertThat(contextManager.getComputeNodeInstanceContext().getAllClusterInstances().size(), is(1));
        assertThat(((CopyOnWriteArrayList<ComputeNodeInstance>) contextManager.getComputeNodeInstanceContext().getAllClusterInstances()).get(0).getMetaData(), is(instanceMetaData1));
        InstanceMetaData instanceMetaData2 = new ProxyInstanceMetaData("foo_instance_3308", 3308);
        InstanceOnlineEvent instanceOnlineEvent2 = new InstanceOnlineEvent(instanceMetaData2);
        subscriber.renew(instanceOnlineEvent2);
        assertThat(contextManager.getComputeNodeInstanceContext().getAllClusterInstances().size(), is(2));
        assertThat(((CopyOnWriteArrayList<ComputeNodeInstance>) contextManager.getComputeNodeInstanceContext().getAllClusterInstances()).get(1).getMetaData(), is(instanceMetaData2));
        subscriber.renew(instanceOnlineEvent1);
        assertThat(contextManager.getComputeNodeInstanceContext().getAllClusterInstances().size(), is(2));
        assertThat(((CopyOnWriteArrayList<ComputeNodeInstance>) contextManager.getComputeNodeInstanceContext().getAllClusterInstances()).get(1).getMetaData(), is(instanceMetaData1));
    }
    
    private ContextManagerBuilderParameter createContextManagerBuilderParameter() {
        ModeConfiguration modeConfig = new ModeConfiguration("Cluster", new ClusterPersistRepositoryConfiguration("FIXTURE", "", "", new Properties()));
        InstanceMetaData instanceMetaData = new ProxyInstanceMetaData("foo_instance_id", 3307);
        return new ContextManagerBuilderParameter(modeConfig, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyList(),
                new Properties(), Collections.emptyList(), instanceMetaData, false);
    }
}
