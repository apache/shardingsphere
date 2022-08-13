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
 * XOpen standard SQL state.
 */
@RequiredArgsConstructor
@Getter
public enum XOpenSQLState implements SQLState {
    
    SUCCESSFUL_COMPLETION("00000"),
    
    GENERAL_WARNING("01000"),
    
    PRIVILEGE_NOT_GRANTED("01007"),
    
    DATA_SOURCE_REJECTED_CONNECTION_ATTEMPT("08004"),
    
    FEATURE_NOT_SUPPORTED("0A000"),
    
    MISMATCH_INSERT_VALUES_AND_COLUMNS("21S01"),
    
    INVALID_AUTHORIZATION_SPECIFICATION("28000"),
    
    INVALID_CATALOG_NAME("3D000"),
    
    SYNTAX_ERROR("42000"),
    
    DUPLICATE("42S01"),
    
    NOT_FOUND("42S02"),
    
    GENERAL_ERROR("HY000");
    
    private final String value;
}
