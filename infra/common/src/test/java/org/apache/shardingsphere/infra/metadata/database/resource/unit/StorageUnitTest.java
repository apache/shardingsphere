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

package org.apache.shardingsphere.infra.metadata.database.resource.unit;

import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionProperties;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionPropertiesParser;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StorageUnitTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataSourcePoolProperties dataSourcePoolProperties;
    
    @Test
    void assertNewWithoutUsernameAndIsNotInstanceConnectionAvailable() {
        Map<String, Object> standardProperties = Collections.singletonMap("url", "jdbc:mock://127.0.0.1/foo_ds");
        try (MockedStatic<DatabaseTypedSPILoader> mockedLoader = mockStatic(DatabaseTypedSPILoader.class)) {
            when(dataSourcePoolProperties.getConnectionPropertySynonyms().getStandardProperties()).thenReturn(standardProperties);
            mockedLoader.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(mock(DialectDatabaseMetaData.class, RETURNS_DEEP_STUBS));
            ConnectionPropertiesParser parser = mock(ConnectionPropertiesParser.class);
            when(parser.parse("jdbc:mock://127.0.0.1/foo_ds", "", null)).thenReturn(new ConnectionProperties("127.0.0.1", 3307, null, "foo_schema", new Properties()));
            mockedLoader.when(() -> DatabaseTypedSPILoader.getService(ConnectionPropertiesParser.class, databaseType)).thenReturn(parser);
            StorageNode storageNode = mock(StorageNode.class);
            DataSource dataSource = mock(DataSource.class);
            StorageUnit actual = new StorageUnit(storageNode, dataSourcePoolProperties, dataSource);
            assertThat(actual.getStorageNode(), is(storageNode));
            assertThat(actual.getStorageType(), is(databaseType));
            assertThat(actual.getDataSource(), is(dataSource));
            assertThat(actual.getConnectionProperties().getHostname(), is("127.0.0.1"));
            assertThat(actual.getConnectionProperties().getPort(), is(3307));
            assertNull(actual.getConnectionProperties().getCatalog());
            assertThat(actual.getConnectionProperties().getSchema(), is("foo_schema"));
        }
    }
    
    @Test
    void assertNewWithUsernameAndIsInstanceConnectionAvailable() {
        Map<String, Object> standardProperties = new HashMap<>(2, 1F);
        standardProperties.put("url", "jdbc:mock://127.0.0.1/foo_ds");
        standardProperties.put("username", "sa");
        try (MockedStatic<DatabaseTypedSPILoader> mockedLoader = mockStatic(DatabaseTypedSPILoader.class)) {
            when(dataSourcePoolProperties.getConnectionPropertySynonyms().getStandardProperties()).thenReturn(standardProperties);
            DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class, RETURNS_DEEP_STUBS);
            when(dialectDatabaseMetaData.getConnectionOption().isInstanceConnectionAvailable()).thenReturn(true);
            mockedLoader.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(dialectDatabaseMetaData);
            ConnectionPropertiesParser parser = mock(ConnectionPropertiesParser.class);
            when(parser.parse("jdbc:mock://127.0.0.1/foo_ds", "sa", null))
                    .thenReturn(new ConnectionProperties("127.0.0.1", 3307, "foo_catalog", "foo_schema", new Properties()));
            when(parser.parse("jdbc:mock://127.0.0.1/foo_ds", "sa", "foo_catalog"))
                    .thenReturn(new ConnectionProperties("127.0.0.1", 3307, "foo_catalog", "foo_schema", new Properties()));
            mockedLoader.when(() -> DatabaseTypedSPILoader.getService(ConnectionPropertiesParser.class, databaseType)).thenReturn(parser);
            StorageUnit actual = new StorageUnit(mock(), dataSourcePoolProperties, mock());
            assertThat(actual.getConnectionProperties().getHostname(), is("127.0.0.1"));
            assertThat(actual.getConnectionProperties().getPort(), is(3307));
            assertThat(actual.getConnectionProperties().getCatalog(), is("foo_catalog"));
            assertThat(actual.getConnectionProperties().getSchema(), is("foo_schema"));
        }
    }
}
