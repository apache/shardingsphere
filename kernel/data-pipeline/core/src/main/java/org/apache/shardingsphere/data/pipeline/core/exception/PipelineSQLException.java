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

package org.apache.shardingsphere.data.pipeline.core.exception;

import org.apache.shardingsphere.infra.util.exception.external.sql.sqlstate.SQLState;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.kernel.KernelSQLException;

/**
 * Pipeline SQL exception.
 */
public abstract class PipelineSQLException extends KernelSQLException {
    
    private static final long serialVersionUID = 139616805450096292L;
    
    private static final int KERNEL_CODE = 8;
    
    protected PipelineSQLException(final SQLState sqlState, final int errorCode, final String reason) {
        super(sqlState, KERNEL_CODE, errorCode, reason);
    }
    
    protected PipelineSQLException(final SQLState sqlState, final int errorCode, final String reason, final Exception cause) {
        super(sqlState, KERNEL_CODE, errorCode, reason, cause);
    }
}
