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
 * Database rule meta data node.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseRuleMetaDataNode {
    
    private static final String ROOT_NODE = "metadata";
    
    private static final String RULE_NODE = "rules";
    
    private static final String ACTIVE_VERSION = "active_version";
    
    private static final String VERSIONS = "versions";
    
    /**
     * Get database rule active version node.
     *
     * @param databaseName database name
     * @param ruleName rule name
     * @param key key
     * @return database rule active version node
     */
    public static String getDatabaseRuleActiveVersionNode(final String databaseName, final String ruleName, final String key) {
        return String.join("/", getDatabaseRuleNode(databaseName, ruleName), key, ACTIVE_VERSION);
    }
    
    /**
     * Get database rule versions node.
     *
     * @param databaseName database name
     * @param ruleName rule name
     * @param key key
     * @return database rule versions node
     */
    public static String getDatabaseRuleVersionsNode(final String databaseName, final String ruleName, final String key) {
        return String.join("/", getDatabaseRuleNode(databaseName, ruleName), key, VERSIONS);
    }
    
    /**
     * Get database rule version node.
     *
     * @param databaseName database name
     * @param ruleName rule name
     * @param key key
     * @param version version
     * @return database rule next version
     */
    public static String getDatabaseRuleVersionNode(final String databaseName, final String ruleName, final String key, final String version) {
        return String.join("/", getDatabaseRuleNode(databaseName, ruleName), key, VERSIONS, version);
    }
    
    /**
     * Get database rule node.
     *
     * @param databaseName database name
     * @param ruleName rule name
     * @param key key
     * @return database rule node without version
     */
    public static String getDatabaseRuleNode(final String databaseName, final String ruleName, final String key) {
        return String.join("/", getDatabaseRuleNode(databaseName, ruleName), key);
    }
    
    /**
     * Get database rule root node.
     *
     * @param databaseName database name
     * @param ruleName rule name
     * @return database rule root node
     */
    public static String getDatabaseRuleNode(final String databaseName, final String ruleName) {
        return String.join("/", getRulesNode(databaseName), ruleName);
    }
    
    /**
     * Get database rules node.
     *
     * @param databaseName database name
     * @return database rules node
     */
    public static String getRulesNode(final String databaseName) {
        return String.join("/", getMetaDataNode(), databaseName, RULE_NODE);
    }
    
    private static String getMetaDataNode() {
        return String.join("/", "", ROOT_NODE);
    }
}
