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
import lombok.SneakyThrows;
import org.apache.shardingsphere.db.protocol.opengauss.packet.command.generic.OpenGaussErrorResponsePacket;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLMessageSeverityLevel;
import org.apache.shardingsphere.dialect.exception.SQLDialectException;
import org.apache.shardingsphere.dialect.mapper.SQLDialectExceptionMapperFactory;
import org.apache.shardingsphere.dialect.postgresql.vendor.PostgreSQLVendorError;
import org.apache.shardingsphere.infra.util.exception.sql.ShardingSphereSQLException;
import org.apache.shardingsphere.proxy.frontend.postgresql.authentication.exception.PostgreSQLAuthenticationException;
import org.apache.shardingsphere.proxy.frontend.postgresql.authentication.exception.PostgreSQLProtocolViolationException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Map;

/**
 * Error packet factory for openGauss.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OpenGaussErrorPacketFactory {
    
    private static final Class<?> PSQL_EXCEPTION_CLASS;
    
    private static final Method GET_SERVER_ERROR_MESSAGE_METHOD;
    
    private static final Class<?> SERVER_ERROR_MESSAGE_CLASS;
    
    private static final Field MESSAGE_PARTS_FIELD;
    
    static {
        try {
            PSQL_EXCEPTION_CLASS = Class.forName("org.opengauss.util.PSQLException");
            GET_SERVER_ERROR_MESSAGE_METHOD = PSQL_EXCEPTION_CLASS.getMethod("getServerErrorMessage");
            SERVER_ERROR_MESSAGE_CLASS = Class.forName("org.opengauss.util.ServerErrorMessage");
            MESSAGE_PARTS_FIELD = SERVER_ERROR_MESSAGE_CLASS.getDeclaredField("m_mesgParts");
            MESSAGE_PARTS_FIELD.setAccessible(true);
        } catch (final ClassNotFoundException | NoSuchMethodException | NoSuchFieldException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Create new instance of openGauss error packet.
     *
     * @param cause cause
     * @return created instance
     */
    public static OpenGaussErrorResponsePacket newInstance(final Exception cause) {
        if (existsServerErrorMessage(cause)) {
            return createErrorResponsePacket(getServerErrorMessageMap(cause));
        }
        if (cause instanceof SQLException) {
            return createErrorResponsePacket((SQLException) cause);
        }
        if (cause instanceof SQLDialectException) {
            return createErrorResponsePacket(SQLDialectExceptionMapperFactory.getInstance("PostgreSQL").convert((SQLDialectException) cause));
        }
        if (cause instanceof ShardingSphereSQLException) {
            return createErrorResponsePacket(((ShardingSphereSQLException) cause).toSQLException());
        }
        if (cause instanceof PostgreSQLProtocolViolationException) {
            return new OpenGaussErrorResponsePacket(PostgreSQLMessageSeverityLevel.FATAL, PostgreSQLVendorError.PROTOCOL_VIOLATION.getSqlState().getValue(),
                    String.format("expected %s response, got message type %s", ((PostgreSQLProtocolViolationException) cause).getExpectedMessageType(),
                            ((PostgreSQLProtocolViolationException) cause).getActualMessageType()));
        }
        if (cause instanceof PostgreSQLAuthenticationException) {
            return new OpenGaussErrorResponsePacket(PostgreSQLMessageSeverityLevel.FATAL, ((PostgreSQLAuthenticationException) cause).getVendorError().getSqlState().getValue(), cause.getMessage());
        }
        // TODO OpenGauss need consider FrontendConnectionLimitException
        return createErrorResponsePacketForUnknownException(cause);
    }
    
    @SneakyThrows({IllegalAccessException.class, IllegalArgumentException.class, InvocationTargetException.class})
    private static boolean existsServerErrorMessage(final Exception cause) {
        return PSQL_EXCEPTION_CLASS.isInstance(cause) && null != GET_SERVER_ERROR_MESSAGE_METHOD.invoke(cause);
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows({IllegalAccessException.class, IllegalArgumentException.class, InvocationTargetException.class})
    private static Map<Character, String> getServerErrorMessageMap(final Exception cause) {
        return (Map<Character, String>) MESSAGE_PARTS_FIELD.get(GET_SERVER_ERROR_MESSAGE_METHOD.invoke(cause));
    }
    
    private static OpenGaussErrorResponsePacket createErrorResponsePacket(final SQLException cause) {
        // TODO consider what severity to use
        String sqlState = Strings.isNullOrEmpty(cause.getSQLState()) ? PostgreSQLVendorError.SYSTEM_ERROR.getSqlState().getValue() : cause.getSQLState();
        String message = Strings.isNullOrEmpty(cause.getMessage()) ? cause.toString() : cause.getMessage();
        return new OpenGaussErrorResponsePacket(PostgreSQLMessageSeverityLevel.ERROR, sqlState, message);
    }
    
    private static OpenGaussErrorResponsePacket createErrorResponsePacket(final Map<Character, String> serverErrorMessageMap) {
        return new OpenGaussErrorResponsePacket(serverErrorMessageMap);
    }
    
    private static OpenGaussErrorResponsePacket createErrorResponsePacketForUnknownException(final Exception cause) {
        // TODO add FIELD_TYPE_CODE for common error and consider what severity to use
        String message = Strings.isNullOrEmpty(cause.getLocalizedMessage()) ? cause.toString() : cause.getLocalizedMessage();
        return new OpenGaussErrorResponsePacket(PostgreSQLMessageSeverityLevel.ERROR, PostgreSQLVendorError.SYSTEM_ERROR.getSqlState().getValue(), message);
    }
}
