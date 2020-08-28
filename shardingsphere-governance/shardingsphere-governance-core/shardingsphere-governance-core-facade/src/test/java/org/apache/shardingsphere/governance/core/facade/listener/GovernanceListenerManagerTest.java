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

package org.apache.shardingsphere.governance.core.facade.listener;

import org.apache.shardingsphere.governance.core.config.listener.ConfigurationListenerManager;
import org.apache.shardingsphere.governance.core.facade.util.FieldUtil;
import org.apache.shardingsphere.governance.core.metadata.listener.MetaDataListenerManager;
import org.apache.shardingsphere.governance.core.registry.listener.RegistryListenerManager;
import org.apache.shardingsphere.governance.repository.api.ConfigurationRepository;
import org.apache.shardingsphere.governance.repository.api.RegistryRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class GovernanceListenerManagerTest {
    
    @Mock
    private RegistryRepository registryRepository;
    
    @Mock
    private ConfigurationRepository configurationRepository;
    
    @Mock
    private ConfigurationListenerManager configurationListenerManager;
    
    @Mock
    private MetaDataListenerManager metaDataListenerManager;
    
    @Mock
    private RegistryListenerManager registryListenerManager;
    
    @Test
    public void assertInit() {
        GovernanceListenerManager actual = new GovernanceListenerManager(registryRepository, configurationRepository, Collections.emptyList());
        FieldUtil.setField(actual, "configurationListenerManager", configurationListenerManager);
        FieldUtil.setField(actual, "registryListenerManager", registryListenerManager);
        FieldUtil.setField(actual, "metaDataListenerManager", metaDataListenerManager);
        actual.init();
        verify(configurationListenerManager).initListeners();
        verify(registryListenerManager).initListeners();
        verify(metaDataListenerManager).initListeners();
    }
}
