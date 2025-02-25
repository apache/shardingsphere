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

package org.apache.shardingsphere.mode.metadata.changed;

import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mode.metadata.changed.executor.RuleItemChangedBuildExecutor;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.apache.shardingsphere.mode.node.path.type.metadata.rule.DatabaseRuleNodePath;
import org.apache.shardingsphere.mode.node.spi.DatabaseRuleNodeProvider;
import org.apache.shardingsphere.mode.spi.rule.item.RuleChangedItem;

import java.util.Optional;

/**
 * Rule item changed builder.
 */
public final class RuleItemChangedBuilder {
    
    /**
     * Build rule item changed.
     *
     * @param databaseName database name
     * @param path path
     * @param activeVersion active version
     * @param executor rule item changed build executor
     * @param <T> type of rule changed item
     * @return built rule item
     */
    public <T extends RuleChangedItem> Optional<T> build(final String databaseName, final String path, final Integer activeVersion, final RuleItemChangedBuildExecutor<T> executor) {
        for (DatabaseRuleNodeProvider each : ShardingSphereServiceLoader.getServiceInstances(DatabaseRuleNodeProvider.class)) {
            if (!NodePathSearcher.isMatchedPath(path, DatabaseRuleNodePath.createValidRuleTypeSearchCriteria(each.getDatabaseRuleNode().getRuleType()))) {
                continue;
            }
            Optional<T> result = executor.build(each.getDatabaseRuleNode(), databaseName, path, activeVersion);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }
}
