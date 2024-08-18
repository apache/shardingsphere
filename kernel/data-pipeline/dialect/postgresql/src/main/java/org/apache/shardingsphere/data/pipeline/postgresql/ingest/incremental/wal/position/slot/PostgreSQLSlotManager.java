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

package org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.position.slot;

import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * PostgreSQL slot manager.
 */
@RequiredArgsConstructor
public final class PostgreSQLSlotManager {
    
    private static final String DUPLICATE_OBJECT_ERROR_CODE = "42710";
    
    private final String decodePlugin;
    
    /**
     * Create slot.
     *
     * @param connection connection
     * @param slotNameSuffix slot name suffix
     * @throws SQLException SQL exception
     */
    public void create(final Connection connection, final String slotNameSuffix) throws SQLException {
        String slotName = PostgreSQLSlotNameGenerator.getUniqueSlotName(connection, slotNameSuffix);
        Optional<PostgreSQLReplicationSlotInfo> slotInfo = load(connection, slotName);
        if (!slotInfo.isPresent()) {
            doCreate(connection, slotName);
            return;
        }
        if (null == slotInfo.get().getDatabaseName()) {
            doDrop(connection, slotName);
            doCreate(connection, slotName);
        }
    }
    
    private Optional<PostgreSQLReplicationSlotInfo> load(final Connection connection, final String slotName) throws SQLException {
        String sql = "SELECT slot_name, database FROM pg_replication_slots WHERE slot_name=? AND plugin=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, slotName);
            preparedStatement.setString(2, decodePlugin);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() ? Optional.of(new PostgreSQLReplicationSlotInfo(resultSet.getString(1), resultSet.getString(2))) : Optional.empty();
            }
        }
    }
    
    private void doCreate(final Connection connection, final String slotName) throws SQLException {
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
    
    /**
     * Drop slot if existed.
     *
     * @param connection connection
     * @param slotNameSuffix slot name suffix
     * @throws SQLException SQL exception
     */
    public void dropIfExisted(final Connection connection, final String slotNameSuffix) throws SQLException {
        String slotName = PostgreSQLSlotNameGenerator.getUniqueSlotName(connection, slotNameSuffix);
        if (!load(connection, slotName).isPresent()) {
            return;
        }
        doDrop(connection, slotName);
    }
    
    private void doDrop(final Connection connection, final String slotName) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT pg_drop_replication_slot(?)")) {
            preparedStatement.setString(1, slotName);
            preparedStatement.execute();
        }
    }
}
