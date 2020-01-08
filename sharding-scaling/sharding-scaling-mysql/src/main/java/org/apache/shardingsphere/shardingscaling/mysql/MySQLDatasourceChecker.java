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

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.shardingscaling.core.exception.DatasourceCheckFailedException;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.checker.AbstractDatasourceChecker;
import org.apache.shardingsphere.shardingscaling.core.util.DataSourceFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Datasource checker for MySQL.
 *
 * @author ssxlulu
 */
public class MySQLDatasourceChecker extends AbstractDatasourceChecker {

    public MySQLDatasourceChecker(final DataSourceFactory dataSourceFactory) {
        super(dataSourceFactory);
    }

    @Override
    public final void checkPrivilege() {
        DataSourceFactory dataSourceFactory = getDataSourceFactory();
        try {
            for (HikariDataSource hikariDataSource : dataSourceFactory.getSourceDatasources().values()) {
                String tableName;
                Connection connection = hikariDataSource.getConnection();
                ResultSet tables = connection.getMetaData().getTables(connection.getCatalog(), null, "%", new String[]{"TABLE"});
                if (tables.next()) {
                    tableName = tables.getString(3);
                } else {
                    throw new DatasourceCheckFailedException("No tables find in the source datasource");
                }
                connection.prepareStatement(String.format("select * from %s limit 1", tableName)).executeQuery();
                connection.prepareStatement("show master status").executeQuery();
            }
        } catch (SQLException e) {
            throw new DatasourceCheckFailedException("Datasources check failed!");
        }
    }
}
