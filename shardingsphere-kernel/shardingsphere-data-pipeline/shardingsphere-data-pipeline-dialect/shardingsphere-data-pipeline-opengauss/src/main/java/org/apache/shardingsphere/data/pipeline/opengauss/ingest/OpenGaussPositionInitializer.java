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

import org.apache.shardingsphere.data.pipeline.opengauss.ingest.wal.OpenGaussLogicalReplication;
import org.apache.shardingsphere.data.pipeline.opengauss.ingest.wal.decode.OpenGaussLogSequenceNumber;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.WalPosition;
import org.apache.shardingsphere.data.pipeline.spi.ingest.position.PositionInitializer;
import org.opengauss.replication.LogSequenceNumber;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * OpenGauss wal position initializer.
 */
public final class OpenGaussPositionInitializer implements PositionInitializer {
    
    @Override
    public WalPosition init(final DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return getWalPosition(connection);
        }
    }
    
    @Override
    public WalPosition init(final String data) {
        return new WalPosition(new OpenGaussLogSequenceNumber(LogSequenceNumber.valueOf(Long.parseLong(data)))
        );
    }

    private WalPosition getWalPosition(final Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(getSql());
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return new WalPosition(new OpenGaussLogSequenceNumber(LogSequenceNumber.valueOf(rs.getString(1))));
        }
    }
    
    private String getSql() {
        return "SELECT PG_CURRENT_XLOG_LOCATION()";
    }
    
    @Override
    public void destroy(final DataSource dataSource) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            OpenGaussLogicalReplication.dropSlot(conn);
        }
    }
}
