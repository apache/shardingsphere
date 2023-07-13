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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shardingsphere.data.pipeline.opengauss.ingest.wal.decode.OpenGaussLogSequenceNumber;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.pojo.ReplicationSlotInfo;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.WALPosition;
import org.apache.shardingsphere.data.pipeline.spi.ingest.position.PositionInitializer;
import org.opengauss.replication.LogSequenceNumber;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * OpenGauss WAL position initializer.
 */
// TODO reuse PostgreSQLPositionInitializer
@Slf4j
public final class OpenGaussPositionInitializer implements PositionInitializer {
    
    private static final String SLOT_NAME_PREFIX = "pipeline";
    
    private static final String DECODE_PLUGIN = "mppdb_decoding";
    
    private static final String DUPLICATE_OBJECT_ERROR_CODE = "42710";
    
    @Override
    public WALPosition init(final DataSource dataSource, final String slotNameSuffix) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            createSlotIfNotExist(connection, slotNameSuffix);
            return getWalPosition(connection);
        }
    }
    
    @Override
    public WALPosition init(final String data) {
        return new WALPosition(new OpenGaussLogSequenceNumber(LogSequenceNumber.valueOf(Long.parseLong(data))));
    }
    
    /**
     * Create logical replication slot if it does not exist.
     *
     * @param connection connection
     * @param slotNameSuffix slotName suffix
     * @throws SQLException SQL exception
     */
    private void createSlotIfNotExist(final Connection connection, final String slotNameSuffix) throws SQLException {
        String slotName = getUniqueSlotName(connection, slotNameSuffix);
        Optional<ReplicationSlotInfo> slotInfo = getSlotInfo(connection, slotName);
        if (!slotInfo.isPresent()) {
            createSlotBySQL(connection, slotName);
            return;
        }
        if (null == slotInfo.get().getDatabaseName()) {
            dropSlotIfExist(connection, slotName);
            createSlotBySQL(connection, slotName);
        }
    }
    
    private Optional<ReplicationSlotInfo> getSlotInfo(final Connection connection, final String slotName) throws SQLException {
        String sql = "SELECT slot_name, database FROM pg_replication_slots WHERE slot_name=? AND plugin=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, slotName);
            preparedStatement.setString(2, DECODE_PLUGIN);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(new ReplicationSlotInfo(resultSet.getString(1), resultSet.getString(2)));
            }
        }
    }
    
    private void createSlotBySQL(final Connection connection, final String slotName) throws SQLException {
        String sql = String.format("SELECT * FROM pg_create_logical_replication_slot('%s', '%s')", slotName, DECODE_PLUGIN);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.execute();
        } catch (final SQLException ex) {
            if (!DUPLICATE_OBJECT_ERROR_CODE.equals(ex.getSQLState())) {
                throw ex;
            }
        }
    }
    
    private WALPosition getWalPosition(final Connection connection) throws SQLException {
        try (
                PreparedStatement preparedStatement = connection.prepareStatement("SELECT PG_CURRENT_XLOG_LOCATION()");
                ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            return new WALPosition(new OpenGaussLogSequenceNumber(LogSequenceNumber.valueOf(resultSet.getString(1))));
        }
    }
    
    @Override
    public void destroy(final DataSource dataSource, final String slotNameSuffix) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            dropSlotIfExist(connection, getUniqueSlotName(connection, slotNameSuffix));
        }
    }
    
    private void dropSlotIfExist(final Connection connection, final String slotName) throws SQLException {
        if (!getSlotInfo(connection, slotName).isPresent()) {
            log.info("dropSlotIfExist, slot not exist, ignore, slotName={}", slotName);
            return;
        }
        String sql = String.format("select * from pg_drop_replication_slot('%s')", slotName);
        try (CallableStatement callableStatement = connection.prepareCall(sql)) {
            callableStatement.execute();
        }
    }
    
    /**
     * Get the unique slot name by connection.
     *
     * @param connection connection
     * @param slotNameSuffix slot name suffix
     * @return the unique name by connection
     * @throws SQLException failed when getCatalog
     */
    public static String getUniqueSlotName(final Connection connection, final String slotNameSuffix) throws SQLException {
        // same as PostgreSQL, but length over 64 will throw an exception directly
        String slotName = DigestUtils.md5Hex(String.join("_", connection.getCatalog(), slotNameSuffix).getBytes());
        return String.format("%s_%s", SLOT_NAME_PREFIX, slotName);
    }
    
    @Override
    public String getDatabaseType() {
        return "openGauss";
    }
}
