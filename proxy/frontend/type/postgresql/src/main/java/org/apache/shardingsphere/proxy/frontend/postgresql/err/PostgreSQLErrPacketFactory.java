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
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLMessageSeverityLevel;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLErrorResponsePacket;
import org.apache.shardingsphere.dialect.SQLExceptionTransformEngine;
import org.apache.shardingsphere.dialect.exception.SQLDialectException;
import org.apache.shardingsphere.dialect.postgresql.exception.PostgreSQLException;
import org.apache.shardingsphere.dialect.postgresql.vendor.PostgreSQLVendorError;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.util.exception.external.sql.ShardingSphereSQLException;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;

import java.sql.SQLException;

/**
 * ERR packet factory for PostgreSQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLErrPacketFactory {
    
    /**
     * Create new instance of PostgreSQL ERR packet.
     * 
     * @param cause cause
     * @return created instance
     */
    @SuppressWarnings("ConstantConditions")
    public static PostgreSQLErrorResponsePacket newInstance(final Exception cause) {
        if (cause instanceof PSQLException && null != ((PSQLException) cause).getServerErrorMessage()) {
            return createErrorResponsePacket(((PSQLException) cause).getServerErrorMessage());
        }
        if (cause instanceof SQLException || cause instanceof ShardingSphereSQLException || cause instanceof SQLDialectException) {
            return createErrorResponsePacket(SQLExceptionTransformEngine.toSQLException(cause, TypedSPILoader.getService(DatabaseType.class, "PostgreSQL")));
        }
        // TODO PostgreSQL need consider FrontendConnectionLimitException
        return createErrorResponsePacketForUnknownException(cause);
    }
    
    private static PostgreSQLErrorResponsePacket createErrorResponsePacket(final PostgreSQLException.ServerErrorMessage serverErrorMessage) {
        return PostgreSQLErrorResponsePacket.newBuilder(serverErrorMessage.getSeverity(), serverErrorMessage.getSqlState(), serverErrorMessage.getMessage())
                .build();
    }
    
    private static PostgreSQLErrorResponsePacket createErrorResponsePacket(final ServerErrorMessage serverErrorMessage) {
        return PostgreSQLErrorResponsePacket.newBuilder(serverErrorMessage.getSeverity(), serverErrorMessage.getSQLState(), serverErrorMessage.getMessage())
                .detail(serverErrorMessage.getDetail()).hint(serverErrorMessage.getHint()).position(serverErrorMessage.getPosition())
                .internalQueryAndInternalPosition(serverErrorMessage.getInternalQuery(), serverErrorMessage.getInternalPosition()).where(serverErrorMessage.getWhere())
                .schemaName(serverErrorMessage.getSchema()).tableName(serverErrorMessage.getTable()).columnName(serverErrorMessage.getColumn()).dataTypeName(serverErrorMessage.getDatatype())
                .constraintName(serverErrorMessage.getConstraint()).file(serverErrorMessage.getFile()).line(serverErrorMessage.getLine()).routine(serverErrorMessage.getRoutine()).build();
    }
    
    @SuppressWarnings("ConstantConditions")
    private static PostgreSQLErrorResponsePacket createErrorResponsePacket(final SQLException cause) {
        if (cause instanceof PostgreSQLException && null != ((PostgreSQLException) cause).getServerErrorMessage()) {
            return createErrorResponsePacket(((PostgreSQLException) cause).getServerErrorMessage());
        }
        if (cause instanceof PSQLException && null != ((PSQLException) cause).getServerErrorMessage()) {
            return createErrorResponsePacket(((PSQLException) cause).getServerErrorMessage());
        }
        String sqlState = Strings.isNullOrEmpty(cause.getSQLState()) ? PostgreSQLVendorError.SYSTEM_ERROR.getSqlState().getValue() : cause.getSQLState();
        String message = Strings.isNullOrEmpty(cause.getMessage()) ? cause.toString() : cause.getMessage();
        return PostgreSQLErrorResponsePacket.newBuilder(PostgreSQLMessageSeverityLevel.ERROR, sqlState, message).build();
    }
    
    private static PostgreSQLErrorResponsePacket createErrorResponsePacketForUnknownException(final Exception cause) {
        // TODO add FIELD_TYPE_CODE for common error and consider what severity to use
        String message = Strings.isNullOrEmpty(cause.getLocalizedMessage()) ? cause.toString() : cause.getLocalizedMessage();
        return PostgreSQLErrorResponsePacket.newBuilder(PostgreSQLMessageSeverityLevel.ERROR, PostgreSQLVendorError.SYSTEM_ERROR, message).build();
    }
}
