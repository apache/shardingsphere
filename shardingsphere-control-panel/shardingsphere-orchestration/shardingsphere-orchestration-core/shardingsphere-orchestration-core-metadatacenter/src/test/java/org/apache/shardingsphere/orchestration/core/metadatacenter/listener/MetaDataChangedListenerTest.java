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

package org.apache.shardingsphere.orchestration.core.metadatacenter.listener;

import org.apache.shardingsphere.orchestration.center.CenterRepository;
import org.apache.shardingsphere.orchestration.center.listener.DataChangedEvent;
import org.apache.shardingsphere.orchestration.core.metadatacenter.MetaDataTest;
import org.apache.shardingsphere.orchestration.core.metadatacenter.event.MetaDataChangedEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class MetaDataChangedListenerTest {
    
    private MetaDataChangedListener metaDataChangedListener;
    
    @Mock
    private CenterRepository centerRepository;
    
    @Before
    public void setUp() {
        metaDataChangedListener = new MetaDataChangedListener("test", centerRepository, Collections.singleton("schema"));
    }
    
    @Test
    public void createShardingOrchestrationEvent() {
        DataChangedEvent event = new DataChangedEvent("/test/metadata/schema", MetaDataTest.META_DATA, DataChangedEvent.ChangedType.UPDATED);
        MetaDataChangedEvent metaDataChangedEvent = (MetaDataChangedEvent) metaDataChangedListener.createShardingOrchestrationEvent(event);
        assertNotNull(metaDataChangedEvent);
        assertThat(metaDataChangedEvent.getSchemaNames(), is(Collections.singleton("schema")));
    }
}
