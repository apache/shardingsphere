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

package org.apache.shardingsphere.data.pipeline.opengauss.ingest;

import org.apache.shardingsphere.data.pipeline.core.ingest.position.DialectIngestPositionManager;
import org.apache.shardingsphere.data.pipeline.opengauss.ingest.wal.decode.OpenGaussLogSequenceNumber;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.PostgreSQLIngestPositionCreator;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.PostgreSQLSlotNameGenerator;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.WALPosition;
import org.opengauss.replication.LogSequenceNumber;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Ingest position manager for openGauss.
 */
public final class OpenGaussIngestPositionManager implements DialectIngestPositionManager {
    
    private final PostgreSQLIngestPositionCreator positionCreator = new PostgreSQLIngestPositionCreator("mppdb_decoding");
    
    @Override
    public WALPosition init(final String data) {
        return new WALPosition(new OpenGaussLogSequenceNumber(LogSequenceNumber.valueOf(data)));
    }
    
    @Override
    public WALPosition init(final DataSource dataSource, final String slotNameSuffix) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return positionCreator.create(connection, slotNameSuffix, "SELECT PG_CURRENT_XLOG_LOCATION()", lsn -> new OpenGaussLogSequenceNumber((LogSequenceNumber) lsn));
        }
    }
    
    @Override
    public void destroy(final DataSource dataSource, final String slotNameSuffix) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            positionCreator.dropSlotIfExisted(connection, PostgreSQLSlotNameGenerator.getUniqueSlotName(connection, slotNameSuffix));
        }
    }
    
    @Override
    public String getDatabaseType() {
        return "openGauss";
    }
}
