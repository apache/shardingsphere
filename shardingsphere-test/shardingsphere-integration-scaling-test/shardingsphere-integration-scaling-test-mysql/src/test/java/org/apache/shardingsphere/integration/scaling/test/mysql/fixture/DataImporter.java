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

package org.apache.shardingsphere.integration.scaling.test.mysql.fixture;

import lombok.SneakyThrows;
import org.apache.shardingsphere.integration.scaling.test.mysql.env.ITEnvironmentContext;
import org.apache.shardingsphere.integration.scaling.test.mysql.env.cases.DataSet;
import org.apache.shardingsphere.integration.scaling.test.mysql.env.cases.Type;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Data importer.
 */
public final class DataImporter {
    
    private static final String CREATE_SQL = "CREATE TABLE IF NOT EXISTS %s(id INT PRIMARY KEY AUTO_INCREMENT, %s %s)";
    
    private static final String INSERT_SQL = "INSERT INTO %s(%s) VALUES(?)";
    
    private final DataSet testCases = ITEnvironmentContext.INSTANCE.getTestCases();
    
    private final DataSource sourceDataSource = ITEnvironmentContext.INSTANCE.getSourceDataSource();
    
    private final DataSource targetDataSource = ITEnvironmentContext.INSTANCE.getTargetDataSource();
    
    /**
     * Create tables.
     */
    public void createTables() {
        DataSet testCases = ITEnvironmentContext.INSTANCE.getTestCases();
        for (Type type : testCases.getTypes()) {
            createTable(sourceDataSource, type.getTableName(), type.getColumnName(), type.getColumnType());
            createTable(targetDataSource, type.getTableName(), type.getColumnName(), type.getColumnType());
        }
    }
    
    @SneakyThrows(SQLException.class)
    private void createTable(final DataSource dataSource, final String tableName, final String columnName, final String columnType) {
        try (Connection connection = dataSource.getConnection()) {
            connection.prepareStatement(String.format(CREATE_SQL, tableName, columnName, columnType)).execute();
        }
    }
    
    /**
     * Import data.
     */
    @SneakyThrows(SQLException.class)
    public void importData() {
        for (Type type : testCases.getTypes()) {
            for (String value : type.getValues()) {
                try (Connection connection = sourceDataSource.getConnection()) {
                    PreparedStatement ps = connection.prepareStatement(String.format(INSERT_SQL, type.getTableName(), type.getColumnName()));
                    ps.setString(1, value);
                    ps.execute();
                }
            }
        }
    }
}
