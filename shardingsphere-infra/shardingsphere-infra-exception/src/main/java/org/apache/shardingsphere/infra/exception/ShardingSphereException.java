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

/**
 * Basic exception of ShardingSphere.
 */
public class ShardingSphereException extends ShardingSphereSQLException {
    
    private static final long serialVersionUID = -1343739516839252250L;

    public ShardingSphereException(final String errorMessage) {
        super(errorMessage);
    }

    public ShardingSphereException(final Throwable cause) {
        super(cause);
    }

    /**
     * Constructs an exception with formatted SQL error code and arguments.
     *
     * @param sqlErrorCode formatted SQL error code
     * @param errorMessageArguments arguments of error message
     */
    public ShardingSphereException(final SQLErrorCode sqlErrorCode, final Object... errorMessageArguments) {
        super(sqlErrorCode.getErrorCode(), sqlErrorCode.getSqlState(), String.format(sqlErrorCode.getErrorMessage(), errorMessageArguments));
    }

    /**
     * Constructs an exception with cause and formatted SQL error code and arguments.
     *
     * @param cause throwable
     * @param sqlErrorCode formatted SQL error code
     * @param errorMessageArguments arguments of error message
     */
    public ShardingSphereException(final Throwable cause, final SQLErrorCode sqlErrorCode, final Object... errorMessageArguments) {
        super(sqlErrorCode.getErrorCode(), sqlErrorCode.getSqlState(), String.format(sqlErrorCode.getErrorMessage(), getErrorMessageArguments(cause, errorMessageArguments)), cause);
    }
}
