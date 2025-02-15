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

package org.apache.shardingsphere.mode.node.path.metadata.storage;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePathGenerator;

/**
 * Storage node node path generator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StorageNodeNodePathGenerator {
    
    private static final String NODES_NODE = "nodes";
    
    /**
     * Get storage node root path.
     *
     * @param databaseName database name
     * @return storage node root path
     */
    public static String getRootPath(final String databaseName) {
        return String.join("/", DataSourceNodePathGenerator.getRootPath(databaseName), NODES_NODE);
    }
    
    /**
     * Get storage node path.
     *
     * @param databaseName database name
     * @param storageNodeName storage node name
     * @return storage node path
     */
    public static String getStorageNodePath(final String databaseName, final String storageNodeName) {
        return String.join("/", getRootPath(databaseName), storageNodeName);
    }
    
    /**
     * Get storage node version node path generator.
     *
     * @param databaseName database name
     * @param storageNodeName storage node name
     * @return storage node version node path generator
     */
    public static VersionNodePathGenerator getVersion(final String databaseName, final String storageNodeName) {
        return new VersionNodePathGenerator(String.join("/", getRootPath(databaseName), storageNodeName));
    }
}
