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

package org.apache.shardingsphere.distsql.handler.executor.ral.plugin;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseSupportedTypedSPI;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.show.ShowPluginsStatement;
import org.apache.shardingsphere.infra.exception.generic.PluginNotFoundException;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.spi.type.typed.plugin.PluginTypeAndClassMapper;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class ShowPluginsExecutorTest {
    
    private final ShowPluginsExecutor executor = (ShowPluginsExecutor) TypedSPILoader.getService(DistSQLQueryExecutor.class, ShowPluginsStatement.class);
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getColumnNamesArguments")
    void assertGetColumnNames(final String name, final ShowPluginsStatement sqlStatement, final Class<? extends TypedSPI> mappedPluginClass,
                              final Collection<String> expectedColumnNames, final Class<? extends Throwable> expectedExceptionType, final String expectedExceptionMessage) {
        try (MockedStatic<TypedSPILoader> mockedStatic = mockStatic(TypedSPILoader.class)) {
            if (null != mappedPluginClass) {
                PluginTypeAndClassMapper pluginTypeAndClassMapper = mock(PluginTypeAndClassMapper.class);
                when(pluginTypeAndClassMapper.getPluginClass()).thenAnswer(inv -> mappedPluginClass);
                mockedStatic.when(() -> TypedSPILoader.getService(PluginTypeAndClassMapper.class, "fixture_mapper")).thenReturn(pluginTypeAndClassMapper);
            }
            if (null == expectedExceptionType) {
                assertThat(executor.getColumnNames(sqlStatement), is(expectedColumnNames));
            } else {
                assertThat(assertThrows(expectedExceptionType, () -> executor.getColumnNames(sqlStatement)).getMessage(), is(expectedExceptionMessage));
            }
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getRowsArguments")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void assertGetRows(final String name, final ShowPluginsStatement sqlStatement, final Class<? extends TypedSPI> mappedPluginClass,
                       final Class<? extends TypedSPI> servicePluginClass, final Collection<TypedSPI> serviceInstances, final Collection<DatabaseType> databaseTypes,
                       final Collection<String> expectedCells, final Class<? extends Throwable> expectedExceptionType, final String expectedExceptionMessage) {
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class);
                MockedStatic<ShardingSphereServiceLoader> serviceLoader = mockStatic(ShardingSphereServiceLoader.class)) {
            if (null != mappedPluginClass) {
                PluginTypeAndClassMapper pluginTypeAndClassMapper = mock(PluginTypeAndClassMapper.class);
                when(pluginTypeAndClassMapper.getPluginClass()).thenAnswer(inv -> mappedPluginClass);
                typedSPILoader.when(() -> TypedSPILoader.getService(PluginTypeAndClassMapper.class, "fixture_mapper")).thenReturn(pluginTypeAndClassMapper);
            }
            if (null != servicePluginClass) {
                serviceLoader.when(() -> ShardingSphereServiceLoader.getServiceInstances((Class) servicePluginClass)).thenReturn(serviceInstances);
            }
            if (!databaseTypes.isEmpty()) {
                serviceLoader.when(() -> ShardingSphereServiceLoader.getServiceInstances(DatabaseType.class)).thenReturn(databaseTypes);
            }
            if (null == expectedExceptionType) {
                Collection<LocalDataQueryResultRow> actual = executor.getRows(sqlStatement, mock(ContextManager.class));
                assertThat(actual.size(), is(1));
                LocalDataQueryResultRow actualRow = actual.iterator().next();
                int index = 1;
                for (String each : expectedCells) {
                    assertThat(actualRow.getCell(index), is(each));
                    index++;
                }
            } else {
                assertThat(assertThrows(expectedExceptionType, () -> executor.getRows(sqlStatement, mock(ContextManager.class))).getMessage(), is(expectedExceptionMessage));
            }
        }
    }
    
    private static Stream<Arguments> getColumnNamesArguments() {
        return Stream.of(
                Arguments.of("explicit typed plugin class",
                        new ShowPluginsStatement("ignored_type", TypedSPI.class.getName()), null, Arrays.asList("type", "type_aliases", "description"), null, null),
                Arguments.of("mapped database supported plugin class",
                        new ShowPluginsStatement("fixture_mapper"), DatabaseSupportedTypedSPI.class, Arrays.asList("type", "type_aliases", "supported_database_types", "description"), null, null),
                Arguments.of("explicit non typed plugin class",
                        new ShowPluginsStatement("ignored_type", String.class.getName()), null, null, UnsupportedOperationException.class, "The plugin class to be queried must extend TypedSPI."),
                Arguments.of("unknown plugin class", new ShowPluginsStatement("ignored_type", "org.apache.shardingsphere.missing.UnknownPlugin"),
                        null, null, PluginNotFoundException.class, "Can not find plugin class 'org.apache.shardingsphere.missing.UnknownPlugin'."));
    }
    
    private static Stream<Arguments> getRowsArguments() {
        return Stream.of(
                Arguments.of("explicit typed plugin rows", new ShowPluginsStatement("ignored_type", TypedSPI.class.getName()), null,
                        TypedSPI.class, Collections.singleton(createRegularTypedSPI()), Collections.emptyList(), Arrays.asList("regular_type", "regular_alias", ""), null, null),
                Arguments.of("mapped database supported plugin rows", new ShowPluginsStatement("fixture_mapper"), DatabaseSupportedTypedSPI.class,
                        DatabaseSupportedTypedSPI.class, Collections.singleton(createDatabaseAwareTypedSPI()),
                        Arrays.asList(createDatabaseType("foo_db"), createDatabaseType("bar_db")), Arrays.asList("database_aware_type", "database_aware_alias", "foo_db,bar_db", ""), null, null),
                Arguments.of("explicit non typed plugin rows", new ShowPluginsStatement("ignored_type", String.class.getName()), null,
                        null, Collections.emptyList(), Collections.emptyList(), null, UnsupportedOperationException.class, "The plugin class to be queried must extend TypedSPI."),
                Arguments.of("unknown plugin rows", new ShowPluginsStatement("ignored_type", "org.apache.shardingsphere.missing.UnknownPlugin"), null,
                        null, Collections.emptyList(), Collections.emptyList(), null, PluginNotFoundException.class, "Can not find plugin class 'org.apache.shardingsphere.missing.UnknownPlugin'."));
    }
    
    private static TypedSPI createRegularTypedSPI() {
        TypedSPI result = mock(TypedSPI.class);
        when(result.getType()).thenReturn("regular_type");
        when(result.getTypeAliases()).thenReturn(Collections.singleton("regular_alias"));
        return result;
    }
    
    private static DatabaseSupportedTypedSPI createDatabaseAwareTypedSPI() {
        DatabaseSupportedTypedSPI result = mock(DatabaseSupportedTypedSPI.class);
        when(result.getType()).thenReturn("database_aware_type");
        when(result.getTypeAliases()).thenReturn(Collections.singleton("database_aware_alias"));
        when(result.getSupportedDatabaseTypes()).thenReturn(Collections.emptyList());
        return result;
    }
    
    private static DatabaseType createDatabaseType(final String type) {
        DatabaseType result = mock(DatabaseType.class);
        when(result.getType()).thenReturn(type);
        return result;
    }
}
