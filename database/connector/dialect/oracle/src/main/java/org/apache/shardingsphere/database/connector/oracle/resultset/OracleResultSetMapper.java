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

package org.apache.shardingsphere.database.connector.oracle.resultset;

import org.apache.shardingsphere.database.connector.core.resultset.DialectResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Result set mapper of Oracle.
 */
public final class OracleResultSetMapper implements DialectResultSetMapper {
    
    /**
     * Oracle JDBC driver type code for TIMESTAMP WITH TIME ZONE.
     * Oracle uses non-standard type code -101 instead of {@link Types#TIMESTAMP_WITH_TIMEZONE}.
     */
    private static final int ORACLE_TIMESTAMP_WITH_TIME_ZONE = -101;
    
    /**
     * Oracle JDBC driver type code for TIMESTAMP WITH LOCAL TIME ZONE.
     */
    private static final int ORACLE_TIMESTAMP_WITH_LOCAL_TIME_ZONE = -102;
    
    @Override
    public Object getSmallintValue(final ResultSet resultSet, final int columnIndex) throws SQLException {
        return resultSet.getInt(columnIndex);
    }
    
    @Override
    public Object getDateValue(final ResultSet resultSet, final int columnIndex) throws SQLException {
        return resultSet.getDate(columnIndex);
    }
    
    @Override
    public Object getTimestampValue(final ResultSet resultSet, final int columnIndex, final int columnType) throws SQLException {
        if (isOracleTimestampWithTimeZone(columnType)) {
            return resultSet.getTimestamp(columnIndex);
        }
        return resultSet.getObject(columnIndex);
    }
    
    private boolean isOracleTimestampWithTimeZone(final int columnType) {
        return ORACLE_TIMESTAMP_WITH_TIME_ZONE == columnType || ORACLE_TIMESTAMP_WITH_LOCAL_TIME_ZONE == columnType;
    }
    
    @Override
    public String getDatabaseType() {
        return "Oracle";
    }
}
