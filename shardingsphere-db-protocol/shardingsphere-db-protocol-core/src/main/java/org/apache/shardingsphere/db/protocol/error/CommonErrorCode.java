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

package org.apache.shardingsphere.db.protocol.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Common error code.
 */
@RequiredArgsConstructor
@Getter
public enum CommonErrorCode implements SQLErrorCode {
    
    CIRCUIT_BREAK_MODE(1000, "C1000", "Circuit break mode is ON."),
    
    SCALING_JOB_NOT_EXIST(1201, "C1201", "Scaling job %s does not exist."),
    
    SCALING_OPERATE_FAILED(1209, "C1209", "Scaling Operate Failed: [%s]"),
    
    TABLE_LOCK_WAIT_TIMEOUT(1301, "C1301", "The table %s of schema %s lock wait timeout of %s ms exceeded"),
    
    TABLE_LOCKED(1302, "C1302", "The table %s of schema %s is locked"),
    
    RUNTIME_EXCEPTION(1997, "C1997", "Runtime exception: [%s]"),
    
    UNSUPPORTED_COMMAND(1998, "C1998", "Unsupported command: [%s]"),
    
    UNKNOWN_EXCEPTION(1999, "C1999", "Unknown exception: [%s]");
    
    private final int errorCode;
    
    private final String sqlState;
    
    private final String errorMessage;
}
