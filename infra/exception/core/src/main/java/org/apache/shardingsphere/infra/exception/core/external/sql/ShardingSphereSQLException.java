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

package org.apache.shardingsphere.infra.exception.core.external.sql;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.exception.core.external.ShardingSphereExternalException;
import org.apache.shardingsphere.infra.exception.core.external.sql.sqlstate.SQLState;

import java.sql.SQLException;

/**
 * ShardingSphere SQL exception.
 */
public abstract class ShardingSphereSQLException extends ShardingSphereExternalException {
    
    private static final long serialVersionUID = -8238061892944243621L;
    
    private final String sqlState;
    
    private final int vendorCode;
    
    private final String reason;
    
    private final Exception cause;
    
    protected ShardingSphereSQLException(final SQLState sqlState, final int typeOffset, final int errorCode, final String reason, final Object... messageArgs) {
        this(sqlState.getValue(), typeOffset, errorCode, reason, messageArgs);
    }
    
    protected ShardingSphereSQLException(final String sqlState, final int typeOffset, final int errorCode, final String reason, final Object... messageArgs) {
        this(sqlState, typeOffset, errorCode, null == reason ? null : String.format(reason, messageArgs), (Exception) null);
    }
    
    protected ShardingSphereSQLException(final String sqlState, final int typeOffset, final int errorCode, final String reason, final Exception cause) {
        super(reason, cause);
        this.sqlState = sqlState;
        Preconditions.checkArgument(typeOffset > 0 && typeOffset < 4, "The value range of type offset should be [1,3].");
        Preconditions.checkArgument(errorCode >= 0 && errorCode < 10000, "The value range of error code should be [0, 10000).");
        vendorCode = typeOffset * 10000 + errorCode;
        this.reason = reason;
        this.cause = cause;
    }
    
    /**
     * To SQL exception.
     * 
     * @return SQL exception
     */
    public final SQLException toSQLException() {
        return new SQLException(reason, sqlState, vendorCode, cause);
    }
}
