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

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.BinlogPosition;
import org.apache.shardingsphere.data.pipeline.spi.ingest.position.PositionInitializer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MySQL binlog position initializer.
 */
public final class MySQLPositionInitializer implements PositionInitializer {
    
    @Override
    public BinlogPosition init(final DataSource dataSource, final String slotNameSuffix) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            BinlogPosition result = getBinlogPosition(connection);
            result.setServerId(getServerId(connection));
            return result;
        }
    }
    
    @Override
    public BinlogPosition init(final String data) {
        String[] array = data.split("#");
        Preconditions.checkArgument(array.length == 2, "Unknown binlog position: " + data);
        return new BinlogPosition(array[0], Long.parseLong(array[1]));
    }
    
    private BinlogPosition getBinlogPosition(final Connection connection) throws SQLException {
        try (
                PreparedStatement preparedStatement = connection.prepareStatement("SHOW MASTER STATUS");
                ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            return new BinlogPosition(resultSet.getString(1), resultSet.getLong(2));
        }
    }
    
    private long getServerId(final Connection connection) throws SQLException {
        try (
                PreparedStatement preparedStatement = connection.prepareStatement("SHOW VARIABLES LIKE 'server_id'");
                ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            return resultSet.getLong(2);
        }
    }
    
    @Override
    public String getType() {
        return "MySQL";
    }
}
