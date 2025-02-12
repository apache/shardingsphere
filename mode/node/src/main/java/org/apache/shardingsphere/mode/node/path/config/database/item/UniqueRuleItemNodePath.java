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

package org.apache.shardingsphere.mode.node.path.config.database.item;

import lombok.Getter;
import org.apache.shardingsphere.mode.node.path.config.database.root.RuleRootNodePath;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePathParser;

/**
 * Unique rule item node path.
 */
public final class UniqueRuleItemNodePath {
    
    private final String parentNode;
    
    private final String type;
    
    @Getter
    private final VersionNodePathParser versionNodePathParser;
    
    public UniqueRuleItemNodePath(final RuleRootNodePath ruleRootNodePath, final String type) {
        parentNode = null;
        this.type = type;
        versionNodePathParser = new VersionNodePathParser(ruleRootNodePath.getNodePrefix() + type);
    }
    
    public UniqueRuleItemNodePath(final RuleRootNodePath ruleRootNodePath, final String parentNode, final String type) {
        this.parentNode = parentNode;
        this.type = type;
        versionNodePathParser = new VersionNodePathParser(String.join("/", ruleRootNodePath.getNodePrefix() + parentNode, type));
    }
    
    /**
     * Get path.
     *
     * @return path
     */
    public String getPath() {
        return null == parentNode ? String.join("/", type) : String.join("/", parentNode, type);
    }
}
