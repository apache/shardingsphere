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

import org.apache.shardingsphere.shardingscaling.core.exception.PrepareFailedException;
import org.apache.shardingsphere.shardingscaling.core.job.preparer.checker.AbstractDataSourceChecker;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Data source checker for MySQL.
 */
public final class MySQLDataSourceChecker extends AbstractDataSourceChecker {
    
    private static final String QUERY_SQL = "SELECT * FROM %s LIMIT 1";
    
    private static final String SHOW_MASTER_STATUS_SQL = "SHOW MASTER STATUS";
    
    @Override
    public void checkPrivilege(final Collection<? extends DataSource> dataSources) {
        for (DataSource each : dataSources) {
            checkPrivilege0(each);
        }
    }
    
    private void checkPrivilege0(final DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String tableName = getFirstTableName(connection);
            checkQueuePrivilege(connection, tableName);
            checkBinlogPrivilege(connection);
        } catch (SQLException e) {
            throw new PrepareFailedException("Datasources privileges check failed!");
        }
    }
    
    private String getFirstTableName(final Connection connection) throws SQLException {
        try (ResultSet tables = connection.getMetaData().getTables(connection.getCatalog(), null, "%", new String[]{"TABLE"})) {
            if (tables.next()) {
                return tables.getString(3);
            }
            throw new PrepareFailedException("No tables find in the source datasource.");
        }
    }
    
    private void checkQueuePrivilege(final Connection connection, final String tableName) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(String.format(QUERY_SQL, tableName))) {
            preparedStatement.executeQuery();
        } catch (SQLException e) {
            throw new PrepareFailedException("Source datasource is lack of query privileges.");
        }
    }
    
    private void checkBinlogPrivilege(final Connection connection) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(SHOW_MASTER_STATUS_SQL);
            ResultSet resultSet = preparedStatement.executeQuery()) {
            if (!resultSet.next()) {
                throw new PrepareFailedException("Source datasource do not open binlog.");
            }
        } catch (SQLException e) {
            throw new PrepareFailedException("Source datasource is lack of replication(binlog) privileges.");
        }
    }
}
