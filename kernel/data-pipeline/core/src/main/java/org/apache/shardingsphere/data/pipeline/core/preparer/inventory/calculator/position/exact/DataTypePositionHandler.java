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

package org.apache.shardingsphere.data.pipeline.core.preparer.inventory.calculator.position.exact;

import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.PrimaryKeyIngestPosition;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data type position handler.
 *
 * @param <T> data type
 */
public interface DataTypePositionHandler<T> {
    
    /**
     * Read column value from result set.
     *
     * @param resultSet result set
     * @param columnIndex column index
     * @return column value
     * @throws SQLException SQL exception
     */
    T readColumnValue(ResultSet resultSet, int columnIndex) throws SQLException;
    
    /**
     * Set prepared statement value.
     *
     * @param preparedStatement prepared statement
     * @param parameterIndex parameter index
     * @param value value
     * @throws SQLException SQL exception
     */
    void setPreparedStatementValue(PreparedStatement preparedStatement, int parameterIndex, T value) throws SQLException;
    
    /**
     * Create ingest position.
     *
     * @param lowerBound lower bound
     * @param upperBound upper bound
     * @return ingest position
     */
    PrimaryKeyIngestPosition<T> createIngestPosition(T lowerBound, T upperBound);
}
