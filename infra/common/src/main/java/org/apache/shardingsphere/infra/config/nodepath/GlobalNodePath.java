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

package org.apache.shardingsphere.infra.config.nodepath;

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
    
    private static final String RULES_NODE = "rules";
    
    private static final String PROPS_NODE = "props";
    
    private static final String VERSIONS = "versions";
    
    /**
     * Get version.
     *
     * @param ruleName rule name
     * @param rulePath rule path
     * @return version
     */
    public static Optional<String> getVersion(final String ruleName, final String rulePath) {
        Pattern pattern = Pattern.compile(getVersionsNode(ruleName) + "/(\\d+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
    
    private static String getVersionsNode(final String ruleName) {
        return String.join("/", "", RULES_NODE, ruleName, VERSIONS);
    }
    
    /**
     * Is rule active version path.
     *
     * @param rulePath rule path
     * @return true or false
     */
    public static boolean isRuleActiveVersionPath(final String rulePath) {
        Pattern pattern = Pattern.compile(getRuleNameNode() + "/(\\w+)/active_version$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find();
    }
    
    /**
     * Is props active version path.
     *
     * @param propsPath props path
     * @return true or false
     */
    public static boolean isPropsActiveVersionPath(final String propsPath) {
        Pattern pattern = Pattern.compile(getPropsNode() + "/active_version$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(propsPath);
        return matcher.find();
    }
    
    private static String getPropsNode() {
        return String.join("/", "", PROPS_NODE);
    }
    
    /**
     * Get rule name.
     *
     * @param rulePath rule path
     * @return rule name
     */
    public static Optional<String> getRuleName(final String rulePath) {
        Pattern pattern = Pattern.compile(getRuleNameNode() + "/(\\w+)/active_version$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
    
    private static String getRuleNameNode() {
        return String.join("/", "", RULES_NODE);
    }
}
