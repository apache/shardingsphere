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

package org.apache.shardingsphere.error.sqlstate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * ShardingSphere SQL state.
 */
@RequiredArgsConstructor
@Getter
public enum ShardingSphereSQLState implements SQLState {
    
    CIRCUIT_BREAK_MODE("C1000"),
    
    SCALING_JOB_NOT_EXIST("C1201"),
    
    SCALING_OPERATE_FAILED("C1209"),
    
    DATABASE_WRITE_LOCKED("C1300"),
    
    TABLE_LOCK_WAIT_TIMEOUT("C1301"),
    
    TABLE_LOCKED("C1302"),
    
    UNSUPPORTED_COMMAND("C1998"),
    
    UNKNOWN_EXCEPTION("C1999");
    
    private final String value;
}
