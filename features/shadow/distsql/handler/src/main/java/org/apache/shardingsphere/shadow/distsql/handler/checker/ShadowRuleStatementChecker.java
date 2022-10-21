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

import org.apache.shardingsphere.infra.config.rule.scope.DatabaseRuleConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.resource.MissingRequiredResourcesException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
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
    
    public static final String SHADOW = "shadow";
    
    /**
     * Check if the configuration exists.
     *
     * @param databaseName database name
     * @param config configuration
     */
    public static void checkConfigurationExist(final String databaseName, final DatabaseRuleConfiguration config) {
        ShardingSpherePreconditions.checkNotNull(config, () -> new MissingRequiredRuleException(SHADOW, databaseName));
    }
    
    /**
     * Check if resources exist in meta data.
     *
     * @param resources resource being checked
     * @param database database
     */
    public static void checkResourceExist(final Collection<String> resources, final ShardingSphereDatabase database) {
        Collection<String> notExistedResources = database.getResourceMetaData().getNotExistedResources(resources);
        ShardingSpherePreconditions.checkState(notExistedResources.isEmpty(), () -> new MissingRequiredResourcesException(database.getName(), notExistedResources));
    }
    
    /**
     * Check the completeness of the algorithm.
     *
     * @param algorithmSegments algorithmSegments to be checked
     */
    public static void checkAlgorithmCompleteness(final Collection<ShadowAlgorithmSegment> algorithmSegments) {
        Set<ShadowAlgorithmSegment> incompleteAlgorithms = algorithmSegments.stream().filter(each -> !each.isComplete()).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkState(incompleteAlgorithms.isEmpty(), () -> new InvalidAlgorithmConfigurationException(SHADOW));
    }
    
    /**
     * Check if the rules exist.
     * 
     * @param requireRules require rules
     * @param currentRules current rules
     * @param thrower thrower
     */
    public static void checkRulesExist(final Collection<String> requireRules,
                                       final Collection<String> currentRules, final Function<Collection<String>, DistSQLException> thrower) {
        ShadowRuleStatementChecker.checkAnyDifferent(requireRules, currentRules, thrower);
    }
    
    /**
     * Check if the algorithms exist.
     * 
     * @param requireAlgorithms require algorithms
     * @param currentAlgorithms current algorithms
     * @param thrower thrower
     */
    public static void checkAlgorithmExist(final Collection<String> requireAlgorithms,
                                           final Collection<String> currentAlgorithms, final Function<Collection<String>, DistSQLException> thrower) {
        ShadowRuleStatementChecker.checkAnyDifferent(requireAlgorithms, currentAlgorithms, thrower);
    }
    
    /**
     * Check for any duplicate data in the rules, and throw the specified exception.
     * 
     * @param rules rules to be checked
     * @param thrower exception thrower
     */
    public static void checkAnyDuplicate(final Collection<String> rules, final Function<Collection<String>, DistSQLException> thrower) {
        Collection<String> duplicateRequire = getDuplicate(rules);
        ShardingSpherePreconditions.checkState(duplicateRequire.isEmpty(), () -> thrower.apply(duplicateRequire));
    }
    
    /**
     * Check if there are duplicates in the rules, and throw the specified exception.
     *
     * @param requireRules rules to be checked
     * @param currentRules rules to be checked
     * @param thrower exception thrower
     */
    public static void checkAnyDuplicate(final Collection<String> requireRules,
                                         final Collection<String> currentRules, final Function<Collection<String>, DistSQLException> thrower) {
        Collection<String> identical = getIdentical(requireRules, currentRules);
        ShardingSpherePreconditions.checkState(identical.isEmpty(), () -> thrower.apply(identical));
    }
    
    /**
     * Check for any different data in the rules, and throw the specified exception.
     *
     * @param requireRules rules to be checked
     * @param currentRules rules to be checked
     * @param thrower exception thrower
     */
    public static void checkAnyDifferent(final Collection<String> requireRules,
                                         final Collection<String> currentRules, final Function<Collection<String>, DistSQLException> thrower) {
        Collection<String> different = getDifferent(requireRules, currentRules);
        ShardingSpherePreconditions.checkState(different.isEmpty(), () -> thrower.apply(different));
    }
    
    private static Collection<String> getDuplicate(final Collection<String> require) {
        return require.stream().collect(Collectors.groupingBy(each -> each, Collectors.counting())).entrySet().stream()
                .filter(each -> each.getValue() > 1).map(Map.Entry::getKey).collect(Collectors.toSet());
    }
    
    private static Collection<String> getDifferent(final Collection<String> require, final Collection<String> current) {
        return require.stream().filter(each -> !current.contains(each)).collect(Collectors.toSet());
    }
    
    private static Collection<String> getIdentical(final Collection<String> require, final Collection<String> current) {
        return require.stream().filter(current::contains).collect(Collectors.toSet());
    }
}
