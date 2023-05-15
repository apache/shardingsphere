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

package org.apache.shardingsphere.metadata.persist.node;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.metadata.persist.node.metadata.DataSourceNode;

/**
 * TODO Rename DatabaseMetaDataNode when metadata structure adjustment completed. #25485
 * New database meta data node.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NewDatabaseMetaDataNode {
    
    private static final String ROOT_NODE = "metadata";
    
    /**
     * Get data Sources path.
     *
     * @param databaseName database name
     * @return data sources path
     */
    public static String getDataSourcesPath(final String databaseName) {
        return String.join("/", getMetaDataNodePath(), DataSourceNode.getDataSourcesPath(databaseName));
    }
    
    /**
     * Get data Source path.
     *
     * @param databaseName database name
     * @param dataSourceName data source name
     * @param version version
     * @return data source path
     */
    public static String getDataSourcePath(final String databaseName, final String dataSourceName, final String version) {
        return String.join("/", getMetaDataNodePath(), DataSourceNode.getDataSourcePath(databaseName, dataSourceName, version));
    }
    
    /**
     * Get data Source active version path.
     *
     * @param databaseName database name
     * @param dataSourceName data source name
     * @return data source active version path
     */
    public static String getDataSourceActiveVersionPath(final String databaseName, final String dataSourceName) {
        return String.join("/", getMetaDataNodePath(), DataSourceNode.getActiveVersionPath(databaseName, dataSourceName));
    }
    
    /**
     * Get meta data node path.
     *
     * @return meta data node path
     */
    public static String getMetaDataNodePath() {
        return String.join("/", "", ROOT_NODE);
    }
}
