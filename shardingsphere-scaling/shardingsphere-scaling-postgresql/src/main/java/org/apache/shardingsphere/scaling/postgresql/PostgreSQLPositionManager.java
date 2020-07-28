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

package org.apache.shardingsphere.scaling.postgresql;

import org.apache.shardingsphere.scaling.core.job.position.PositionManager;
import org.apache.shardingsphere.scaling.postgresql.wal.WalPosition;
import org.postgresql.replication.LogSequenceNumber;
import org.postgresql.util.PSQLException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * PostgreSQL position manager.
 */
public final class PostgreSQLPositionManager implements PositionManager<WalPosition> {
    
    public static final String SLOT_NAME = "sharding_scaling";
    
    public static final String DECODE_PLUGIN = "test_decoding";
    
    public static final String DUPLICATE_OBJECT_ERROR_CODE = "42710";
    
    private DataSource dataSource;
    
    private WalPosition currentPosition;
    
    public PostgreSQLPositionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    public PostgreSQLPositionManager(final String position) {
        this.currentPosition = new WalPosition(LogSequenceNumber.valueOf(position));
    }
    
    @Override
    public WalPosition getCurrentPosition() {
        if (null == currentPosition) {
            getCurrentPositionFromSource();
        }
        return currentPosition;
    }
    
    private void getCurrentPositionFromSource() {
        try (Connection connection = dataSource.getConnection()) {
            // Need to create slot first, hold oldest wal event.
            createIfNotExists(connection);
            currentPosition = getCurrentLsn(connection);
        } catch (SQLException ex) {
            throw new RuntimeException("markPosition error", ex);
        }
    }
    
    private void createIfNotExists(final Connection connection) throws SQLException {
        try {
            PreparedStatement ps = connection.prepareStatement(String.format("SELECT * FROM pg_create_logical_replication_slot('%s', '%s')", SLOT_NAME, DECODE_PLUGIN));
            ps.execute();
        } catch (PSQLException ex) {
            if (!DUPLICATE_OBJECT_ERROR_CODE.equals(ex.getSQLState())) {
                throw ex;
            }
        }
    }
    
    private WalPosition getCurrentLsn(final Connection connection) throws SQLException {
        String sql;
        if (9 == connection.getMetaData().getDatabaseMajorVersion() && 6 <= connection.getMetaData().getDatabaseMinorVersion()) {
            sql = "SELECT PG_CURRENT_XLOG_LOCATION()";
        } else if (10 <= connection.getMetaData().getDatabaseMajorVersion()) {
            sql = "SELECT PG_CURRENT_WAL_LSN()";
        } else {
            throw new RuntimeException("Not support PostgreSQL version:" + connection.getMetaData().getDatabaseProductVersion());
        }
        PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        rs.next();
        return new WalPosition(LogSequenceNumber.valueOf(rs.getString(1)));
    }
    
    @Override
    public void updateCurrentPosition(final WalPosition newPosition) {
        currentPosition = newPosition;
    }
}
