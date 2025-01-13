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

package org.apache.shardingsphere.mode.node.path;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Global props node path.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlobalRuleNodePath {
    
    private static final String ROOT_NODE = "/rules";
    
    private static final String VERSIONS_NODE = "versions";
    
    private static final String ACTIVE_VERSION_NODE = "active_version";
    
    private static final String IDENTIFIER_PATTERN = "(\\w+)";
    
    private static final String VERSION_PATTERN = "(\\d+)";
    
    /**
     * Get global rule root path.
     *
     * @return global rule root path
     */
    public static String getRootPath() {
        return ROOT_NODE;
    }
    
    /**
     * Get global rule path.
     *
     * @param ruleTypeName rule type name
     * @return global rule path
     */
    public static String getRulePath(final String ruleTypeName) {
        return String.join("/", getRootPath(), ruleTypeName);
    }
    
    /**
     * Get global rule version root path.
     *
     * @param ruleTypeName rule type name
     * @return global rule version root path
     */
    public static String getVersionRootPath(final String ruleTypeName) {
        return String.join("/", getRulePath(ruleTypeName), VERSIONS_NODE);
    }
    
    /**
     * Get global rule version path.
     *
     * @param ruleTypeName rule type name
     * @param version version
     * @return global rule version path
     */
    public static String getVersionPath(final String ruleTypeName, final String version) {
        return String.join("/", getVersionRootPath(ruleTypeName), version);
    }
    
    /**
     * Get global rule active version path.
     *
     * @param ruleTypeName rule type name
     * @return global rule active version path
     */
    public static String getActiveVersionPath(final String ruleTypeName) {
        return String.join("/", getRulePath(ruleTypeName), ACTIVE_VERSION_NODE);
    }
    
    /**
     * Find rule type name from active version.
     *
     * @param path path to be found
     * @return found rule type name
     */
    public static Optional<String> findRuleTypeNameFromActiveVersion(final String path) {
        Pattern pattern = Pattern.compile(getActiveVersionPath(IDENTIFIER_PATTERN) + "$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
    
    /**
     * Find version.
     *
     * @param ruleTypeName rule type name
     * @param path path to be found
     * @return found version
     */
    public static Optional<String> findVersion(final String ruleTypeName, final String path) {
        Pattern pattern = Pattern.compile(getVersionPath(ruleTypeName, VERSION_PATTERN) + "$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
}
