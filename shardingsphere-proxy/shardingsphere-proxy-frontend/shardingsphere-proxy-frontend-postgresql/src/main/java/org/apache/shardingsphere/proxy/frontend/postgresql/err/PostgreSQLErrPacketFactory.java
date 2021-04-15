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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLErrorResponsePacket;
import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;

import java.sql.SQLException;

/**
 * ERR packet factory for PostgreSQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLErrPacketFactory {
    
    /**
     * New instance of PostgreSQL ERR packet.
     * 
     * @param cause cause
     * @return instance of PostgreSQL ERR packet
     */
    public static PostgreSQLErrorResponsePacket newInstance(final Exception cause) {
        if (cause instanceof PSQLException && null != ((PSQLException) cause).getServerErrorMessage()) {
            return createErrorResponsePacket(((PSQLException) cause).getServerErrorMessage());
        }
        if (cause instanceof SQLException) {
            return createErrorResponsePacket((SQLException) cause);
        }
        return createErrorResponsePacket(cause);
    }
    
    private static PostgreSQLErrorResponsePacket createErrorResponsePacket(final ServerErrorMessage serverErrorMessage) {
        PostgreSQLErrorResponsePacket result = new PostgreSQLErrorResponsePacket();
        result.addField(PostgreSQLErrorResponsePacket.FIELD_TYPE_CODE, serverErrorMessage.getSQLState());
        result.addField(PostgreSQLErrorResponsePacket.FIELD_TYPE_MESSAGE, serverErrorMessage.getMessage());
        result.addField(PostgreSQLErrorResponsePacket.FIELD_TYPE_SEVERITY, serverErrorMessage.getSeverity());
        result.addField(PostgreSQLErrorResponsePacket.FIELD_TYPE_POSITION, Integer.toString(serverErrorMessage.getPosition()));
        return result;
    }
    
    private static PostgreSQLErrorResponsePacket createErrorResponsePacket(final SQLException cause) {
        PostgreSQLErrorResponsePacket result = new PostgreSQLErrorResponsePacket();
        result.addField(PostgreSQLErrorResponsePacket.FIELD_TYPE_CODE, cause.getSQLState());
        result.addField(PostgreSQLErrorResponsePacket.FIELD_TYPE_MESSAGE, cause.getMessage());
        return result;
    }
    
    private static PostgreSQLErrorResponsePacket createErrorResponsePacket(final Exception cause) {
        PostgreSQLErrorResponsePacket result = new PostgreSQLErrorResponsePacket();
        // TODO add FIELD_TYPE_CODE for common error
        result.addField(PostgreSQLErrorResponsePacket.FIELD_TYPE_MESSAGE, cause.getMessage());
        return result;
    }
}
