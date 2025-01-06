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
 * Global node path.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlobalNodePath {
    
    private static final String RULE_NODE = "/rules";
    
    private static final String PROPS_NODE = "/props";
    
    private static final String VERSIONS_NODE = "versions";
    
    private static final String ACTIVE_VERSION_NODE = "active_version";
    
    /**
     * Get global rule root path.
     *
     * @return global rule root path
     */
    public static String getRuleRootPath() {
        return RULE_NODE;
    }
    
    /**
     * Get global rule path.
     *
     * @param ruleName rule name
     * @return global rule path
     */
    public static String getRulePath(final String ruleName) {
        return String.join("/", getRuleRootPath(), ruleName);
    }
    
    /**
     * Get global rule versions path.
     *
     * @param ruleName rule name
     * @return global rule versions path
     */
    public static String getRuleVersionsPath(final String ruleName) {
        return String.join("/", getRulePath(ruleName), VERSIONS_NODE);
    }
    
    /**
     * Get global rule version path.
     *
     * @param ruleName rule name
     * @param version version
     * @return global rule version path
     */
    public static String getRuleVersionPath(final String ruleName, final String version) {
        return String.join("/", getRuleVersionsPath(ruleName), version);
    }
    
    /**
     * Get global rule active version path.
     *
     * @param ruleName rule name
     * @return global rule active version path
     */
    public static String getRuleActiveVersionPath(final String ruleName) {
        return String.join("/", getRulePath(ruleName), ACTIVE_VERSION_NODE);
    }
    
    /**
     * Get properties path.
     *
     * @return properties path
     */
    public static String getPropsRootPath() {
        return PROPS_NODE;
    }
    
    /**
     * Get properties versions path.
     *
     * @return properties versions path
     */
    public static String getPropsVersionsPath() {
        return String.join("/", getPropsRootPath(), VERSIONS_NODE);
    }
    
    /**
     * Get properties version path.
     *
     * @param version version
     * @return properties version path
     */
    public static String getPropsVersionPath(final String version) {
        return String.join("/", getPropsVersionsPath(), version);
    }
    
    /**
     * Get properties active version path.
     *
     * @return properties active version path
     */
    public static String getPropsActiveVersionPath() {
        return String.join("/", getPropsRootPath(), ACTIVE_VERSION_NODE);
    }
}
