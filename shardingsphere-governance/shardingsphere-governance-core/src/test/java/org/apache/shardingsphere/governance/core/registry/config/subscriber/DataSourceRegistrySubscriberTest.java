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

package org.apache.shardingsphere.governance.core.registry.config.subscriber;

import org.apache.shardingsphere.governance.core.registry.config.event.datasource.DataSourceAddedSQLNotificationEvent;
import org.apache.shardingsphere.governance.core.registry.config.event.datasource.DataSourceDroppedSQLNotificationEvent;
import org.apache.shardingsphere.governance.core.registry.config.service.impl.DataSourcePersistService;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DataSourceRegistrySubscriberTest {
    
    private DataSourceRegistrySubscriber dataSourceRegistrySubscriber;
    
    @Mock
    private DataSourcePersistService persistService;
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        dataSourceRegistrySubscriber = new DataSourceRegistrySubscriber(mock(RegistryCenterRepository.class));
        Field field = dataSourceRegistrySubscriber.getClass().getDeclaredField("persistService");
        field.setAccessible(true);
        field.set(dataSourceRegistrySubscriber, persistService);
    }
    
    @Test
    public void assertUpdateWithDataSourceAddedEvent() {
        Map<String, DataSourceConfiguration> dataSourceConfigs = createDataSourceConfigurations();
        DataSourceAddedSQLNotificationEvent event = new DataSourceAddedSQLNotificationEvent("foo_db", dataSourceConfigs);
        dataSourceRegistrySubscriber.update(event);
        verify(persistService).persist("foo_db", dataSourceConfigs);
    }
    
    @Test
    public void assertUpdateWithDataSourceAlteredEvent() {
        DataSourceDroppedSQLNotificationEvent event = new DataSourceDroppedSQLNotificationEvent("foo_db", Collections.singletonList("ds_0"));
        Map<String, DataSourceConfiguration> dataSourceConfigs = createDataSourceConfigurations();
        when(persistService.load("foo_db")).thenReturn(dataSourceConfigs);
        dataSourceRegistrySubscriber.update(event);
        dataSourceConfigs.remove("ds_0");
        verify(persistService).persist("foo_db", dataSourceConfigs);
    }
    
    private Map<String, DataSourceConfiguration> createDataSourceConfigurations() {
        return createDataSourceMap().entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry ->
                DataSourceConfiguration.getDataSourceConfiguration(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new LinkedHashMap<>(2, 1);
        result.put("ds_0", createDataSource("ds_0"));
        result.put("ds_1", createDataSource("ds_1"));
        return result;
    }
    
    private DataSource createDataSource(final String name) {
        MockedDataSource result = new MockedDataSource();
        result.setDriverClassName("com.mysql.jdbc.Driver");
        result.setUrl("jdbc:mysql://localhost:3306/" + name);
        result.setUsername("root");
        result.setPassword("root");
        result.setConnectionInitSqls(Arrays.asList("set names utf8mb4;", "set names utf8;"));
        return result;
    }
}
