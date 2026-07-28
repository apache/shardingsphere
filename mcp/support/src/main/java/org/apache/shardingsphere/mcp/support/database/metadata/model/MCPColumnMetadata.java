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

package org.apache.shardingsphere.mcp.support.database.metadata.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.DatabaseMetaData;

/**
 * MCP column metadata.
 */
@RequiredArgsConstructor
@Getter
public final class MCPColumnMetadata {
    
    private final String relationName;
    
    private final String name;
    
    private final int ordinalPosition;
    
    private final int jdbcType;
    
    private final String nativeTypeName;
    
    private final Nullability nullability;
    
    /**
     * JDBC column nullability.
     */
    @Getter
    @RequiredArgsConstructor
    public enum Nullability {
        
        NULLABLE("nullable"),
        
        NOT_NULLABLE("not_nullable"),
        
        UNKNOWN("unknown");
        
        private final String value;
        
        /**
         * Create nullability from JDBC metadata value.
         *
         * @param jdbcValue JDBC metadata value
         * @return nullability
         */
        public static Nullability fromJdbcValue(final int jdbcValue) {
            if (DatabaseMetaData.columnNullable == jdbcValue) {
                return NULLABLE;
            }
            if (DatabaseMetaData.columnNoNulls == jdbcValue) {
                return NOT_NULLABLE;
            }
            return UNKNOWN;
        }
        
    }
}
