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

package io.shardingsphere.core.transport;

import io.shardingsphere.core.jdbc.adapter.AbstractConnectionAdapter;
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
public class JDBCSqlTransportTest {
    
    private final JDBCSqlTransport sqlTransport = new JDBCSqlTransport();
    
    private final String dsName = "ds";
    
    private final String sql = "SELECT now()";
    
    @Mock
    private AbstractConnectionAdapter connection1;
    
    @Mock
    private Connection actualConnect;
    
    @Mock
    private PreparedStatement preparedStatement;
    
    @Mock
    private AbstractConnectionAdapter connection2;
    
    @Before
    public void setUp() throws SQLException {
        when(actualConnect.prepareStatement(sql)).thenReturn(preparedStatement);
        when(connection1.getConnection(dsName)).thenReturn(actualConnect);
        when(connection2.getConnection(dsName)).thenReturn(null);
    }
    
    @Test
    public void assertGetConnection() throws SQLException {
        sqlTransport.setShardingConnection(connection1);
        sqlTransport.with(dsName, sql, new ArrayList<List<String>>());
        verify(connection1).getConnection(dsName);
    }
    
    @Test
    public void assertSetSqlParams() throws SQLException {
        sqlTransport.setShardingConnection(connection1);
        final List<String> list = new ArrayList<>();
        final String param1 = "1";
        final String param2 = "xxx";
        list.add(param1);
        list.add(param2);
        sqlTransport.with(dsName, sql, new ArrayList<List<String>>() {{add(list);}});
        verify(preparedStatement).setObject(1, param1);
        verify(preparedStatement).setObject(2, param2);
    }
    
    @Test(expected = NullPointerException.class)
    public void assertRenew() throws SQLException {
        sqlTransport.setShardingConnection(connection1);
        sqlTransport.renew(connection2);
        verify(connection1).close();
        sqlTransport.with(dsName, sql, new ArrayList<List<String>>());
    }
}
