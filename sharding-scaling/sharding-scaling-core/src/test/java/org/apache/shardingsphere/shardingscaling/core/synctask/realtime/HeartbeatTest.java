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

package org.apache.shardingsphere.shardingscaling.core.synctask.realtime;

import lombok.SneakyThrows;
import org.apache.shardingsphere.shardingscaling.core.config.JdbcDataSourceConfiguration;
import org.apache.shardingsphere.shardingscaling.core.util.DataSourceFactory;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;

import static org.hamcrest.CoreMatchers.anything;
import static org.junit.Assert.assertThat;

public final class HeartbeatTest {
    
    private static String dataSourceUrl = "jdbc:h2:mem:test_db;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL";
    
    private static String userName = "root";
    
    private static String password = "password";
    
    @Test
    @SneakyThrows
    public void assertStart() {
        JdbcDataSourceConfiguration jdbcDataSourceConfiguration = mockConfig();
        Heartbeat heartbeat = new Heartbeat(jdbcDataSourceConfiguration);
        heartbeat.start();
        DataSource dataSource = DataSourceFactory.getDataSource(jdbcDataSourceConfiguration);
        try (Connection connection = dataSource.getConnection()) {
            ResultSet resultSet = connection.prepareStatement("SELECT LAST_UPDATE_TIME FROM _SHARDING_SCALING_HEARTBEAT").executeQuery();
            if (resultSet.next()) {
                long lastUpdateTime = resultSet.getLong(1);
                assertThat(lastUpdateTime, anything());
            }
        }
    }
    
    private JdbcDataSourceConfiguration mockConfig() {
        return new JdbcDataSourceConfiguration(dataSourceUrl, userName, password);
    }
}
