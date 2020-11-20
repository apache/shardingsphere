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

package org.apache.shardingsphere.scaling.postgresql.component;

import lombok.SneakyThrows;
import org.apache.shardingsphere.scaling.core.config.DumperConfiguration;
import org.apache.shardingsphere.scaling.core.config.InventoryDumperConfiguration;
import org.apache.shardingsphere.scaling.core.config.rule.StandardJDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.datasource.DataSourceManager;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class PostgreSQLJdbcDumperTest {
    
    private DataSourceManager dataSourceManager;
    
    private PostgreSQLJdbcDumper postgreSQLJdbcDumper;
    
    @Before
    public void setUp() {
        dataSourceManager = new DataSourceManager();
        postgreSQLJdbcDumper = new PostgreSQLJdbcDumper(mockInventoryDumperConfiguration(), dataSourceManager);
    }
    
    private InventoryDumperConfiguration mockInventoryDumperConfiguration() {
        DumperConfiguration dumperConfig = mockDumperConfiguration();
        initTableData(dumperConfig);
        InventoryDumperConfiguration result = new InventoryDumperConfiguration(dumperConfig);
        result.setSourceTable("t_order");
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
    public void assertCreatePreparedStatement() throws SQLException {
        DataSource dataSource = dataSourceManager.getDataSource(mockDumperConfiguration().getDataSourceConfiguration());
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = postgreSQLJdbcDumper.createPreparedStatement(connection, "SELECT * FROM t_order")) {
            assertThat(preparedStatement.getFetchSize(), is(1));
        }
    }
    
    private DumperConfiguration mockDumperConfiguration() {
        DumperConfiguration result = new DumperConfiguration();
        result.setDataSourceConfiguration(new StandardJDBCDataSourceConfiguration("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=PostgreSQL", "root", "root"));
        return result;
    }
}
