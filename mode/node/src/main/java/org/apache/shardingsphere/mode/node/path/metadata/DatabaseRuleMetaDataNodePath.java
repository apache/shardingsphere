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
import org.apache.shardingsphere.mode.node.path.config.RuleTypeNode;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePathGenerator;

/**
 * Database rule meta data node path.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseRuleMetaDataNodePath {
    
    private static final String ROOT_NODE = "/metadata";
    
    private static final String RULE_NODE = "rules";
    
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
     * @param ruleTypeNode rule type node
     * @return database rule path
     */
    public static String getRulePath(final String databaseName, final RuleTypeNode ruleTypeNode) {
        return String.join("/", getRootPath(databaseName), ruleTypeNode.getPersistKey());
    }
    
    /**
     * Get database rule path.
     *
     * @param databaseName database name
     * @param ruleTypeNode rule type node
     * @param ruleItemName rule item name
     * @return database rule path
     */
    public static String getRulePath(final String databaseName, final RuleTypeNode ruleTypeNode, final String ruleItemName) {
        return String.join("/", getRulePath(databaseName, ruleTypeNode), ruleItemName);
    }
    
    /**
     * Get database rule version node path generator.
     *
     * @param databaseName database name
     * @param ruleTypeNode rule type node
     * @param ruleItemName rule item name
     * @return database rule version node path generator
     */
    public static VersionNodePathGenerator getVersionNodePathGenerator(final String databaseName, final RuleTypeNode ruleTypeNode, final String ruleItemName) {
        return new VersionNodePathGenerator(getRulePath(databaseName, ruleTypeNode, ruleItemName));
    }
}
