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

package org.apache.shardingsphere.governance.core.registry.listener;

import org.apache.shardingsphere.governance.core.registry.util.FieldUtil;
import org.apache.shardingsphere.governance.repository.api.RegistryRepository;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.ChangedType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class RegistryListenerManagerTest {
    
    @Mock
    private RegistryRepository registryRepository;
    
    @Mock
    private InstanceStateChangedListener instanceStateChangedListener;
    
    @Mock
    private DataSourceStateChangedListener dataSourceStateChangedListener;
    
    @Test
    public void assertInitListeners() {
        RegistryListenerManager actual = new RegistryListenerManager(registryRepository, Arrays.asList("sharding_db", "primary_replica_replication_db", "encrypt_db"));
        FieldUtil.setField(actual, "instanceStateChangedListener", instanceStateChangedListener);
        FieldUtil.setField(actual, "dataSourceStateChangedListener", dataSourceStateChangedListener);
        actual.initListeners();
        verify(instanceStateChangedListener).watch(ChangedType.UPDATED);
        verify(dataSourceStateChangedListener).watch(ChangedType.UPDATED, ChangedType.DELETED, ChangedType.ADDED);
    }
}
