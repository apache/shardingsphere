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
import org.apache.shardingsphere.database.connector.core.jdbcurl.DialectJdbcUrlFetcher;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.MetaDataLoaderConnection;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
    
    @Test
    void assertGetWithTrunkDatabaseMetaData() throws SQLException {
        DatabaseType expectedDatabaseType = mock(DatabaseType.class);
        when(expectedDatabaseType.getJdbcUrlPrefixes()).thenReturn(Collections.singleton("jdbc:trunk:"));
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        when(metaData.getURL()).thenReturn("jdbc:trunk://localhost:3306/test");
        when(ShardingSphereServiceLoader.getServiceInstances(DatabaseType.class)).thenReturn(Collections.singletonList(expectedDatabaseType));
        assertThat(DatabaseTypeFactory.get(metaData), is(expectedDatabaseType));
    }
    
    @Test
    void assertGetWithBranchOnlyDatabaseMetaData() throws SQLException {
        DatabaseType expectedDatabaseType = mock(DatabaseType.class);
        when(expectedDatabaseType.getJdbcUrlPrefixes()).thenReturn(Collections.singleton("jdbc:branch-only:"));
        DatabaseType trunkDatabaseType = mock(DatabaseType.class);
        when(expectedDatabaseType.getTrunkDatabaseType()).thenReturn(Optional.of(trunkDatabaseType));
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        when(metaData.getURL()).thenReturn("jdbc:branch-only://localhost:3306/test");
        when(ShardingSphereServiceLoader.getServiceInstances(DatabaseType.class)).thenReturn(Collections.singletonList(expectedDatabaseType));
        assertThat(DatabaseTypeFactory.get(metaData), is(expectedDatabaseType));
    }
    
    @Test
    void assertGetWithNativeConnection() throws SQLException {
        DatabaseType expectedDatabaseType = mock(DatabaseType.class);
        when(expectedDatabaseType.getJdbcUrlPrefixes()).thenReturn(Collections.singleton("jdbc:trunk:"));
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        Connection connection = mock(MetaDataLoaderConnection.class);
        DialectJdbcUrlFetcher jdbcUrlFetcher = mock(DialectJdbcUrlFetcher.class);
        when(metaData.getURL()).thenThrow(new SQLFeatureNotSupportedException("unsupported"));
        when(metaData.getConnection()).thenReturn(connection);
        doReturn(MetaDataLoaderConnection.class).when(jdbcUrlFetcher).getConnectionClass();
        when(jdbcUrlFetcher.fetch(connection)).thenReturn("jdbc:trunk://localhost:3306/test");
        when(ShardingSphereServiceLoader.getServiceInstances(DialectJdbcUrlFetcher.class)).thenReturn(Collections.singleton(jdbcUrlFetcher));
        when(ShardingSphereServiceLoader.getServiceInstances(DatabaseType.class)).thenReturn(Collections.singletonList(expectedDatabaseType));
        assertThat(DatabaseTypeFactory.get(metaData), is(expectedDatabaseType));
        verify(connection, never()).isWrapperFor(MetaDataLoaderConnection.class);
    }
    
    @Test
    void assertGetWithUnsupportedMetadataURLAndDialectJdbcUrlFetcher() throws SQLException {
        DatabaseType databaseType = mockDatabaseType("jdbc:trunk:", null);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        Connection connection = mock(Connection.class);
        DialectJdbcUrlFetcher jdbcUrlFetcher = mock(DialectJdbcUrlFetcher.class);
        when(metaData.getURL()).thenThrow(new SQLFeatureNotSupportedException("unsupported"));
        when(metaData.getConnection()).thenReturn(connection);
        doReturn(MetaDataLoaderConnection.class).when(jdbcUrlFetcher).getConnectionClass();
        when(connection.isWrapperFor(MetaDataLoaderConnection.class)).thenReturn(true);
        when(jdbcUrlFetcher.fetch(connection)).thenReturn("jdbc:trunk://localhost:3306/test");
        when(ShardingSphereServiceLoader.getServiceInstances(DialectJdbcUrlFetcher.class)).thenReturn(Collections.singleton(jdbcUrlFetcher));
        when(ShardingSphereServiceLoader.getServiceInstances(DatabaseType.class)).thenReturn(Collections.singleton(databaseType));
        assertThat(DatabaseTypeFactory.get(metaData), is(databaseType));
    }
    
    @Test
    void assertGetWithSQLExceptionMetadataURLAndDialectJdbcUrlFetcher() throws SQLException {
        DatabaseType databaseType = mockDatabaseType("jdbc:trunk:", null);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        Connection connection = mock(Connection.class);
        DialectJdbcUrlFetcher jdbcUrlFetcher = mock(DialectJdbcUrlFetcher.class);
        when(metaData.getURL()).thenThrow(new SQLException("unsupported"));
        when(metaData.getConnection()).thenReturn(connection);
        doReturn(MetaDataLoaderConnection.class).when(jdbcUrlFetcher).getConnectionClass();
        when(connection.isWrapperFor(MetaDataLoaderConnection.class)).thenReturn(true);
        when(jdbcUrlFetcher.fetch(connection)).thenReturn("jdbc:trunk://localhost:3306/test");
        when(ShardingSphereServiceLoader.getServiceInstances(DialectJdbcUrlFetcher.class)).thenReturn(Collections.singleton(jdbcUrlFetcher));
        when(ShardingSphereServiceLoader.getServiceInstances(DatabaseType.class)).thenReturn(Collections.singleton(databaseType));
        assertThat(DatabaseTypeFactory.get(metaData), is(databaseType));
    }
    
    @Test
    void assertGetWithUnsupportedMetadataURLAndNoDialectJdbcUrlFetcher() throws SQLException {
        SQLFeatureNotSupportedException expectedException = new SQLFeatureNotSupportedException("unsupported");
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        Connection connection = mock(Connection.class);
        DialectJdbcUrlFetcher jdbcUrlFetcher = mock(DialectJdbcUrlFetcher.class);
        when(metaData.getURL()).thenThrow(expectedException);
        when(metaData.getConnection()).thenReturn(connection);
        doReturn(MetaDataLoaderConnection.class).when(jdbcUrlFetcher).getConnectionClass();
        when(ShardingSphereServiceLoader.getServiceInstances(DialectJdbcUrlFetcher.class)).thenReturn(Collections.singleton(jdbcUrlFetcher));
        SQLFeatureNotSupportedException actualException = assertThrows(SQLFeatureNotSupportedException.class, () -> DatabaseTypeFactory.get(metaData));
        assertThat(actualException, is(expectedException));
    }
    
    @Test
    void assertGetWithSQLExceptionMetadataURLAndNoDialectJdbcUrlFetcher() throws SQLException {
        SQLException expectedException = new SQLException("unsupported");
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        Connection connection = mock(Connection.class);
        DialectJdbcUrlFetcher jdbcUrlFetcher = mock(DialectJdbcUrlFetcher.class);
        when(metaData.getURL()).thenThrow(expectedException);
        when(metaData.getConnection()).thenReturn(connection);
        doReturn(MetaDataLoaderConnection.class).when(jdbcUrlFetcher).getConnectionClass();
        when(ShardingSphereServiceLoader.getServiceInstances(DialectJdbcUrlFetcher.class)).thenReturn(Collections.singleton(jdbcUrlFetcher));
        SQLException actualException = assertThrows(SQLException.class, () -> DatabaseTypeFactory.get(metaData));
        assertThat(actualException, is(expectedException));
    }
    
    @Test
    void assertGetWithUnsupportedDialectJdbcUrlFetcherURL() throws SQLException {
        DatabaseType databaseType = mock(DatabaseType.class);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        Connection connection = mock(Connection.class);
        DialectJdbcUrlFetcher jdbcUrlFetcher = mock(DialectJdbcUrlFetcher.class);
        when(databaseType.getJdbcUrlPrefixes()).thenReturn(Collections.singleton("jdbc:trunk:"));
        when(metaData.getURL()).thenThrow(new SQLFeatureNotSupportedException("unsupported"));
        when(metaData.getConnection()).thenReturn(connection);
        doReturn(MetaDataLoaderConnection.class).when(jdbcUrlFetcher).getConnectionClass();
        when(connection.isWrapperFor(MetaDataLoaderConnection.class)).thenReturn(true);
        when(jdbcUrlFetcher.fetch(connection)).thenReturn("jdbc:not-existed:test");
        when(ShardingSphereServiceLoader.getServiceInstances(DialectJdbcUrlFetcher.class)).thenReturn(Collections.singleton(jdbcUrlFetcher));
        when(ShardingSphereServiceLoader.getServiceInstances(DatabaseType.class)).thenReturn(Collections.singleton(databaseType));
        assertThrows(UnsupportedStorageTypeException.class, () -> DatabaseTypeFactory.get(metaData));
    }
    
    @Test
    void assertGetWithWrapperSQLException() throws SQLException {
        SQLException expectedException = new SQLException("wrapper failed");
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        Connection connection = mock(Connection.class);
        DialectJdbcUrlFetcher jdbcUrlFetcher = mock(DialectJdbcUrlFetcher.class);
        when(metaData.getURL()).thenThrow(new SQLFeatureNotSupportedException("unsupported"));
        when(metaData.getConnection()).thenReturn(connection);
        doReturn(MetaDataLoaderConnection.class).when(jdbcUrlFetcher).getConnectionClass();
        when(connection.isWrapperFor(MetaDataLoaderConnection.class)).thenThrow(expectedException);
        when(ShardingSphereServiceLoader.getServiceInstances(DialectJdbcUrlFetcher.class)).thenReturn(Collections.singleton(jdbcUrlFetcher));
        SQLException actual = assertThrows(SQLException.class, () -> DatabaseTypeFactory.get(metaData));
        assertThat(actual, sameInstance(expectedException));
    }
    
    private static Stream<Arguments> getDatabaseTypeWithRecognizedURLArguments() {
        DatabaseType trunkDatabaseType = mockDatabaseType("jdbc:trunk:", null);
        DatabaseType branchDatabaseType = mockDatabaseType("jdbc:trunk:branch:", trunkDatabaseType);
        DatabaseType branchOnlyDatabaseType = mockDatabaseType("jdbc:branch-only:", mock(DatabaseType.class));
        return Stream.of(
                Arguments.of("trunk url", "jdbc:trunk://localhost:3306/test", Collections.singletonList(trunkDatabaseType), trunkDatabaseType),
                Arguments.of("branch url", "jdbc:trunk:branch://localhost:3306/test?databaseType=BRANCH",
                        Arrays.asList(trunkDatabaseType, branchDatabaseType), trunkDatabaseType),
                Arguments.of("branch only url", "jdbc:branch-only://localhost:3306/test",
                        Collections.singletonList(branchOnlyDatabaseType), branchOnlyDatabaseType));
    }
    
    private static DatabaseType mockDatabaseType(final String jdbcUrlPrefix, final DatabaseType trunkDatabaseType) {
        DatabaseType result = mock(DatabaseType.class);
        when(result.getJdbcUrlPrefixes()).thenReturn(Collections.singleton(jdbcUrlPrefix));
        when(result.getTrunkDatabaseType()).thenReturn(Optional.ofNullable(trunkDatabaseType));
        return result;
    }
}
