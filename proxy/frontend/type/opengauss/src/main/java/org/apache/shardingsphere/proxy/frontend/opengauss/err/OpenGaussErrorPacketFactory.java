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
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLMessageSeverityLevel;
import org.apache.shardingsphere.dialect.SQLExceptionTransformEngine;
import org.apache.shardingsphere.dialect.exception.SQLDialectException;
import org.apache.shardingsphere.dialect.postgresql.vendor.PostgreSQLVendorError;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.util.exception.external.sql.ShardingSphereSQLException;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.opengauss.util.PSQLException;

import java.sql.SQLException;

/**
 * Error packet factory for openGauss.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OpenGaussErrorPacketFactory {
    
    /**
     * Create new instance of openGauss error packet.
     *
     * @param cause cause
     * @return created instance
     */
    public static OpenGaussErrorResponsePacket newInstance(final Exception cause) {
        if (existsServerErrorMessage(cause)) {
            return new OpenGaussErrorResponsePacket(((PSQLException) cause).getServerErrorMessage());
        }
        if (cause instanceof SQLException || cause instanceof ShardingSphereSQLException || cause instanceof SQLDialectException) {
            return createErrorResponsePacket(SQLExceptionTransformEngine.toSQLException(cause, TypedSPILoader.getService(DatabaseType.class, "PostgreSQL")));
        }
        // TODO OpenGauss need consider FrontendConnectionLimitException
        return createErrorResponsePacketForUnknownException(cause);
    }
    
    private static boolean existsServerErrorMessage(final Exception cause) {
        return cause instanceof PSQLException && null != ((PSQLException) cause).getServerErrorMessage();
    }
    
    private static OpenGaussErrorResponsePacket createErrorResponsePacket(final SQLException cause) {
        // TODO consider what severity to use
        String sqlState = Strings.isNullOrEmpty(cause.getSQLState()) ? PostgreSQLVendorError.SYSTEM_ERROR.getSqlState().getValue() : cause.getSQLState();
        String message = Strings.isNullOrEmpty(cause.getMessage()) ? cause.toString() : cause.getMessage();
        return new OpenGaussErrorResponsePacket(PostgreSQLMessageSeverityLevel.ERROR, sqlState, message);
    }
    
    private static OpenGaussErrorResponsePacket createErrorResponsePacketForUnknownException(final Exception cause) {
        // TODO add FIELD_TYPE_CODE for common error and consider what severity to use
        String message = Strings.isNullOrEmpty(cause.getLocalizedMessage()) ? cause.toString() : cause.getLocalizedMessage();
        return new OpenGaussErrorResponsePacket(PostgreSQLMessageSeverityLevel.ERROR, PostgreSQLVendorError.SYSTEM_ERROR.getSqlState().getValue(), message);
    }
}
