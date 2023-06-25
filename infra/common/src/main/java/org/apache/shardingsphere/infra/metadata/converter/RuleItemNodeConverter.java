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

package org.apache.shardingsphere.infra.metadata.converter;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rule item node converter.
 */
public final class RuleItemNodeConverter {
    
    private static final String RULE_NAME = "/([\\w\\-]+)?";
    
    private static final String RULE_ACTIVE_VERSION = "/([\\w\\-]+)/active_version$";
    
    private final String itemsNode;
    
    private final Pattern itemsPathPattern;
    
    private final Pattern itemNamePathPattern;
    
    private final Pattern itemVersionPathPattern;
    
    public RuleItemNodeConverter(final RuleRootNodeConverter ruleRootNodeConverter, final String itemsNode) {
        this.itemsNode = itemsNode;
        itemsPathPattern = Pattern.compile(ruleRootNodeConverter.getRuleNodePrefix() + "/" + itemsNode + "\\.*", Pattern.CASE_INSENSITIVE);
        itemNamePathPattern = Pattern.compile(ruleRootNodeConverter.getRuleNodePrefix() + "/" + itemsNode + RULE_NAME, Pattern.CASE_INSENSITIVE);
        itemVersionPathPattern = Pattern.compile(ruleRootNodeConverter.getRuleNodePrefix() + "/" + itemsNode + RULE_ACTIVE_VERSION, Pattern.CASE_INSENSITIVE);
    }
    
    /**
     * Get item name path.
     *
     * @param itemName item name
     * @return item name path
     */
    public String getNamePath(final String itemName) {
        return String.join("/", itemsNode, itemName);
    }
    
    /**
     * Is item path.
     *
     * @param rulePath rule path
     * @return true or false
     */
    public boolean isPath(final String rulePath) {
        return itemsPathPattern.matcher(rulePath).find();
    }
    
    /**
     * Get item name.
     *
     * @param rulePath rule path
     * @return item name
     */
    public Optional<String> getName(final String rulePath) {
        Matcher matcher = itemNamePathPattern.matcher(rulePath);
        return matcher.find() ? Optional.of(matcher.group(3)) : Optional.empty();
    }
    
    /**
     * Get item name by active version path.
     *
     * @param rulePath rule path
     * @return item version
     */
    public Optional<String> getNameByActiveVersionPath(final String rulePath) {
        Matcher matcher = itemVersionPathPattern.matcher(rulePath);
        return matcher.find() ? Optional.of(matcher.group(3)) : Optional.empty();
    }
}
