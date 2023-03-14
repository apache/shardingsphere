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
import org.apache.shardingsphere.mode.repository.standalone.jdbc.fixture.JDBCRepositoryProviderFixture;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.h2.jdbc.JdbcCallableStatement;
import org.h2.jdbc.JdbcConnection;
import org.h2.jdbc.JdbcResultSet;
import org.h2.jdbc.JdbcStatement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public final class JDBCRepositoryTest {
    
    private final JDBCRepositoryProviderFixture fixture = new JDBCRepositoryProviderFixture();
    
    @Mock
    private JdbcConnection mockJdbcConnection;
    
    @Mock
    private JdbcStatement mockStatement;
    
    @Mock
    private JdbcCallableStatement mockPreparedStatement;
    
    @Mock
    private JdbcCallableStatement mockPreparedStatementForPersist;
    
    @Mock
    private JdbcResultSet mockResultSet;
    
    private MockedConstruction<HikariDataSource> mockedConstruction;
    
    private JDBCRepository repository;
    
    @BeforeEach
    public void setup() throws Exception {
        mockedConstruction = mockConstruction(HikariDataSource.class, (mock, context) -> when(mock.getConnection()).thenReturn(mockJdbcConnection));
        when(mockJdbcConnection.createStatement()).thenReturn(mockStatement);
        repository = new JDBCRepository();
        Properties props = PropertiesBuilder.build(
                new Property("jdbc_url", "jdbc:h2:mem:config;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL"),
                new Property("username", "sa"),
                new Property("password", ""),
                new Property("provider", "FIXTURE"));
        repository.init(props);
    }
    
    @AfterEach
    public void tearDown() {
        mockedConstruction.close();
    }
    
    @Test
    public void assertInit() throws Exception {
        verify(mockStatement).execute(fixture.dropTableSQL());
        verify(mockStatement).execute(fixture.createTableSQL());
    }
    
    @Test
    public void assertGet() throws Exception {
        String key = "key";
        String value = "value";
        when(mockJdbcConnection.prepareStatement(fixture.selectByKeySQL())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("value")).thenReturn(value);
        String actual = repository.getDirectly(key);
        verify(mockPreparedStatement).setString(1, key);
        assertThat(actual, is(value));
    }
    
    @Test
    public void assertGetFailure() throws Exception {
        when(mockJdbcConnection.prepareStatement(fixture.selectByKeySQL())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        String actual = repository.getDirectly("key");
        assertThat(actual, is(""));
    }
    
    @Test
    public void assertPersistAndGetChildrenKeys() throws Exception {
        when(mockJdbcConnection.prepareStatement(fixture.selectByParentKeySQL())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, true, false);
        when(mockResultSet.getString("key")).thenReturn("parent1/test1", "parent1/test2", "");
        List<String> childrenKeys = repository.getChildrenKeys("/testPath");
        assertThat(childrenKeys.get(0), is("test1"));
        assertThat(childrenKeys.get(1), is("test2"));
    }
    
    @Test
    public void assertPersistAndGetChildrenKeysFailure() throws Exception {
        when(mockJdbcConnection.prepareStatement(fixture.selectByParentKeySQL())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        List<String> actual = repository.getChildrenKeys("key");
        assertTrue(actual.isEmpty());
    }
    
    @Test
    public void assertPersistWithUpdateForSimpleKeys() throws Exception {
        final String key = "key";
        final String value = "value";
        when(mockJdbcConnection.prepareStatement(fixture.selectByKeySQL())).thenReturn(mockPreparedStatement);
        when(mockJdbcConnection.prepareStatement(fixture.updateSQL())).thenReturn(mockPreparedStatementForPersist);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("value")).thenReturn("oldValue");
        repository.persist(key, value);
        verify(mockPreparedStatement).setString(1, key);
        verify(mockPreparedStatementForPersist).setString(eq(1), anyString());
        verify(mockPreparedStatementForPersist).setString(1, value);
        verify(mockPreparedStatementForPersist).setString(2, key);
        verify(mockPreparedStatementForPersist).executeUpdate();
    }
    
    @Test
    public void assertPersistForDirectory() throws Exception {
        final String key = "/parent/child/test1";
        final String value = "test1_content";
        when(mockJdbcConnection.prepareStatement(fixture.selectByKeySQL())).thenReturn(mockPreparedStatement);
        when(mockJdbcConnection.prepareStatement(fixture.insertSQL())).thenReturn(mockPreparedStatementForPersist);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        repository.persist(key, value);
        int depthOfDirectory = (int) key.chars().filter(ch -> ch == '/').count();
        int beginIndex = 0;
        String parentDirectory = "/";
        for (int i = 0; i < depthOfDirectory; i++) {
            int separatorIndex = key.indexOf('/', beginIndex);
            int nextSeparatorIndex = key.indexOf('/', separatorIndex + 1);
            if (nextSeparatorIndex == -1) {
                nextSeparatorIndex = key.length();
            }
            String directoryPath = key.substring(0, nextSeparatorIndex);
            // Verifying if get operation is called for every directory level
            verify(mockPreparedStatement).setString(1, directoryPath);
            // Verifying that during insert operation, setString at index 2 is called for every directory level
            verify(mockPreparedStatementForPersist).setString(2, directoryPath);
            // Verifying that during insert operation, setString at index 4 is called for every parent directory
            verify(mockPreparedStatementForPersist).setString(4, parentDirectory);
            beginIndex = nextSeparatorIndex;
            parentDirectory = directoryPath;
        }
        // Verifying that during insert operation, setString at index 3 is called with "" for all the parent directories
        verify(mockPreparedStatementForPersist, times(depthOfDirectory - 1)).setString(3, "");
        // Verifying that during insert operation, setString at index 3 is called with the leaf node once
        verify(mockPreparedStatementForPersist).setString(3, "test1_content");
        // Verifying that during insert operation, setString at index 1 is called with a UUID
        verify(mockPreparedStatementForPersist, times(depthOfDirectory)).setString(eq(1), anyString());
        // Verifying that executeOperation in insert is called for all the directory levels
        verify(mockPreparedStatementForPersist, times(depthOfDirectory)).executeUpdate();
    }
    
    @Test
    public void assertPersistFailureDuringUpdate() throws Exception {
        final String key = "key";
        when(mockJdbcConnection.prepareStatement(fixture.selectByKeySQL())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("value")).thenReturn("oldValue");
        when(mockJdbcConnection.prepareStatement(fixture.updateSQL())).thenReturn(mockPreparedStatement);
        repository.persist(key, "value");
        verify(mockPreparedStatementForPersist, times(0)).executeUpdate();
    }
    
    @Test
    public void assertPersistWithInsertForSimpleKeys() throws Exception {
        final String key = "key";
        final String value = "value";
        when(mockJdbcConnection.prepareStatement(fixture.selectByKeySQL())).thenReturn(mockPreparedStatement);
        when(mockJdbcConnection.prepareStatement(fixture.insertSQL())).thenReturn(mockPreparedStatementForPersist);
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
    public void assertPersistFailureDuringInsert() throws Exception {
        when(mockJdbcConnection.prepareStatement(fixture.selectByKeySQL())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        when(mockJdbcConnection.prepareStatement(fixture.insertSQL())).thenReturn(mockPreparedStatement);
        repository.persist("key", "value");
        verify(mockPreparedStatementForPersist, times(0)).executeUpdate();
    }
    
    @Test
    public void assertDelete() throws Exception {
        String key = "key";
        when(mockJdbcConnection.prepareStatement(fixture.deleteSQL())).thenReturn(mockPreparedStatement);
        repository.delete(key);
        verify(mockPreparedStatement).setString(1, key);
        verify(mockPreparedStatement).executeUpdate();
    }
    
    @Test
    public void assertDeleteFailure() throws Exception {
        String key = "key";
        when(mockJdbcConnection.prepareStatement(fixture.deleteSQL())).thenReturn(mockPreparedStatementForPersist);
        repository.delete(key);
        verify(mockPreparedStatement, times(0)).executeUpdate();
    }
    
    @Test
    public void assertClose() {
        repository.close();
        HikariDataSource hikariDataSource = mockedConstruction.constructed().get(0);
        verify(hikariDataSource).close();
    }
}
