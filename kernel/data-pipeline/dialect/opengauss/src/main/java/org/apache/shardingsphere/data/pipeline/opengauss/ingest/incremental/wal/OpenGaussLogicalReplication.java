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

package org.apache.shardingsphere.data.pipeline.opengauss.ingest.incremental.wal;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.type.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.decode.BaseLogSequenceNumber;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionProperties;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionPropertiesParser;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.opengauss.PGProperty;
import org.opengauss.jdbc.PgConnection;
import org.opengauss.replication.LogSequenceNumber;
import org.opengauss.replication.PGReplicationStream;
import org.opengauss.replication.fluent.logical.ChainedLogicalStreamBuilder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Logical replication for openGauss.
 */
@Slf4j
public final class OpenGaussLogicalReplication {
    
    private static final String HA_PORT_ERROR_MESSAGE_KEY = "HA port";
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "openGauss");
    
    /**
     * Create connection.
     *
     * @param pipelineDataSourceConfig pipeline data source configuration
     * @return connection
     * @throws SQLException SQL exception
     */
    public Connection createConnection(final StandardPipelineDataSourceConfiguration pipelineDataSourceConfig) throws SQLException {
        Properties props = new Properties();
        PGProperty.USER.set(props, pipelineDataSourceConfig.getUsername());
        PGProperty.PASSWORD.set(props, pipelineDataSourceConfig.getPassword());
        PGProperty.ASSUME_MIN_SERVER_VERSION.set(props, "9.4");
        PGProperty.REPLICATION.set(props, "database");
        PGProperty.PREFER_QUERY_MODE.set(props, "simple");
        try {
            return DriverManager.getConnection(pipelineDataSourceConfig.getUrl(), props);
        } catch (final SQLException ex) {
            if (failedBecauseOfNonHAPort(ex)) {
                log.info("Failed to connect to openGauss caused by: {} - {}. Try connecting to HA port.", ex.getSQLState(), ex.getMessage());
                return tryConnectingToHAPort(pipelineDataSourceConfig.getUrl(), props);
            }
            throw ex;
        }
    }
    
    private boolean failedBecauseOfNonHAPort(final SQLException ex) {
        return ex.getMessage().contains(HA_PORT_ERROR_MESSAGE_KEY);
    }
    
    private Connection tryConnectingToHAPort(final String jdbcUrl, final Properties props) throws SQLException {
        ConnectionProperties connectionProps = DatabaseTypedSPILoader.getService(ConnectionPropertiesParser.class, databaseType).parse(jdbcUrl, null, null);
        PGProperty.PG_HOST.set(props, connectionProps.getHostname());
        PGProperty.PG_DBNAME.set(props, connectionProps.getCatalog());
        PGProperty.PG_PORT.set(props, connectionProps.getPort() + 1);
        return DriverManager.getConnection(databaseType.getJdbcUrlPrefixes().iterator().next(), props);
    }
    
    /**
     * Create OpenGauss replication stream.
     *
     * @param connection connection
     * @param startPosition start position
     * @param slotName slot name
     * @param majorVersion version
     * @return replication stream
     * @throws SQLException SQL exception
     */
    public PGReplicationStream createReplicationStream(final PgConnection connection, final BaseLogSequenceNumber startPosition, final String slotName, final int majorVersion) throws SQLException {
        ChainedLogicalStreamBuilder logicalStreamBuilder = connection.getReplicationAPI()
                .replicationStream()
                .logical()
                .withSlotName(slotName)
                .withSlotOption("include-xids", true)
                .withSlotOption("skip-empty-xacts", true)
                .withStartPosition((LogSequenceNumber) startPosition.get());
        if (majorVersion < 3) {
            return logicalStreamBuilder.start();
        }
        return logicalStreamBuilder.withSlotOption("parallel-decode-num", 10).withSlotOption("decode-style", "j").withSlotOption("sending-batch", 0).start();
    }
}
