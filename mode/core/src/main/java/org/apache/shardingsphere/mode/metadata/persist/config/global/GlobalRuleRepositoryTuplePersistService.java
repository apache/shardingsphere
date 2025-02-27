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

package org.apache.shardingsphere.mode.metadata.persist.config.global;

import org.apache.shardingsphere.mode.metadata.persist.config.RuleRepositoryTuplePersistService;
import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.type.global.GlobalRuleNodePath;
import org.apache.shardingsphere.mode.node.path.type.version.VersionNodePath;
import org.apache.shardingsphere.mode.node.rule.tuple.RuleRepositoryTuple;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Global rule repository tuple persist service.
 */
public final class GlobalRuleRepositoryTuplePersistService {
    
    private final PersistRepository repository;
    
    private final RuleRepositoryTuplePersistService tuplePersistService;
    
    public GlobalRuleRepositoryTuplePersistService(final PersistRepository repository) {
        this.repository = repository;
        tuplePersistService = new RuleRepositoryTuplePersistService(repository);
    }
    
    /**
     * Load rule repository tuples.
     *
     * @return loaded tuples
     */
    public Collection<RuleRepositoryTuple> load() {
        Collection<RuleRepositoryTuple> result = new LinkedList<>();
        for (String each : repository.getChildrenKeys(NodePathGenerator.toPath(new GlobalRuleNodePath(null), false))) {
            result.add(load(each));
        }
        return result;
    }
    
    /**
     * Load rule repository tuple.
     *
     * @param ruleType rule type
     * @return loaded tuple
     */
    public RuleRepositoryTuple load(final String ruleType) {
        return tuplePersistService.load(new VersionNodePath(new GlobalRuleNodePath(ruleType)).getActiveVersionPath())
                .orElseThrow(() -> new IllegalStateException(String.format("Can not load rule type: %s", ruleType)));
    }
}
