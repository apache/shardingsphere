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

package org.apache.shardingsphere.proxy.exception;

import lombok.Getter;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.sql.SQLException;

/**
 * ShardingSphere SQL exception.
 */
@Getter
public abstract class ProxySQLException extends RuntimeException {

    private static final long serialVersionUID = -2361593557266150160L;

    private final int errorCode;

    private final String sqlState;

    private final String errorMessage;

    private final SQLException sqlException;

    public ProxySQLException() {
        this(0, null, null, null);
    }

    public ProxySQLException(final int errorCode, final String sqlState, final String errorMessage) {
        this(errorCode, sqlState, errorMessage, null);
    }

    public ProxySQLException(final int errorCode, final String sqlState, final String errorMessage, final Throwable cause) {
        this.errorCode = errorCode;
        this.sqlState = sqlState;
        this.errorMessage = errorMessage;
        this.sqlException = new SQLException(errorMessage, sqlState, errorCode, cause);
    }

    public ProxySQLException(final SQLException cause) {
        super(cause);
        this.errorCode = cause.getErrorCode();
        this.sqlState = cause.getSQLState();
        this.errorMessage = ExceptionUtils.getRootCauseMessage(cause);
        this.sqlException = cause;
    }

    public ProxySQLException(final Throwable throwable) {
        super(throwable);
        ProxySQLException cause;
        if (ProxySQLException.class.isAssignableFrom(throwable.getClass())) {
            cause = (ProxySQLException) throwable;
        } else {
            cause = transform(throwable);
        }
        this.errorCode = cause.getErrorCode();
        this.sqlState = cause.getSqlState();
        this.errorMessage = cause.getErrorMessage();
        this.sqlException = new SQLException(errorMessage, sqlState, errorCode, cause);
    }

    protected abstract ProxySQLException transform(Throwable cause);

    protected ProxySQLException transform(final int errorCode, final String sqlState, final String errorMessage) {
        return new UnknownProxySQLException(errorCode, sqlState, errorMessage);
    }

    protected static class UnknownProxySQLException extends ProxySQLException {

        public UnknownProxySQLException(final int errorCode, final String sqlState, final String errorMessage) {
            super(errorCode, sqlState, errorMessage);
        }

        @Override
        protected ProxySQLException transform(final Throwable cause) {
            return null;
        }
    }
}
