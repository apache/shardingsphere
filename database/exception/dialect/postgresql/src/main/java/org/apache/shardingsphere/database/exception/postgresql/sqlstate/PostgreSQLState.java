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

package org.apache.shardingsphere.database.exception.postgresql.sqlstate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.SQLState;

/**
 * PostgreSQL SQL state.
 */
@RequiredArgsConstructor
@Getter
public enum PostgreSQLState implements SQLState {
    
    PROTOCOL_VIOLATION("08P01"),
    
    SYNTAX_ERROR("42601"),
    
    DUPLICATE_DATABASE("42P04"),
    
    DUPLICATE_TABLE("42P07"),
    
    INVALID_PASSWORD("28P01"),
    
    UNDEFINED_TABLE("42P01"),
    
    UNDEFINED_COLUMN("42703"),
    
    SYSTEM_ERROR("58000"),
    
    UNEXPECTED_ERROR("99999");
    
    private final String value;
}
