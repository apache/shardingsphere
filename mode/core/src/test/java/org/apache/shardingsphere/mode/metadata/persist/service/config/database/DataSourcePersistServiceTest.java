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

package org.apache.shardingsphere.mode.metadata.persist.service.config.database;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.mode.persist.PersistRepository;
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DataSourcePersistServiceTest {
    
    @Mock
    private PersistRepository repository;
    
    @Test
    public void assertLoad() {
        when(repository.getDirectly("/metadata/foo_db/active_version")).thenReturn("0");
        when(repository.getDirectly("/metadata/foo_db/versions/0/dataSources")).thenReturn(readDataSourceYaml("yaml/persist/data-source.yaml"));
        Map<String, DataSourceProperties> actual = new DataSourcePersistService(repository).load("foo_db");
        assertThat(actual.size(), is(2));
        assertDataSourceProperties(actual.get("ds_0"), DataSourcePropertiesCreator.create(createDataSource("ds_0")));
        assertDataSourceProperties(actual.get("ds_1"), DataSourcePropertiesCreator.create(createDataSource("ds_1")));
    }
    
    @SneakyThrows({IOException.class, URISyntaxException.class})
    private String readDataSourceYaml(final String path) {
        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource(path).toURI()))
                .stream().filter(each -> !"".equals(each.trim()) && !each.startsWith("#")).map(each -> each + System.lineSeparator()).collect(Collectors.joining());
    }
    
    private void assertDataSourceProperties(final DataSourceProperties actual, final DataSourceProperties expected) {
        assertThat(actual.getDataSourceClassName(), is(expected.getDataSourceClassName()));
        assertThat(actual.getAllLocalProperties().get("url"), is(expected.getAllLocalProperties().get("url")));
        assertThat(actual.getAllLocalProperties().get("username"), is(expected.getAllLocalProperties().get("username")));
        assertThat(actual.getAllLocalProperties().get("password"), is(expected.getAllLocalProperties().get("password")));
        assertThat(actual.getAllLocalProperties().get("connectionInitSqls"), is(expected.getAllLocalProperties().get("connectionInitSqls")));
    }
    
    @Test
    public void assertLoadWithoutPath() {
        when(repository.getDirectly("/metadata/foo_db/active_version")).thenReturn("0");
        Map<String, DataSourceProperties> actual = new DataSourcePersistService(repository).load("foo_db");
        assertTrue(actual.isEmpty());
    }
    
    @Test
    public void assertAppend() {
        when(repository.getDirectly("/metadata/foo_db/active_version")).thenReturn("0");
        new DataSourcePersistService(repository).append("foo_db", Collections.singletonMap("foo_ds", DataSourcePropertiesCreator.create(createDataSource("foo_ds"))));
        String expected = readDataSourceYaml("yaml/persist/data-source-foo.yaml");
        verify(repository).persist("/metadata/foo_db/versions/0/dataSources", expected);
    }
    
    private DataSource createDataSource(final String name) {
        MockedDataSource result = new MockedDataSource();
        result.setUrl("jdbc:mysql://localhost:3306/" + name);
        result.setUsername("root");
        result.setPassword("root");
        result.setConnectionInitSqls(Arrays.asList("set names utf8mb4;", "set names utf8;"));
        return result;
    }
}
