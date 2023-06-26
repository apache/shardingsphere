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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rule default item node converter.
 */
public final class RuleDefaultItemNodeConverter {
    
    private static final String VERSIONS = "/versions/\\d+$";
    
    private static final String ACTIVE_VERSION = "/active_version$";
    
    private final String parentNode;
    
    private final String itemsNode;
    
    private final Pattern itemsPathPattern;
    
    private final Pattern activeVersionPathPattern;
    
    public RuleDefaultItemNodeConverter(final RuleRootNodePath ruleRootNodePath, final String itemsNode) {
        parentNode = null;
        this.itemsNode = itemsNode;
        itemsPathPattern = Pattern.compile(ruleRootNodePath.getNodePrefix() + "/" + itemsNode + VERSIONS, Pattern.CASE_INSENSITIVE);
        activeVersionPathPattern = Pattern.compile(ruleRootNodePath.getNodePrefix() + "/" + itemsNode + ACTIVE_VERSION, Pattern.CASE_INSENSITIVE);
    }
    
    public RuleDefaultItemNodeConverter(final RuleRootNodePath ruleRootNodePath, final String parentNode, final String itemsNode) {
        this.parentNode = parentNode;
        this.itemsNode = itemsNode;
        itemsPathPattern = Pattern.compile(ruleRootNodePath.getNodePrefix() + "/" + parentNode + "/" + itemsNode + VERSIONS, Pattern.CASE_INSENSITIVE);
        activeVersionPathPattern = Pattern.compile(ruleRootNodePath.getNodePrefix() + "/" + parentNode + "/" + itemsNode + ACTIVE_VERSION, Pattern.CASE_INSENSITIVE);
    }
    
    /**
     * Get path.
     *
     * @return path
     */
    public String getPath() {
        return null == parentNode ? String.join("/", itemsNode) : String.join("/", parentNode, itemsNode);
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
     * Judge whether active version path.
     * 
     * @param path path to be judged
     * @return is active version path or not
     */
    public boolean isActiveVersionPath(final String path) {
        Matcher matcher = activeVersionPathPattern.matcher(path);
        return matcher.find();
    }
}
