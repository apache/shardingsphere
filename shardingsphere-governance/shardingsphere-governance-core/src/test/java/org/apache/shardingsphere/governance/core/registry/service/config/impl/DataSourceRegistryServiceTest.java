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

package org.apache.shardingsphere.governance.core.registry.service.config.impl;

import lombok.SneakyThrows;
import org.apache.shardingsphere.governance.core.registry.MockDataSource;
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
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DataSourceRegistryServiceTest {
    
    private static final String DATA_SOURCE_YAM = "yaml/regcenter/data-source.yaml";
    
    private static final String DATA_SOURCE_YAML_WITH_CONNECTION_INIT_SQL = "yaml/regcenter/data-source-init-sql.yaml";
    
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
        when(registryCenterRepository.get("/metadata/sharding_db/dataSources")).thenReturn(readYAML(DATA_SOURCE_YAM));
        Map<String, DataSourceConfiguration> actual = dataSourceRegistryService.load("sharding_db");
        assertThat(actual.size(), is(2));
        assertDataSourceConfiguration(actual.get("ds_0"), createDataSourceConfiguration(createDataSource("ds_0")));
        assertDataSourceConfiguration(actual.get("ds_1"), createDataSourceConfiguration(createDataSource("ds_1")));
    }
    
    private void assertDataSourceConfiguration(final DataSourceConfiguration actual, final DataSourceConfiguration expected) {
        assertThat(actual.getDataSourceClassName(), is(expected.getDataSourceClassName()));
        assertThat(actual.getProps().get("url"), is(expected.getProps().get("url")));
        assertThat(actual.getProps().get("username"), is(expected.getProps().get("username")));
        assertThat(actual.getProps().get("password"), is(expected.getProps().get("password")));
    }
    
    @Test
    public void assertLoadWhenPathNotExist() {
        when(registryCenterRepository.get("/metadata/sharding_db/dataSources")).thenReturn("");
        Map<String, DataSourceConfiguration> actual = dataSourceRegistryService.load("sharding_db");
        assertThat(actual.size(), is(0));
    }
    
    @Test
    public void assertLoadWithConnectionInitSQLs() {
        when(registryCenterRepository.get("/metadata/sharding_db/dataSources")).thenReturn(readYAML(DATA_SOURCE_YAML_WITH_CONNECTION_INIT_SQL));
        Map<String, DataSourceConfiguration> actual = dataSourceRegistryService.load("sharding_db");
        assertThat(actual.size(), is(2));
        assertDataSourceConfigurationWithConnectionInitSQLs(actual.get("ds_0"), createDataSourceConfiguration(createDataSourceWithConnectionInitSQLs("ds_0")));
        assertDataSourceConfigurationWithConnectionInitSQLs(actual.get("ds_1"), createDataSourceConfiguration(createDataSourceWithConnectionInitSQLs("ds_1")));
    }
    
    private DataSource createDataSourceWithConnectionInitSQLs(final String name) {
        MockDataSource result = new MockDataSource();
        result.setDriverClassName("com.mysql.jdbc.Driver");
        result.setUrl("jdbc:mysql://localhost:3306/" + name);
        result.setUsername("root");
        result.setPassword("root");
        result.setConnectionInitSqls(Arrays.asList("set names utf8mb4;", "set names utf8;"));
        return result;
    }
    
    private void assertDataSourceConfigurationWithConnectionInitSQLs(final DataSourceConfiguration actual, final DataSourceConfiguration expected) {
        assertThat(actual.getDataSourceClassName(), is(expected.getDataSourceClassName()));
        assertThat(actual.getProps().get("url"), is(expected.getProps().get("url")));
        assertThat(actual.getProps().get("username"), is(expected.getProps().get("username")));
        assertThat(actual.getProps().get("password"), is(expected.getProps().get("password")));
        assertThat(actual.getProps().get("connectionInitSqls"), is(expected.getProps().get("connectionInitSqls")));
    }
    
    private DataSourceConfiguration createDataSourceConfiguration(final DataSource dataSource) {
        return DataSourceConfiguration.getDataSourceConfiguration(dataSource);
    }
    
    @SneakyThrows({IOException.class, URISyntaxException.class})
    private String readYAML(final String yamlFile) {
        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource(yamlFile).toURI()))
                .stream().filter(each -> !each.startsWith("#")).map(each -> each + System.lineSeparator()).collect(Collectors.joining());
    }
    
    private DataSource createDataSource(final String name) {
        MockDataSource result = new MockDataSource();
        result.setDriverClassName("com.mysql.jdbc.Driver");
        result.setUrl("jdbc:mysql://localhost:3306/" + name);
        result.setUsername("root");
        result.setPassword("root");
        return result;
    }
}
