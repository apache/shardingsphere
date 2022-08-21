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

package org.apache.shardingsphere.data.pipeline.mysql.ingest;

import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.InventoryDumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.datasource.DefaultPipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.ingest.channel.memory.SimpleMemoryPipelineChannel;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataLoader;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class MySQLInventoryDumperTest {
    
    private MySQLInventoryDumper mysqlJdbcDumper;
    
    @Before
    public void setUp() {
        PipelineDataSourceManager dataSourceManager = new DefaultPipelineDataSourceManager();
        InventoryDumperConfiguration dumperConfig = mockInventoryDumperConfiguration();
        PipelineDataSourceWrapper dataSource = dataSourceManager.getDataSource(dumperConfig.getDataSourceConfig());
        mysqlJdbcDumper = new MySQLInventoryDumper(mockInventoryDumperConfiguration(), new SimpleMemoryPipelineChannel(100), dataSource, new PipelineTableMetaDataLoader(dataSource));
        initTableData(dataSource);
    }
    
    private InventoryDumperConfiguration mockInventoryDumperConfiguration() {
        DumperConfiguration dumperConfig = mockDumperConfiguration();
        InventoryDumperConfiguration result = new InventoryDumperConfiguration(dumperConfig);
        result.setActualTableName("t_order_0");
        result.setLogicTableName("t_order");
        return result;
    }
    
    private DumperConfiguration mockDumperConfiguration() {
        DumperConfiguration result = new DumperConfiguration();
        result.setDataSourceConfig(new StandardPipelineDataSourceConfiguration("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL", "root", "root"));
        return result;
    }
    
    @SneakyThrows(SQLException.class)
    private void initTableData(final DataSource dataSource) {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id VARCHAR(12))");
            statement.execute("INSERT INTO t_order (order_id, user_id) VALUES (1, 'xxx'), (999, 'yyy')");
        }
    }
    
    @Test
    public void assertReadValue() throws SQLException {
        String mockDateString = "2022-6-30";
        Object mockObject = new Object();
        Date mockDate = Date.valueOf(mockDateString);
        ResultSet resultSet = mock(ResultSet.class);
        String yearDataType = "YEAR";
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSet.wasNull()).thenReturn(false);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSet.getString(1)).thenReturn(mockDateString);
        when(resultSet.getObject(2)).thenReturn(mockObject);
        when(resultSet.getObject(3)).thenReturn(mockDate);
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.TIMESTAMP);
        when(resultSetMetaData.getColumnType(2)).thenReturn(Types.VARCHAR);
        when(resultSetMetaData.getColumnTypeName(3)).thenReturn(yearDataType);
        String resultTimeStamp = (String) mysqlJdbcDumper.readValue(resultSet, 1);
        Object resultObject = mysqlJdbcDumper.readValue(resultSet, 2);
        Date resultDate = (Date) mysqlJdbcDumper.readValue(resultSet, 3);
        assertThat(resultTimeStamp, is(mockDateString));
        assertThat(resultObject, is(mockObject));
        assertThat(resultDate, is(mockDate));
        verify(resultSet).getString(1);
        verify(resultSet).getObject(2);
        verify(resultSet).getObject(3);
    }
    
    @Test
    public void assertCreatePreparedStatement() throws SQLException {
        Connection connection = mock(Connection.class);
        when(connection.prepareStatement("", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)).thenReturn(mock(PreparedStatement.class));
        PreparedStatement preparedStatement = mysqlJdbcDumper.createPreparedStatement(connection, "");
        verify(preparedStatement).setFetchSize(Integer.MIN_VALUE);
    }
}
