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

package org.apache.shardingsphere.database.connector.core.type;

import org.apache.shardingsphere.database.connector.core.exception.UnsupportedStorageTypeException;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ShardingSphereServiceLoader.class)
class DatabaseTypeFactoryTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDatabaseTypeWithRecognizedURLArguments")
    void assertGetWithRecognizedURL(final String name, final String url, final Collection<DatabaseType> databaseTypes, final DatabaseType expectedDatabaseType) {
        when(ShardingSphereServiceLoader.getServiceInstances(DatabaseType.class)).thenReturn(databaseTypes);
        assertThat(DatabaseTypeFactory.get(url), is(expectedDatabaseType));
    }
    
    @Test
    void assertGetWithUnrecognizedURL() {
        DatabaseType databaseType = mock(DatabaseType.class);
        when(databaseType.getJdbcUrlPrefixes()).thenReturn(Collections.singleton("jdbc:trunk:"));
        when(ShardingSphereServiceLoader.getServiceInstances(DatabaseType.class)).thenReturn(Collections.singletonList(databaseType));
        assertThrows(UnsupportedStorageTypeException.class, () -> DatabaseTypeFactory.get("jdbc:not-existed:test"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDatabaseTypeWithDatabaseMetaDataArguments")
    void assertGetWithDatabaseMetaData(final String name, final String productName, final String url, final Collection<DatabaseType> databaseTypes,
                                       final DatabaseType expectedDatabaseType, final boolean isHive) throws SQLException {
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        when(metaData.getDatabaseProductName()).thenReturn(productName);
        if (isHive) {
            DatabaseType hiveDatabaseType = mock(DatabaseType.class);
            try (MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class)) {
                typedSPILoader.when(() -> TypedSPILoader.getService(DatabaseType.class, "Hive")).thenReturn(hiveDatabaseType);
                assertThat(DatabaseTypeFactory.get(metaData), is(hiveDatabaseType));
            }
            return;
        }
        when(metaData.getURL()).thenReturn(url);
        when(ShardingSphereServiceLoader.getServiceInstances(DatabaseType.class)).thenReturn(databaseTypes);
        assertThat(DatabaseTypeFactory.get(metaData), is(expectedDatabaseType));
    }
    
    private static Stream<Arguments> getDatabaseTypeWithRecognizedURLArguments() {
        DatabaseType trunkDatabaseType = mockDatabaseType("jdbc:trunk:", null);
        DatabaseType branchDatabaseType = mockDatabaseType("jdbc:trunk:branch:", trunkDatabaseType);
        DatabaseType branchOnlyDatabaseType = mockDatabaseType("jdbc:branch-only:", mock(DatabaseType.class));
        return Stream.of(
                Arguments.of("trunk url", "jdbc:trunk://localhost:3306/test", Collections.singletonList(trunkDatabaseType), trunkDatabaseType),
                Arguments.of("branch url", "jdbc:trunk:branch://localhost:3306/test?databaseType=BRANCH", Arrays.asList(trunkDatabaseType, branchDatabaseType), trunkDatabaseType),
                Arguments.of("branch only url", "jdbc:branch-only://localhost:3306/test", Collections.singletonList(branchOnlyDatabaseType), branchOnlyDatabaseType));
    }
    
    private static Stream<Arguments> getDatabaseTypeWithDatabaseMetaDataArguments() {
        DatabaseType trunkDatabaseType = mockDatabaseType("jdbc:trunk:", null);
        DatabaseType branchOnlyDatabaseType = mockDatabaseType("jdbc:branch-only:", mock(DatabaseType.class));
        return Stream.of(
                Arguments.of("hive database product", "Apache Hive", "jdbc:hive://localhost:3306/test", Collections.emptyList(), null, true),
                Arguments.of("non hive with trunk url", "MySQL", "jdbc:trunk://localhost:3306/test", Collections.singletonList(trunkDatabaseType), trunkDatabaseType, false),
                Arguments.of("non hive with branch only url", "MySQL", "jdbc:branch-only://localhost:3306/test", Collections.singletonList(branchOnlyDatabaseType), branchOnlyDatabaseType, false));
    }
    
    private static DatabaseType mockDatabaseType(final String jdbcUrlPrefix, final DatabaseType trunkDatabaseType) {
        DatabaseType result = mock(DatabaseType.class);
        when(result.getJdbcUrlPrefixes()).thenReturn(Collections.singleton(jdbcUrlPrefix));
        when(result.getTrunkDatabaseType()).thenReturn(Optional.ofNullable(trunkDatabaseType));
        return result;
    }
}
