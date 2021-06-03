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

package org.apache.shardingsphere.governance.core.registry.config.service.impl;

import lombok.SneakyThrows;
import org.apache.shardingsphere.governance.core.registry.MockDataSource;
import org.apache.shardingsphere.governance.core.registry.config.event.datasource.DataSourceAddedSQLNotificationEvent;
import org.apache.shardingsphere.governance.core.registry.config.event.datasource.DataSourceDroppedSQLNotificationEvent;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DataSourceRegistryServiceTest {
    
    @Mock
    private RegistryCenterRepository registryCenterRepository;
    
    private DataSourceRegistryService dataSourceRegistryService;
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        dataSourceRegistryService = new DataSourceRegistryService(registryCenterRepository);
        Field field = dataSourceRegistryService.getClass().getDeclaredField("repository");
        field.setAccessible(true);
        field.set(dataSourceRegistryService, registryCenterRepository);
    }
    
    @Test
    public void assertLoad() {
        when(registryCenterRepository.get("/metadata/foo_db/dataSources")).thenReturn(readDataSourceYaml());
        Map<String, DataSourceConfiguration> actual = dataSourceRegistryService.load("foo_db");
        assertThat(actual.size(), is(2));
        assertDataSourceConfiguration(actual.get("ds_0"), DataSourceConfiguration.getDataSourceConfiguration(createDataSource("ds_0")));
        assertDataSourceConfiguration(actual.get("ds_1"), DataSourceConfiguration.getDataSourceConfiguration(createDataSource("ds_1")));
    }
    
    @SneakyThrows({IOException.class, URISyntaxException.class})
    private String readDataSourceYaml() {
        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource("yaml/regcenter/data-source.yaml").toURI()))
                .stream().filter(each -> !each.startsWith("#")).map(each -> each + System.lineSeparator()).collect(Collectors.joining());
    }
    
    private void assertDataSourceConfiguration(final DataSourceConfiguration actual, final DataSourceConfiguration expected) {
        assertThat(actual.getDataSourceClassName(), is(expected.getDataSourceClassName()));
        assertThat(actual.getProps().get("url"), is(expected.getProps().get("url")));
        assertThat(actual.getProps().get("username"), is(expected.getProps().get("username")));
        assertThat(actual.getProps().get("password"), is(expected.getProps().get("password")));
        assertThat(actual.getProps().get("connectionInitSqls"), is(expected.getProps().get("connectionInitSqls")));
    }
    
    @Test
    public void assertLoadWithoutPath() {
        when(registryCenterRepository.get("/metadata/foo_db/dataSources")).thenReturn("");
        Map<String, DataSourceConfiguration> actual = dataSourceRegistryService.load("foo_db");
        assertThat(actual.size(), is(0));
    }
    
    @Test
    public void assertUpdateWithDataSourceAddedEvent() {
        DataSourceAddedSQLNotificationEvent event = new DataSourceAddedSQLNotificationEvent("foo_db", createDataSourceConfigurations());
        dataSourceRegistryService.update(event);
        verify(registryCenterRepository).persist(startsWith("/metadata/foo_db/dataSources"), anyString());
    }
    
    @Test
    public void assertUpdateWithDataSourceAlteredEvent() {
        DataSourceDroppedSQLNotificationEvent event = new DataSourceDroppedSQLNotificationEvent("foo_db", Collections.singletonList("ds_0"));
        dataSourceRegistryService.update(event);
        verify(registryCenterRepository).persist(startsWith("/metadata/foo_db/dataSources"), anyString());
    }
    
    private Map<String, DataSourceConfiguration> createDataSourceConfigurations() {
        return createDataSourceMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry ->
                DataSourceConfiguration.getDataSourceConfiguration(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new LinkedHashMap<>(2, 1);
        result.put("ds_0", createDataSource("ds_0"));
        result.put("ds_1", createDataSource("ds_1"));
        return result;
    }
    
    private DataSource createDataSource(final String name) {
        MockDataSource result = new MockDataSource();
        result.setDriverClassName("com.mysql.jdbc.Driver");
        result.setUrl("jdbc:mysql://localhost:3306/" + name);
        result.setUsername("root");
        result.setPassword("root");
        result.setConnectionInitSqls(Arrays.asList("set names utf8mb4;", "set names utf8;"));
        return result;
    }
}
