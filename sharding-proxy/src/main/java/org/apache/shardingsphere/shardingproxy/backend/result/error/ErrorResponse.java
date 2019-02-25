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

package org.apache.shardingsphere.shardingproxy.backend.result.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.shardingproxy.backend.result.BackendResponse;
import org.apache.shardingsphere.shardingproxy.transport.mysql.constant.MySQLServerErrorCode;

import java.sql.SQLException;

/**
 * Error response.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public final class ErrorResponse implements BackendResponse {
    
    private final int errorCode;
    
    private final String sqlState;
    
    private final String errorMessage;
    
    public ErrorResponse(final MySQLServerErrorCode errorCode, final Object... errorMessageArguments) {
        this(errorCode.getErrorCode(), errorCode.getSqlState(), String.format(errorCode.getErrorMessage(), errorMessageArguments));
    }
    
    public ErrorResponse(final SQLException cause) {
        this(cause.getErrorCode(), cause.getSQLState(), cause.getMessage());
    }
}
