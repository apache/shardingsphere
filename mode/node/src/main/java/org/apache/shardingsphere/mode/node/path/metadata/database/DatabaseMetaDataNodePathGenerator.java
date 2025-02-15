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

package org.apache.shardingsphere.mode.node.path.metadata.database;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Database meta data node path generator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseMetaDataNodePathGenerator {
    
    private static final String ROOT_NODE = "/metadata";
    
    private static final String SCHEMAS_NODE = "schemas";
    
    /**
     * Get meta data root path.
     *
     * @return meta data root path
     */
    public static String getRootPath() {
        return ROOT_NODE;
    }
    
    /**
     * Get database path.
     *
     * @param databaseName database name
     * @return database path
     */
    public static String getDatabasePath(final String databaseName) {
        return String.join("/", getRootPath(), databaseName);
    }
    
    /**
     * Get schema root path.
     *
     * @param databaseName database name
     * @return schema root path
     */
    public static String getSchemaRootPath(final String databaseName) {
        return String.join("/", getDatabasePath(databaseName), SCHEMAS_NODE);
    }
    
    /**
     * Get schema path.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @return schema path
     */
    public static String getSchemaPath(final String databaseName, final String schemaName) {
        return String.join("/", getSchemaRootPath(databaseName), schemaName);
    }
}
