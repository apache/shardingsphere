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

package org.apache.shardingsphere.data.pipeline.postgresql.ingest;

import org.apache.shardingsphere.data.pipeline.core.exception.PipelineInternalException;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.DialectIngestPositionManager;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.WALPosition;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.decode.PostgreSQLLogSequenceNumber;
import org.postgresql.replication.LogSequenceNumber;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Ingest position manager for PostgreSQL.
 */
public final class PostgreSQLIngestPositionManager implements DialectIngestPositionManager {
    
    private final PostgreSQLIngestPositionCreator positionCreator = new PostgreSQLIngestPositionCreator("test_decoding");
    
    @Override
    public WALPosition init(final String data) {
        return new WALPosition(new PostgreSQLLogSequenceNumber(LogSequenceNumber.valueOf(data)));
    }
    
    @Override
    public WALPosition init(final DataSource dataSource, final String slotNameSuffix) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return positionCreator.create(connection, slotNameSuffix, getLogSequenceNumberSQL(connection.getMetaData()), lsn -> new PostgreSQLLogSequenceNumber((LogSequenceNumber) lsn));
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
            positionCreator.dropSlotIfExisted(connection, PostgreSQLSlotNameGenerator.getUniqueSlotName(connection, slotNameSuffix));
        }
    }
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }
}
