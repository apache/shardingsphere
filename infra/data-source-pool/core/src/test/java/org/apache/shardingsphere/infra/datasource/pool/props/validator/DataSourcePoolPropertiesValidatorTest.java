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

package org.apache.shardingsphere.infra.datasource.pool.props.validator;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.database.connector.core.checker.DialectDatabasePrivilegeChecker;
import org.apache.shardingsphere.database.connector.core.checker.PrivilegeCheckType;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDriver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DataSourcePoolPropertiesValidatorTest {
    
    private static final String MOCKED_URL = "jdbc:mock://127.0.0.1/foo_ds";
    
    @BeforeAll
    static void setUp() throws ClassNotFoundException {
        Class.forName(MockedDriver.class.getName());
    }
    
    @Test
    void assertValidateWithEmptyPropsMap() {
        assertTrue(DataSourcePoolPropertiesValidator.validate(Collections.emptyMap(), Collections.emptySet()).isEmpty());
    }
    
    @Test
    void assertValidateWithValidHikariDataSource() {
        assertTrue(DataSourcePoolPropertiesValidator.validate(
                Collections.singletonMap("foo_ds", new DataSourcePoolProperties(HikariDataSource.class.getName(), createHikariDataSourceProperties(MOCKED_URL))), Collections.emptySet()).isEmpty());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("validateWithMockedDataSourcePrivilegesArguments")
    void assertValidateWithMockedDataSourcePrivileges(final String name, final Collection<PrivilegeCheckType> expectedPrivileges) {
        assertTrue(DataSourcePoolPropertiesValidator.validate(
                Collections.singletonMap("foo_ds", new DataSourcePoolProperties(MockedDataSource.class.getName(), createMockedDataSourceProperties())), expectedPrivileges).isEmpty());
    }
    
    @Test
    void assertValidateWithInvalidContentProperties() {
        DataSourcePoolPropertiesContentValidator validator = mock(DataSourcePoolPropertiesContentValidator.class);
        DataSourcePoolProperties dataSourcePoolProperties = new DataSourcePoolProperties(HikariDataSource.class.getName(), createHikariDataSourceProperties(MOCKED_URL));
        doThrow(new IllegalArgumentException("mock invalid properties")).when(validator).validate(dataSourcePoolProperties);
        try (MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class, CALLS_REAL_METHODS)) {
            typedSPILoader.when(() -> TypedSPILoader.findService(DataSourcePoolPropertiesContentValidator.class, HikariDataSource.class.getName())).thenReturn(Optional.of(validator));
            assertInvalidDataSource(DataSourcePoolPropertiesValidator.validate(
                    Collections.singletonMap("foo_ds", dataSourcePoolProperties), Collections.emptySet()), "Invalid data source `foo_ds`, error message is: mock invalid properties");
        }
    }
    
    @Test
    void assertValidateWithCreateFailure() {
        Map<String, Object> props = createMockedDataSourceProperties();
        props.put("connectionTimeout", "invalid");
        assertInvalidDataSource(DataSourcePoolPropertiesValidator.validate(Collections.singletonMap("foo_ds", new DataSourcePoolProperties(MockedDataSource.class.getName(), props)),
                Collections.singleton(PrivilegeCheckType.SELECT)), "Invalid data source `foo_ds`, error message is: For input string: \"invalid\"");
    }
    
    @Test
    void assertValidateWithCreateFailureBeforeAssignment() {
        final Map<String, Object> props = createMockedDataSourceProperties();
        DataSourcePoolProperties dataSourcePoolProperties = new DataSourcePoolProperties(MockedDataSource.class.getName(), props);
        try (MockedStatic<DataSourcePoolCreator> dataSourcePoolCreator = mockStatic(DataSourcePoolCreator.class)) {
            dataSourcePoolCreator.when(() -> DataSourcePoolCreator.create(dataSourcePoolProperties)).thenThrow(new IllegalStateException("mock create failure"));
            assertInvalidDataSource(DataSourcePoolPropertiesValidator.validate(Collections.singletonMap("foo_ds", dataSourcePoolProperties), Collections.emptySet()),
                    "Invalid data source `foo_ds`, error message is: mock create failure");
        }
    }
    
    private void assertInvalidDataSource(final Map<String, Exception> actual, final String expectedMessage) {
        assertThat(actual.size(), is(1));
        assertThat(actual.get("foo_ds"), isA(InvalidDataSourcePoolPropertiesException.class));
        assertThat(actual.get("foo_ds").getMessage(), is(expectedMessage));
    }
    
    @Test
    void assertValidateWithConnectionFailure() {
        Map<String, Exception> actual = DataSourcePoolPropertiesValidator.validate(
                Collections.singletonMap("foo_ds", new DataSourcePoolProperties(HikariDataSource.class.getName(), createHikariDataSourceProperties("jdbc:mock://127.0.0.1/invalid"))),
                Collections.singleton(PrivilegeCheckType.SELECT));
        assertThat(actual.size(), is(1));
        assertThat(actual.get("foo_ds"), isA(InvalidDataSourcePoolPropertiesException.class));
    }
    
    private Map<String, Object> createHikariDataSourceProperties(final String jdbcUrl) {
        Map<String, Object> result = new LinkedHashMap<>(2, 1F);
        result.put("driverClassName", MockedDriver.class.getName());
        result.put("jdbcUrl", jdbcUrl);
        return result;
    }
    
    @Test
    void assertValidateWithNullConnection() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Map<String, Object> props = createMockedDataSourceProperties();
        DataSourcePoolProperties dataSourcePoolProperties = new DataSourcePoolProperties(MockedDataSource.class.getName(), props);
        when(dataSource.getConnection()).thenReturn(null);
        try (MockedStatic<DataSourcePoolCreator> dataSourcePoolCreator = mockStatic(DataSourcePoolCreator.class)) {
            dataSourcePoolCreator.when(() -> DataSourcePoolCreator.create(dataSourcePoolProperties)).thenReturn(dataSource);
            assertTrue(DataSourcePoolPropertiesValidator.validate(Collections.singletonMap("foo_ds", dataSourcePoolProperties), Collections.emptySet()).isEmpty());
        }
    }
    
    @Test
    void assertValidateWithPrivilegesAndChecker() {
        DatabaseType databaseType = mock(DatabaseType.class);
        DialectDatabasePrivilegeChecker checker = mock(DialectDatabasePrivilegeChecker.class);
        try (
                MockedStatic<DatabaseTypeFactory> databaseTypeFactory = mockStatic(DatabaseTypeFactory.class);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            databaseTypeFactory.when(() -> DatabaseTypeFactory.get(MOCKED_URL)).thenReturn(databaseType);
            databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.findService(DialectDatabasePrivilegeChecker.class, databaseType)).thenReturn(Optional.of(checker));
            Map<String, Object> props = createMockedDataSourceProperties();
            assertTrue(DataSourcePoolPropertiesValidator.validate(Collections.singletonMap("foo_ds", new DataSourcePoolProperties(MockedDataSource.class.getName(), props)),
                    Arrays.asList(PrivilegeCheckType.SELECT, PrivilegeCheckType.XA)).isEmpty());
            verify(checker).check(any(DataSource.class), eq(PrivilegeCheckType.SELECT));
            verify(checker).check(any(DataSource.class), eq(PrivilegeCheckType.XA));
        }
    }
    
    private Map<String, Object> createMockedDataSourceProperties() {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("url", MOCKED_URL);
        result.put("username", "root");
        result.put("password", "root");
        result.put("driverClassName", MockedDataSource.class.getName());
        result.put("connectionTimeout", "120");
        return result;
    }
    
    private static Stream<Arguments> validateWithMockedDataSourcePrivilegesArguments() {
        return Stream.of(
                Arguments.of("empty privileges", Collections.<PrivilegeCheckType>emptySet()),
                Arguments.of("none privilege", Collections.singleton(PrivilegeCheckType.NONE)),
                Arguments.of("privilege without checker", Collections.singleton(PrivilegeCheckType.SELECT)));
    }
}
