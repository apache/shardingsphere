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

package org.apache.shardingsphere.readwritesplitting.deliver;

import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDataSource;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.mode.deliver.DeliverEventSubscriber;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReadwriteSplittingQualifiedDataSourceChangedSubscriberTest {
    
    private ReadwriteSplittingQualifiedDataSourceChangedSubscriber subscriber;
    
    @Mock
    private PersistRepository repository;
    
    @BeforeEach
    void setUp() {
        subscriber = new ReadwriteSplittingQualifiedDataSourceChangedSubscriber();
        subscriber.setRepository(repository);
    }
    
    @Test
    void assertDeleteStorageNodeDataSourceDataSourceState() {
        subscriber.delete(new QualifiedDataSourceDeletedEvent(new QualifiedDataSource("foo_db", "foo_group", "foo_ds")));
        verify(repository).delete("/nodes/qualified_data_sources/foo_db.foo_group.foo_ds");
    }
    
    @Test
    void assertRepositoryIsolationAcrossEventBusContexts() {
        PersistRepository anotherRepository = mock(PersistRepository.class);
        EventBusContext firstEventBusContext = new EventBusContext();
        EventBusContext secondEventBusContext = new EventBusContext();
        DeliverEventSubscriber actualFirstSubscriber = ShardingSphereServiceLoader.getServiceInstances(DeliverEventSubscriber.class).iterator().next();
        actualFirstSubscriber.setRepository(repository);
        firstEventBusContext.register(actualFirstSubscriber);
        DeliverEventSubscriber actualSecondSubscriber = ShardingSphereServiceLoader.getServiceInstances(DeliverEventSubscriber.class).iterator().next();
        actualSecondSubscriber.setRepository(anotherRepository);
        secondEventBusContext.register(actualSecondSubscriber);
        assertThat(actualFirstSubscriber, not(sameInstance(actualSecondSubscriber)));
        firstEventBusContext.post(new QualifiedDataSourceDeletedEvent(new QualifiedDataSource("foo_db", "foo_group", "foo_ds")));
        verify(repository).delete("/nodes/qualified_data_sources/foo_db.foo_group.foo_ds");
        verify(anotherRepository, never()).delete("/nodes/qualified_data_sources/foo_db.foo_group.foo_ds");
        secondEventBusContext.post(new QualifiedDataSourceDeletedEvent(new QualifiedDataSource("bar_db", "bar_group", "bar_ds")));
        verify(anotherRepository).delete("/nodes/qualified_data_sources/bar_db.bar_group.bar_ds");
        verify(repository, never()).delete("/nodes/qualified_data_sources/bar_db.bar_group.bar_ds");
    }
}
