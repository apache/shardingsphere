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
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLErrorCode;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLMessageSeverityLevel;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLErrorResponsePacket;
import org.apache.shardingsphere.infra.exception.InsertColumnsAndValuesMismatchedException;
import org.apache.shardingsphere.proxy.backend.exception.DBCreateExistsException;
import org.apache.shardingsphere.proxy.backend.exception.InTransactionException;
import org.apache.shardingsphere.proxy.backend.exception.UnsupportedUpdateOperationException;
import org.apache.shardingsphere.proxy.frontend.postgresql.authentication.exception.InvalidAuthorizationSpecificationException;
import org.apache.shardingsphere.proxy.frontend.postgresql.authentication.exception.PostgreSQLAuthenticationException;
import org.apache.shardingsphere.proxy.frontend.postgresql.authentication.exception.PostgreSQLProtocolViolationException;
import org.apache.shardingsphere.proxy.backend.exception.InvalidParameterValueException;
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
    public static PostgreSQLErrorResponsePacket newInstance(final Exception cause) {
        if (cause instanceof PSQLException && null != ((PSQLException) cause).getServerErrorMessage()) {
            return createErrorResponsePacket(((PSQLException) cause).getServerErrorMessage());
        }
        if (cause instanceof InTransactionException) {
            return PostgreSQLErrorResponsePacket.newBuilder(PostgreSQLMessageSeverityLevel.WARNING, PostgreSQLErrorCode.WARNING.getErrorCode(), cause.getMessage()).build();
        }
        if (cause instanceof SQLException) {
            return createErrorResponsePacket((SQLException) cause);
        }
        if (cause instanceof InsertColumnsAndValuesMismatchedException) {
            return PostgreSQLErrorResponsePacket.newBuilder(PostgreSQLMessageSeverityLevel.ERROR, PostgreSQLErrorCode.SYNTAX_ERROR, cause.getMessage()).build();
        }
        if (cause instanceof InvalidAuthorizationSpecificationException) {
            return PostgreSQLErrorResponsePacket.newBuilder(PostgreSQLMessageSeverityLevel.FATAL, PostgreSQLErrorCode.INVALID_AUTHORIZATION_SPECIFICATION, cause.getMessage()).build();
        }
        if (cause instanceof PostgreSQLProtocolViolationException) {
            return PostgreSQLErrorResponsePacket.newBuilder(PostgreSQLMessageSeverityLevel.FATAL, PostgreSQLErrorCode.PROTOCOL_VIOLATION,
                    String.format("expected %s response, got message type %s", ((PostgreSQLProtocolViolationException) cause).getExpectedMessageType(),
                            ((PostgreSQLProtocolViolationException) cause).getActualMessageType()))
                    .build();
        }
        if (cause instanceof PostgreSQLAuthenticationException) {
            return PostgreSQLErrorResponsePacket.newBuilder(PostgreSQLMessageSeverityLevel.FATAL, ((PostgreSQLAuthenticationException) cause).getErrorCode(), cause.getMessage()).build();
        }
        if (cause instanceof InvalidParameterValueException) {
            InvalidParameterValueException ex = (InvalidParameterValueException) cause;
            String message = String.format("invalid value for parameter \"%s\": \"%s\"", ex.getParameterName(), ex.getParameterValue());
            return PostgreSQLErrorResponsePacket.newBuilder(PostgreSQLMessageSeverityLevel.ERROR, PostgreSQLErrorCode.INVALID_PARAMETER_VALUE, message).build();
        }
        if (cause instanceof DBCreateExistsException) {
            return PostgreSQLErrorResponsePacket.newBuilder(PostgreSQLMessageSeverityLevel.ERROR, PostgreSQLErrorCode.DUPLICATE_DATABASE,
                    String.format(PostgreSQLErrorCode.DUPLICATE_DATABASE.getConditionName(), ((DBCreateExistsException) cause).getDatabaseName())).build();
        }
        if (cause instanceof UnsupportedUpdateOperationException) {
            UnsupportedUpdateOperationException exception = (UnsupportedUpdateOperationException) cause;
            return PostgreSQLErrorResponsePacket.newBuilder(PostgreSQLMessageSeverityLevel.ERROR, PostgreSQLErrorCode.MODIFYING_SQL_DATA_NOT_PERMITTED, exception.getErrorMessage()).build();
        }
        // TODO PostgreSQL need consider FrontendConnectionLimitException
        return createErrorResponsePacketForUnknownException(cause);
    }
    
    private static PostgreSQLErrorResponsePacket createErrorResponsePacket(final SQLException cause) {
        // TODO consider what severity to use
        String sqlState = Strings.isNullOrEmpty(cause.getSQLState()) ? PostgreSQLErrorCode.SYSTEM_ERROR.getErrorCode() : cause.getSQLState();
        String message = Strings.isNullOrEmpty(cause.getMessage()) ? cause.toString() : cause.getMessage();
        return PostgreSQLErrorResponsePacket.newBuilder(PostgreSQLMessageSeverityLevel.ERROR, sqlState, message).build();
    }
    
    private static PostgreSQLErrorResponsePacket createErrorResponsePacket(final ServerErrorMessage serverErrorMessage) {
        return PostgreSQLErrorResponsePacket.newBuilder(serverErrorMessage.getSeverity(), serverErrorMessage.getSQLState(), serverErrorMessage.getMessage())
                .detail(serverErrorMessage.getDetail()).hint(serverErrorMessage.getHint()).position(serverErrorMessage.getPosition())
                .internalQueryAndInternalPosition(serverErrorMessage.getInternalQuery(), serverErrorMessage.getInternalPosition()).where(serverErrorMessage.getWhere())
                .schemaName(serverErrorMessage.getSchema()).tableName(serverErrorMessage.getTable()).columnName(serverErrorMessage.getColumn()).dataTypeName(serverErrorMessage.getDatatype())
                .constraintName(serverErrorMessage.getConstraint()).file(serverErrorMessage.getFile()).line(serverErrorMessage.getLine()).routine(serverErrorMessage.getRoutine()).build();
    }
    
    private static PostgreSQLErrorResponsePacket createErrorResponsePacketForUnknownException(final Exception cause) {
        // TODO add FIELD_TYPE_CODE for common error and consider what severity to use
        String message = Strings.isNullOrEmpty(cause.getLocalizedMessage()) ? cause.toString() : cause.getLocalizedMessage();
        return PostgreSQLErrorResponsePacket.newBuilder(PostgreSQLMessageSeverityLevel.ERROR, PostgreSQLErrorCode.SYSTEM_ERROR, message).build();
    }
}
