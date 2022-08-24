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

package org.apache.shardingsphere.data.pipeline.mysql.ingest;

import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.BasicColumnValueReader;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Column value reader for MySQL.
 */
public final class MySQLColumnValueReader extends BasicColumnValueReader {
    
    private static final String YEAR_DATA_TYPE = "YEAR";
    
    @Override
    public Object readValue(final ResultSet resultSet, final ResultSetMetaData resultSetMetaData, final int columnIndex) throws SQLException {
        if (isYearDataType(resultSetMetaData.getColumnTypeName(columnIndex))) {
            Object result = resultSet.getObject(columnIndex);
            return resultSet.wasNull() ? null : result;
        } else {
            return super.readValue(resultSet, resultSetMetaData, columnIndex);
        }
    }
    
    private boolean isYearDataType(final String columnDataTypeName) {
        return YEAR_DATA_TYPE.equalsIgnoreCase(columnDataTypeName);
    }
    
    @Override
    public String getType() {
        return "MySQL";
    }
}
