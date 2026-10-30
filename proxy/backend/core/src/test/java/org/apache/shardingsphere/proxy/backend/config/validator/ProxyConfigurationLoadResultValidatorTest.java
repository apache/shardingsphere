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

package org.apache.shardingsphere.proxy.backend.config.validator;

import org.apache.shardingsphere.infra.datasource.pool.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.DuplicateStorageUnitException;
import org.apache.shardingsphere.proxy.backend.config.ProxyConfigurationLoadResult;
import org.apache.shardingsphere.proxy.backend.config.ProxyDatabaseConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class ProxyConfigurationLoadResultValidatorTest {
    
    @Test
    void assertValidateWithoutDuplicates() {
        ProxyConfigurationLoadResult loadResult = createConfig("foo_db", "foo_db", Collections.singletonMap("global_ds", mock(DataSourceConfiguration.class)),
                Collections.singletonMap("db_ds", mock(DataSourceConfiguration.class)));
        assertDoesNotThrow(() -> ProxyConfigurationLoadResultValidator.validate(loadResult));
    }
    
    private ProxyConfigurationLoadResult createConfig(final String databaseKey, final String databaseName, final Map<String, DataSourceConfiguration> globalDataSources,
                                                      final Map<String, DataSourceConfiguration> databaseDataSources) {
        return new ProxyConfigurationLoadResult(null, globalDataSources, Collections.emptyList(), new Properties(),
                Collections.singletonMap(databaseKey, new ProxyDatabaseConfiguration(databaseName, databaseDataSources, Collections.emptyList())));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("duplicateDataSourceArguments")
    void assertValidateWithDuplicateDataSource(final String name, final String databaseKey, final String databaseName, final String dataSourceName, final String expectedMessage) {
        Map<String, DataSourceConfiguration> dataSources = Collections.singletonMap(dataSourceName, mock(DataSourceConfiguration.class));
        ProxyConfigurationLoadResult loadResult = createConfig(databaseKey, databaseName, dataSources, dataSources);
        DuplicateStorageUnitException actual = assertThrows(DuplicateStorageUnitException.class, () -> ProxyConfigurationLoadResultValidator.validate(loadResult));
        assertThat(actual.getMessage(), is(expectedMessage));
        SQLException actualSQLException = actual.toSQLException();
        assertThat(actualSQLException.getMessage(), is(expectedMessage));
        assertThat(actualSQLException.getSQLState(), is("42S01"));
        assertThat(actualSQLException.getErrorCode(), is(10104));
    }
    
    private static Stream<Arguments> duplicateDataSourceArguments() {
        return Stream.of(
                Arguments.of("same database key and name", "foo_db", "foo_db", "ds_0", "Duplicate storage unit names 'ds_0' on database 'foo_db'."),
                Arguments.of("different database key and name", "map_key", "configured_db", "ds_0", "Duplicate storage unit names 'ds_0' on database 'configured_db'."),
                Arguments.of("special characters in map keys", "db.[key]{value}${suffix}\\key", "foo_db", "ds.[0]{value}${suffix}\\name",
                        "Duplicate storage unit names 'ds.[0]{value}${suffix}\\name' on database 'foo_db'."));
    }
    
    @Test
    void assertValidateWithMultipleDuplicateDataSources() {
        Map<String, DataSourceConfiguration> dataSources = new HashMap<>(2, 1F);
        dataSources.put("ds_0", mock(DataSourceConfiguration.class));
        dataSources.put("ds_1", mock(DataSourceConfiguration.class));
        ProxyConfigurationLoadResult loadResult = createConfig("foo_db", "foo_db", dataSources, dataSources);
        DuplicateStorageUnitException actual = assertThrows(DuplicateStorageUnitException.class, () -> ProxyConfigurationLoadResultValidator.validate(loadResult));
        assertThat(actual.getMessage(), anyOf(is("Duplicate storage unit names 'ds_0, ds_1' on database 'foo_db'."), is("Duplicate storage unit names 'ds_1, ds_0' on database 'foo_db'.")));
        SQLException actualSQLException = actual.toSQLException();
        assertThat(actualSQLException.getMessage(), is(actual.getMessage()));
        assertThat(actualSQLException.getSQLState(), is("42S01"));
        assertThat(actualSQLException.getErrorCode(), is(10104));
    }
}
