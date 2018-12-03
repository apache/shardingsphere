/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.orchestration.internal.registry.state.service;

import io.shardingsphere.orchestration.internal.registry.config.service.ConfigurationService;
import io.shardingsphere.orchestration.internal.registry.state.schema.OrchestrationShardingSchemaGroup;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;
import io.shardingsphere.orchestration.util.FieldUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DataSourceServiceTest {
    
    @Mock
    private RegistryCenter regCenter;
    
    @Mock
    private ConfigurationService configService;
    
    private DataSourceService dataSourceService;
    
    @Before
    public void setUp() {
        dataSourceService = new DataSourceService("test", regCenter);
        FieldUtil.setField(dataSourceService, "configService", configService);
    }
    
    @Test
    public void assertInitDataSourcesNode() {
        dataSourceService.initDataSourcesNode();
        verify(regCenter).persist("/test/state/datasources", "");
    }
    
    @Test
    public void assertGetDisabledSlaveSchemaGroup() {
        mockDisabledSlaveSchemaGroup();
        assertTrue(dataSourceService.getDisabledSlaveSchemaGroup().getDataSourceNames("sharding_schema").contains("slave_ds_0"));
    }
    
    private void mockDisabledSlaveSchemaGroup() {
        OrchestrationShardingSchemaGroup slaveGroup = mock(OrchestrationShardingSchemaGroup.class);
        when(slaveGroup.getDataSourceNames("sharding_schema")).thenReturn(Arrays.asList("slave_ds_0", "slave_ds_1"));
        when(configService.getAllSlaveDataSourceNames()).thenReturn(slaveGroup);
        when(regCenter.getChildrenKeys("/test/state/datasources")).thenReturn(Collections.singletonList("sharding_schema.slave_ds_0"));
        when(regCenter.get("/test/state/datasources/sharding_schema.slave_ds_0")).thenReturn("disabled");
    }
}
