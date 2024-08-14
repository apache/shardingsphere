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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.pojo.ReplicationSlotInfo;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.WALPosition;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.decode.BaseLogSequenceNumber;
import org.postgresql.replication.LogSequenceNumber;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Function;

/**
 * PostgreSQL Ingest position creator.
 */
@RequiredArgsConstructor
public final class PostgreSQLIngestPositionCreator {
    
    private static final String DUPLICATE_OBJECT_ERROR_CODE = "42710";
    
    private final String decodePlugin;
    
    /**
     * Create WAL position.
     * 
     * @param connection connection
     * @param slotNameSuffix slot name suffix
     * @param logSequenceNumberFetchSQL log sequence number fetch SQL
     * @param logSequenceNumberCreator log sequence number creator
     * @return WAL position
     */
    public WALPosition create(final Connection connection, final String slotNameSuffix, final String logSequenceNumberFetchSQL,
                              final Function<Object, BaseLogSequenceNumber> logSequenceNumberCreator) throws SQLException {
        createSlotIfNotExist(connection, PostgreSQLSlotNameGenerator.getUniqueSlotName(connection, slotNameSuffix));
        return getWALPosition(connection, logSequenceNumberFetchSQL, logSequenceNumberCreator);
    }
    
    private void createSlotIfNotExist(final Connection connection, final String slotName) throws SQLException {
        Optional<ReplicationSlotInfo> slotInfo = getSlotInfo(connection, slotName);
        if (!slotInfo.isPresent()) {
            createSlot(connection, slotName);
            return;
        }
        if (null == slotInfo.get().getDatabaseName()) {
            dropSlotIfExisted(connection, slotName);
            createSlot(connection, slotName);
        }
    }
    
    private Optional<ReplicationSlotInfo> getSlotInfo(final Connection connection, final String slotName) throws SQLException {
        String sql = "SELECT slot_name, database FROM pg_replication_slots WHERE slot_name=? AND plugin=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, slotName);
            preparedStatement.setString(2, decodePlugin);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() ? Optional.of(new ReplicationSlotInfo(resultSet.getString(1), resultSet.getString(2))) : Optional.empty();
            }
        }
    }
    
    private void createSlot(final Connection connection, final String slotName) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM pg_create_logical_replication_slot(?, ?)")) {
            preparedStatement.setString(1, slotName);
            preparedStatement.setString(2, decodePlugin);
            preparedStatement.execute();
        } catch (final SQLException ex) {
            if (!DUPLICATE_OBJECT_ERROR_CODE.equals(ex.getSQLState())) {
                throw ex;
            }
        }
    }
    
    private WALPosition getWALPosition(final Connection connection, final String logSequenceNumberSQL,
                                       final Function<Object, BaseLogSequenceNumber> logSequenceNumberCreator) throws SQLException {
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(logSequenceNumberSQL);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            return new WALPosition(logSequenceNumberCreator.apply(LogSequenceNumber.valueOf(resultSet.getString(1))));
        }
    }
    
    /**
     * Drop slot if existed.
     *
     * @param connection connection
     * @param slotName slot name
     * @throws SQLException SQL exception
     */
    public void dropSlotIfExisted(final Connection connection, final String slotName) throws SQLException {
        if (!getSlotInfo(connection, slotName).isPresent()) {
            return;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT pg_drop_replication_slot(?)")) {
            preparedStatement.setString(1, slotName);
            preparedStatement.execute();
        }
    }
}
