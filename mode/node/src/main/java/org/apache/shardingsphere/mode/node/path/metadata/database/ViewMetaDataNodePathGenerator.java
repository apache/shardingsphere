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
import org.apache.shardingsphere.mode.node.path.version.VersionNodePathGenerator;

/**
 * View meta data path generator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ViewMetaDataNodePathGenerator {
    
    private static final String VIEWS_NODE = "views";
    
    /**
     * Get view root path.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @return view root path
     */
    public static String getRootPath(final String databaseName, final String schemaName) {
        return String.join("/", SchemaMetaDataNodePathGenerator.getSchemaPath(databaseName, schemaName), VIEWS_NODE);
    }
    
    /**
     * Get view path.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param viewName view name
     * @return view path
     */
    public static String getViewPath(final String databaseName, final String schemaName, final String viewName) {
        return String.join("/", getRootPath(databaseName, schemaName), viewName);
    }
    
    /**
     * Get view version node path generator.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param viewName view name
     * @return view version node path generator
     */
    public static VersionNodePathGenerator getVersion(final String databaseName, final String schemaName, final String viewName) {
        return new VersionNodePathGenerator(getViewPath(databaseName, schemaName, viewName));
    }
}
