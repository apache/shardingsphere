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

package org.apache.shardingsphere.proxy.frontend.opengauss.err;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.db.protocol.opengauss.packet.command.generic.OpenGaussErrorResponsePacket;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLErrorCode;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLMessageSeverityLevel;
import org.apache.shardingsphere.proxy.frontend.postgresql.authentication.exception.InvalidAuthorizationSpecificationException;
import org.apache.shardingsphere.proxy.frontend.postgresql.authentication.exception.PostgreSQLAuthenticationException;
import org.apache.shardingsphere.proxy.frontend.postgresql.authentication.exception.PostgreSQLProtocolViolationException;
import org.opengauss.util.PSQLException;
import org.opengauss.util.ServerErrorMessage;

import java.sql.SQLException;

/**
 * Error packet factory for openGauss.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OpenGaussErrorPacketFactory {
    
    /**
     * New instance of openGauss error packet.
     *
     * @param cause cause
     * @return instance of openGauss error packet
     */
    public static OpenGaussErrorResponsePacket newInstance(final Exception cause) {
        if (cause instanceof PSQLException && null != ((PSQLException) cause).getServerErrorMessage()) {
            return createErrorResponsePacket(((PSQLException) cause).getServerErrorMessage());
        }
        if (cause instanceof SQLException) {
            return createErrorResponsePacket((SQLException) cause);
        }
        if (cause instanceof InvalidAuthorizationSpecificationException) {
            return OpenGaussErrorResponsePacket.newBuilder(PostgreSQLMessageSeverityLevel.FATAL, PostgreSQLErrorCode.INVALID_AUTHORIZATION_SPECIFICATION, cause.getMessage()).build();
        }
        if (cause instanceof PostgreSQLProtocolViolationException) {
            return OpenGaussErrorResponsePacket.newBuilder(PostgreSQLMessageSeverityLevel.FATAL, PostgreSQLErrorCode.PROTOCOL_VIOLATION,
                    String.format("expected %s response, got message type %s", ((PostgreSQLProtocolViolationException) cause).getExpectedMessageType(),
                            ((PostgreSQLProtocolViolationException) cause).getActualMessageType())).build();
        }
        if (cause instanceof PostgreSQLAuthenticationException) {
            return OpenGaussErrorResponsePacket.newBuilder(PostgreSQLMessageSeverityLevel.FATAL, ((PostgreSQLAuthenticationException) cause).getErrorCode(), cause.getMessage()).build();
        }
        return createErrorResponsePacketForUnknownException(cause);
    }
    
    private static OpenGaussErrorResponsePacket createErrorResponsePacket(final SQLException cause) {
        // TODO consider what severity to use
        String sqlState = Strings.isNullOrEmpty(cause.getSQLState()) ? PostgreSQLErrorCode.SYSTEM_ERROR.getErrorCode() : cause.getSQLState();
        String message = Strings.isNullOrEmpty(cause.getMessage()) ? cause.toString() : cause.getMessage();
        return OpenGaussErrorResponsePacket.newBuilder(PostgreSQLMessageSeverityLevel.ERROR, sqlState, message).build();
    }
    
    private static OpenGaussErrorResponsePacket createErrorResponsePacket(final ServerErrorMessage serverErrorMessage) {
        return OpenGaussErrorResponsePacket.newBuilder(PostgreSQLMessageSeverityLevel.valueOf(serverErrorMessage.getSeverity()), serverErrorMessage.getSQLState(), serverErrorMessage.getMessage())
                .errorcode(serverErrorMessage.getERRORCODE()).detail(serverErrorMessage.getDetail()).hint(serverErrorMessage.getHint()).position(serverErrorMessage.getPosition())
                .internalQueryAndInternalPosition(serverErrorMessage.getInternalQuery(), serverErrorMessage.getInternalPosition()).where(serverErrorMessage.getWhere())
                .file(serverErrorMessage.getFile()).line(serverErrorMessage.getLine()).routine(serverErrorMessage.getRoutine()).build();
    }
    
    private static OpenGaussErrorResponsePacket createErrorResponsePacketForUnknownException(final Exception cause) {
        // TODO add FIELD_TYPE_CODE for common error and consider what severity to use
        String message = Strings.isNullOrEmpty(cause.getLocalizedMessage()) ? cause.toString() : cause.getLocalizedMessage();
        return OpenGaussErrorResponsePacket.newBuilder(PostgreSQLMessageSeverityLevel.ERROR, PostgreSQLErrorCode.SYSTEM_ERROR, message).build();
    }
}
