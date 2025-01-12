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

package org.apache.shardingsphere.mode.path;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Global node path.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlobalNodePath {
    
    private static final String RULES_NODE = "/rules";
    
    private static final String PROPS_NODE = "/props";
    
    private static final String VERSIONS_NODE = "versions";
    
    private static final String ACTIVE_VERSION_NODE = "active_version";
    
    private static final String IDENTIFIER_PATTERN = "(\\w+)";
    
    private static final String VERSION_PATTERN = "(\\d+)";
    
    /**
     * Get global rule root path.
     *
     * @return global rule root path
     */
    public static String getRuleRootPath() {
        return RULES_NODE;
    }
    
    /**
     * Get global rule path.
     *
     * @param ruleTypeName rule type name
     * @return global rule path
     */
    public static String getRulePath(final String ruleTypeName) {
        return String.join("/", getRuleRootPath(), ruleTypeName);
    }
    
    /**
     * Get global rule versions path.
     *
     * @param ruleTypeName rule type name
     * @return global rule versions path
     */
    public static String getRuleVersionsPath(final String ruleTypeName) {
        return String.join("/", getRulePath(ruleTypeName), VERSIONS_NODE);
    }
    
    /**
     * Get global rule version path.
     *
     * @param ruleTypeName rule type name
     * @param version version
     * @return global rule version path
     */
    public static String getRuleVersionPath(final String ruleTypeName, final String version) {
        return String.join("/", getRuleVersionsPath(ruleTypeName), version);
    }
    
    /**
     * Get global rule active version path.
     *
     * @param ruleTypeName rule type name
     * @return global rule active version path
     */
    public static String getRuleActiveVersionPath(final String ruleTypeName) {
        return String.join("/", getRulePath(ruleTypeName), ACTIVE_VERSION_NODE);
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
    
    /**
     * Find rule type name from active version.
     *
     * @param path path to be found
     * @return found rule type name
     */
    public static Optional<String> findRuleTypeNameFromActiveVersion(final String path) {
        Pattern pattern = Pattern.compile(getRuleActiveVersionPath(IDENTIFIER_PATTERN) + "$", Pattern.CASE_INSENSITIVE);
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
        Pattern pattern = Pattern.compile(getRuleVersionPath(ruleTypeName, VERSION_PATTERN) + "$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
    
    /**
     * Is props active version path.
     *
     * @param propsPath props path
     * @return true or false
     */
    public static boolean isPropsActiveVersionPath(final String propsPath) {
        Pattern pattern = Pattern.compile(getPropsActiveVersionPath() + "$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(propsPath);
        return matcher.find();
    }
}
