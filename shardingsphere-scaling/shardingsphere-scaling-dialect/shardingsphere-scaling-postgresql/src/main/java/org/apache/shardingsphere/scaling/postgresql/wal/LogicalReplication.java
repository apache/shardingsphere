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

package org.apache.shardingsphere.scaling.postgresql.wal;

import org.apache.shardingsphere.scaling.core.config.datasource.StandardJDBCDataSourceConfiguration;
import org.postgresql.PGConnection;
import org.postgresql.PGProperty;
import org.postgresql.replication.LogSequenceNumber;
import org.postgresql.replication.PGReplicationStream;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * PostgreSQL logical replication.
 */
public final class LogicalReplication {
    
    /**
     * Create PostgreSQL connection.
     *
     * @param jdbcDataSourceConfig JDBC data source configuration
     * @return PostgreSQL connection
     * @throws SQLException sql exception
     */
    public Connection createPgConnection(final StandardJDBCDataSourceConfiguration jdbcDataSourceConfig) throws SQLException {
        return createConnection(jdbcDataSourceConfig);
    }
    
    private Connection createConnection(final StandardJDBCDataSourceConfiguration jdbcDataSourceConfig) throws SQLException {
        Properties props = new Properties();
        PGProperty.USER.set(props, jdbcDataSourceConfig.getUsername());
        PGProperty.PASSWORD.set(props, jdbcDataSourceConfig.getPassword());
        PGProperty.ASSUME_MIN_SERVER_VERSION.set(props, "9.6");
        PGProperty.REPLICATION.set(props, "database");
        PGProperty.PREFER_QUERY_MODE.set(props, "simple");
        return DriverManager.getConnection(jdbcDataSourceConfig.getJdbcUrl(), props);
    }
    
    /**
     * Create PostgreSQL replication stream.
     *
     * @param pgConnection PostgreSQL connection
     * @param slotName slot name
     * @param startPosition start position
     * @return replication stream
     * @throws SQLException sql exception
     */
    public PGReplicationStream createReplicationStream(final Connection pgConnection, final String slotName, final LogSequenceNumber startPosition) throws SQLException {
        return pgConnection.unwrap(PGConnection.class).getReplicationAPI()
                .replicationStream()
                .logical()
                .withStartPosition(startPosition)
                .withSlotName(slotName)
                .withSlotOption("include-xids", true)
                .withSlotOption("skip-empty-xacts", true)
                .start();
    }
}
