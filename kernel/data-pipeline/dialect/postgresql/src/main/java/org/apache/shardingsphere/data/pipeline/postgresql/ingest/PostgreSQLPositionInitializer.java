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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineInternalException;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.WALPosition;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.decode.PostgreSQLLogSequenceNumber;
import org.apache.shardingsphere.data.pipeline.spi.ingest.position.PositionInitializer;
import org.postgresql.replication.LogSequenceNumber;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * PostgreSQL WAL position initializer.
 */
@Slf4j
public final class PostgreSQLPositionInitializer implements PositionInitializer {
    
    private static final String SLOT_NAME_PREFIX = "pipeline";
    
    private static final String DECODE_PLUGIN = "test_decoding";
    
    private static final String DUPLICATE_OBJECT_ERROR_CODE = "42710";
    
    @Override
    public WALPosition init(final DataSource dataSource, final String slotNameSuffix) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            createSlotIfNotExist(connection, getUniqueSlotName(connection, slotNameSuffix));
            return getWalPosition(connection);
        }
    }
    
    @Override
    public WALPosition init(final String data) {
        return new WALPosition(new PostgreSQLLogSequenceNumber(LogSequenceNumber.valueOf(Long.parseLong(data))));
    }
    
    private void createSlotIfNotExist(final Connection connection, final String slotName) throws SQLException {
        if (isSlotExisting(connection, slotName)) {
            log.info("createSlotIfNotExist, slot exist, slotName={}", slotName);
            return;
        }
        String createSlotSQL = String.format("SELECT * FROM pg_create_logical_replication_slot('%s', '%s')", slotName, DECODE_PLUGIN);
        try (PreparedStatement preparedStatement = connection.prepareStatement(createSlotSQL)) {
            preparedStatement.execute();
        } catch (final SQLException ex) {
            if (!DUPLICATE_OBJECT_ERROR_CODE.equals(ex.getSQLState())) {
                throw ex;
            }
        }
    }
    
    private boolean isSlotExisting(final Connection connection, final String slotName) throws SQLException {
        String checkSlotSQL = "SELECT slot_name FROM pg_replication_slots WHERE slot_name=? AND plugin=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(checkSlotSQL)) {
            preparedStatement.setString(1, slotName);
            preparedStatement.setString(2, DECODE_PLUGIN);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }
    
    private WALPosition getWalPosition(final Connection connection) throws SQLException {
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(getLogSequenceNumberSQL(connection));
                ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            return new WALPosition(new PostgreSQLLogSequenceNumber(LogSequenceNumber.valueOf(resultSet.getString(1))));
        }
    }
    
    private String getLogSequenceNumberSQL(final Connection connection) throws SQLException {
        if (9 == connection.getMetaData().getDatabaseMajorVersion() && 6 <= connection.getMetaData().getDatabaseMinorVersion()) {
            return "SELECT PG_CURRENT_XLOG_LOCATION()";
        }
        if (10 <= connection.getMetaData().getDatabaseMajorVersion()) {
            return "SELECT PG_CURRENT_WAL_LSN()";
        }
        throw new PipelineInternalException("Unsupported PostgreSQL version: " + connection.getMetaData().getDatabaseProductVersion());
    }
    
    @Override
    public void destroy(final DataSource dataSource, final String slotNameSuffix) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            dropSlotIfExist(connection, slotNameSuffix);
        }
    }
    
    private void dropSlotIfExist(final Connection connection, final String slotNameSuffix) throws SQLException {
        String slotName = getUniqueSlotName(connection, slotNameSuffix);
        if (!isSlotExisting(connection, slotName)) {
            log.info("dropSlotIfExist, slot not exist, slotName={}", slotName);
            return;
        }
        log.info("dropSlotIfExist, slot exist, slotName={}", slotName);
        String dropSlotSQL = "SELECT pg_drop_replication_slot(?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(dropSlotSQL)) {
            preparedStatement.setString(1, slotName);
            preparedStatement.execute();
        }
    }
    
    /**
     * Get the unique slot name by connection.
     *
     * @param connection the connection
     * @param slotNameSuffix slot name suffix
     * @return the unique name by connection
     * @throws SQLException failed when getCatalog
     */
    public static String getUniqueSlotName(final Connection connection, final String slotNameSuffix) throws SQLException {
        // PostgreSQL slot name maximum length can't exceed 64,automatic truncation when the length exceeds the limit
        String slotName = DigestUtils.md5Hex(String.join("_", connection.getCatalog(), slotNameSuffix).getBytes());
        return String.format("%s_%s", SLOT_NAME_PREFIX, slotName);
    }
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }
}
