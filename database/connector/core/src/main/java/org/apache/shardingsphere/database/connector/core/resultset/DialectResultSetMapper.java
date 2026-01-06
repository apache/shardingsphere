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

package org.apache.shardingsphere.database.connector.core.resultset;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPI;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Dialect result set mapper.
 */
@SingletonSPI
public interface DialectResultSetMapper extends DatabaseTypedSPI {
    
    /**
     * Get smallint value from result set.
     *
     * @param resultSet result set
     * @param columnIndex column index
     * @return smallint value
     * @throws SQLException SQL exception
     */
    Object getSmallintValue(ResultSet resultSet, int columnIndex) throws SQLException;
    
    /**
     * Get date value from result set.
     *
     * @param resultSet result set
     * @param columnIndex column index
     * @return date value
     * @throws SQLException SQL exception
     */
    Object getDateValue(ResultSet resultSet, int columnIndex) throws SQLException;
    
    /**
     * Get default value from result set for unhandled column types.
     *
     * @param resultSet result set
     * @param columnIndex column index
     * @param columnType column type from metadata
     * @return value for unhandled column type
     * @throws SQLException SQL exception
     */
    default Object getDefaultValue(final ResultSet resultSet, final int columnIndex, final int columnType) throws SQLException {
        return resultSet.getObject(columnIndex);
    }
}
