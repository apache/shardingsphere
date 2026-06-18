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

package org.apache.shardingsphere.driver.exception;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.SQLState;
import org.apache.shardingsphere.infra.exception.external.sql.type.kernel.category.ConnectionSQLException;

/**
 * Driver connection exception.
 */
public abstract class DriverConnectionException extends ConnectionSQLException {
    
    private static final long serialVersionUID = -4543547821709772632L;
    
    private static final int DRIVER_CODE = 2;
    
    protected DriverConnectionException(final SQLState sqlState, final int errorCode, final String reason, final Object... messageArgs) {
        super(sqlState, getErrorCode(errorCode), reason, messageArgs);
    }
    
    protected DriverConnectionException(final SQLState sqlState, final int errorCode, final Exception cause, final String reason, final Object... messageArgs) {
        super(sqlState, getErrorCode(errorCode), cause, reason, messageArgs);
    }
    
    private static int getErrorCode(final int errorCode) {
        Preconditions.checkArgument(errorCode >= 0 && errorCode < 100, "The value range of error code should be [0, 100).");
        return DRIVER_CODE * 100 + errorCode;
    }
}
