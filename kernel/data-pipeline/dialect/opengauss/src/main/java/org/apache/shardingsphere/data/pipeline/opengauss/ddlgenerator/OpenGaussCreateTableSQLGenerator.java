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

package org.apache.shardingsphere.data.pipeline.opengauss.ddlgenerator;

import org.apache.shardingsphere.data.pipeline.core.exception.syntax.CreateTableSQLGenerateException;
import org.apache.shardingsphere.data.pipeline.spi.ddlgenerator.CreateTableSQLGenerator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;

/**
* Create table SQL generator for openGauss.
 */
public final class OpenGaussCreateTableSQLGenerator implements CreateTableSQLGenerator {
    
    private static final String SELECT_TABLE_DEF_SQL = "SELECT * FROM pg_get_tabledef('%s.%s')";
    
    private static final String COLUMN_LABEL = "pg_get_tabledef";
    
    private static final String DELIMITER = ";";
    
    @Override
    public Collection<String> generate(final DataSource dataSource, final String schemaName, final String tableName) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(String.format(SELECT_TABLE_DEF_SQL, schemaName, tableName))) {
            if (resultSet.next()) {
                // TODO use ";" to split is not always correct
                return Arrays.asList(resultSet.getString(COLUMN_LABEL).split(DELIMITER));
            }
        }
        throw new CreateTableSQLGenerateException(tableName);
    }
    
    @Override
    public String getDatabaseType() {
        return "openGauss";
    }
}
