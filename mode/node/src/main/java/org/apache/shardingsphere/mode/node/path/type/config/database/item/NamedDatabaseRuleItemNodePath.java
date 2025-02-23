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

package org.apache.shardingsphere.mode.node.path.type.config.database.item;

import lombok.Getter;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathPattern;
import org.apache.shardingsphere.mode.node.path.type.config.database.root.DatabaseRuleRootNodePath;
import org.apache.shardingsphere.mode.node.path.type.version.VersionNodePathParser;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Named database rule item node path.
 */
public final class NamedDatabaseRuleItemNodePath {
    
    private final String type;
    
    private final Pattern itemPathPattern;
    
    @Getter
    private final VersionNodePathParser versionNodePathParser;
    
    public NamedDatabaseRuleItemNodePath(final DatabaseRuleRootNodePath rootNodePath, final String type) {
        this.type = type;
        String pattern = String.join("/", rootNodePath.getNodePrefix() + type, NodePathPattern.IDENTIFIER);
        itemPathPattern = Pattern.compile(pattern + "$");
        versionNodePathParser = new VersionNodePathParser(pattern);
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
     * Find rule item name by item path.
     *
     * @param itemPath item path
     * @return found rule item name
     */
    public Optional<String> findNameByItemPath(final String itemPath) {
        Matcher matcher = itemPathPattern.matcher(itemPath);
        return matcher.find() ? Optional.of(matcher.group(2)) : Optional.empty();
    }
}
