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

package org.apache.shardingsphere.mode.metadata.changed.executor;

import org.apache.shardingsphere.mode.node.path.type.config.database.DatabaseRuleNodePath;
import org.apache.shardingsphere.mode.spi.rule.item.RuleChangedItem;

import java.util.Optional;

/**
 * Rule item changed build executor.
 * 
 * @param <T> type of rule changed item
 */
public interface RuleItemChangedBuildExecutor<T extends RuleChangedItem> {
    
    /**
     * Build rule item.
     *
     * @param databaseRuleNodePath rule node path
     * @param databaseName database name
     * @param path path
     * @param activeVersion active version
     * @return built rule item
     */
    Optional<T> build(DatabaseRuleNodePath databaseRuleNodePath, String databaseName, String path, Integer activeVersion);
}
