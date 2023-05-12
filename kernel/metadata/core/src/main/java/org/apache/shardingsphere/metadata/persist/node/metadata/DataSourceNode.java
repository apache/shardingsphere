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

package org.apache.shardingsphere.metadata.persist.node.metadata;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Database source node.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceNode {
    
    private static final String ROOT_NODE = "data_sources";
    
    private static final String ACTIVE_VERSION = "active_version";
    
    private static final String VERSIONS = "versions";
    
    /**
     * Get data sources path.
     *
     * @param databaseName database name
     * @return data sources path
     */
    public static String getDataSourcesPath(final String databaseName) {
        return String.join("/", databaseName, ROOT_NODE);
    }
    
    /**
     * Get data source path.
     *
     * @param databaseName database name
     * @param dataSourceName data source name
     * @param version data source version
     * @return data source path
     */
    public static String getDataSourcePath(final String databaseName, final String dataSourceName, final String version) {
        return String.join("/", databaseName, ROOT_NODE, dataSourceName, VERSIONS, version);
    }
    
    /**
     * Get data source path.
     *
     * @param databaseName database name
     * @param dataSourceName data source name
     * @return active version path
     */
    public static String getActiveVersionPath(final String databaseName, final String dataSourceName) {
        return String.join("/", databaseName, ROOT_NODE, dataSourceName, ACTIVE_VERSION);
    }
}
