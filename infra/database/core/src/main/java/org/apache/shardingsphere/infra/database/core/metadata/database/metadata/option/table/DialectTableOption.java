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

package org.apache.shardingsphere.infra.database.core.metadata.database.metadata.option.table;

import lombok.RequiredArgsConstructor;

/**
 * Dialect table option.
 */
@RequiredArgsConstructor
public final class DialectTableOption {
    
    private final Type type;
    
    /**
     * Format table name pattern.
     *
     * @param tableNamePattern table name pattern
     * @return formatted table name pattern
     */
    public String formatTableNamePattern(final String tableNamePattern) {
        switch (type) {
            case UPPER_CASE:
                return tableNamePattern.toUpperCase();
            case LOWER_CASE:
                return tableNamePattern.toLowerCase();
            case KEEP_ORIGIN:
            default:
                return tableNamePattern;
        }
    }
    
    /**
     * Dialect table option type.
     */
    public enum Type {
        
        UPPER_CASE, LOWER_CASE, KEEP_ORIGIN
    }
}
