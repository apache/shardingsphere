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

package org.apache.shardingsphere.mode.node.path.metadata.rule;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mode.node.path.config.database.item.DatabaseRuleItem;
import org.apache.shardingsphere.mode.node.path.metadata.DatabaseNodePathGenerator;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePathGenerator;

/**
 * Database rule node path generator.
 */
@RequiredArgsConstructor
public final class DatabaseRuleNodePathGenerator {
    
    private static final String RULE_NODE = "rules";
    
    private final String databaseName;
    
    /**
     * Get database root path.
     *
     * @return database root path
     */
    public String getRootPath() {
        return String.join("/", new DatabaseNodePathGenerator().getRootPath(), databaseName, RULE_NODE);
    }
    
    /**
     * Get database rule path.
     *
     * @param ruleType rule type
     * @return database rule path
     */
    public String getRulePath(final String ruleType) {
        return String.join("/", getRootPath(), ruleType);
    }
    
    /**
     * Get database rule path.
     *
     * @param ruleType rule type
     * @param databaseRuleItem database rule item
     * @return database rule path
     */
    public String getRulePath(final String ruleType, final DatabaseRuleItem databaseRuleItem) {
        return String.join("/", getRulePath(ruleType), databaseRuleItem.toString());
    }
    
    /**
     * Get database rule version node path generator.
     *
     * @param ruleType rule type
     * @param databaseRuleItem database rule item
     * @return database rule version node path generator
     */
    public VersionNodePathGenerator getVersion(final String ruleType, final DatabaseRuleItem databaseRuleItem) {
        return new VersionNodePathGenerator(getRulePath(ruleType, databaseRuleItem));
    }
}
