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
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.SQLExceptionTransformEngine;
import org.apache.shardingsphere.database.exception.postgresql.vendor.PostgreSQLVendorError;
import org.apache.shardingsphere.database.protocol.opengauss.packet.command.generic.OpenGaussErrorResponsePacket;
import org.apache.shardingsphere.database.protocol.postgresql.constant.PostgreSQLMessageSeverityLevel;
import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.XOpenSQLState;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.opengauss.util.PSQLException;
import org.opengauss.util.ServerErrorMessage;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Error packet factory for openGauss.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OpenGaussErrorPacketFactory {
    
    private static final DatabaseType DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    /**
     * Create new instance of openGauss error packet.
     *
     * @param cause cause
     * @return created instance
     */
    public static OpenGaussErrorResponsePacket newInstance(final Exception cause) {
        Optional<ServerErrorMessage> serverErrorMessage = findServerErrorMessage(cause);
        return serverErrorMessage.map(OpenGaussErrorResponsePacket::new).orElseGet(() -> createErrorResponsePacket(SQLExceptionTransformEngine.toSQLException(cause, DATABASE_TYPE)));
    }
    
    private static Optional<ServerErrorMessage> findServerErrorMessage(final Exception cause) {
        return cause instanceof PSQLException ? Optional.ofNullable(((PSQLException) cause).getServerErrorMessage()) : Optional.empty();
    }
    
    private static OpenGaussErrorResponsePacket createErrorResponsePacket(final SQLException cause) {
        String sqlState = Strings.isNullOrEmpty(cause.getSQLState()) || XOpenSQLState.GENERAL_ERROR.getValue().equals(cause.getSQLState())
                ? PostgreSQLVendorError.SYSTEM_ERROR.getSqlState().getValue()
                : cause.getSQLState();
        String message = Strings.isNullOrEmpty(cause.getMessage()) ? cause.toString() : cause.getMessage();
        return new OpenGaussErrorResponsePacket(PostgreSQLMessageSeverityLevel.ERROR, sqlState, message);
    }
}
