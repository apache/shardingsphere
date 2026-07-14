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

package org.apache.shardingsphere.database.connector.core.metadata.identifier;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.IdentifierPatternType;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(TypedSPILoader.class)
class IdentifierCasePolicyResolverTest {
    
    @Test
    void assertResolveProtocolWithProvider() {
        DatabaseType databaseType = mock(DatabaseType.class);
        IdentifierCasePolicySet expected = IdentifierCasePolicyFactory.newLowerCasePolicySet();
        IdentifierCasePolicyProvider provider = mock(IdentifierCasePolicyProvider.class);
        when(provider.provide()).thenReturn(expected);
        when(TypedSPILoader.findService(IdentifierCasePolicyProvider.class, databaseType)).thenReturn(Optional.of(provider));
        assertThat(IdentifierCasePolicyResolver.resolveProtocol(databaseType), is(expected));
    }
    
    @Test
    void assertResolveProtocolWithDialectMetadata() {
        DatabaseType databaseType = mock(DatabaseType.class);
        DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class);
        when(TypedSPILoader.findService(IdentifierCasePolicyProvider.class, databaseType)).thenReturn(Optional.empty());
        when(dialectDatabaseMetaData.getIdentifierPatternType()).thenReturn(IdentifierPatternType.KEEP_ORIGIN);
        when(dialectDatabaseMetaData.isCaseSensitive()).thenReturn(true);
        try (MockedStatic<DatabaseTypedSPILoader> mockedLoader = mockStatic(DatabaseTypedSPILoader.class)) {
            mockedLoader.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(dialectDatabaseMetaData);
            IdentifierCasePolicy actual = IdentifierCasePolicyResolver.resolveProtocol(databaseType).getPolicy(IdentifierScope.TABLE);
            assertFalse(actual.matches("foo_table", "FOO_TABLE", QuoteCharacter.NONE));
        }
    }
    
    @Test
    void assertResolveStorageWithStaticPolicy() throws SQLException {
        DatabaseType databaseType = mock(DatabaseType.class);
        DataSource dataSource = mock(DataSource.class);
        DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class);
        when(TypedSPILoader.findService(DialectIdentifierCasePolicyLoader.class, databaseType)).thenReturn(Optional.empty());
        when(TypedSPILoader.findService(IdentifierCasePolicyProvider.class, databaseType)).thenReturn(Optional.empty());
        when(dialectDatabaseMetaData.getIdentifierPatternType()).thenReturn(IdentifierPatternType.KEEP_ORIGIN);
        try (MockedStatic<DatabaseTypedSPILoader> mockedLoader = mockStatic(DatabaseTypedSPILoader.class)) {
            mockedLoader.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(dialectDatabaseMetaData);
            IdentifierCasePolicy actual = IdentifierCasePolicyResolver.resolveStorage(databaseType, dataSource).getPolicy(IdentifierScope.TABLE);
            assertTrue(actual.matches("foo_table", "FOO_TABLE", QuoteCharacter.NONE));
            verifyNoInteractions(dataSource);
        }
    }
    
    @Test
    void assertResolveStorageWithDataSource() throws SQLException {
        DatabaseType databaseType = mock(DatabaseType.class);
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        DialectIdentifierCasePolicyLoader loader = mock(DialectIdentifierCasePolicyLoader.class);
        IdentifierCasePolicySet expected = IdentifierCasePolicyFactory.newSensitivePolicySet();
        when(dataSource.getConnection()).thenReturn(connection);
        when(loader.load(connection)).thenReturn(expected);
        when(TypedSPILoader.findService(DialectIdentifierCasePolicyLoader.class, databaseType)).thenReturn(Optional.of(loader));
        assertThat(IdentifierCasePolicyResolver.resolveStorage(databaseType, dataSource), is(expected));
        verify(connection).close();
    }
    
    @Test
    void assertResolveStorageWithConnection() throws SQLException {
        DatabaseType databaseType = mock(DatabaseType.class);
        Connection connection = mock(Connection.class);
        DialectIdentifierCasePolicyLoader loader = mock(DialectIdentifierCasePolicyLoader.class);
        IdentifierCasePolicySet expected = IdentifierCasePolicyFactory.newSensitivePolicySet();
        when(loader.load(connection)).thenReturn(expected);
        when(TypedSPILoader.findService(DialectIdentifierCasePolicyLoader.class, databaseType)).thenReturn(Optional.of(loader));
        assertThat(IdentifierCasePolicyResolver.resolveStorage(databaseType, connection), is(expected));
        verify(connection, never()).close();
    }
}
