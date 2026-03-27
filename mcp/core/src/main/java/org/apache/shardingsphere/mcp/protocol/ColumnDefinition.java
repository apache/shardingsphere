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

package org.apache.shardingsphere.mcp.protocol;

import lombok.Getter;

/**
 * Column metadata definition for result sets.
 */
@Getter
public final class ColumnDefinition {
    
    private final String columnName;
    
    private final String logicalType;
    
    private final String nativeType;
    
    private final boolean nullable;
    
    /**
     * Construct a column definition.
     *
     * @param columnName column name
     * @param logicalType logical type
     * @param nativeType native type
     * @param nullable nullable flag
     */
    public ColumnDefinition(final String columnName, final String logicalType, final String nativeType, final boolean nullable) {
        this.columnName = columnName;
        this.logicalType = logicalType;
        this.nativeType = nativeType;
        this.nullable = nullable;
    }
}
