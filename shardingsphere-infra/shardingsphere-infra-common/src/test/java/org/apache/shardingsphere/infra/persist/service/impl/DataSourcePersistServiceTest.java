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

package org.apache.shardingsphere.infra.persist.service.impl;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.mode.repository.PersistRepository;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DataSourcePersistServiceTest {
    
    @Mock
    private PersistRepository repository;
    
    @Test
    public void assertLoad() {
        when(repository.get("/metadata/foo_db/dataSources")).thenReturn(readDataSourceYaml());
        Map<String, DataSourceConfiguration> actual = new DataSourcePersistService(repository).load("foo_db");
        assertThat(actual.size(), is(2));
        assertDataSourceConfiguration(actual.get("ds_0"), DataSourceConfiguration.getDataSourceConfiguration(createDataSource("ds_0")));
        assertDataSourceConfiguration(actual.get("ds_1"), DataSourceConfiguration.getDataSourceConfiguration(createDataSource("ds_1")));
    }
    
    @SneakyThrows({IOException.class, URISyntaxException.class})
    private String readDataSourceYaml() {
        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource("yaml/persist/data-source.yaml").toURI()))
                .stream().map(each -> each + System.lineSeparator()).collect(Collectors.joining());
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
        when(repository.get("/metadata/foo_db/dataSources")).thenReturn("");
        Map<String, DataSourceConfiguration> actual = new DataSourcePersistService(repository).load("foo_db");
        assertTrue(actual.isEmpty());
    }
    
    @Test
    public void assertAppend() {
        when(repository.get("/metadata/foo_db/dataSources")).thenReturn("");
        new DataSourcePersistService(repository).append("foo_db", Collections.singletonMap("foo_ds", DataSourceConfiguration.getDataSourceConfiguration(createDataSource("foo_ds"))));
        // TODO load from YAML file
        String expected = "foo_ds:\n" + "  driverClassName: com.mysql.jdbc.Driver\n" + "  password: root\n"
                + "  dataSourceClassName: org.apache.shardingsphere.test.mock.MockedDataSource\n" + "  connectionInitSqls:\n" + "  - set names utf8mb4;\n"
                + "  - set names utf8;\n" + "  url: jdbc:mysql://localhost:3306/foo_ds\n" + "  username: root\n";
        verify(repository).persist("/metadata/foo_db/dataSources", expected);
    }
    
    @Test
    public void assertDrop() {
        // TODO load from YAML file
        String actual = "foo_ds:\n" + "  driverClassName: com.mysql.jdbc.Driver\n" + "  password: root\n"
                + "  dataSourceClassName: org.apache.shardingsphere.test.mock.MockedDataSource\n" + "  connectionInitSqls:\n" + "  - set names utf8mb4;\n"
                + "  - set names utf8;\n" + "  url: jdbc:mysql://localhost:3306/foo_ds\n" + "  username: root\n";
        when(repository.get("/metadata/foo_db/dataSources")).thenReturn(actual);
        new DataSourcePersistService(repository).drop("foo_db", Collections.singleton("foo_ds"));
        verify(repository).persist("/metadata/foo_db/dataSources", "{}\n");
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
