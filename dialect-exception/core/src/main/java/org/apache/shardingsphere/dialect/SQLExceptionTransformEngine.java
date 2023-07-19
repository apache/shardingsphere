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

package org.apache.shardingsphere.dialect;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.dialect.exception.SQLDialectException;
import org.apache.shardingsphere.dialect.exception.protocol.DatabaseProtocolException;
import org.apache.shardingsphere.dialect.mapper.SQLDialectExceptionMapper;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.util.exception.external.sql.ShardingSphereSQLException;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.generic.DatabaseProtocolSQLException;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.generic.UnknownSQLException;

import java.sql.SQLException;
import java.util.Optional;

/**
 * SQL Exception transform engine.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLExceptionTransformEngine {
    
    /**
     * To SQL exception.
     * 
     * @param cause cause
     * @param databaseType database type
     * @return SQL exception
     */
    public static SQLException toSQLException(final Exception cause, final DatabaseType databaseType) {
        if (cause instanceof SQLException) {
            return (SQLException) cause;
        }
        if (cause instanceof ShardingSphereSQLException) {
            return ((ShardingSphereSQLException) cause).toSQLException();
        }
        if (cause instanceof SQLDialectException) {
            if (cause instanceof DatabaseProtocolException) {
                return new DatabaseProtocolSQLException(cause.getMessage()).toSQLException();
            }
            Optional<SQLDialectExceptionMapper> dialectExceptionMapper = DatabaseTypedSPILoader.findService(SQLDialectExceptionMapper.class, databaseType);
            if (dialectExceptionMapper.isPresent()) {
                return dialectExceptionMapper.get().convert((SQLDialectException) cause);
            }
        }
        return new UnknownSQLException(cause).toSQLException();
    }
}
