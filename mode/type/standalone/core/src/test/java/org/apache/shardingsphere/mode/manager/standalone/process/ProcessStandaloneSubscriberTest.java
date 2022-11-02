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

package org.apache.shardingsphere.mode.manager.standalone.process;

import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.mode.manager.standalone.subscriber.ProcessStandaloneSubscriber;
import org.apache.shardingsphere.mode.process.ShowProcessListManager;
import org.apache.shardingsphere.mode.process.event.ShowProcessListRequestEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class ProcessStandaloneSubscriberTest {
    
    private final EventBusContext eventBusContext = new EventBusContext();
    
    private ProcessStandaloneSubscriber processRegistrySubscriber;
    
    private ShowProcessListManager showProcessListManager;
    
    private MockedStatic<ShowProcessListManager> mockedStatic;
    
    @Before
    public void setUp() {
        processRegistrySubscriber = new ProcessStandaloneSubscriber(eventBusContext);
        mockedStatic = mockStatic(ShowProcessListManager.class);
        showProcessListManager = mock(ShowProcessListManager.class);
        mockedStatic.when(ShowProcessListManager::getInstance).thenReturn(showProcessListManager);
    }
    
    @Test
    public void assertLoadShowProcessListData() {
        ShowProcessListRequestEvent showProcessListRequestEvent = mock(ShowProcessListRequestEvent.class);
        when(showProcessListManager.getProcessContexts()).thenReturn(Collections.emptyMap());
        processRegistrySubscriber.loadShowProcessListData(showProcessListRequestEvent);
        verify(showProcessListManager, times(1)).getProcessContexts();
    }
    
    @After
    public void tearDown() {
        mockedStatic.close();
    }
}
