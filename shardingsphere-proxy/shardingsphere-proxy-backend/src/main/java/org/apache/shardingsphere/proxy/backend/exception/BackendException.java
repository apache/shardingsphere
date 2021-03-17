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

package org.apache.shardingsphere.proxy.backend.exception;

import lombok.Getter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.shardingsphere.db.protocol.error.CommonErrorCode;
import org.apache.shardingsphere.db.protocol.error.SQLErrorCode;
import org.apache.shardingsphere.proxy.exception.ProxySQLException;

import java.sql.SQLException;

/**
 * Backend exception.
 */
@Getter
public abstract class BackendException extends ProxySQLException {
    
    private static final long serialVersionUID = -2361593557266150160L;

    public BackendException() {
    }

    public BackendException(final SQLErrorCode sqlErrorCode, final Object... errorMessageArguments) {
        super(sqlErrorCode.getErrorCode(), sqlErrorCode.getSqlState(), String.format(sqlErrorCode.getErrorMessage(), errorMessageArguments));
    }

    public BackendException(final SQLException cause) {
        super(cause);
    }

    public BackendException(final Throwable cause) {
        super(cause);
    }

    @Override
    protected ProxySQLException transform(final Throwable cause) {
        SQLErrorCode sqlErrorCode = CommonErrorCode.UNKNOWN_EXCEPTION;
        return transform(sqlErrorCode.getErrorCode(), sqlErrorCode.getSqlState(), String.format(sqlErrorCode.getErrorMessage(), ExceptionUtils.getRootCauseMessage(cause)));
    }
}
