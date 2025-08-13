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

package org.apache.shardingsphere.mode.node.path.type.database.metadata.rule;

import lombok.Getter;
import org.apache.shardingsphere.mode.node.path.NodePath;
import org.apache.shardingsphere.mode.node.path.NodePathEntity;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathPattern;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearchCriteria;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.apache.shardingsphere.mode.node.path.type.database.metadata.DatabaseMetaDataNodePath;

/**
 * Database rule node path.
 */
@NodePathEntity("${database}/rules/${ruleType}/${databaseRuleItem}")
@Getter
public final class DatabaseRuleNodePath implements NodePath {
    
    private final DatabaseMetaDataNodePath database;
    
    private final String ruleType;
    
    private final DatabaseRuleItem databaseRuleItem;
    
    public DatabaseRuleNodePath(final String databaseName, final String ruleType, final DatabaseRuleItem databaseRuleItem) {
        database = new DatabaseMetaDataNodePath(databaseName);
        this.ruleType = ruleType;
        this.databaseRuleItem = databaseRuleItem;
    }
    
    /**
     * Create rule type search criteria.
     *
     * @param databaseName database name
     * @return create search criteria
     */
    public static NodePathSearchCriteria createRuleTypeSearchCriteria(final String databaseName) {
        return new NodePathSearchCriteria(new DatabaseRuleNodePath(databaseName, NodePathPattern.IDENTIFIER, null), true, 1);
    }
    
    /**
     * Create rule item name search criteria.
     *
     * @param databaseName database name
     * @param ruleType rule type
     * @param ruleItemType rule item type
     * @return create search criteria
     */
    public static NodePathSearchCriteria createRuleItemNameSearchCriteria(final String databaseName, final String ruleType, final String ruleItemType) {
        return new NodePathSearchCriteria(new DatabaseRuleNodePath(databaseName, ruleType, new DatabaseRuleItem(ruleItemType, NodePathPattern.QUALIFIED_IDENTIFIER)), true, 1);
    }
    
    /**
     * Check if the path is a rule type path.
     *
     * @param databaseName database name
     * @param path path
     * @return true if the path is a rule type path, otherwise false
     */
    public static boolean isRuleTypePath(final String databaseName, final String path) {
        return NodePathSearcher.find(path, new NodePathSearchCriteria(new DatabaseRuleNodePath(databaseName, NodePathPattern.IDENTIFIER, null), false, 1))
                .isPresent();
    }
    
    /**
     * Check if the path is a named rule item path.
     *
     * @param databaseName database name
     * @param ruleType rule type
     * @param ruleItemType rule item type
     * @param path path
     * @return true if the path is a rule item path, otherwise false
     */
    public static boolean isNamedRuleItemPath(final String databaseName, final String ruleType, final String ruleItemType, final String path) {
        return NodePathSearcher.find(path, new NodePathSearchCriteria(new DatabaseRuleNodePath(databaseName, ruleType,
                new DatabaseRuleItem(ruleItemType, NodePathPattern.QUALIFIED_IDENTIFIER)), false, 1)).isPresent();
    }
    
    /**
     * Check if the path is a unique rule item path.
     *
     * @param databaseName database name
     * @param ruleType rule type
     * @param ruleItemType rule item type
     * @param path path
     * @return true if the path is a unique rule item path, otherwise false
     */
    public static boolean isUniqueRuleItemPath(final String databaseName, final String ruleType, final String ruleItemType, final String path) {
        return NodePathSearcher.isMatchedPath(path, new NodePathSearchCriteria(new DatabaseRuleNodePath(databaseName, ruleType,
                new DatabaseRuleItem(ruleItemType, null)), false, 0));
    }
}
