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

package org.apache.shardingsphere.mask.context;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mask.spi.context.MaskContext;

/**
 * Mask context builder.
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class MaskContextBuilder {
    
    /**
     * Build mask context.
     * 
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param columnName column name
     * @return mask context
     */
    public static MaskContext build(final String databaseName, final String schemaName, final String tableName, final String columnName) {
        return new MaskContext(databaseName, schemaName, tableName, columnName);
    }
}
