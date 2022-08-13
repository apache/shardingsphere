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

package org.apache.shardingsphere.error.postgresql.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * PostgreSQL error code.
 *
 * @see <a href="https://www.postgresql.org/docs/12/errcodes-appendix.html">Appendix A. PostgreSQL Error Codes</a>
 */
@RequiredArgsConstructor
@Getter
public enum PostgreSQLErrorCode {
    
    SUCCESSFUL_COMPLETION("00000", "successful_completion"),
    
    PRIVILEGE_NOT_GRANTED("01007", "privilege_not_granted"),
    
    PROTOCOL_VIOLATION("08P01", "protocol_violation"),
    
    FEATURE_NOT_SUPPORTED("0A000", "feature_not_supported"),
    
    DUPLICATE_DATABASE("42P04", "Database '%s' already exists"),
    
    INVALID_AUTHORIZATION_SPECIFICATION("28000", "invalid_authorization_specification"),
    
    INVALID_PASSWORD("28P01", "invalid_password"),
    
    INVALID_CATALOG_NAME("3D000", "invalid_catalog_name"),
    
    UNDEFINED_COLUMN("42703", "undefined_column"),
    
    TOO_MANY_CONNECTIONS("53300", "too_many_connections"),
    
    SYSTEM_ERROR("58000", "system_error");
    
    private final String errorCode;
    
    private final String conditionName;
}
