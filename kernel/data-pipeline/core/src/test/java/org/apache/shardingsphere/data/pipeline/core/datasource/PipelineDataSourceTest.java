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

package org.apache.shardingsphere.data.pipeline.core.datasource;

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PipelineDataSourceTest {
    
    private static final String CLIENT_USERNAME = "username";
    
    private static final String CLIENT_PASSWORD = "password";
    
    private static final int LOGIN_TIMEOUT = 15;
    
    @Mock(extraInterfaces = AutoCloseable.class)
    private DataSource dataSource;
    
    @Mock
    private Connection connection;
    
    @Mock
    private PrintWriter printWriter;
    
    @Mock
    private Logger parentLogger;
    
    private PipelineDataSource pipelineDataSource;
    
    @BeforeEach
    void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(dataSource.getConnection(CLIENT_USERNAME, CLIENT_PASSWORD)).thenReturn(connection);
        when(dataSource.getLogWriter()).thenReturn(printWriter);
        when(dataSource.getLoginTimeout()).thenReturn(LOGIN_TIMEOUT);
        when(dataSource.isWrapperFor(any())).thenReturn(Boolean.TRUE);
        when(dataSource.getParentLogger()).thenReturn(parentLogger);
        pipelineDataSource = new PipelineDataSource(dataSource, TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
    }
    
    @Test
    void assertGetConnection() throws SQLException {
        assertThat(pipelineDataSource.getConnection(), is(connection));
        assertThat(pipelineDataSource.getConnection(CLIENT_USERNAME, CLIENT_PASSWORD), is(connection));
        assertTrue(pipelineDataSource.isWrapperFor(any()));
        assertThat(pipelineDataSource.getLogWriter(), is(printWriter));
        assertThat(pipelineDataSource.getLoginTimeout(), is(LOGIN_TIMEOUT));
        assertThat(pipelineDataSource.getParentLogger(), is(parentLogger));
    }
    
    @Test
    void assertUnwrap() throws SQLException {
        when(dataSource.unwrap(String.class)).thenReturn("1");
        assertThat(pipelineDataSource.unwrap(String.class), is("1"));
    }
    
    @Test
    void assertSetLoginTimeout() throws SQLException {
        pipelineDataSource.setLoginTimeout(LOGIN_TIMEOUT);
        verify(dataSource).setLoginTimeout(LOGIN_TIMEOUT);
    }
    
    @Test
    void assertSetLogWriter() throws SQLException {
        pipelineDataSource.setLogWriter(printWriter);
        verify(dataSource).setLogWriter(printWriter);
    }
    
    @Test
    void assertCloseTwice() {
        assertFalse(pipelineDataSource.isClosed());
        pipelineDataSource.close();
        assertTrue(pipelineDataSource.isClosed());
        pipelineDataSource.close();
        assertTrue(pipelineDataSource.isClosed());
    }
    
    @Test
    void assertCloseWithNotAutoCloseableDataSource() {
        PipelineDataSource pipelineDataSource = new PipelineDataSource(mock(DataSource.class), TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        assertFalse(pipelineDataSource.isClosed());
        pipelineDataSource.close();
        assertFalse(pipelineDataSource.isClosed());
    }
    
    @Test
    void assertIdentifierContextIsResolvedLazilyAndReused() throws SQLException {
        DataSource nativeDataSource = mockNativeDataSource(1);
        PipelineDataSource pipelineDataSource = new PipelineDataSource(nativeDataSource, TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        verifyNoInteractions(nativeDataSource);
        DatabaseIdentifierContext actual = pipelineDataSource.getIdentifierContext();
        assertThat(pipelineDataSource.getIdentifierContext(), sameInstance(actual));
        verify(nativeDataSource).getConnection();
    }
    
    private DataSource mockNativeDataSource(final int lowerCaseTableNames) throws SQLException {
        DataSource result = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(result.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT @@lower_case_table_names")).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(lowerCaseTableNames);
        return result;
    }
    
    @Test
    void assertIdentifierContextsAreIsolatedByDataSource() throws SQLException {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        PipelineDataSource preservingDataSource = new PipelineDataSource(mockNativeDataSource(0), databaseType);
        PipelineDataSource foldingDataSource = new PipelineDataSource(mockNativeDataSource(1), databaseType);
        assertThat(preservingDataSource.getIdentifierContext().normalizeStorage(IdentifierScope.TABLE, new IdentifierValue("T_User")), is("T_User"));
        assertThat(foldingDataSource.getIdentifierContext().normalizeStorage(IdentifierScope.TABLE, new IdentifierValue("T_User")), is("t_user"));
    }
    
    @Test
    void assertEmbeddedDataSourceUsesProtocolWithoutStorageConnection() {
        ShardingSphereDataSource embeddedDataSource = mock(ShardingSphereDataSource.class);
        PipelineDataSource pipelineDataSource = new PipelineDataSource(embeddedDataSource, TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        assertThat(pipelineDataSource.getIdentifierContext().normalizeStorage(IdentifierScope.TABLE, new IdentifierValue("T_User")), is("t_user"));
        verifyNoInteractions(embeddedDataSource);
    }
    
    @Test
    void assertIdentifierContextRetainsProviderFallback() throws SQLException {
        DataSource unavailableDataSource = mock(DataSource.class);
        when(unavailableDataSource.getConnection()).thenThrow(new SQLException("unavailable"));
        PipelineDataSource pipelineDataSource = new PipelineDataSource(unavailableDataSource, TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        DatabaseIdentifierContext actual = pipelineDataSource.getIdentifierContext();
        assertThat(actual.normalizeStorage(IdentifierScope.TABLE, new IdentifierValue("T_User")), is("t_user"));
        assertThat(pipelineDataSource.getIdentifierContext(), sameInstance(actual));
        verify(unavailableDataSource).getConnection();
    }
    
}
