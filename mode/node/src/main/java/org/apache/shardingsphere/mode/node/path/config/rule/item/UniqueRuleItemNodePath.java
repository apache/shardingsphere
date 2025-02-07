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

package org.apache.shardingsphere.mode.node.path.config.rule.item;

import org.apache.shardingsphere.mode.node.path.config.rule.root.RuleRootNodePath;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePath;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Unique rule item node path.
 */
public final class UniqueRuleItemNodePath {
    
    private final String parentNode;
    
    private final String type;
    
    private final Pattern pathPattern;
    
    private final Pattern activeVersionPathPattern;
    
    public UniqueRuleItemNodePath(final RuleRootNodePath ruleRootNodePath, final String type) {
        parentNode = null;
        this.type = type;
        VersionNodePath versionNodePath = new VersionNodePath(ruleRootNodePath.getNodePrefix() + type);
        pathPattern = Pattern.compile(String.join("/", versionNodePath.getVersionsPath(), VersionNodePath.VERSION_PATTERN));
        activeVersionPathPattern = Pattern.compile(versionNodePath.getActiveVersionPath() + "$");
    }
    
    public UniqueRuleItemNodePath(final RuleRootNodePath ruleRootNodePath, final String parentNode, final String type) {
        this.parentNode = parentNode;
        this.type = type;
        VersionNodePath versionNodePath = new VersionNodePath(ruleRootNodePath.getNodePrefix() + parentNode + "/" + type);
        pathPattern = Pattern.compile(String.join("/", versionNodePath.getVersionsPath(), VersionNodePath.VERSION_PATTERN));
        activeVersionPathPattern = Pattern.compile(versionNodePath.getActiveVersionPath() + "$");
    }
    
    /**
     * Get path.
     *
     * @return path
     */
    public String getPath() {
        return null == parentNode ? String.join("/", type) : String.join("/", parentNode, type);
    }
    
    /**
     * Judge whether is validated rule item path.
     *
     * @param path path to be judged
     * @return is validated rule item path or not
     */
    public boolean isValidatedPath(final String path) {
        return pathPattern.matcher(path).find();
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
