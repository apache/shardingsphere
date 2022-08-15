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

package org.apache.shardingsphere.error;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.error.dialect.SQLDialectException;
import org.apache.shardingsphere.error.mapper.SQLDialectExceptionMapperFactory;
import org.apache.shardingsphere.infra.util.exception.ShardingSphereInsideException;
import org.apache.shardingsphere.infra.util.exception.sql.ShardingSphereSQLException;
import org.apache.shardingsphere.infra.util.exception.sql.vendor.ShardingSphereVendorError;
import org.apache.shardingsphere.infra.util.exception.sql.vendor.VendorError;

import java.sql.SQLException;

/**
 * SQL exception handler.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLExceptionHandler {
    
    /**
     * Convert to SQL exception.
     * 
     * @param databaseType database type
     * @param insideException inside exception
     * @return SQL exception
     */
    public static SQLException convert(final String databaseType, final ShardingSphereInsideException insideException) {
        if (insideException instanceof SQLDialectException) {
            return SQLDialectExceptionMapperFactory.getInstance(databaseType).convert((SQLDialectException) insideException);
        }
        if (insideException instanceof ShardingSphereSQLException) {
            return ((ShardingSphereSQLException) insideException).toSQLException();
        }
        return toSQLException(ShardingSphereVendorError.UNKNOWN_EXCEPTION, insideException);
    }
    
    private static SQLException toSQLException(final VendorError vendorError, final ShardingSphereInsideException insideException) {
        return new SQLException(String.format(vendorError.getReason(), insideException.getMessage()), vendorError.getSqlState().getValue(), vendorError.getVendorCode());
    }
}
