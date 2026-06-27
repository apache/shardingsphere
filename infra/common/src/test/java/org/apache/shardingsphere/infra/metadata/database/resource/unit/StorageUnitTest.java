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
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.datasource.pool.CatalogSwitchableDataSource;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class StorageUnitTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("newArguments")
    void assertNew(final String name, final String username,
                   final boolean instanceStorageNode, final boolean expectedCatalogSwitchableDataSource, final boolean expectedReuseOriginalDataSource, final String expectedCatalog) {
        String url = "jdbc:mock://127.0.0.1/foo_ds";
        DataSource dataSource = mock(DataSource.class);
        ConnectionPropertiesParser parser = mock(ConnectionPropertiesParser.class);
        StorageNode storageNode = mock(StorageNode.class);
        when(storageNode.isInstanceStorageNode()).thenReturn(instanceStorageNode);
        DataSourcePoolProperties dataSourcePoolProps = createDataSourcePoolProperties(username);
        try (
                MockedStatic<DatabaseTypeFactory> mockedDatabaseTypeFactory = mockStatic(DatabaseTypeFactory.class);
                MockedStatic<DatabaseTypedSPILoader> mockedLoader = mockStatic(DatabaseTypedSPILoader.class)) {
            mockedDatabaseTypeFactory.when(() -> DatabaseTypeFactory.get(url)).thenReturn(databaseType);
            mockedLoader.when(() -> DatabaseTypedSPILoader.getService(ConnectionPropertiesParser.class, databaseType)).thenReturn(parser);
            if (instanceStorageNode) {
                when(parser.parse(url, username, null)).thenReturn(new ConnectionProperties("127.0.0.1", 3307, expectedCatalog, "ignored_schema", new Properties()));
                when(parser.parse(url, username, expectedCatalog)).thenReturn(new ConnectionProperties("127.0.0.1", 3307, expectedCatalog, "foo_schema", new Properties()));
            } else {
                when(parser.parse(url, null == username ? "" : username, null)).thenReturn(new ConnectionProperties("127.0.0.1", 3307, null, "foo_schema", new Properties()));
            }
            StorageUnit actual = new StorageUnit(storageNode, dataSourcePoolProps, dataSource);
            assertThat(actual.getStorageNode(), is(storageNode));
            assertThat(actual.getStorageType(), is(databaseType));
            assertThat(actual.getDataSourcePoolProperties(), is(dataSourcePoolProps));
            assertThat(actual.getDataSource() instanceof CatalogSwitchableDataSource, is(expectedCatalogSwitchableDataSource));
            assertThat(actual.getDataSource() == dataSource, is(expectedReuseOriginalDataSource));
            assertThat(actual.getConnectionProperties().getHostname(), is("127.0.0.1"));
            assertThat(actual.getConnectionProperties().getPort(), is(3307));
            assertThat(actual.getConnectionProperties().getCatalog(), is(expectedCatalog));
            assertThat(actual.getConnectionProperties().getSchema(), is("foo_schema"));
        }
    }
    
    private DataSourcePoolProperties createDataSourcePoolProperties(final String username) {
        Map<String, Object> standardProps = new HashMap<>(2, 1F);
        standardProps.put("url", "jdbc:mock://127.0.0.1/foo_ds");
        if (null != username) {
            standardProps.put("username", username);
        }
        DataSourcePoolProperties result = mock(DataSourcePoolProperties.class, RETURNS_DEEP_STUBS);
        when(result.getConnectionPropertySynonyms().getStandardProperties()).thenReturn(standardProps);
        return result;
    }
    
    private static Stream<Arguments> newArguments() {
        return Stream.of(
                Arguments.of("without username and non-instance storage node", null, false, false, true, null),
                Arguments.of("with username and non-instance storage node", "sa", false, false, true, null),
                Arguments.of("with username and instance storage node", "sa", true, true, false, "foo_catalog"));
    }
}
