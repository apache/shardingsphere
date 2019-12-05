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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.shardingscaling.core.config.JdbcDataSourceConfiguration;
import org.apache.shardingsphere.shardingscaling.core.exception.HeartbeatInitException;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.record.DataRecord;
import org.apache.shardingsphere.shardingscaling.core.util.DataSourceFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Heartbeat, use to detect synchronize delay.
 *
 * @author avalon566
 */
@Slf4j
@RequiredArgsConstructor
public final class Heartbeat {
    
    private static final String TABLE_NAME = "_SHARDING_SCALING_HEARTBEAT";
    
    private static final String CREATE_TABLE_DDL = String.format("CREATE TABLE %s ("
            + "   ID INT,"
            + "   LAST_UPDATE_TIME BIGINT)", TABLE_NAME);
    
    private static final String INSERT_DML = String.format("INSERT INTO %s (ID, LAST_UPDATE_TIME)"
            + "VALUES (1, %%d)", TABLE_NAME);
    
    private static final String UPDATE_DML = String.format("UPDATE _SHARDING_SCALING_HEARTBEAT SET"
            + "   LAST_UPDATE_TIME = %%d"
            + "   WHERE ID = 1", TABLE_NAME);
    
    private final JdbcDataSourceConfiguration jdbcDataSourceConfiguration;
    
    private final Timer timer = new Timer();
    
    private long lastUpdateTime;
    
    /**
     * Start timing update timestamp value.
     */
    public void start() {
        final DataSource dataSource = DataSourceFactory.getDataSource(jdbcDataSourceConfiguration);
        try {
            try (Connection connection = dataSource.getConnection()) {
                createTable(connection);
            }
        } catch (SQLException ex) {
            throw new HeartbeatInitException(ex);
        }
        timer.schedule(new TimerTask() {
            
            @Override
            public void run() {
                try {
                    try (Connection connection = dataSource.getConnection()) {
                        updateTimestamp(connection, System.currentTimeMillis());
                    }
                } catch (SQLException ex) {
                    throw new HeartbeatInitException(ex);
                }
            }
        }, 1000, 1000);
    }
    
    private void createTable(final Connection connection) throws SQLException {
        connection.prepareStatement(CREATE_TABLE_DDL).execute();
        connection.prepareStatement(String.format(INSERT_DML, System.currentTimeMillis())).execute();
    }
    
    private void updateTimestamp(final Connection connection, final long timestamp) throws SQLException {
        connection.prepareStatement(String.format(UPDATE_DML, timestamp)).execute();
    }
    
    /**
     * Update last update time by {@code DataRecord}.
     *
     * @param dataRecord data record
     */
    public void updateLastUpdateTime(final DataRecord dataRecord) {
        if (TABLE_NAME.equals(dataRecord.getTableName())) {
            lastUpdateTime = (long) dataRecord.getColumn(2).getValue();
        }
    }
    
    /**
     * Get delay second from now.
     *
     * @return delay second
     */
    public long getDelay() {
        return System.currentTimeMillis() - lastUpdateTime;
    }
}
