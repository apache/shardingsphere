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

import org.apache.shardingsphere.mode.node.path.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.config.database.item.DatabaseRuleItem;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePathGenerator;

/**
 * Database rule item node path generator.
 */
public final class DatabaseRuleItemNodePathGenerator implements NodePathGenerator<DatabaseRuleItem> {
    
    private final DatabaseRuleNodePathGenerator databaseRuleNodePathGenerator;
    
    private final String ruleType;
    
    public DatabaseRuleItemNodePathGenerator(final String databaseName, final String ruleType) {
        databaseRuleNodePathGenerator = new DatabaseRuleNodePathGenerator(databaseName);
        this.ruleType = ruleType;
    }
    
    @Override
    public String getRootPath() {
        return databaseRuleNodePathGenerator.getPath(ruleType);
    }
    
    @Override
    public String getPath(final DatabaseRuleItem databaseRuleItem) {
        return String.join("/", getRootPath(), databaseRuleItem.toString());
    }
    
    /**
     * Get database rule item version node path generator.
     *
     * @param databaseRuleItem database rule item
     * @return database rule item version node path generator
     */
    public VersionNodePathGenerator getVersion(final DatabaseRuleItem databaseRuleItem) {
        return new VersionNodePathGenerator(getPath(databaseRuleItem));
    }
}
