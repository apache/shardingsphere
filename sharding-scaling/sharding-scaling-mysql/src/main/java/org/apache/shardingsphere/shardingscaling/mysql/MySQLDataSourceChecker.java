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

import org.apache.shardingsphere.shardingscaling.core.exception.DatasourceCheckFailedException;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.checker.AbstractDataSourceChecker;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Data source checker for MySQL.
 */
public final class MySQLDataSourceChecker extends AbstractDataSourceChecker {
    
    @Override
    public void checkPrivilege(final Collection<DataSource> dataSources) {
        try {
            for (DataSource dataSource : dataSources) {
                String tableName;
                Connection connection = dataSource.getConnection();
                ResultSet tables = connection.getMetaData().getTables(connection.getCatalog(), null, "%", new String[]{"TABLE"});
                if (tables.next()) {
                    tableName = tables.getString(3);
                } else {
                    throw new DatasourceCheckFailedException("No tables find in the source datasource");
                }
                connection.prepareStatement(String.format("SELECT * FROM %s LIMIT 1", tableName)).executeQuery();
                connection.prepareStatement("SHOW MASTER STATUS").executeQuery();
            }
        } catch (SQLException e) {
            throw new DatasourceCheckFailedException("Datasources check failed!");
        }
    }
}
