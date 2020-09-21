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

import com.google.gson.Gson;
import org.apache.shardingsphere.scaling.core.job.position.BasePositionManager;
import org.apache.shardingsphere.scaling.mysql.binlog.BinlogPosition;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MySQL position manager, based on binlog mechanism.
 */
public final class MySQLPositionManager extends BasePositionManager<BinlogPosition> {
    
    private static final Gson GSON = new Gson();
    
    private DataSource dataSource;
    
    public MySQLPositionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    public MySQLPositionManager(final String position) {
        setPosition(GSON.fromJson(position, BinlogPosition.class));
    }
    
    @Override
    public BinlogPosition getPosition() {
        BinlogPosition position = super.getPosition();
        if (null != position) {
            return position;
        }
        initPosition();
        return super.getPosition();
    }
    
    private void initPosition() {
        try (Connection connection = dataSource.getConnection()) {
            BinlogPosition binlogPosition = getBinlogPosition(connection);
            binlogPosition.setServerId(getServerId(connection));
            setPosition(binlogPosition);
        } catch (final SQLException ex) {
            throw new RuntimeException("init position failed.", ex);
        }
    }
    
    private BinlogPosition getBinlogPosition(final Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SHOW MASTER STATUS");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            return new BinlogPosition(resultSet.getString(1), resultSet.getLong(2));
        }
    }
    
    private long getServerId(final Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SHOW VARIABLES LIKE 'server_id'");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            return resultSet.getLong(2);
        }
    }
}
