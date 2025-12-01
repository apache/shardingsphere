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

package org.apache.shardingsphere.proxy.frontend.postgresql.err;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.SQLExceptionTransformEngine;
import org.apache.shardingsphere.database.exception.postgresql.exception.PostgreSQLException;
import org.apache.shardingsphere.database.exception.postgresql.vendor.PostgreSQLVendorError;
import org.apache.shardingsphere.database.protocol.postgresql.constant.PostgreSQLMessageSeverityLevel;
import org.apache.shardingsphere.database.protocol.postgresql.packet.generic.PostgreSQLErrorResponsePacket;
import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.XOpenSQLState;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Error packet factory for PostgreSQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLErrorPacketFactory {
    
    private static final DatabaseType DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    /**
     * Create new instance of PostgreSQL error packet.
     *
     * @param cause cause
     * @return created instance
     */
    public static PostgreSQLErrorResponsePacket newInstance(final Exception cause) {
        Optional<ServerErrorMessage> serverErrorMessage = findServerErrorMessage(cause);
        return serverErrorMessage.map(PostgreSQLErrorPacketFactory::createErrorResponsePacket)
                .orElseGet(() -> createErrorResponsePacket(SQLExceptionTransformEngine.toSQLException(cause, DATABASE_TYPE)));
    }
    
    private static Optional<ServerErrorMessage> findServerErrorMessage(final Exception cause) {
        return cause instanceof PSQLException ? Optional.ofNullable(((PSQLException) cause).getServerErrorMessage()) : Optional.empty();
    }
    
    private static PostgreSQLErrorResponsePacket createErrorResponsePacket(final ServerErrorMessage serverErrorMessage) {
        return PostgreSQLErrorResponsePacket.newBuilder(serverErrorMessage.getSeverity(), serverErrorMessage.getSQLState(), serverErrorMessage.getMessage())
                .detail(serverErrorMessage.getDetail()).hint(serverErrorMessage.getHint()).position(serverErrorMessage.getPosition())
                .internalQueryAndInternalPosition(serverErrorMessage.getInternalQuery(), serverErrorMessage.getInternalPosition()).where(serverErrorMessage.getWhere())
                .schemaName(serverErrorMessage.getSchema()).tableName(serverErrorMessage.getTable()).columnName(serverErrorMessage.getColumn()).dataTypeName(serverErrorMessage.getDatatype())
                .constraintName(serverErrorMessage.getConstraint()).file(serverErrorMessage.getFile()).line(serverErrorMessage.getLine()).routine(serverErrorMessage.getRoutine()).build();
    }
    
    private static PostgreSQLErrorResponsePacket createErrorResponsePacket(final SQLException cause) {
        if (cause instanceof PostgreSQLException && null != ((PostgreSQLException) cause).getServerErrorMessage()) {
            return createErrorResponsePacket(((PostgreSQLException) cause).getServerErrorMessage());
        }
        if (cause instanceof PSQLException && null != ((PSQLException) cause).getServerErrorMessage()) {
            return createErrorResponsePacket(((PSQLException) cause).getServerErrorMessage());
        }
        String sqlState = Strings.isNullOrEmpty(cause.getSQLState()) || XOpenSQLState.GENERAL_ERROR.getValue().equals(cause.getSQLState())
                ? PostgreSQLVendorError.SYSTEM_ERROR.getSqlState().getValue()
                : cause.getSQLState();
        String message = Strings.isNullOrEmpty(cause.getMessage()) ? cause.toString() : cause.getMessage();
        return PostgreSQLErrorResponsePacket.newBuilder(PostgreSQLMessageSeverityLevel.ERROR, sqlState, message).build();
    }
    
    private static PostgreSQLErrorResponsePacket createErrorResponsePacket(final PostgreSQLException.ServerErrorMessage serverErrorMessage) {
        return PostgreSQLErrorResponsePacket.newBuilder(serverErrorMessage.getSeverity(), serverErrorMessage.getSqlState(), serverErrorMessage.getMessage()).build();
    }
}
