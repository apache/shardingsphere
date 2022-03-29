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

package org.apache.shardingsphere.data.pipeline.opengauss.ingest.wal;

import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.yaml.YamlJdbcConfiguration;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.decode.BaseLogSequenceNumber;
import org.opengauss.PGProperty;
import org.opengauss.jdbc.PgConnection;
import org.opengauss.replication.LogSequenceNumber;
import org.opengauss.replication.PGReplicationStream;
import org.opengauss.util.PSQLException;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Logical replication for openGauss.
 */
public final class OpenGaussLogicalReplication {
    
    public static final String SLOT_NAME_PREFIX = "sharding_scaling";
    
    public static final String DECODE_PLUGIN = "mppdb_decoding";
    
    public static final String DUPLICATE_OBJECT_ERROR_CODE = "42710";
    
    /**
     * Create connection.
     *
     * @param pipelineDataSourceConfig pipeline data source configuration
     * @return connection
     * @throws SQLException SQL exception
     */
    public Connection createConnection(final StandardPipelineDataSourceConfiguration pipelineDataSourceConfig) throws SQLException {
        Properties props = new Properties();
        YamlJdbcConfiguration jdbcConfig = pipelineDataSourceConfig.getJdbcConfig();
        PGProperty.USER.set(props, jdbcConfig.getUsername());
        PGProperty.PASSWORD.set(props, jdbcConfig.getPassword());
        PGProperty.ASSUME_MIN_SERVER_VERSION.set(props, "9.4");
        PGProperty.REPLICATION.set(props, "database");
        PGProperty.PREFER_QUERY_MODE.set(props, "simple");
        return DriverManager.getConnection(jdbcConfig.getJdbcUrl(), props);
    }
    
    /**
     * Create OpenGauss replication stream.
     *
     * @param connection connection
     * @param startPosition start position
     * @param slotName slot name
     * @return replication stream
     * @throws SQLException SQL exception
     */
    public PGReplicationStream createReplicationStream(final PgConnection connection, final BaseLogSequenceNumber startPosition, final String slotName) throws SQLException {
        return connection.getReplicationAPI()
                .replicationStream()
                .logical()
                .withSlotName(slotName)
                .withSlotOption("include-xids", true)
                .withSlotOption("skip-empty-xacts", true)
                .withStartPosition((LogSequenceNumber) startPosition.get())
                .start();
    }
    
    /**
     * Create slots (drop existed slot before create).
     *
     * @param connection connection
     * @throws SQLException SQL exception
     */
    public static void createIfNotExists(final Connection connection) throws SQLException {
        if (!isSlotNameExist(connection)) {
            createSlotBySQL(connection);
        }
    }
    
    /**
     * Drop replication slot by connection.
     *
     * @param connection connection
     * @throws SQLException drop SQL with error
     */
    public static void dropSlot(final Connection connection) throws SQLException {
        String sql = String.format("select * from pg_drop_replication_slot('%s')", getUniqueSlotName(connection));
        try (CallableStatement callableStatement = connection.prepareCall(sql)) {
            callableStatement.execute();
        }
    }
    
    private static boolean isSlotNameExist(final Connection connection) throws SQLException {
        String sql = "select * from pg_replication_slots where slot_name=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, getUniqueSlotName(connection));
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }
    
    private static void createSlotBySQL(final Connection connection) throws SQLException {
        String sql = String.format("SELECT * FROM pg_create_logical_replication_slot('%s', '%s')", getUniqueSlotName(connection), DECODE_PLUGIN);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.execute();
        } catch (final PSQLException ex) {
            if (!DUPLICATE_OBJECT_ERROR_CODE.equals(ex.getSQLState())) {
                throw ex;
            }
        }
    }
    
    /**
     * Get the unique slot name by connection.
     *
     * @param connection connection
     * @return the unique name by connection
     * @throws SQLException failed when getCatalog
     */
    public static String getUniqueSlotName(final Connection connection) throws SQLException {
        return String.format("%s_%s", SLOT_NAME_PREFIX, connection.getCatalog());
    }
}
