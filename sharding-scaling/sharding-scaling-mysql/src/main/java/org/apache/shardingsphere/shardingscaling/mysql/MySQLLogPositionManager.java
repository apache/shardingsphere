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

package org.apache.shardingsphere.shardingscaling.mysql;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.shardingscaling.core.job.position.LogPositionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MySQL log manager, based on binlog mechanism.
 */
@RequiredArgsConstructor
public final class MySQLLogPositionManager implements LogPositionManager<BinlogPosition> {
    
    private final DataSource dataSource;
    
    private BinlogPosition currentPosition;
    
    @Override
    public BinlogPosition getCurrentPosition() {
        if (null == currentPosition) {
            getCurrentPositionFromSource();
        }
        return currentPosition;
    }
    
    private void getCurrentPositionFromSource() {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SHOW MASTER STATUS");
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            currentPosition = new BinlogPosition(resultSet.getString(1), resultSet.getLong(2));
            preparedStatement = connection.prepareStatement("SHOW VARIABLES LIKE 'server_id'");
            resultSet = preparedStatement.executeQuery();
            resultSet.next();
            currentPosition.setServerId(resultSet.getLong(2));
        } catch (final SQLException ex) {
            throw new RuntimeException("markPosition error", ex);
        }
    }
    
    @Override
    public void updateCurrentPosition(final BinlogPosition newLogPosition) {
        this.currentPosition = newLogPosition;
    }
}
