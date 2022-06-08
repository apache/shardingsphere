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

import org.apache.shardingsphere.data.pipeline.spi.ddlgenerator.CreateTableSQLGenerator;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
* Create table SQL generator for openGauss.
 */
public final class OpenGaussCreateTableSQLGenerator implements CreateTableSQLGenerator {
    
    private static final String SELECT_TABLE_DEF_SQL = "SELECT * FROM pg_get_tabledef('%s.%s')";
    
    private static final String COLUMN_LABEL = "pg_get_tabledef";
    
    @Override
    public String generate(final String tableName, final String schemaName, final DataSource dataSource) throws SQLException {
        try (
                Statement statement = dataSource.getConnection().createStatement();
                ResultSet resultSet = statement.executeQuery(String.format(SELECT_TABLE_DEF_SQL, schemaName, tableName))) {
            if (resultSet.next()) {
                return resultSet.getString(COLUMN_LABEL);
            }
        }
        throw new ShardingSphereException("Failed to get ddl sql for table %s", tableName);
    }
    
    @Override
    public String getType() {
        return "openGauss";
    }
}
