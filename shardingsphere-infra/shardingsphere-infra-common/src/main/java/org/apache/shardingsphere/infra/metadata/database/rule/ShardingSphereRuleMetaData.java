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

package org.apache.shardingsphere.infra.metadata.database.rule;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * ShardingSphere rule meta data.
 */
@Getter
public final class ShardingSphereRuleMetaData {
    
    private final Collection<ShardingSphereRule> rules;
    
    public ShardingSphereRuleMetaData(final Collection<ShardingSphereRule> rules) {
        this.rules = new CopyOnWriteArrayList<>(rules);
    }
    
    /**
     * Get rule configurations.
     * 
     * @return got rule configurations
     */
    public Collection<RuleConfiguration> getConfigurations() {
        return rules.stream().map(ShardingSphereRule::getConfiguration).collect(Collectors.toList());
    }
    
    /**
     * Find rules by class.
     *
     * @param clazz target class
     * @param <T> type of rule
     * @return found rules
     */
    public <T extends ShardingSphereRule> Collection<T> findRules(final Class<T> clazz) {
        List<T> result = new LinkedList<>();
        for (ShardingSphereRule each : rules) {
            if (clazz.isAssignableFrom(each.getClass())) {
                result.add(clazz.cast(each));
            }
        }
        return result;
    }
    
    /**
     * Find single rule by class.
     *
     * @param clazz target class
     * @param <T> type of rule
     * @return found single rule
     */
    public <T extends ShardingSphereRule> Optional<T> findSingleRule(final Class<T> clazz) {
        Collection<T> foundRules = findRules(clazz);
        return foundRules.isEmpty() ? Optional.empty() : Optional.of(foundRules.iterator().next());
    }
    
    /**
     * Find single rule by class.
     *
     * @param clazz target class
     * @param <T> type of rule
     * @return found single rule
     */
    public <T extends ShardingSphereRule> T getSingleRule(final Class<T> clazz) {
        Collection<T> foundRules = findRules(clazz);
        Preconditions.checkState(1 == foundRules.size(), "Rule `%s` should have and only have one instance.", clazz.getSimpleName());
        return foundRules.iterator().next();
    }
}
