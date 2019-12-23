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

package org.apache.shardingsphere.shardingscaling.postgresql;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.position.LogPositionManager;
import org.postgresql.replication.LogSequenceNumber;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * PostgreSQL log position manager.
 *
 * @author avalon566
 */
@RequiredArgsConstructor
public final class PostgreSQLLogPositionManager implements LogPositionManager<WalPosition> {
    
    private final DataSource dataSource;
    
    private WalPosition currentPosition;
    
    @Override
    public WalPosition getCurrentPosition() {
        if (null == currentPosition) {
            getCurrentPositionFromSource();
        }
        return currentPosition;
    }
    
    private void getCurrentPositionFromSource() {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "";
            if (9 == connection.getMetaData().getDatabaseMajorVersion() && 6 <= connection.getMetaData().getDatabaseMinorVersion()) {
                sql = "select pg_current_xlog_location()";
            } else if (10 <= connection.getMetaData().getDatabaseMajorVersion()) {
                sql = "select pg_current_wal_lsn()";
            } else {
                throw new RuntimeException("Not support postgrsql version:" + connection.getMetaData().getDatabaseProductVersion());
            }
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            rs.next();
            currentPosition = new WalPosition(LogSequenceNumber.valueOf(rs.getString(1)));
        } catch (SQLException e) {
            throw new RuntimeException("markPosition error", e);
        }
    }
    
    @Override
    public void updateCurrentPosition(final WalPosition newLogPosition) {
        currentPosition = newLogPosition;
    }
}
