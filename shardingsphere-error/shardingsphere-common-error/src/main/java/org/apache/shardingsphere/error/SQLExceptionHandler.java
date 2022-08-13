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
import org.apache.shardingsphere.error.vendor.VendorError;
import org.apache.shardingsphere.error.vendor.StandardVendorError;
import org.apache.shardingsphere.error.mapper.DialectSQLExceptionMapperFactory;
import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;
import org.apache.shardingsphere.infra.exception.CircuitBreakException;
import org.apache.shardingsphere.infra.exception.ResourceNotExistedException;
import org.apache.shardingsphere.infra.exception.RuleNotExistedException;
import org.apache.shardingsphere.infra.exception.TableLockWaitTimeoutException;
import org.apache.shardingsphere.infra.exception.TableLockedException;
import org.apache.shardingsphere.infra.exception.UnsupportedCommandException;
import org.apache.shardingsphere.infra.util.exception.inside.InsideDialectSQLException;
import org.apache.shardingsphere.infra.util.exception.inside.ShardingSphereInsideException;
import org.apache.shardingsphere.sql.parser.exception.SQLParsingException;

import java.sql.SQLException;
import java.util.Optional;

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
        if (insideException instanceof InsideDialectSQLException) {
            return DialectSQLExceptionMapperFactory.getInstance(databaseType).convert((InsideDialectSQLException) insideException);
        }
        return convert(insideException).orElseGet(() -> toSQLException(StandardVendorError.UNKNOWN_EXCEPTION, insideException.getMessage()));
    }
    
    private static Optional<SQLException> convert(final ShardingSphereInsideException insideException) {
        if (insideException instanceof CircuitBreakException) {
            return Optional.of(toSQLException(StandardVendorError.CIRCUIT_BREAK_MODE));
        }
        if (insideException instanceof TableLockWaitTimeoutException) {
            TableLockWaitTimeoutException exception = (TableLockWaitTimeoutException) insideException;
            return Optional.of(toSQLException(StandardVendorError.TABLE_LOCK_WAIT_TIMEOUT, exception.getTableName(), exception.getSchemaName(), exception.getTimeoutMilliseconds()));
        }
        if (insideException instanceof TableLockedException) {
            TableLockedException exception = (TableLockedException) insideException;
            return Optional.of(toSQLException(StandardVendorError.TABLE_LOCKED, exception.getTableName(), exception.getSchemaName()));
        }
        if (insideException instanceof RuleNotExistedException || insideException instanceof ResourceNotExistedException) {
            return Optional.of(toSQLException(StandardVendorError.RESOURCE_OR_RULE_NOT_EXIST));
        }
        if (insideException instanceof ShardingSphereConfigurationException || insideException instanceof SQLParsingException) {
            return Optional.of(toSQLException(StandardVendorError.UNSUPPORTED_SQL, insideException.getMessage()));
        }
        if (insideException instanceof UnsupportedCommandException) {
            return Optional.of(toSQLException(StandardVendorError.UNSUPPORTED_COMMAND, ((UnsupportedCommandException) insideException).getCommandType()));
        }
        return Optional.empty();
    }
    
    private static SQLException toSQLException(final VendorError vendorError, final Object... messageArguments) {
        return new SQLException(String.format(vendorError.getReason(), messageArguments), vendorError.getSqlState().getValue(), vendorError.getVendorCode());
    }
}
