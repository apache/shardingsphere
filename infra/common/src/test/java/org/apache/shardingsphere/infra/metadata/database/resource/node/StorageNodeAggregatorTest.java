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

package org.apache.shardingsphere.infra.metadata.database.resource.node;

import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.MockedConstruction;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

class StorageNodeAggregatorTest {
    
    @Test
    void assertAggregateDataSources() {
        DataSource dataSource = mock(DataSource.class);
        Map<StorageNode, DataSource> actual = StorageNodeAggregator.aggregateDataSources(Collections.singletonMap("foo_ds", dataSource));
        assertThat(actual.size(), is(1));
        assertThat(actual.get(new StorageNode("foo_ds")), is(dataSource));
    }
    
    @Test
    void assertAggregateDataSourcePoolPropertiesWithInstanceConnectionUnavailable() {
        Map<String, Object> standardProps = new HashMap<>(2, 1F);
        standardProps.put("url", "jdbc:mock://127.0.0.1/foo_db");
        standardProps.put("username", "root");
        DataSourcePoolProperties dataSourcePoolProps = mock(DataSourcePoolProperties.class, Answers.RETURNS_DEEP_STUBS);
        when(dataSourcePoolProps.getConnectionPropertySynonyms().getStandardProperties()).thenReturn(standardProps);
        Map<StorageNode, DataSourcePoolProperties> actual = StorageNodeAggregator.aggregateDataSourcePoolProperties(Collections.singletonMap("foo_ds", dataSourcePoolProps), true);
        assertThat(actual.size(), is(1));
        assertThat(actual.get(actual.keySet().iterator().next()), is(dataSourcePoolProps));
    }
    
    @Test
    void assertAggregateDataSourcePoolPropertiesWithInstanceConnectionAvailable() {
        Map<String, Object> standardProps = new HashMap<>(2, 1F);
        standardProps.put("url", "jdbc:mock://127.0.0.1/foo_db");
        standardProps.put("username", "root");
        DataSourcePoolProperties dataSourcePoolProps = mock(DataSourcePoolProperties.class, Answers.RETURNS_DEEP_STUBS);
        when(dataSourcePoolProps.getConnectionPropertySynonyms().getStandardProperties()).thenReturn(standardProps);
        try (MockedConstruction<DatabaseTypeRegistry> ignored = mockConstruction(DatabaseTypeRegistry.class, (mock, mockContext) -> {
            DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class, RETURNS_DEEP_STUBS);
            when(dialectDatabaseMetaData.getConnectionOption().isInstanceConnectionAvailable()).thenReturn(true);
            when(mock.getDialectDatabaseMetaData()).thenReturn(dialectDatabaseMetaData);
        })) {
            Map<StorageNode, DataSourcePoolProperties> actual = StorageNodeAggregator.aggregateDataSourcePoolProperties(Collections.singletonMap("foo_ds", dataSourcePoolProps), true);
            assertThat(actual.size(), is(1));
            assertThat(actual.get(new StorageNode("127.0.0.1_-1_root")), is(dataSourcePoolProps));
        }
    }
    
    @Test
    void assertAggregateDataSourcePoolPropertiesWithUnrecognizedDatabaseURL() {
        Map<String, Object> standardProps = new HashMap<>(2, 1F);
        standardProps.put("url", "jdbc:h2:invalid_format");
        standardProps.put("username", "root");
        DataSourcePoolProperties dataSourcePoolProps = mock(DataSourcePoolProperties.class, Answers.RETURNS_DEEP_STUBS);
        when(dataSourcePoolProps.getConnectionPropertySynonyms().getStandardProperties()).thenReturn(standardProps);
        Map<StorageNode, DataSourcePoolProperties> actual = StorageNodeAggregator.aggregateDataSourcePoolProperties(Collections.singletonMap("foo_ds", dataSourcePoolProps), true);
        assertThat(actual.size(), is(1));
        assertThat(actual.get(new StorageNode("foo_ds")), is(dataSourcePoolProps));
    }
}
