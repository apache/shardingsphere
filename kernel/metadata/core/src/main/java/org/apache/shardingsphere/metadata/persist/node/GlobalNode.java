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

/**
 * Global node.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlobalNode {
    
    private static final String RULE_NODE = "rules";
    
    private static final String PROPS_NODE = "props";
    
    private static final String ACTIVE_VERSION = "active_version";
    
    private static final String VERSIONS = "versions";
    
    /**
     * Get global rule node.
     *
     * @param rulePath rule path
     * @return global rule node
     */
    public static String getGlobalRuleNode(final String rulePath) {
        return String.join("/", getGlobalRuleRootNode(), rulePath);
    }
    
    /**
     * Get global rule active version node.
     *
     * @param rulePath rule path
     * @return global rule active version node
     */
    public static String getGlobalRuleActiveVersionNode(final String rulePath) {
        return String.join("/", getGlobalRuleRootNode(), rulePath, ACTIVE_VERSION);
    }
    
    /**
     * Get global rule versions node.
     *
     * @param ruleName rule name
     * @return global rule versions node
     */
    public static String getGlobalRuleVersionsNode(final String ruleName) {
        return String.join("/", getGlobalRuleRootNode(), ruleName, VERSIONS);
    }
    
    /**
     * Get global rule version node.
     *
     * @param ruleName rule name
     * @param version version
     * @return global rule version node
     */
    public static String getGlobalRuleVersionNode(final String ruleName, final String version) {
        return String.join("/", getGlobalRuleVersionsNode(ruleName), version);
    }
    
    /**
     * Get global rule root node.
     *
     * @return global rule root node
     */
    public static String getGlobalRuleRootNode() {
        return String.join("/", "", RULE_NODE);
    }
    
    /**
     * Get properties active version node.
     *
     * @return properties active version node
     */
    public static String getPropsActiveVersionNode() {
        return String.join("/", getPropsRootNode(), ACTIVE_VERSION);
    }
    
    /**
     * Get properties version node.
     *
     * @param version version
     * @return properties version node
     */
    public static String getPropsVersionNode(final String version) {
        return String.join("/", getPropsVersionsNode(), version);
    }
    
    /**
     * Get properties versions node.
     *
     * @return properties versions node
     */
    public static String getPropsVersionsNode() {
        return String.join("/", getPropsRootNode(), VERSIONS);
    }
    
    /**
     * Get properties node.
     *
     * @return properties node
     */
    public static String getPropsRootNode() {
        return String.join("/", "", PROPS_NODE);
    }
}
