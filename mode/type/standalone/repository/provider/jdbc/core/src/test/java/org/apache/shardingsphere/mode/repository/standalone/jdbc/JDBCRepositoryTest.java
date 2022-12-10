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
import org.h2.jdbc.JdbcCallableStatement;
import org.h2.jdbc.JdbcConnection;
import org.h2.jdbc.JdbcResultSet;
import org.h2.jdbc.JdbcStatement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class JDBCRepositoryTest {
    
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
    
    private final JDBCRepositoryProviderFixture fixture = new JDBCRepositoryProviderFixture();
    
    private MockedConstruction<HikariDataSource> mockedConstruction;
    
    private JDBCRepository repository;
    
    @Before
    public void setup() throws Exception {
        this.mockedConstruction = mockConstruction(HikariDataSource.class, (mock, context) -> when(mock.getConnection()).thenReturn(mockJdbcConnection));
        when(mockJdbcConnection.createStatement()).thenReturn(mockStatement);
        repository = new JDBCRepository();
        repository.init(createProperties());
    }
    
    @After
    public void tearDown() {
        this.mockedConstruction.close();
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
        when(mockJdbcConnection.prepareStatement(eq(fixture.selectByKeySQL()))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString(eq("value"))).thenReturn(value);
        String actual = repository.getDirectly(key);
        verify(mockPreparedStatement).setString(eq(1), eq(key));
        assertThat(actual, is(value));
    }
    
    @Test
    public void assertGetFailure() throws Exception {
        when(mockJdbcConnection.prepareStatement(eq(fixture.selectByKeySQL()))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        String actual = repository.getDirectly("key");
        assertThat(actual, is(""));
    }
    
    @Test
    public void assertPersistAndGetChildrenKeys() throws Exception {
        when(mockJdbcConnection.prepareStatement(eq(fixture.selectByParentKeySQL()))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);
        when(mockResultSet.getString(eq("key")))
                .thenReturn("parent1/test1")
                .thenReturn("parent1/test2")
                .thenReturn("");
        List<String> childrenKeys = repository.getChildrenKeys("/testPath");
        assertThat(childrenKeys.get(0), is("test1"));
        assertThat(childrenKeys.get(1), is("test2"));
    }
    
    @Test
    public void assertPersistAndGetChildrenKeysFailure() throws Exception {
        when(mockJdbcConnection.prepareStatement(eq(fixture.selectByParentKeySQL()))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        List<String> actual = repository.getChildrenKeys("key");
        assertThat(actual.size(), is(0));
    }
    
    @Test
    public void assertPersistWithUpdateForSimpleKeys() throws Exception {
        final String key = "key";
        final String value = "value";
        when(mockJdbcConnection.prepareStatement(eq(fixture.selectByKeySQL()))).thenReturn(mockPreparedStatement);
        when(mockJdbcConnection.prepareStatement(eq(fixture.updateSQL()))).thenReturn(mockPreparedStatementForPersist);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString(eq("value"))).thenReturn("oldValue");
        repository.persist(key, value);
        verify(mockPreparedStatement).setString(eq(1), eq(key));
        verify(mockPreparedStatementForPersist).setString(eq(1), anyString());
        verify(mockPreparedStatementForPersist).setString(eq(1), eq(value));
        verify(mockPreparedStatementForPersist).setString(eq(2), eq(key));
        verify(mockPreparedStatementForPersist).executeUpdate();
    }
    
    @Test
    public void assertPersistForDirectory() throws Exception {
        final String key = "/parent/child/test1";
        final String value = "test1_content";
        when(mockJdbcConnection.prepareStatement(eq(fixture.selectByKeySQL()))).thenReturn(mockPreparedStatement);
        when(mockJdbcConnection.prepareStatement(eq(fixture.insertSQL()))).thenReturn(mockPreparedStatementForPersist);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
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
            verify(mockPreparedStatement).setString(eq(1), eq(directoryPath));
            // Verifying that during insert operation, setString at index 2 is called for every directory level
            verify(mockPreparedStatementForPersist).setString(eq(2), eq(directoryPath));
            // Verifying that during insert operation, setString at index 4 is called for every parent directory
            verify(mockPreparedStatementForPersist).setString(eq(4), eq(parentDirectory));
            beginIndex = nextSeparatorIndex;
            parentDirectory = directoryPath;
        }
        // Verifying that during insert operation, setString at index 3 is called with "" for all the parent directories
        verify(mockPreparedStatementForPersist, times(depthOfDirectory - 1)).setString(eq(3), eq(""));
        // Verifying that during insert operation, setString at index 3 is called with the leaf node once
        verify(mockPreparedStatementForPersist, times(1)).setString(eq(3), eq("test1_content"));
        // Verifying that during insert operation, setString at index 1 is called with a UUID
        verify(mockPreparedStatementForPersist, times(depthOfDirectory)).setString(eq(1), anyString());
        // Verifying that executeOperation in insert is called for all the directory levels
        verify(mockPreparedStatementForPersist, times(depthOfDirectory)).executeUpdate();
    }
    
    @Test
    public void assertPersistFailureDuringUpdate() throws Exception {
        final String key = "key";
        when(mockJdbcConnection.prepareStatement(eq(fixture.selectByKeySQL()))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString(eq("value"))).thenReturn("oldValue");
        when(mockJdbcConnection.prepareStatement(eq(fixture.updateSQL()))).thenReturn(mockPreparedStatement);
        repository.persist(key, "value");
        verify(mockPreparedStatementForPersist, times(0)).executeUpdate();
    }
    
    @Test
    public void assertPersistWithInsertForSimpleKeys() throws Exception {
        final String key = "key";
        final String value = "value";
        when(mockJdbcConnection.prepareStatement(eq(fixture.selectByKeySQL()))).thenReturn(mockPreparedStatement);
        when(mockJdbcConnection.prepareStatement(eq(fixture.insertSQL()))).thenReturn(mockPreparedStatementForPersist);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        repository.persist(key, value);
        verify(mockPreparedStatement).setString(eq(1), eq(key));
        verify(mockPreparedStatementForPersist).setString(eq(1), anyString());
        verify(mockPreparedStatementForPersist).setString(eq(2), eq(key));
        verify(mockPreparedStatementForPersist).setString(eq(3), eq(value));
        verify(mockPreparedStatementForPersist).setString(eq(4), eq("/"));
        verify(mockPreparedStatementForPersist).executeUpdate();
    }
    
    @Test
    public void assertPersistFailureDuringInsert() throws Exception {
        when(mockJdbcConnection.prepareStatement(eq(fixture.selectByKeySQL()))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        when(mockJdbcConnection.prepareStatement(eq(fixture.insertSQL()))).thenReturn(mockPreparedStatement);
        repository.persist("key", "value");
        verify(mockPreparedStatementForPersist, times(0)).executeUpdate();
    }
    
    @Test
    public void assertDelete() throws Exception {
        String key = "key";
        when(mockJdbcConnection.prepareStatement(eq(fixture.deleteSQL()))).thenReturn(mockPreparedStatement);
        repository.delete(key);
        verify(mockPreparedStatement).setString(eq(1), eq(key));
        verify(mockPreparedStatement).executeUpdate();
    }
    
    @Test
    public void assertDeleteFailure() throws Exception {
        String key = "key";
        when(mockJdbcConnection.prepareStatement(eq(fixture.deleteSQL()))).thenReturn(mockPreparedStatementForPersist);
        repository.delete(key);
        verify(mockPreparedStatement, times(0)).executeUpdate();
    }
    
    @Test
    public void assertClose() {
        repository.close();
        HikariDataSource hikariDataSource = mockedConstruction.constructed().get(0);
        verify(hikariDataSource).close();
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty("jdbc_url", "jdbc:h2:mem:config;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL");
        result.setProperty("username", "sa");
        result.setProperty("password", "");
        result.setProperty("provider", "FIXTURE");
        return result;
    }
}
