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

package org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.position;

import org.apache.shardingsphere.data.pipeline.core.exception.PipelineInternalException;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.DialectIncrementalPositionManager;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.position.slot.PostgreSQLSlotManager;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.WALPosition;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.decode.PostgreSQLLogSequenceNumber;
import org.postgresql.replication.LogSequenceNumber;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Incremental position manager for PostgreSQL.
 */
public final class PostgreSQLIncrementalPositionManager implements DialectIncrementalPositionManager {
    
    private final PostgreSQLSlotManager slotManager = new PostgreSQLSlotManager("test_decoding");
    
    @Override
    public WALPosition init(final String data) {
        return new WALPosition(new PostgreSQLLogSequenceNumber(LogSequenceNumber.valueOf(data)));
    }
    
    @Override
    public WALPosition init(final DataSource dataSource, final String slotNameSuffix) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            slotManager.create(connection, slotNameSuffix);
            return getWALPosition(connection, getLogSequenceNumberSQL(connection.getMetaData()));
        }
    }
    
    private WALPosition getWALPosition(final Connection connection, final String logSequenceNumberSQL) throws SQLException {
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(logSequenceNumberSQL);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            return new WALPosition(new PostgreSQLLogSequenceNumber(LogSequenceNumber.valueOf(resultSet.getString(1))));
        }
    }
    
    private String getLogSequenceNumberSQL(final DatabaseMetaData metaData) throws SQLException {
        if (9 == metaData.getDatabaseMajorVersion() && 6 <= metaData.getDatabaseMinorVersion()) {
            return "SELECT PG_CURRENT_XLOG_LOCATION()";
        }
        if (10 <= metaData.getDatabaseMajorVersion()) {
            return "SELECT PG_CURRENT_WAL_LSN()";
        }
        throw new PipelineInternalException("Unsupported PostgreSQL version: " + metaData.getDatabaseProductVersion());
    }
    
    @Override
    public void destroy(final DataSource dataSource, final String slotNameSuffix) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            slotManager.dropIfExisted(connection, slotNameSuffix);
        }
    }
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }
}
