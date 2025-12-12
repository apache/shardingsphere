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

package org.apache.shardingsphere.mode.repository.standalone.jdbc;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.mode.repository.standalone.jdbc.sql.JDBCRepositorySQL;
import org.apache.shardingsphere.mode.repository.standalone.jdbc.sql.JDBCRepositorySQLLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JDBCRepositoryTest {
    
    private JDBCRepositorySQL repositorySQL;
    
    @Mock
    private Connection mockJdbcConnection;
    
    @Mock
    private Statement mockStatement;
    
    @Mock
    private PreparedStatement mockPreparedStatement;
    
    @Mock
    private PreparedStatement mockPreparedStatementForPersist;
    
    @Mock
    private ResultSet mockResultSet;
    
    private MockedConstruction<HikariDataSource> mockedConstruction;
    
    private JDBCRepository repository;
    
    @BeforeEach
    void setup() throws SQLException {
        mockedConstruction = mockConstruction(HikariDataSource.class, (mock, context) -> when(mock.getConnection()).thenReturn(mockJdbcConnection));
        when(mockJdbcConnection.createStatement()).thenReturn(mockStatement);
        repository = new JDBCRepository();
        Properties props = PropertiesBuilder.build(
                new Property("jdbc_url", "jdbc:h2:mem:config;DB_CLOSE_DELAY=0;DATABASE_TO_UPPER=false;MODE=MYSQL"),
                new Property("username", "sa"),
                new Property("password", ""),
                new Property("provider", "H2"));
        repository.init(props);
        repositorySQL = JDBCRepositorySQLLoader.load("H2");
    }
    
    @AfterEach
    void tearDown() {
        mockedConstruction.close();
    }
    
    @Test
    void assertInit() throws SQLException {
        verify(mockStatement).execute(repositorySQL.getCreateTableSQL());
    }
    
    @Test
    void assertQuery() throws SQLException {
        String key = "key";
        String value = "value";
        when(mockJdbcConnection.prepareStatement(repositorySQL.getSelectByKeySQL())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("value")).thenReturn(value);
        String actual = repository.query(key);
        verify(mockPreparedStatement).setString(1, key);
        assertThat(actual, is(value));
    }
    
    @Test
    void assertQueryWithoutValue() throws SQLException {
        when(mockJdbcConnection.prepareStatement(repositorySQL.getSelectByKeySQL())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        String actual = repository.query("key");
        assertThat(actual, is(""));
    }
    
    @Test
    void assertQueryFailed() throws SQLException {
        when(mockJdbcConnection.prepareStatement(repositorySQL.getSelectByKeySQL())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenThrow(new SQLException(""));
        String actual = repository.query("key");
        assertThat(actual, is(""));
    }
    
    @Test
    void assertIsExisted() throws SQLException {
        when(mockJdbcConnection.prepareStatement(repositorySQL.getSelectByKeySQL())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        assertTrue(repository.isExisted("key"));
    }
    
    @Test
    void assertIsExistedFailed() throws SQLException {
        when(mockJdbcConnection.prepareStatement(repositorySQL.getSelectByKeySQL())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenThrow(new SQLException(""));
        assertFalse(repository.isExisted("key"));
    }
    
    @Test
    void assertPersistAndGetChildrenKeys() throws SQLException {
        when(mockJdbcConnection.prepareStatement(repositorySQL.getSelectByParentKeySQL())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, true, false);
        when(mockResultSet.getString("key")).thenReturn("parent1/test1", "parent1/test2", "");
        List<String> actual = repository.getChildrenKeys("/testPath");
        assertThat(actual.size(), is(2));
        assertTrue(actual.contains("test1"));
        assertTrue(actual.contains("test2"));
    }
    
    @Test
    void assertPersistAndGetChildrenKeysFailed() throws SQLException {
        when(mockJdbcConnection.prepareStatement(repositorySQL.getSelectByParentKeySQL())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenThrow(new SQLException(""));
        List<String> actual = repository.getChildrenKeys("key");
        assertTrue(actual.isEmpty());
    }
    
    @Test
    void assertPersistWithUpdateForSimpleKeys() throws SQLException {
        final String key = "key";
        final String value = "value";
        when(mockJdbcConnection.prepareStatement(repositorySQL.getSelectByKeySQL())).thenReturn(mockPreparedStatement);
        when(mockJdbcConnection.prepareStatement(repositorySQL.getUpdateSQL())).thenReturn(mockPreparedStatementForPersist);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        repository.persist(key, value);
        verify(mockPreparedStatement).setString(1, key);
        verify(mockPreparedStatementForPersist).setString(eq(1), anyString());
        verify(mockPreparedStatementForPersist).setString(1, value);
        verify(mockPreparedStatementForPersist).setString(2, key);
        verify(mockPreparedStatementForPersist).executeUpdate();
    }
    
    @Test
    void assertPersistForDirectory() throws SQLException {
        final String key = "/parent/child/test1";
        final String value = "test1_content";
        when(mockJdbcConnection.prepareStatement(repositorySQL.getSelectByKeySQL())).thenReturn(mockPreparedStatement);
        when(mockJdbcConnection.prepareStatement(repositorySQL.getInsertSQL())).thenReturn(mockPreparedStatementForPersist);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        repository.persist(key, value);
        int depthOfDirectory = (int) key.chars().filter(each -> each == '/').count();
        int beginIndex = 0;
        String parentDirectory = "/";
        for (int i = 0; i < depthOfDirectory; i++) {
            int separatorIndex = key.indexOf('/', beginIndex);
            int nextSeparatorIndex = key.indexOf('/', separatorIndex + 1);
            if (nextSeparatorIndex == -1) {
                nextSeparatorIndex = key.length();
            }
            String directoryPath = key.substring(0, nextSeparatorIndex);
            verify(mockPreparedStatement).setString(1, directoryPath);
            verify(mockPreparedStatementForPersist).setString(2, directoryPath);
            verify(mockPreparedStatementForPersist).setString(4, parentDirectory);
            beginIndex = nextSeparatorIndex;
            parentDirectory = directoryPath;
        }
        verify(mockPreparedStatementForPersist, times(depthOfDirectory - 1)).setString(3, "");
        verify(mockPreparedStatementForPersist).setString(3, "test1_content");
        verify(mockPreparedStatementForPersist, times(depthOfDirectory)).setString(eq(1), anyString());
        verify(mockPreparedStatementForPersist, times(depthOfDirectory)).executeUpdate();
    }
    
    @Test
    void assertPersistFailureDuringUpdate() throws SQLException {
        final String key = "key";
        when(mockJdbcConnection.prepareStatement(repositorySQL.getSelectByKeySQL())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockJdbcConnection.prepareStatement(repositorySQL.getUpdateSQL())).thenReturn(mockPreparedStatement);
        repository.persist(key, "value");
        verify(mockPreparedStatementForPersist, never()).executeUpdate();
    }
    
    @Test
    void assertPersistWithInsertForSimpleKeys() throws SQLException {
        final String key = "key";
        final String value = "value";
        when(mockJdbcConnection.prepareStatement(repositorySQL.getSelectByKeySQL())).thenReturn(mockPreparedStatement);
        when(mockJdbcConnection.prepareStatement(repositorySQL.getInsertSQL())).thenReturn(mockPreparedStatementForPersist);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        repository.persist(key, value);
        verify(mockPreparedStatement).setString(1, key);
        verify(mockPreparedStatementForPersist).setString(eq(1), anyString());
        verify(mockPreparedStatementForPersist).setString(2, key);
        verify(mockPreparedStatementForPersist).setString(3, value);
        verify(mockPreparedStatementForPersist).setString(4, "/");
        verify(mockPreparedStatementForPersist).executeUpdate();
    }
    
    @Test
    void assertPersistFailureDuringInsert() throws SQLException {
        when(mockJdbcConnection.prepareStatement(repositorySQL.getSelectByKeySQL())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        when(mockJdbcConnection.prepareStatement(repositorySQL.getInsertSQL())).thenReturn(mockPreparedStatement);
        repository.persist("key", "value");
        verify(mockPreparedStatementForPersist, never()).executeUpdate();
    }
    
    @Test
    void assertUpdate() throws SQLException {
        String key = "key";
        String value = "value";
        when(mockJdbcConnection.prepareStatement(repositorySQL.getUpdateSQL())).thenReturn(mockPreparedStatement);
        repository.update(key, value);
        verify(mockPreparedStatement).setString(1, value);
        verify(mockPreparedStatement).setString(2, key);
        verify(mockPreparedStatement).executeUpdate();
    }
    
    @Test
    void assertUpdateFailed() throws SQLException {
        String key = "key";
        String value = "value";
        when(mockJdbcConnection.prepareStatement(repositorySQL.getUpdateSQL())).thenThrow(new SQLException(""));
        repository.update(key, value);
        verify(mockPreparedStatement, never()).executeUpdate();
    }
    
    @Test
    void assertDelete() throws SQLException {
        String key = "key";
        when(mockJdbcConnection.prepareStatement(repositorySQL.getDeleteSQL())).thenReturn(mockPreparedStatement);
        repository.delete(key);
        verify(mockPreparedStatement).setString(1, key + "%");
        verify(mockPreparedStatement).executeUpdate();
    }
    
    @Test
    void assertDeleteFailed() throws SQLException {
        String key = "key";
        when(mockJdbcConnection.prepareStatement(repositorySQL.getDeleteSQL())).thenThrow(new SQLException(""));
        repository.delete(key);
        verify(mockPreparedStatement, never()).executeUpdate();
    }
    
    @Test
    void assertClose() {
        repository.close();
        HikariDataSource hikariDataSource = mockedConstruction.constructed().get(0);
        verify(hikariDataSource).close();
    }
}
