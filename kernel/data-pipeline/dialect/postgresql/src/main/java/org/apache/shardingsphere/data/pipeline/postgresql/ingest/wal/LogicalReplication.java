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

package org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal;

import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.yaml.YamlJdbcConfiguration;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.decode.BaseLogSequenceNumber;
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
// TODO add prefix PostgreSQL
public final class LogicalReplication {
    
    /**
     * Create connection.
     *
     * @param pipelineDataSourceConfig pipeline data source configuration
     * @return PostgreSQL connection
     * @throws SQLException SQL exception
     */
    public Connection createConnection(final StandardPipelineDataSourceConfiguration pipelineDataSourceConfig) throws SQLException {
        Properties props = new Properties();
        YamlJdbcConfiguration jdbcConfig = pipelineDataSourceConfig.getJdbcConfig();
        PGProperty.USER.set(props, jdbcConfig.getUsername());
        PGProperty.PASSWORD.set(props, jdbcConfig.getPassword());
        PGProperty.ASSUME_MIN_SERVER_VERSION.set(props, "9.6");
        PGProperty.REPLICATION.set(props, "database");
        PGProperty.PREFER_QUERY_MODE.set(props, "simple");
        return DriverManager.getConnection(jdbcConfig.getUrl(), props);
    }
    
    /**
     * Create PostgreSQL replication stream.
     *
     * @param connection connection
     * @param slotName slot name
     * @param startPosition start position
     * @return replication stream
     * @throws SQLException SQL exception
     */
    public PGReplicationStream createReplicationStream(final Connection connection, final String slotName, final BaseLogSequenceNumber startPosition) throws SQLException {
        return connection.unwrap(PGConnection.class).getReplicationAPI()
                .replicationStream()
                .logical()
                .withStartPosition((LogSequenceNumber) startPosition.get())
                .withSlotName(slotName)
                .withSlotOption("include-xids", true)
                .withSlotOption("skip-empty-xacts", true)
                .start();
    }
}
