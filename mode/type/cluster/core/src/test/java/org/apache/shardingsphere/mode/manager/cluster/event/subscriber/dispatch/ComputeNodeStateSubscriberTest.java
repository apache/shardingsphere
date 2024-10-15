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

import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.mode.event.dispatch.state.compute.ComputeNodeInstanceStateChangedEvent;
import org.apache.shardingsphere.mode.event.dispatch.state.compute.LabelsEvent;
import org.apache.shardingsphere.mode.event.dispatch.state.compute.WorkerIdEvent;
import org.apache.shardingsphere.mode.event.dispatch.state.compute.instance.InstanceOfflineEvent;
import org.apache.shardingsphere.mode.event.dispatch.state.compute.instance.InstanceOnlineEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComputeNodeStateSubscriberTest {
    
    private ComputeNodeStateSubscriber subscriber;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @BeforeEach
    void setUp() {
        subscriber = new ComputeNodeStateSubscriber(contextManager);
    }
    
    @Test
    void assertRenewWithInstanceOnlineEvent() {
        InstanceMetaData instanceMetaData = mock(InstanceMetaData.class);
        ComputeNodeInstance computeNodeInstance = mock(ComputeNodeInstance.class);
        when(contextManager.getPersistServiceFacade().getComputeNodePersistService().loadComputeNodeInstance(instanceMetaData)).thenReturn(computeNodeInstance);
        subscriber.renew(new InstanceOnlineEvent(instanceMetaData));
        verify(contextManager.getComputeNodeInstanceContext()).addComputeNodeInstance(computeNodeInstance);
    }
    
    @Test
    void assertRenewWithInstanceOfflineEvent() {
        subscriber.renew(new InstanceOfflineEvent(mock(InstanceMetaData.class)));
        verify(contextManager.getComputeNodeInstanceContext()).deleteComputeNodeInstance(any());
    }
    
    @Test
    void assertRenewWithComputeNodeInstanceStateChangedEvent() {
        subscriber.renew(new ComputeNodeInstanceStateChangedEvent("foo_instance_id", "OK"));
        verify(contextManager.getComputeNodeInstanceContext()).updateStatus("foo_instance_id", "OK");
    }
    
    @Test
    void assertRenewWithWorkerIdEvent() {
        subscriber.renew(new WorkerIdEvent("foo_instance_id", 1));
        verify(contextManager.getComputeNodeInstanceContext()).updateWorkerId("foo_instance_id", 1);
    }
    
    @Test
    void assertRenewWithLabelsEvent() {
        subscriber.renew(new LabelsEvent("foo_instance_id", Collections.emptyList()));
        verify(contextManager.getComputeNodeInstanceContext()).updateLabels("foo_instance_id", Collections.emptyList());
    }
}
