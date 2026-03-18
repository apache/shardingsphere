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
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;

class StorageUnitNodeMapCreatorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("createArguments")
    void assertCreate(final String name, final boolean instanceConnectionEnabled, final boolean instanceConnectionAvailable,
                      final boolean unrecognizedDatabaseUrl, final StorageNode expectedStorageNode) {
        String url = "jdbc:mock://127.0.0.1/foo_ds";
        ConnectionPropertiesParser parser = mock(ConnectionPropertiesParser.class);
        DataSourcePoolProperties dataSourcePoolProps = createDataSourcePoolProperties();
        try (
                MockedStatic<DatabaseTypeFactory> mockedDatabaseTypeFactory = mockStatic(DatabaseTypeFactory.class);
                MockedStatic<DatabaseTypedSPILoader> mockedLoader = mockStatic(DatabaseTypedSPILoader.class);
                MockedConstruction<DatabaseTypeRegistry> ignored = mockConstruction(DatabaseTypeRegistry.class, (mock, context) -> {
                    DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class, RETURNS_DEEP_STUBS);
                    when(dialectDatabaseMetaData.getConnectionOption().isInstanceConnectionAvailable()).thenReturn(instanceConnectionAvailable);
                    when(mock.getDialectDatabaseMetaData()).thenReturn(dialectDatabaseMetaData);
                })) {
            mockedDatabaseTypeFactory.when(() -> DatabaseTypeFactory.get(url)).thenReturn(databaseType);
            mockedLoader.when(() -> DatabaseTypedSPILoader.getService(ConnectionPropertiesParser.class, databaseType)).thenReturn(parser);
            if (unrecognizedDatabaseUrl) {
                when(parser.parse(url, "sa", null)).thenThrow(UnrecognizedDatabaseURLException.class);
            } else {
                when(parser.parse(url, "sa", null)).thenReturn(new ConnectionProperties("127.0.0.1", 3307, null, "foo_schema", new Properties()));
            }
            Map<String, StorageNode> actual = StorageUnitNodeMapCreator.create(Collections.singletonMap("foo_ds", dataSourcePoolProps), instanceConnectionEnabled);
            assertThat(actual.size(), is(1));
            assertThat(actual.get("foo_ds"), is(expectedStorageNode));
        }
    }
    
    private DataSourcePoolProperties createDataSourcePoolProperties() {
        Map<String, Object> standardProps = new HashMap<>(2, 1F);
        standardProps.put("url", "jdbc:mock://127.0.0.1/foo_ds");
        standardProps.put("username", "sa");
        DataSourcePoolProperties result = mock(DataSourcePoolProperties.class, RETURNS_DEEP_STUBS);
        when(result.getConnectionPropertySynonyms().getStandardProperties()).thenReturn(standardProps);
        return result;
    }
    
    private static Stream<Arguments> createArguments() {
        return Stream.of(
                Arguments.of("instance connection enabled and available", true, true, false, new StorageNode("127.0.0.1", 3307, "sa")),
                Arguments.of("instance connection enabled and unavailable", true, false, false, new StorageNode("foo_ds")),
                Arguments.of("instance connection disabled and available", false, true, false, new StorageNode("foo_ds")),
                Arguments.of("unrecognized database url", true, true, true, new StorageNode("foo_ds")));
    }
}
