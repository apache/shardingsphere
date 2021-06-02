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

package org.apache.shardingsphere.governance.core.registry.state.service;

import org.apache.shardingsphere.governance.core.registry.state.ResourceState;
import org.apache.shardingsphere.governance.core.registry.state.node.StatesNode;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceDisabledEvent;
import org.apache.shardingsphere.infra.rule.event.impl.PrimaryDataSourceEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DataSourceStatusRegistryServiceTest {
    
    @Mock
    private RegistryCenterRepository registryCenterRepository;
    
    private DataSourceStatusRegistryService dataSourceStatusRegistryService;
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        dataSourceStatusRegistryService = new DataSourceStatusRegistryService(registryCenterRepository);
        Field field = dataSourceStatusRegistryService.getClass().getDeclaredField("repository");
        field.setAccessible(true);
        field.set(dataSourceStatusRegistryService, registryCenterRepository);
    }
    
    @Test
    public void assertLoadDisabledDataSources() {
        List<String> disabledDataSources = Collections.singletonList("replica_ds_0");
        when(registryCenterRepository.getChildrenKeys(anyString())).thenReturn(disabledDataSources);
        dataSourceStatusRegistryService.loadDisabledDataSources("replica_query_db");
        verify(registryCenterRepository).getChildrenKeys(anyString());
        verify(registryCenterRepository).get(anyString());
    }

    @Test
    public void assertUpdateDataSourceDisabledState() {
        assertUpdateDataSourceState(true, ResourceState.DISABLED.toString());
    }

    @Test
    public void assertUpdateDataSourceEnabledState() {
        assertUpdateDataSourceState(false, "");
    }
    
    private void assertUpdateDataSourceState(final boolean isDisabled, final String value) {
        String schemaName = "replica_query_db";
        String dataSourceName = "replica_ds_0";
        DataSourceDisabledEvent dataSourceDisabledEvent = new DataSourceDisabledEvent(schemaName, dataSourceName, isDisabled);
        dataSourceStatusRegistryService.update(dataSourceDisabledEvent);
        verify(registryCenterRepository).persist(StatesNode.getDataSourcePath(schemaName, dataSourceName), value);
    }
    
    @Test
    public void assertUpdatePrimaryDataSourceState() {
        String schemaName = "replica_query_db";
        String groupName = "group1";
        String dataSourceName = "replica_ds_0";
        PrimaryDataSourceEvent primaryDataSourceEvent = new PrimaryDataSourceEvent(schemaName, groupName, dataSourceName);
        dataSourceStatusRegistryService.update(primaryDataSourceEvent);
        verify(registryCenterRepository).persist(StatesNode.getPrimaryDataSourcePath(schemaName, groupName), dataSourceName);
    }
}
