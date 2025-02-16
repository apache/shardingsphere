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

import org.apache.shardingsphere.mode.node.path.version.VersionNodePathGenerator;

/**
 * View path generator.
 */
public final class ViewNodePathGenerator {
    
    private static final String VIEWS_NODE = "views";
    
    private final SchemaNodePathGenerator schemaNodePathGenerator;
    
    private final String schemaName;
    
    public ViewNodePathGenerator(final String databaseName, final String schemaName) {
        schemaNodePathGenerator = new SchemaNodePathGenerator(databaseName);
        this.schemaName = schemaName;
    }
    
    /**
     * Get view root path.
     *
     * @return view root path
     */
    public String getRootPath() {
        return String.join("/", schemaNodePathGenerator.getSchemaPath(schemaName), VIEWS_NODE);
    }
    
    /**
     * Get view path.
     *
     * @param viewName view name
     * @return view path
     */
    public String getViewPath(final String viewName) {
        return String.join("/", getRootPath(), viewName);
    }
    
    /**
     * Get view version node path generator.
     *
     * @param viewName view name
     * @return view version node path generator
     */
    public VersionNodePathGenerator getVersion(final String viewName) {
        return new VersionNodePathGenerator(getViewPath(viewName));
    }
}
