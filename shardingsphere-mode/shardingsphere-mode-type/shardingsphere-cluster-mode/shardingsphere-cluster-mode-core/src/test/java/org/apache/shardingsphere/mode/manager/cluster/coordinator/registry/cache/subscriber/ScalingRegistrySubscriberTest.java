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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.cache.subscriber;

import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.cache.event.StartScalingEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.ScalingTaskFinishedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.version.MetadataVersionPreparedEvent;
import org.apache.shardingsphere.mode.metadata.persist.service.MetaDataVersionPersistService;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class ScalingRegistrySubscriberTest {
    
    @Mock
    private ClusterPersistRepository repository;
    
    @Mock
    private MetaDataVersionPersistService metaDataVersionPersistService;
    
    @Mock
    private EventBusContext eventBusContext;
    
    private ScalingRegistrySubscriber scalingRegistrySubscriber;
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        scalingRegistrySubscriber = new ScalingRegistrySubscriber(repository, eventBusContext);
        Field persistServiceField = ScalingRegistrySubscriber.class.getDeclaredField("metaDataVersionPersistService");
        persistServiceField.setAccessible(true);
        persistServiceField.set(scalingRegistrySubscriber, metaDataVersionPersistService);
    }
    
    @Test
    public void assertStartScaling() {
        verify(eventBusContext).register(scalingRegistrySubscriber);
        when(metaDataVersionPersistService.getActiveVersion("ds_0")).thenReturn(Optional.of("1"));
        when(repository.get(any())).thenReturn("");
        scalingRegistrySubscriber.startScaling(new MetadataVersionPreparedEvent("2", "ds_0"));
        StartScalingEvent startScalingEvent = new StartScalingEvent("ds_0", "", "", "", "", 1, 2);
        verify(eventBusContext).post(ArgumentMatchers.refEq(startScalingEvent));
    }
    
    @Test
    public void assertScalingTaskFinished() {
        when(metaDataVersionPersistService.getActiveVersion("ds_0")).thenReturn(Optional.of("1"));
        scalingRegistrySubscriber.scalingTaskFinished(new ScalingTaskFinishedEvent("ds_0", 1, 2));
        verify(metaDataVersionPersistService).persistActiveVersion("ds_0", "2");
        verify(metaDataVersionPersistService).deleteVersion("ds_0", "1");
    }
}
