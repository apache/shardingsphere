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
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.SQLExceptionTransformEngine;
import org.apache.shardingsphere.database.exception.mysql.vendor.MySQLVendorError;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.sql.SQLException;

/**
 * Error packet factory for MySQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLErrorPacketFactory {
    
    private static final DatabaseType DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    /**
     * Create new instance of MySQL error packet.
     *
     * @param cause cause
     * @return created instance
     */
    public static MySQLErrPacket newInstance(final Exception cause) {
        SQLException sqlException = SQLExceptionTransformEngine.toSQLException(cause, DATABASE_TYPE);
        return null == sqlException.getSQLState() ? new MySQLErrPacket(MySQLVendorError.ER_INTERNAL_ERROR, getErrorMessage(sqlException)) : new MySQLErrPacket(sqlException);
    }
    
    private static String getErrorMessage(final SQLException cause) {
        return null == cause.getNextException() || !Strings.isNullOrEmpty(cause.getMessage()) ? cause.getMessage() : cause.getNextException().getMessage();
    }
}
