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

package org.apache.shardingsphere.broadcast.metadata.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Broadcast node converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BroadcastNodeConverter {
    
    private static final String ROOT_NODE = "broadcast";
    
    private static final String TABLES_NODE = "tables";
    
    private static final String RULES_NODE_PREFIX = "/([\\w\\-]+)/([\\w\\-]+)/rules/";
    
    private static final String VERSION_PATH = "/([\\w\\-]+)/versions/(\\d+)";
    
    private static final String RULE_ACTIVE_VERSION = "/active_version$";
    
    /**
     * Get tables path.
     *
     * @return tables path
     */
    public static String getTablesPath() {
        return TABLES_NODE;
    }
    
    /**
     * Is broadcast path.
     *
     * @param rulePath rule path
     * @return true or false
     */
    public static boolean isBroadcastPath(final String rulePath) {
        Pattern pattern = Pattern.compile(RULES_NODE_PREFIX + ROOT_NODE + "\\.*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find();
    }
    
    /**
     * Is broadcast tables active version path.
     *
     * @param rulePath rule path
     * @return true or false
     */
    public static boolean isTablesActiveVersionPath(final String rulePath) {
        Pattern pattern = Pattern.compile(RULES_NODE_PREFIX + ROOT_NODE + "/" + TABLES_NODE + RULE_ACTIVE_VERSION, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find();
    }
    
    /**
     * Get tables version.
     *
     * @param rulePath rule path
     * @return tables version
     */
    public static Optional<String> getTablesVersion(final String rulePath) {
        Pattern pattern = Pattern.compile(RULES_NODE_PREFIX + ROOT_NODE + "/" + TABLES_NODE + VERSION_PATH, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find() ? Optional.of(matcher.group(4)) : Optional.empty();
    }
}
