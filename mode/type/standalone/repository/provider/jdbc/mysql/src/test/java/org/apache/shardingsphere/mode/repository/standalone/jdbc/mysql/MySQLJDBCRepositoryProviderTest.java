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

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.mode.repository.standalone.jdbc.JDBCRepository;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MySQLJDBCRepositoryProviderTest {
    
    @Mock
    private Connection connection;
    
    @Mock
    private PreparedStatement preparedStatement;
    
    @Mock
    private ResultSet resultSet;
    
    private MockedConstruction<HikariDataSource> construction;
    
    private final MySQLJDBCRepositoryProvider provider = new MySQLJDBCRepositoryProvider();
    
    private final JDBCRepository repository = new JDBCRepository();
    
    @BeforeEach
    public void setUp() throws SQLException {
        construction = mockConstruction(HikariDataSource.class, (mock, context) -> when(mock.getConnection()).thenReturn(connection));
        when(connection.createStatement()).thenReturn(mock(Statement.class));
        repository.init(PropertiesBuilder.build(new Property("jdbc_url", "jdbc:mysql://localhost:3306/config"),
                new Property("username", "sa"), new Property("password", ""), new Property("provider", "MySQL")));
    }
    
    @AfterEach
    public void stop() {
        repository.close();
        construction.close();
    }
    
    @Test
    public void assertPersistAndGet() throws SQLException {
        when(connection.prepareStatement(provider.selectByKeySQL())).thenReturn(preparedStatement);
        when(connection.prepareStatement(provider.insertSQL())).thenReturn(preparedStatement);
        when(connection.prepareStatement(provider.updateSQL())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false, true, true);
        when(resultSet.getString("value")).thenReturn("test1_content", "test1_content", "modify_content");
        repository.persist("/testPath/test1", "test1_content");
        assertThat(repository.getDirectly("/testPath/test1"), is("test1_content"));
        repository.persist("/testPath/test1", "modify_content");
        assertThat(repository.getDirectly("/testPath/test1"), is("modify_content"));
    }
    
    @Test
    public void assertPersistAndGetChildrenKeys() throws SQLException {
        when(connection.prepareStatement(provider.selectByKeySQL())).thenReturn(preparedStatement);
        when(connection.prepareStatement(provider.insertSQL())).thenReturn(preparedStatement);
        when(connection.prepareStatement(provider.selectByParentKeySQL())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false, false, false, false, true, true, false);
        when(resultSet.getString("key")).thenReturn("test1", "test2");
        repository.persist("/testPath/test1", "test1_content");
        repository.persist("/testPath/test2", "test2_content");
        List<String> childrenKeys = repository.getChildrenKeys("/testPath");
        assertThat(childrenKeys.get(0), is("test1"));
        assertThat(childrenKeys.get(1), is("test2"));
    }
    
    @Test
    public void assertDelete() throws SQLException {
        when(connection.prepareStatement(provider.selectByKeySQL())).thenReturn(preparedStatement);
        when(connection.prepareStatement(provider.deleteSQL())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        repository.delete("/testPath");
        assertThat(repository.getDirectly("/testPath"), is(""));
    }
}
