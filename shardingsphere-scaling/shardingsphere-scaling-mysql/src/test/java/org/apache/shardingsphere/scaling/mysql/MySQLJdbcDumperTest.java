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

package org.apache.shardingsphere.scaling.mysql;

import lombok.SneakyThrows;
import org.apache.shardingsphere.scaling.core.config.DumperConfiguration;
import org.apache.shardingsphere.scaling.core.config.InventoryDumperConfiguration;
import org.apache.shardingsphere.scaling.core.config.JDBCScalingDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.datasource.DataSourceManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLJdbcDumperTest {
    
    private DataSourceManager dataSourceManager;
    
    private MySQLJdbcDumper mySQLJdbcDumper;
    
    @Mock
    private Connection connection;
    
    @Before
    public void setUp() {
        dataSourceManager = new DataSourceManager();
        mySQLJdbcDumper = new MySQLJdbcDumper(mockInventoryDumperConfiguration(), dataSourceManager);
    }
    
    private InventoryDumperConfiguration mockInventoryDumperConfiguration() {
        DumperConfiguration dumperConfig = mockDumperConfiguration();
        initTableData(dumperConfig);
        InventoryDumperConfiguration result = new InventoryDumperConfiguration(dumperConfig);
        result.setTableName("t_order");
        return result;
    }
    
    private DumperConfiguration mockDumperConfiguration() {
        DumperConfiguration result = new DumperConfiguration();
        result.setDataSourceConfiguration(new JDBCScalingDataSourceConfiguration("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL", "root", "root"));
        return result;
    }
    
    @SneakyThrows(SQLException.class)
    private void initTableData(final DumperConfiguration dumperConfig) {
        DataSource dataSource = dataSourceManager.getDataSource(dumperConfig.getDataSourceConfiguration());
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (id INT PRIMARY KEY, user_id VARCHAR(12))");
            statement.execute("INSERT INTO t_order (id, user_id) VALUES (1, 'xxx'), (999, 'yyy')");
        }
    }
    
    @Test
    public void assertReadValue() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.TIMESTAMP);
        when(resultSetMetaData.getColumnType(2)).thenReturn(Types.VARCHAR);
        mySQLJdbcDumper.readValue(resultSet, 1);
        mySQLJdbcDumper.readValue(resultSet, 2);
        verify(resultSet).getString(1);
        verify(resultSet).getObject(2);
    }
    
    @Test
    public void assertCreatePreparedStatement() throws SQLException {
        when(connection.prepareStatement("", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)).thenReturn(mock(PreparedStatement.class));
        PreparedStatement preparedStatement = mySQLJdbcDumper.createPreparedStatement(connection, "");
        verify(preparedStatement).setFetchSize(Integer.MIN_VALUE);
    }
}
