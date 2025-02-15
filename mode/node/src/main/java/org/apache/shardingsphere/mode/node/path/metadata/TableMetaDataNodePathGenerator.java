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

package org.apache.shardingsphere.mode.node.path.metadata;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePathGenerator;

/**
 * Table meta data node path generator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableMetaDataNodePathGenerator {
    
    private static final String TABLES_NODE = "tables";
    
    /**
     * Get table root path.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @return table root path
     */
    public static String getTableRootPath(final String databaseName, final String schemaName) {
        return String.join("/", DatabaseMetaDataNodePathGenerator.getSchemaPath(databaseName, schemaName), TABLES_NODE);
    }
    
    /**
     * Get table path.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @return table path
     */
    public static String getTablePath(final String databaseName, final String schemaName, final String tableName) {
        return String.join("/", getTableRootPath(databaseName, schemaName), tableName);
    }
    
    /**
     * Get table version node path generator.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @return table version node path generator
     */
    public static VersionNodePathGenerator getVersion(final String databaseName, final String schemaName, final String tableName) {
        return new VersionNodePathGenerator(getTablePath(databaseName, schemaName, tableName));
    }
}
