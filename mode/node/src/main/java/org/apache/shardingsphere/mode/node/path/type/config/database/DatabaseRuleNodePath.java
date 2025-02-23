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

package org.apache.shardingsphere.mode.node.path.type.config.database;

import lombok.Getter;
import org.apache.shardingsphere.mode.node.path.type.config.database.item.NamedDatabaseRuleItemNodePath;
import org.apache.shardingsphere.mode.node.path.type.config.database.item.UniqueDatabaseRuleItemNodePath;
import org.apache.shardingsphere.mode.node.path.type.config.database.root.DatabaseRuleRootNodePath;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Database rule node path.
 */
@Getter
public final class DatabaseRuleNodePath {
    
    private final DatabaseRuleRootNodePath root;
    
    private final Map<String, NamedDatabaseRuleItemNodePath> namedItems;
    
    private final Map<String, UniqueDatabaseRuleItemNodePath> uniqueItems;
    
    public DatabaseRuleNodePath(final String ruleType, final Collection<String> namedRuleItemNodePathTypes, final Collection<String> uniqueRuleItemNodePathTypes) {
        root = new DatabaseRuleRootNodePath(ruleType);
        namedItems = getNamedRuleItemNodePathMap(namedRuleItemNodePathTypes);
        uniqueItems = getUniqueRuleItemNodePathMap(uniqueRuleItemNodePathTypes);
    }
    
    private Map<String, NamedDatabaseRuleItemNodePath> getNamedRuleItemNodePathMap(final Collection<String> namedRuleItemNodePathTypes) {
        return namedRuleItemNodePathTypes.stream().collect(Collectors.toMap(each -> each, each -> new NamedDatabaseRuleItemNodePath(root, each)));
    }
    
    private Map<String, UniqueDatabaseRuleItemNodePath> getUniqueRuleItemNodePathMap(final Collection<String> uniqueRuleItemNodePathTypes) {
        return uniqueRuleItemNodePathTypes.stream().collect(Collectors.toMap(each -> each, each -> new UniqueDatabaseRuleItemNodePath(root, each)));
    }
    
    /**
     * Get named rule item node path.
     *
     * @param itemType item type
     * @return named rule item node path
     */
    public NamedDatabaseRuleItemNodePath getNamedItem(final String itemType) {
        return namedItems.get(itemType);
    }
    
    /**
     * Get unique rule item node path.
     *
     * @param itemType item type
     * @return unique rule item node path
     */
    public UniqueDatabaseRuleItemNodePath getUniqueItem(final String itemType) {
        return uniqueItems.get(itemType);
    }
}
