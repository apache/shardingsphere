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
import org.apache.shardingsphere.shardingscaling.core.execute.executor.position.LogPositionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MySQL log manager, based on binlog mechanism.
 *
 * @author avalon566
 * @author yangyi
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
            PreparedStatement ps = connection.prepareStatement("show master status");
            ResultSet rs = ps.executeQuery();
            rs.next();
            currentPosition = new BinlogPosition(rs.getString(1), rs.getLong(2));
            ps = connection.prepareStatement("show variables like 'server_id'");
            rs = ps.executeQuery();
            rs.next();
            currentPosition.setServerId(rs.getLong(2));
        } catch (SQLException e) {
            throw new RuntimeException("markPosition error", e);
        }
    }
    
    @Override
    public void updateCurrentPosition(final BinlogPosition newLogPosition) {
        this.currentPosition = newLogPosition;
    }
}
