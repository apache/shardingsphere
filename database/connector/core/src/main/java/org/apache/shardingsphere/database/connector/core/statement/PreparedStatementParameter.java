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

package org.apache.shardingsphere.database.connector.core.statement;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Prepared statement parameter.
 * Records the original setter method and its arguments for replay.
 */
@RequiredArgsConstructor
@Getter
public final class PreparedStatementParameter {
    
    private final int index;
    
    private final SetterMethodType setterMethodType;
    
    private final Object value;
    
    private final long length;
    
    public PreparedStatementParameter(final int index, final SetterMethodType setterMethodType, final Object value) {
        this(index, setterMethodType, value, -1L);
    }
}
