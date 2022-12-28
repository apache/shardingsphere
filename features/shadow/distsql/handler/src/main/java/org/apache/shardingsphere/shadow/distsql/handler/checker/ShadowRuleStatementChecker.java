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

package org.apache.shardingsphere.shadow.distsql.handler.checker;

import org.apache.shardingsphere.distsql.handler.exception.DistSQLException;
import org.apache.shardingsphere.distsql.handler.exception.algorithm.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowAlgorithmSegment;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Shadow rule statement checker.
 */
public class ShadowRuleStatementChecker {
    
    /**
     * Check if the rule configuration exists.
     *
     * @param databaseName database name
     * @param ruleConfig rule configuration
     */
    public static void checkRuleConfigurationExists(final String databaseName, final ShadowRuleConfiguration ruleConfig) {
        ShardingSpherePreconditions.checkNotNull(ruleConfig, () -> new MissingRequiredRuleException("shadow", databaseName));
    }
    
    /**
     * Check if storage units exist in meta data.
     *
     * @param requiredStorageUnits required storage units
     * @param database database
     */
    public static void checkStorageUnitsExist(final Collection<String> requiredStorageUnits, final ShardingSphereDatabase database) {
        Collection<String> notExistedStorageUnits = database.getResourceMetaData().getNotExistedResources(requiredStorageUnits);
        ShardingSpherePreconditions.checkState(notExistedStorageUnits.isEmpty(), () -> new MissingRequiredStorageUnitsException(database.getName(), notExistedStorageUnits));
    }
    
    /**
     * Check the completeness of the algorithms.
     *
     * @param algorithmSegments to be checked segments
     */
    public static void checkAlgorithmCompleteness(final Collection<ShadowAlgorithmSegment> algorithmSegments) {
        Set<ShadowAlgorithmSegment> incompleteAlgorithms = algorithmSegments.stream().filter(each -> !each.isComplete()).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkState(incompleteAlgorithms.isEmpty(), () -> new InvalidAlgorithmConfigurationException("shadow"));
    }
    
    /**
     * Check if there are duplicated rules.
     * 
     * @param rules rules to be checked
     * @param thrower exception thrower
     */
    public static void checkDuplicated(final Collection<String> rules, final Function<Collection<String>, DistSQLException> thrower) {
        Collection<String> duplicated = getDuplicated(rules);
        ShardingSpherePreconditions.checkState(duplicated.isEmpty(), () -> thrower.apply(duplicated));
    }
    
    /**
     * Check if there are duplicated rules.
     *
     * @param requiredRules required rules
     * @param currentRules current rules
     * @param thrower exception thrower
     */
    public static void checkDuplicated(final Collection<String> requiredRules, final Collection<String> currentRules, final Function<Collection<String>, DistSQLException> thrower) {
        Collection<String> duplicated = getDuplicated(requiredRules, currentRules);
        ShardingSpherePreconditions.checkState(duplicated.isEmpty(), () -> thrower.apply(duplicated));
    }
    
    /**
     * Check the required rules existed.
     *
     * @param requiredRules required rules
     * @param currentRules current rules
     * @param thrower exception thrower
     */
    public static void checkExisted(final Collection<String> requiredRules, final Collection<String> currentRules, final Function<Collection<String>, DistSQLException> thrower) {
        Collection<String> notExisted = getNotExisted(requiredRules, currentRules);
        ShardingSpherePreconditions.checkState(notExisted.isEmpty(), () -> thrower.apply(notExisted));
    }
    
    private static Collection<String> getDuplicated(final Collection<String> names) {
        return names.stream().collect(Collectors.groupingBy(each -> each, Collectors.counting())).entrySet().stream()
                .filter(each -> each.getValue() > 1).map(Map.Entry::getKey).collect(Collectors.toSet());
    }
    
    private static Collection<String> getDuplicated(final Collection<String> required, final Collection<String> current) {
        return required.stream().filter(current::contains).collect(Collectors.toSet());
    }
    
    private static Collection<String> getNotExisted(final Collection<String> required, final Collection<String> current) {
        return required.stream().filter(each -> !current.contains(each)).collect(Collectors.toSet());
    }
}
