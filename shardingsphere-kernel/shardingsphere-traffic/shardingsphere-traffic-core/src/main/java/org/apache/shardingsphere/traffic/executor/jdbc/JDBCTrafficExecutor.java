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

package org.apache.shardingsphere.traffic.executor.jdbc;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfigurationValidator;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConverter;
import org.apache.shardingsphere.traffic.executor.TrafficExecutor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JDBC traffic executor.
 */
@RequiredArgsConstructor
public final class JDBCTrafficExecutor implements TrafficExecutor {
    
    private static final String TRAFFIC_DATASOURCE_NAME = "TRAFFIC";
    
    private final DataSourceConfigurationValidator dataSourceConfigValidator;
    
    private Statement statement;
    
    @Override
    public ResultSet executeQuery(final LogicSQL logicSQL, final DataSourceConfiguration dataSourceConfig) throws SQLException {
        dataSourceConfigValidator.validate(createDataSourceConfigs(dataSourceConfig));
        DataSource dataSource = DataSourceConverter.getDataSource(dataSourceConfig);
        Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(logicSQL.getSql());
        this.statement = statement;
        setParameters(statement, logicSQL.getParameters());
        return statement.executeQuery();
    }
    
    @Override
    public int executeUpdate(final LogicSQL logicSQL, final DataSourceConfiguration dataSourceConfig) throws SQLException {
        // TODO implement executeUpdate method
        return 0;
    }
    
    @Override
    public boolean execute(final LogicSQL logicSQL, final DataSourceConfiguration dataSourceConfig) {
        // TODO implement execute method
        return false;
    }
    
    private void setParameters(final PreparedStatement statement, final List<Object> parameters) throws SQLException {
        int index = 1;
        for (Object each : parameters) { 
            statement.setObject(index++, each);
        }
    }
    
    private Map<String, DataSourceConfiguration> createDataSourceConfigs(final DataSourceConfiguration dataSourceConfig) {
        Map<String, DataSourceConfiguration> result = new HashMap<>(1, 1);
        result.put(TRAFFIC_DATASOURCE_NAME, dataSourceConfig);
        return result;
    }
    
    @Override
    public ResultSet getResultSet() throws SQLException {
        return statement.getResultSet();
    }
    
    @Override
    public void close() throws SQLException {
        if (null != statement) {
            Connection connection = statement.getConnection();
            statement.close();
            connection.close();
        }
    }
}
