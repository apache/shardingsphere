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

package org.apache.shardingsphere.proxy.frontend.mysql.err;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.dialect.exception.SQLDialectException;
import org.apache.shardingsphere.dialect.mapper.SQLDialectExceptionMapperFactory;
import org.apache.shardingsphere.dialect.mysql.vendor.MySQLVendorError;
import org.apache.shardingsphere.infra.util.exception.sql.ShardingSphereSQLException;
import org.apache.shardingsphere.infra.util.exception.sql.UnknownSQLException;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.exception.DistSQLException;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.exception.DistSQLVendorError;
import org.apache.shardingsphere.proxy.frontend.exception.UnsupportedPreparedStatementException;

import java.nio.charset.UnsupportedCharsetException;
import java.sql.SQLException;

/**
 * ERR packet factory for MySQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLErrPacketFactory {
    
    /**
     * Create new instance of MySQL ERR packet.
     *
     * @param cause cause
     * @return created instance
     */
    public static MySQLErrPacket newInstance(final Exception cause) {
        if (cause instanceof SQLException) {
            SQLException sqlException = (SQLException) cause;
            return null == sqlException.getSQLState() ? new MySQLErrPacket(1, MySQLVendorError.ER_INTERNAL_ERROR, getErrorMessage(sqlException))
                    : new MySQLErrPacket(1, sqlException.getErrorCode(), sqlException.getSQLState(), sqlException.getMessage());
        }
        if (cause instanceof SQLDialectException) {
            SQLException sqlException = SQLDialectExceptionMapperFactory.getInstance("MySQL").convert((SQLDialectException) cause);
            return new MySQLErrPacket(1, sqlException.getErrorCode(), sqlException.getSQLState(), sqlException.getMessage());
        }
        if (cause instanceof ShardingSphereSQLException) {
            SQLException sqlException = ((ShardingSphereSQLException) cause).toSQLException();
            return new MySQLErrPacket(1, sqlException.getErrorCode(), sqlException.getSQLState(), sqlException.getMessage());
        }
        if (cause instanceof DistSQLException) {
            DistSQLException distSQLException = (DistSQLException) cause;
            return new MySQLErrPacket(1, DistSQLVendorError.valueOf(distSQLException), distSQLException.getVariable());
        }
        if (cause instanceof UnsupportedPreparedStatementException) {
            return new MySQLErrPacket(1, MySQLVendorError.ER_UNSUPPORTED_PS);
        }
        if (cause instanceof UnsupportedCharsetException) {
            return new MySQLErrPacket(1, MySQLVendorError.ER_UNKNOWN_CHARACTER_SET, cause.getMessage());
        }
        SQLException unknownSQLException = new UnknownSQLException(cause).toSQLException();
        return new MySQLErrPacket(1, unknownSQLException.getErrorCode(), unknownSQLException.getSQLState(), unknownSQLException.getMessage());
    }
    
    private static String getErrorMessage(final SQLException cause) {
        return null == cause.getNextException() || !Strings.isNullOrEmpty(cause.getMessage()) ? cause.getMessage() : cause.getNextException().getMessage();
    }
}
