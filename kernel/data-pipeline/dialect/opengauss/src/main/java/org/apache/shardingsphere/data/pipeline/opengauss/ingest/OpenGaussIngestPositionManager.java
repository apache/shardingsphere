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
import org.apache.shardingsphere.data.pipeline.core.ingest.position.DialectIngestPositionManager;
import org.apache.shardingsphere.data.pipeline.opengauss.ingest.wal.decode.OpenGaussLogSequenceNumber;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.PostgreSQLSlotNameGenerator;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.pojo.ReplicationSlotInfo;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.WALPosition;
import org.opengauss.replication.LogSequenceNumber;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/**
 * Ingest position manager for openGauss.
 */
// TODO reuse PostgreSQLIngestPositionManager
@Slf4j
public final class OpenGaussIngestPositionManager implements DialectIngestPositionManager {
    
    private static final String DECODE_PLUGIN = "mppdb_decoding";
    
    private static final String DUPLICATE_OBJECT_ERROR_CODE = "42710";
    
    @Override
    public WALPosition init(final String data) {
        return new WALPosition(new OpenGaussLogSequenceNumber(LogSequenceNumber.valueOf(data)));
    }
    
    @Override
    public WALPosition init(final DataSource dataSource, final String slotNameSuffix) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            createSlotIfNotExist(connection, slotNameSuffix);
            return getWALPosition(connection);
        }
    }
    
    private void createSlotIfNotExist(final Connection connection, final String slotNameSuffix) throws SQLException {
        String slotName = PostgreSQLSlotNameGenerator.getUniqueSlotName(connection, slotNameSuffix);
        Optional<ReplicationSlotInfo> slotInfo = getSlotInfo(connection, slotName);
        if (!slotInfo.isPresent()) {
            createSlot(connection, slotName);
            return;
        }
        if (null == slotInfo.get().getDatabaseName()) {
            dropSlotIfExist(connection, slotName);
            createSlot(connection, slotName);
        }
    }
    
    private Optional<ReplicationSlotInfo> getSlotInfo(final Connection connection, final String slotName) throws SQLException {
        String sql = "SELECT slot_name, database FROM pg_replication_slots WHERE slot_name=? AND plugin=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, slotName);
            preparedStatement.setString(2, DECODE_PLUGIN);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() ? Optional.of(new ReplicationSlotInfo(resultSet.getString(1), resultSet.getString(2))) : Optional.empty();
            }
        }
    }
    
    private void createSlot(final Connection connection, final String slotName) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM pg_create_logical_replication_slot(?, ?)")) {
            preparedStatement.setString(1, slotName);
            preparedStatement.setString(2, DECODE_PLUGIN);
            preparedStatement.execute();
        } catch (final SQLException ex) {
            if (!DUPLICATE_OBJECT_ERROR_CODE.equals(ex.getSQLState())) {
                throw ex;
            }
        }
    }
    
    private void dropSlotIfExist(final Connection connection, final String slotName) throws SQLException {
        if (!getSlotInfo(connection, slotName).isPresent()) {
            log.info("dropSlotIfExist, slot not exist, ignore, slotName={}", slotName);
            return;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * from pg_drop_replication_slot(?)")) {
            preparedStatement.setString(1, slotName);
            preparedStatement.execute();
        }
    }
    
    private WALPosition getWALPosition(final Connection connection) throws SQLException {
        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT PG_CURRENT_XLOG_LOCATION()")) {
            resultSet.next();
            return new WALPosition(new OpenGaussLogSequenceNumber(LogSequenceNumber.valueOf(resultSet.getString(1))));
        }
    }
    
    @Override
    public void destroy(final DataSource dataSource, final String slotNameSuffix) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            dropSlotIfExist(connection, PostgreSQLSlotNameGenerator.getUniqueSlotName(connection, slotNameSuffix));
        }
    }
    
    @Override
    public String getDatabaseType() {
        return "openGauss";
    }
}
