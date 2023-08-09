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

package org.apache.shardingsphere.mode.path.item;

import org.apache.shardingsphere.mode.path.root.RuleRootNodePath;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Named rule item node path.
 */
public final class NamedRuleItemNodePath {
    
    private static final String NAME = "/(\\w+)/versions/\\d+$";
    
    private static final String ACTIVE_VERSION = "/(\\w+)/active_version$";
    
    private final String type;
    
    private final Pattern namePathPattern;
    
    private final Pattern activeVersionPathPattern;
    
    public NamedRuleItemNodePath(final RuleRootNodePath rootNodePath, final String type) {
        this.type = type;
        namePathPattern = Pattern.compile(rootNodePath.getNodePrefix() + type + NAME);
        activeVersionPathPattern = Pattern.compile(rootNodePath.getNodePrefix() + type + ACTIVE_VERSION);
    }
    
    /**
     * Get rule item path.
     *
     * @param itemName item name
     * @return rule item path
     */
    public String getPath(final String itemName) {
        return String.join("/", type, itemName);
    }
    
    /**
     * Get rule item name.
     *
     * @param path path
     * @return got item rule name
     */
    public Optional<String> getName(final String path) {
        Matcher matcher = namePathPattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
    
    /**
     * Get rule item name by active version.
     *
     * @param path path
     * @return got rule item name
     */
    public Optional<String> getNameByActiveVersion(final String path) {
        Matcher matcher = activeVersionPathPattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
}
