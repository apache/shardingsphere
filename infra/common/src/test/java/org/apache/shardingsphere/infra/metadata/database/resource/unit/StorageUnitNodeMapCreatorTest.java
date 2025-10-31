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

import org.apache.shardingsphere.database.connector.core.exception.UnrecognizedDatabaseURLException;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionProperties;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionPropertiesParser;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StorageUnitNodeMapCreatorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataSourcePoolProperties dataSourcePoolProps;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DialectDatabaseMetaData dialectDatabaseMetaData;
    
    @BeforeEach
    void setUp() {
        Map<String, Object> standardProps = new HashMap<>(2, 1F);
        standardProps.put("url", "jdbc:mock://127.0.0.1/foo_ds");
        standardProps.put("username", "sa");
        when(dataSourcePoolProps.getConnectionPropertySynonyms().getStandardProperties()).thenReturn(standardProps);
    }
    
    @Test
    void assertNewWithIsInstanceConnectionAvailable() {
        try (MockedStatic<DatabaseTypedSPILoader> mockedLoader = mockStatic(DatabaseTypedSPILoader.class)) {
            when(dialectDatabaseMetaData.getConnectionOption().isInstanceConnectionAvailable()).thenReturn(true);
            mockedLoader.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(dialectDatabaseMetaData);
            ConnectionPropertiesParser parser = mock(ConnectionPropertiesParser.class);
            when(parser.parse("jdbc:mock://127.0.0.1/foo_ds", "sa", null)).thenReturn(new ConnectionProperties("127.0.0.1", 3307, null, "foo_schema", new Properties()));
            mockedLoader.when(() -> DatabaseTypedSPILoader.getService(ConnectionPropertiesParser.class, databaseType)).thenReturn(parser);
            Map<String, StorageNode> actual = StorageUnitNodeMapCreator.create(Collections.singletonMap("foo_ds", dataSourcePoolProps));
            assertThat(actual.size(), is(1));
            assertTrue(actual.containsKey("foo_ds"));
        }
    }
    
    @Test
    void assertNewWithIsNotInstanceConnectionAvailable() {
        try (MockedStatic<DatabaseTypedSPILoader> mockedLoader = mockStatic(DatabaseTypedSPILoader.class)) {
            mockedLoader.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(dialectDatabaseMetaData);
            ConnectionPropertiesParser parser = mock(ConnectionPropertiesParser.class);
            when(parser.parse("jdbc:mock://127.0.0.1/foo_ds", "sa", null)).thenReturn(new ConnectionProperties("127.0.0.1", 3307, null, "foo_schema", new Properties()));
            mockedLoader.when(() -> DatabaseTypedSPILoader.getService(ConnectionPropertiesParser.class, databaseType)).thenReturn(parser);
            Map<String, StorageNode> actual = StorageUnitNodeMapCreator.create(Collections.singletonMap("foo_ds", dataSourcePoolProps));
            assertThat(actual.size(), is(1));
            assertTrue(actual.containsKey("foo_ds"));
        }
    }
    
    @Test
    void assertNewWithUnrecognizedDatabaseURLException() {
        try (MockedStatic<DatabaseTypedSPILoader> mockedLoader = mockStatic(DatabaseTypedSPILoader.class)) {
            mockedLoader.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(dialectDatabaseMetaData);
            ConnectionPropertiesParser parser = mock(ConnectionPropertiesParser.class);
            when(parser.parse("jdbc:mock://127.0.0.1/foo_ds", "sa", null)).thenThrow(UnrecognizedDatabaseURLException.class);
            mockedLoader.when(() -> DatabaseTypedSPILoader.getService(ConnectionPropertiesParser.class, databaseType)).thenReturn(parser);
            Map<String, StorageNode> actual = StorageUnitNodeMapCreator.create(Collections.singletonMap("foo_ds", dataSourcePoolProps));
            assertThat(actual.size(), is(1));
            assertTrue(actual.containsKey("foo_ds"));
        }
    }
}
