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

package org.apache.shardingsphere.mode.node.path.type.metadata.rule;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mode.node.path.NodePath;
import org.apache.shardingsphere.mode.node.path.NodePathEntity;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathPattern;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearchCriteria;

/**
 * Database rule node path.
 */
@NodePathEntity("/metadata/${databaseName}/rules/${ruleType}/${databaseRuleItem}")
@RequiredArgsConstructor
@Getter
public final class DatabaseRuleNodePath implements NodePath {
    
    private final String databaseName;
    
    private final String ruleType;
    
    private final DatabaseRuleItem databaseRuleItem;
    
    /**
     * Create valid rule type search criteria.
     *
     * @param ruleType rule type
     * @return create search criteria
     */
    public static NodePathSearchCriteria createValidRuleTypeSearchCriteria(final String ruleType) {
        return new NodePathSearchCriteria(new DatabaseRuleNodePath(NodePathPattern.IDENTIFIER, ruleType, null), false, true, 1);
    }
    
    /**
     * Create rule type search criteria.
     *
     * @return create search criteria
     */
    public static NodePathSearchCriteria createRuleTypeSearchCriteria() {
        return new NodePathSearchCriteria(new DatabaseRuleNodePath(NodePathPattern.IDENTIFIER, NodePathPattern.IDENTIFIER, null), false, true, 2);
    }
    
    /**
     * Create rule item name search criteria.
     *
     * @param ruleType rule type
     * @param ruleItemType rule item type
     * @return create search criteria
     */
    public static NodePathSearchCriteria createRuleItemNameSearchCriteria(final String ruleType, final String ruleItemType) {
        return new NodePathSearchCriteria(new DatabaseRuleNodePath(NodePathPattern.IDENTIFIER, ruleType, new DatabaseRuleItem(ruleItemType, NodePathPattern.IDENTIFIER)), false, false, 2);
    }
}
