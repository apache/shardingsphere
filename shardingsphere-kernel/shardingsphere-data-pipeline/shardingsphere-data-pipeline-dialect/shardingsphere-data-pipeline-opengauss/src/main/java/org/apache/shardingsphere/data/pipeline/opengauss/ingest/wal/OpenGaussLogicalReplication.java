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

import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.decode.BaseLogSequenceNumber;
import org.apache.shardingsphere.infra.config.datasource.typed.StandardJDBCDataSourceConfiguration;
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
 * OpenGauss logical replication.
 */
public final class OpenGaussLogicalReplication {

    public static final String SLOT_NAME_PREFIX = "sharding_scaling";

    public static final String DECODE_PLUGIN = "mppdb_decoding";

    public static final String DUPLICATE_OBJECT_ERROR_CODE = "42710";

    /**
     * Create OpenGauss connection.
     *
     * @param jdbcDataSourceConfig JDBC data source configuration
     * @return OpenGauss connection
     * @throws SQLException SQL exception
     */
    public Connection createPgConnection(final StandardJDBCDataSourceConfiguration jdbcDataSourceConfig) throws SQLException {
        return createConnection(jdbcDataSourceConfig);
    }
    
    private Connection createConnection(final StandardJDBCDataSourceConfiguration jdbcDataSourceConfig) throws SQLException {
        Properties props = new Properties();
        PGProperty.USER.set(props, jdbcDataSourceConfig.getHikariConfig().getUsername());
        PGProperty.PASSWORD.set(props, jdbcDataSourceConfig.getHikariConfig().getPassword());
        PGProperty.ASSUME_MIN_SERVER_VERSION.set(props, "9.4");
        PGProperty.REPLICATION.set(props, "database");
        PGProperty.PREFER_QUERY_MODE.set(props, "simple");
        return DriverManager.getConnection(jdbcDataSourceConfig.getHikariConfig().getJdbcUrl(), props);
    }
    
    /**
     * Create OpenGauss replication stream.
     *
     * @param pgConnection OpenGauss connection
     * @param startPosition start position
     * @param slotName the setted slotName
     * @return replication stream
     * @throws SQLException SQL exception
     */
    public PGReplicationStream createReplicationStream(final PgConnection pgConnection, final BaseLogSequenceNumber startPosition, final String slotName) throws SQLException {
        return pgConnection.getReplicationAPI()
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
     * @param conn the datasource connection
     * @throws SQLException the sql exp
     */
    public static void createIfNotExists(final Connection conn) throws SQLException {
        if (isSlotNameExist(conn)) {
            return;
        }
        createSlotBySql(conn);
    }
    
    /**
     * Drop replication slot by connection.
     *
     * @param conn the database connection
     * @throws SQLException drop sql with error
     */
    public static void dropSlot(final Connection conn) throws SQLException {
        String sql = String.format("select * from pg_drop_replication_slot('%s')", getUniqueSlotName(conn));
        try (CallableStatement cs = conn.prepareCall(sql)) {
            cs.execute();
        }
    }

    private static boolean isSlotNameExist(final Connection conn) throws SQLException {
        String sql = "select * from pg_replication_slots where slot_name=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, getUniqueSlotName(conn));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void createSlotBySql(final Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                String.format("SELECT * FROM pg_create_logical_replication_slot('%s', '%s')",
                        getUniqueSlotName(connection),
                        DECODE_PLUGIN))) {
            ps.execute();
        } catch (final PSQLException ex) {
            if (!DUPLICATE_OBJECT_ERROR_CODE.equals(ex.getSQLState())) {
                throw ex;
            }
        }
    }
    
    /**
     * Get the unique slot name by connection.
     *
     * @param conn the connection
     * @return the unique name by connection
     * @throws SQLException failed when getCatalog
     */
    public static String getUniqueSlotName(final Connection conn) throws SQLException {
        return String.format("%s_%s", SLOT_NAME_PREFIX, conn.getCatalog());
    }
}
