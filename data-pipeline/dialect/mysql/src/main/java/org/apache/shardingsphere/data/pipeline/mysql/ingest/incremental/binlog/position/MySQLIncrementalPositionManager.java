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

package org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.position;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.DialectIncrementalPositionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Incremental position manager for MySQL.
 */
public final class MySQLIncrementalPositionManager implements DialectIncrementalPositionManager {
    
    private static final String MYSQL_DATABASE_PRODUCT_NAME = "MySQL";
    
    private static final String SHOW_MASTER_STATUS_SQL = "SHOW MASTER STATUS";
    
    private static final String SHOW_BINARY_LOG_STATUS_SQL = "SHOW BINARY LOG STATUS";
    
    @Override
    public MySQLBinlogPosition init(final String data) {
        String[] array = data.split("#");
        Preconditions.checkArgument(2 == array.length, "Unknown binlog position: %s", data);
        return new MySQLBinlogPosition(array[0], Long.parseLong(array[1]));
    }
    
    @Override
    public MySQLBinlogPosition init(final DataSource dataSource, final String slotNameSuffix) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return getBinlogPosition(connection);
        }
    }
    
    private MySQLBinlogPosition getBinlogPosition(final Connection connection) throws SQLException {
        String sql = getShowBinlogStatusSQL(connection);
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            return new MySQLBinlogPosition(resultSet.getString(1), resultSet.getLong(2));
        }
    }
    
    private String getShowBinlogStatusSQL(final Connection connection) throws SQLException {
        DatabaseMetaData databaseMetaData = connection.getMetaData();
        return isShowBinaryLogStatusSupported(databaseMetaData) ? SHOW_BINARY_LOG_STATUS_SQL : SHOW_MASTER_STATUS_SQL;
    }
    
    private boolean isShowBinaryLogStatusSupported(final DatabaseMetaData databaseMetaData) throws SQLException {
        if (!MYSQL_DATABASE_PRODUCT_NAME.equals(databaseMetaData.getDatabaseProductName())) {
            return false;
        }
        int majorVersion = databaseMetaData.getDatabaseMajorVersion();
        return majorVersion > 8 || 8 == majorVersion && databaseMetaData.getDatabaseMinorVersion() >= 4;
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
