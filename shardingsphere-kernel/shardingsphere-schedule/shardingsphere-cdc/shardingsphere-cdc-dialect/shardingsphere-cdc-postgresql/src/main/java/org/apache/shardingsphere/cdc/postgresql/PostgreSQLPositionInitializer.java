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

package org.apache.shardingsphere.cdc.postgresql;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.cdc.core.position.PositionInitializer;
import org.apache.shardingsphere.cdc.postgresql.wal.WalPosition;
import org.apache.shardingsphere.cdc.postgresql.wal.decode.PostgreSQLLogSequenceNumber;
import org.postgresql.replication.LogSequenceNumber;
import org.postgresql.util.PSQLException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * PostgreSQL wal position initializer.
 */
@Slf4j
public final class PostgreSQLPositionInitializer implements PositionInitializer {
    
    public static final String SLOT_NAME = "sharding_scaling";
    
    public static final String DECODE_PLUGIN = "test_decoding";
    
    public static final String DUPLICATE_OBJECT_ERROR_CODE = "42710";
    
    @Override
    public WalPosition init(final DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            createIfNotExists(connection);
            return getWalPosition(connection);
        }
    }
    
    @Override
    public WalPosition init(final String data) {
        return new WalPosition(new PostgreSQLLogSequenceNumber(LogSequenceNumber.valueOf(Long.parseLong(data))));
    }
    
    private void createIfNotExists(final Connection connection) throws SQLException {
        if (checkSlotExistsOrNot(connection)) {
            log.info("replication slot already exist, slot name: {}", SLOT_NAME);
            return;
        }
        try (PreparedStatement ps = connection.prepareStatement(String.format("SELECT * FROM pg_create_logical_replication_slot('%s', '%s')", SLOT_NAME, DECODE_PLUGIN))) {
            ps.execute();
        } catch (final PSQLException ex) {
            if (!DUPLICATE_OBJECT_ERROR_CODE.equals(ex.getSQLState())) {
                throw ex;
            }
        }
    }
    
    private boolean checkSlotExistsOrNot(final Connection connection) throws SQLException {
        String checkSlotSQL = "SELECT slot_name FROM pg_replication_slots WHERE slot_name=? AND plugin=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(checkSlotSQL)) {
            preparedStatement.setString(1, SLOT_NAME);
            preparedStatement.setString(2, DECODE_PLUGIN);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }
    
    private WalPosition getWalPosition(final Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(getSql(connection));
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return new WalPosition(new PostgreSQLLogSequenceNumber(LogSequenceNumber.valueOf(rs.getString(1))));
        }
    }
    
    private String getSql(final Connection connection) throws SQLException {
        if (9 == connection.getMetaData().getDatabaseMajorVersion() && 6 <= connection.getMetaData().getDatabaseMinorVersion()) {
            return "SELECT PG_CURRENT_XLOG_LOCATION()";
        }
        if (10 <= connection.getMetaData().getDatabaseMajorVersion()) {
            return "SELECT PG_CURRENT_WAL_LSN()";
        }
        throw new RuntimeException("Not support PostgreSQL version:" + connection.getMetaData().getDatabaseProductVersion());
    }
    
    @Override
    public void destroy(final DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            dropSlotIfExists(connection);
        }
    }
    
    private void dropSlotIfExists(final Connection connection) throws SQLException {
        if (!checkSlotExistsOrNot(connection)) {
            log.info("drop, slot not exist, slot name: {}", SLOT_NAME);
            return;
        }
        log.info("drop, slot exist, slot name: {}", SLOT_NAME);
        String dropSlotSQL = "SELECT pg_drop_replication_slot(?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(dropSlotSQL)) {
            preparedStatement.setString(1, SLOT_NAME);
            preparedStatement.execute();
        }
    }
}
