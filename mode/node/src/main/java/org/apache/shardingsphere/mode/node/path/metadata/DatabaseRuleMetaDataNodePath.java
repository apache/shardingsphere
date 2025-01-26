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

/**
 * Database rule meta data node path.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseRuleMetaDataNodePath {
    
    private static final String ROOT_NODE = "/metadata";
    
    private static final String RULE_NODE = "rules";
    
    private static final String VERSIONS_NODE = "versions";
    
    private static final String ACTIVE_VERSION_NODE = "active_version";
    
    /**
     * Get database root path.
     *
     * @param databaseName database name
     * @return database root path
     */
    public static String getRootPath(final String databaseName) {
        return String.join("/", ROOT_NODE, databaseName, RULE_NODE);
    }
    
    /**
     * Get database rule path.
     *
     * @param databaseName database name
     * @param ruleTypeName rule type name
     * @return database rule path
     */
    public static String getRulePath(final String databaseName, final String ruleTypeName) {
        return String.join("/", getRootPath(databaseName), ruleTypeName);
    }
    
    /**
     * Get database rule path.
     *
     * @param databaseName database name
     * @param ruleTypeName rule type name
     * @param key key
     * @return database rule path without version
     */
    public static String getRulePath(final String databaseName, final String ruleTypeName, final String key) {
        return String.join("/", getRulePath(databaseName, ruleTypeName), key);
    }
    
    /**
     * Get database rule versions path.
     *
     * @param databaseName database name
     * @param ruleTypeName rule type name
     * @param key key
     * @return database rule versions path
     */
    public static String getVersionsPath(final String databaseName, final String ruleTypeName, final String key) {
        return String.join("/", getRulePath(databaseName, ruleTypeName, key), VERSIONS_NODE);
    }
    
    /**
     * Get database rule version path.
     *
     * @param databaseName database name
     * @param ruleTypeName rule type name
     * @param key key
     * @param version version
     * @return database rule next version
     */
    public static String getVersionPath(final String databaseName, final String ruleTypeName, final String key, final String version) {
        return String.join("/", getVersionsPath(databaseName, ruleTypeName, key), version);
    }
    
    /**
     * Get database rule active version path.
     *
     * @param databaseName database name
     * @param ruleTypeName rule type name
     * @param key key
     * @return database rule active version path
     */
    public static String getActiveVersionPath(final String databaseName, final String ruleTypeName, final String key) {
        return String.join("/", getRulePath(databaseName, ruleTypeName, key), ACTIVE_VERSION_NODE);
    }
}
