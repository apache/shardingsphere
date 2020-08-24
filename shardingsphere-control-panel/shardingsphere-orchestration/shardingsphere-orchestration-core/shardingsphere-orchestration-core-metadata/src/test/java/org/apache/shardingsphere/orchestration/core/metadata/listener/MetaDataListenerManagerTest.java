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

package org.apache.shardingsphere.orchestration.core.metadata.listener;

import org.apache.shardingsphere.orchestration.core.metadata.util.FieldUtil;
import org.apache.shardingsphere.orchestration.repository.api.OrchestrationRepository;
import org.apache.shardingsphere.orchestration.repository.api.listener.DataChangedEvent.ChangedType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class MetaDataListenerManagerTest {
    
    @Mock
    private MetaDataChangedListener metaDataChangedListener;
    
    @Mock
    private OrchestrationRepository orchestrationRepository;
    
    @Test
    public void assertInitListeners() {
        MetaDataListenerManager actual = new MetaDataListenerManager("test", orchestrationRepository, Collections.singleton("sharding_db"));
        FieldUtil.setField(actual, "metaDataChangedListener", metaDataChangedListener);
        actual.initListeners();
        verify(metaDataChangedListener).watch(ChangedType.UPDATED);
    }
}
