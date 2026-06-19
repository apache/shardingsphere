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

package org.apache.shardingsphere.infra.datasource.pool;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.stream.Stream;
import java.util.logging.Logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@SuppressWarnings("resource")
@ExtendWith(MockitoExtension.class)
class CatalogSwitchableDataSourceTest {
    
    @Mock
    private DataSource dataSource;
    
    @Mock
    private Connection connection;
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getConnectionArguments")
    void assertGetConnection(final String name, final String catalog, final String currentCatalog, final int expectedGetCatalogCount, final int expectedSetCatalogCount) throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        if (0 < expectedGetCatalogCount) {
            when(connection.getCatalog()).thenReturn(currentCatalog);
        }
        Connection actualConnection = new CatalogSwitchableDataSource(dataSource, catalog, "jdbc:mysql://localhost:3306/db").getConnection();
        assertThat(actualConnection, is(connection));
        verify(connection, times(expectedSetCatalogCount)).setCatalog(catalog);
    }
    
    @SuppressWarnings("JDBCResourceOpenedButNotSafelyClosed")
    @Test
    void assertGetConnectionWithCredentials() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getCatalog()).thenReturn("other_db");
        Connection actualConnection = new CatalogSwitchableDataSource(dataSource, "db", "jdbc:mysql://localhost:3306/db").getConnection("foo_user", "foo_password");
        assertThat(actualConnection, is(connection));
        verify(dataSource).getConnection();
        verify(connection).setCatalog("db");
    }
    
    @Test
    void assertGetLogWriter() throws SQLException {
        PrintWriter expectedLogWriter = new PrintWriter(new StringWriter());
        when(dataSource.getLogWriter()).thenReturn(expectedLogWriter);
        assertThat(new CatalogSwitchableDataSource(dataSource, "db", "jdbc:mysql://localhost:3306/db").getLogWriter(), is(expectedLogWriter));
    }
    
    @Test
    void assertSetLogWriter() throws SQLException {
        PrintWriter logWriter = new PrintWriter(new StringWriter());
        new CatalogSwitchableDataSource(dataSource, "db", "jdbc:mysql://localhost:3306/db").setLogWriter(logWriter);
        verify(dataSource).setLogWriter(logWriter);
    }
    
    @Test
    void assertSetLoginTimeout() throws SQLException {
        new CatalogSwitchableDataSource(dataSource, "db", "jdbc:mysql://localhost:3306/db").setLoginTimeout(30);
        verify(dataSource).setLoginTimeout(30);
    }
    
    @Test
    void assertGetLoginTimeout() throws SQLException {
        when(dataSource.getLoginTimeout()).thenReturn(30);
        assertThat(new CatalogSwitchableDataSource(dataSource, "db", "jdbc:mysql://localhost:3306/db").getLoginTimeout(), is(30));
    }
    
    @Test
    void assertGetParentLogger() throws SQLException {
        Logger expectedLogger = Logger.getLogger("foo_logger");
        when(dataSource.getParentLogger()).thenReturn(expectedLogger);
        assertThat(new CatalogSwitchableDataSource(dataSource, "db", "jdbc:mysql://localhost:3306/db").getParentLogger(), is(expectedLogger));
    }
    
    @Test
    void assertUnwrap() throws SQLException {
        when(dataSource.unwrap(DataSource.class)).thenReturn(dataSource);
        assertThat(new CatalogSwitchableDataSource(dataSource, "db", "jdbc:mysql://localhost:3306/db").unwrap(DataSource.class), is(dataSource));
    }
    
    @Test
    void assertIsWrapperFor() throws SQLException {
        when(dataSource.isWrapperFor(DataSource.class)).thenReturn(true);
        assertTrue(new CatalogSwitchableDataSource(dataSource, "db", "jdbc:mysql://localhost:3306/db").isWrapperFor(DataSource.class));
    }
    
    @Test
    void assertCloseWhenDataSourceIsAutoCloseable() throws Exception {
        DataSource autoCloseableDataSource = mock(DataSource.class, withSettings().extraInterfaces(AutoCloseable.class));
        new CatalogSwitchableDataSource(autoCloseableDataSource, "db", "jdbc:mysql://localhost:3306/db").close();
        verify((AutoCloseable) autoCloseableDataSource).close();
    }
    
    @Test
    void assertCloseWhenDataSourceIsNotAutoCloseable() throws Exception {
        new CatalogSwitchableDataSource(dataSource, "db", "jdbc:mysql://localhost:3306/db").close();
        verifyNoInteractions(dataSource);
    }
    
    private static Stream<Arguments> getConnectionArguments() {
        return Stream.of(
                Arguments.of("catalog is null", null, "other_db", 0, 0),
                Arguments.of("catalog matches current catalog", "db", "db", 1, 0),
                Arguments.of("catalog differs from current catalog", "db", "other_db", 1, 1));
    }
}
