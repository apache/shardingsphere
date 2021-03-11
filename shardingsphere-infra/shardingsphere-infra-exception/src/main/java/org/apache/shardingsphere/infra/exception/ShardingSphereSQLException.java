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

package org.apache.shardingsphere.infra.exception;

import lombok.Getter;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.sql.SQLException;

/**
 * ShardingSphere SQL exception.
 */
@Getter
public abstract class ShardingSphereSQLException extends RuntimeException {

    private static final long serialVersionUID = -2361593557266150160L;

    private final int errorCode;

    private final String sqlState;

    private final String errorMessage;

    private final SQLException sqlException;

    public ShardingSphereSQLException() {
        this(0, null, null, null);
    }

    public ShardingSphereSQLException(final int errorCode, final String sqlState, final String errorMessage) {
        this(errorCode, sqlState, errorMessage, null);
    }

    public ShardingSphereSQLException(final int errorCode, final String sqlState, final String errorMessage, final Throwable cause) {
        this.errorCode = errorCode;
        this.sqlState = sqlState;
        this.errorMessage = errorMessage;
        this.sqlException = new SQLException(errorMessage, sqlState, errorCode, cause);
    }

    public ShardingSphereSQLException(final SQLException cause) {
        super(cause);
        this.errorCode = cause.getErrorCode();
        this.sqlState = cause.getSQLState();
        this.errorMessage = ExceptionUtils.getRootCauseMessage(cause);
        this.sqlException = cause;
    }

    public ShardingSphereSQLException(final Throwable throwable) {
        super(throwable);
        ShardingSphereSQLException cause;
        if (ShardingSphereSQLException.class.isAssignableFrom(throwable.getClass())) {
            cause = (ShardingSphereSQLException) throwable;
        } else {
            cause = transform(throwable);
        }
        this.errorCode = cause.getErrorCode();
        this.sqlState = cause.getSqlState();
        this.errorMessage = cause.getErrorMessage();
        this.sqlException = new SQLException(errorMessage, sqlState, errorCode, cause);
    }

    public ShardingSphereSQLException(final String errorMessage) {
        super(errorMessage);
        ShardingSphereSQLException cause = transform(errorMessage);
        this.errorCode = cause.getErrorCode();
        this.sqlState = cause.getSqlState();
        this.errorMessage = cause.getErrorMessage();
        this.sqlException = new SQLException(errorMessage, sqlState, errorCode, cause);
    }

    private ShardingSphereSQLException transform(final Throwable cause) {
        SQLErrorCode sqlErrorCode = CommonErrorCode.UNKNOWN_EXCEPTION;
        return new ShardingSphereSQLException(sqlErrorCode.getErrorCode(), sqlErrorCode.getSqlState(), String.format(sqlErrorCode.getErrorMessage(), ExceptionUtils.getRootCauseMessage(cause))) {
        };
    }

    private ShardingSphereSQLException transform(final String errorMessage) {
        SQLErrorCode sqlErrorCode = CommonErrorCode.UNKNOWN_EXCEPTION;
        return new ShardingSphereSQLException(sqlErrorCode.getErrorCode(), sqlErrorCode.getSqlState(), String.format(sqlErrorCode.getErrorMessage(), errorMessage)) {
        };
    }

    protected static Object[] getErrorMessageArguments(final Throwable cause, final Object... errorMessageArguments) {
        if ((null == errorMessageArguments || errorMessageArguments.length == 0) && null != cause) {
            return new Object[] {ExceptionUtils.getRootCauseMessage(cause)};
        }
        return errorMessageArguments;
    }
}
