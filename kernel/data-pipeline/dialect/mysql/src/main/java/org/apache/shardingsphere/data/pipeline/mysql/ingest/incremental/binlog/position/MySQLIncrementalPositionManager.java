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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Incremental position manager for MySQL.
 */
public final class MySQLIncrementalPositionManager implements DialectIncrementalPositionManager {
    
    @Override
    public MySQLBinlogPosition init(final String data) {
        String[] array = data.split("#");
        Preconditions.checkArgument(2 == array.length, "Unknown binlog position: %s", data);
        return new MySQLBinlogPosition(array[0], Long.parseLong(array[1]));
    }
    
    @Override
    public MySQLBinlogPosition init(final DataSource dataSource, final String slotNameSuffix) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement("SHOW MASTER STATUS");
                ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            return new MySQLBinlogPosition(resultSet.getString(1), resultSet.getLong(2));
        }
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
