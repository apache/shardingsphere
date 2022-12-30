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

package org.apache.shardingsphere.mode.repository.standalone.jdbc.mysql;

import com.mysql.jdbc.ConnectionImpl;
import com.mysql.jdbc.JDBC42CallableStatement;
import com.mysql.jdbc.ResultSetImpl;
import com.mysql.jdbc.StatementImpl;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.mode.repository.standalone.jdbc.JDBCRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MySQLJDBCRepositoryProviderTest {
    
    @Mock
    private ConnectionImpl mockJdbcConnection;
    
    @Mock
    private StatementImpl mockStatement;
    
    @Mock
    private JDBC42CallableStatement mockPreparedStatement;
    
    @Mock
    private JDBC42CallableStatement mockPreparedStatementForPersist;
    
    @Mock
    private ResultSetImpl mockResultSet;
    
    private final MySQLJDBCRepositoryProvider provider = new MySQLJDBCRepositoryProvider();
    
    private MockedConstruction<HikariDataSource> mockedConstruction;
    
    private JDBCRepository repository;
    
    @Before
    public void setUp() throws SQLException {
        this.mockedConstruction = mockConstruction(HikariDataSource.class, (mock, context) -> when(mock.getConnection()).thenReturn(mockJdbcConnection));
        when(mockJdbcConnection.createStatement()).thenReturn(mockStatement);
        Properties props = new Properties();
        props.setProperty("jdbc_url", "jdbc:mysql://localhost:3306/config");
        props.setProperty("username", "sa");
        props.setProperty("password", "");
        props.setProperty("provider", "MYSQL");
        repository = new JDBCRepository();
        repository.init(props);
    }
    
    @After
    public void stop() {
        repository.close();
        this.mockedConstruction.close();
    }
    
    @Test
    public void assertPersistAndGet() throws SQLException {
        when(mockJdbcConnection.prepareStatement(eq(provider.selectByKeySQL()))).thenReturn(mockPreparedStatement);
        when(mockJdbcConnection.prepareStatement(eq(provider.insertSQL()))).thenReturn(mockPreparedStatementForPersist);
        when(mockJdbcConnection.prepareStatement(eq(provider.updateSQL()))).thenReturn(mockPreparedStatementForPersist);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next())
                .thenReturn(false)
                .thenReturn(true)
                .thenReturn(true);
        when(mockResultSet.getString(eq("value")))
                .thenReturn("test1_content")
                .thenReturn("test1_content")
                .thenReturn("modify_content");
        repository.persist("/testPath/test1", "test1_content");
        assertThat(repository.getDirectly("/testPath/test1"), is("test1_content"));
        repository.persist("/testPath/test1", "modify_content");
        assertThat(repository.getDirectly("/testPath/test1"), is("modify_content"));
    }
    
    @Test
    public void assertPersistAndGetChildrenKeys() throws SQLException {
        when(mockJdbcConnection.prepareStatement(eq(provider.selectByKeySQL()))).thenReturn(mockPreparedStatement);
        when(mockJdbcConnection.prepareStatement(eq(provider.insertSQL()))).thenReturn(mockPreparedStatementForPersist);
        when(mockJdbcConnection.prepareStatement(eq(provider.selectByParentKeySQL()))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next())
                .thenReturn(false)
                .thenReturn(false)
                .thenReturn(false)
                .thenReturn(false)
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);
        when(mockResultSet.getString(eq("key")))
                .thenReturn("test1")
                .thenReturn("test2");
        repository.persist("/testPath/test1", "test1_content");
        repository.persist("/testPath/test2", "test2_content");
        List<String> childrenKeys = repository.getChildrenKeys("/testPath");
        assertThat(childrenKeys.get(0), is("test1"));
        assertThat(childrenKeys.get(1), is("test2"));
    }
    
    @Test
    public void assertDelete() throws SQLException {
        when(mockJdbcConnection.prepareStatement(eq(provider.selectByKeySQL()))).thenReturn(mockPreparedStatement);
        when(mockJdbcConnection.prepareStatement(eq(provider.deleteSQL()))).thenReturn(mockPreparedStatementForPersist);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        repository.delete("/testPath");
        assertThat(repository.getDirectly("/testPath"), is(""));
    }
}
