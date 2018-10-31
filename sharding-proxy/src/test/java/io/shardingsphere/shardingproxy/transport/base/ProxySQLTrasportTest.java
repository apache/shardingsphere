/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingproxy.transport.base;

import io.shardingsphere.shardingproxy.backend.jdbc.datasource.JDBCBackendDataSource;
import org.apache.servicecomb.saga.core.TransportFailedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProxySQLTrasportTest {
    
    private final String dsName = "ds";
    
    private final String sql = "SELECT now()";
    
    @Mock
    private JDBCBackendDataSource jdbcBackendDataSource;
    
    @Mock
    private Connection actualConnect;
    
    @Mock
    private PreparedStatement preparedStatement;
    
    @Before
    public void setUp() throws SQLException {
        when(actualConnect.prepareStatement(sql)).thenReturn(preparedStatement);
    }
    
    @Test
    public void assertGetConnection() throws SQLException {
        ProxySQLTransport proxySQLTransport = new ProxySQLTransport(jdbcBackendDataSource);
        when(jdbcBackendDataSource.getConnection(dsName)).thenReturn(actualConnect);
        proxySQLTransport.with(dsName, sql, new ArrayList<List<String>>());
        verify(jdbcBackendDataSource).getConnection(dsName);
    }
    
    @Test(expected = TransportFailedException.class)
    public void assertGetConnectionFail() throws SQLException {
        ProxySQLTransport proxySQLTransport = new ProxySQLTransport(jdbcBackendDataSource);
        when(jdbcBackendDataSource.getConnection(dsName)).thenThrow(new SQLException("test"));
        proxySQLTransport.with(dsName, sql, new ArrayList<List<String>>());
    }
    
    @Test
    public void assertSetSqlParams() throws SQLException {
        when(jdbcBackendDataSource.getConnection(dsName)).thenReturn(actualConnect);
        ProxySQLTransport proxySQLTransport = new ProxySQLTransport(jdbcBackendDataSource);
        final List<String> list = new ArrayList<>();
        final String param1 = "1";
        final String param2 = "xxx";
        list.add(param1);
        list.add(param2);
        proxySQLTransport.with(dsName, sql, new ArrayList<List<String>>() {{add(list);}});
        verify(preparedStatement).setObject(1, param1);
        verify(preparedStatement).setObject(2, param2);
    }
}
