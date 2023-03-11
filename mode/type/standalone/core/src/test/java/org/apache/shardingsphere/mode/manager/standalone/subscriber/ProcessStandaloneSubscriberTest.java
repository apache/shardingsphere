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

package org.apache.shardingsphere.mode.manager.standalone.subscriber;

import org.apache.shardingsphere.infra.executor.sql.process.ShowProcessListManager;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.mode.process.event.ShowProcessListRequestEvent;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ShowProcessListManager.class)
public final class ProcessStandaloneSubscriberTest {
    
    @Test
    public void assertLoadShowProcessListData() {
        ShowProcessListManager showProcessListManager = mock(ShowProcessListManager.class);
        when(ShowProcessListManager.getInstance()).thenReturn(showProcessListManager);
        new ProcessStandaloneSubscriber(new EventBusContext()).loadShowProcessListData(mock(ShowProcessListRequestEvent.class));
        verify(showProcessListManager).getProcessContexts();
    }
}
