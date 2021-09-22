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

import org.apache.shardingsphere.infra.config.scope.SchemaRuleConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.resource.RequiredResourceMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
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
     * @param schemaName schema name
     * @param configuration configuration
     * @throws DistSQLException DistSQL exception
     */
    public static void checkConfigurationExist(final String schemaName, final SchemaRuleConfiguration configuration) throws DistSQLException {
        DistSQLException.predictionThrow(null != configuration, new RequiredRuleMissedException(SHADOW, schemaName));
    }
    
    /**
     * Check if resources exists in meta data.
     *
     * @param resources resource being checked
     * @param metaData meta data
     * @param schemaName schema name
     * @throws DistSQLException DistSQL exception
     */
    public static void checkResourceExist(final Collection<String> resources, final ShardingSphereMetaData metaData, final String schemaName) throws DistSQLException {
        Collection<String> notExistedResources = metaData.getResource().getNotExistedResources(resources);
        DistSQLException.predictionThrow(notExistedResources.isEmpty(), new RequiredResourceMissedException(schemaName, notExistedResources));
    }
    
    /**
     * Check the completeness of the algorithm.
     *
     * @param algorithmSegments algorithmSegments being checked
     * @throws DistSQLException DistSQL exception
     */
    public static void checkAlgorithmCompleteness(final Collection<ShadowAlgorithmSegment> algorithmSegments) throws DistSQLException {
        Set<ShadowAlgorithmSegment> incompleteAlgorithms = algorithmSegments.stream().filter(each -> !each.isComplete()).collect(Collectors.toSet());
        DistSQLException.predictionThrow(incompleteAlgorithms.isEmpty(), new InvalidAlgorithmConfigurationException(SHADOW));
    }
    
    /**
     * Check whether the data is duplicated, and throw an exception if there is duplicate data.
     *
     * @param data data being checked
     * @param thrower exception thrower
     * @throws DistSQLException DistSQL exception
     */
    public static void checkDuplicate(final Collection<String> data, final Function<Set<String>, DistSQLException> thrower) throws DistSQLException {
        Set<String> duplicateRequire = getDuplicate(data);
        DistSQLException.predictionThrow(duplicateRequire.isEmpty(), thrower.apply(duplicateRequire));
    }
    
    /**
     * Check whether the two data are different, and throw an exception if there are different data.
     *
     * @param requireData data being checked
     * @param currentData data being checked
     * @param thrower exception thrower
     * @throws DistSQLException DistSQL exception
     */
    public static void checkDifferent(final Collection<String> requireData, final Collection<String> currentData, final Function<Set<String>, DistSQLException> thrower) throws DistSQLException {
        Set<String> different = getDifferent(requireData, currentData);
        DistSQLException.predictionThrow(different.isEmpty(), thrower.apply(different));
    }
    
    /**
     * Check whether the two data are identical, and throw an exception if there are identical data.
     *
     * @param requireData data being checked
     * @param currentData data being checked
     * @param thrower exception thrower
     * @throws DistSQLException DistSQL exception
     */
    public static void checkIdentical(final Collection<String> requireData, final Collection<String> currentData, final Function<Set<String>, DistSQLException> thrower) throws DistSQLException {
        Set<String> identical = getIdentical(requireData, currentData);
        DistSQLException.predictionThrow(identical.isEmpty(), thrower.apply(identical));
    }
    
    private static Set<String> getDuplicate(final Collection<String> requires) {
        return requires.stream().collect(Collectors.groupingBy(each -> each, Collectors.counting())).entrySet().stream()
                .filter(each -> each.getValue() > 1).map(Map.Entry::getKey).collect(Collectors.toSet());
    }
    
    private static Set<String> getDifferent(final Collection<String> require, final Collection<String> current) {
        return require.stream().filter(each -> !current.contains(each)).collect(Collectors.toSet());
    }
    
    private static Set<String> getIdentical(final Collection<String> require, final Collection<String> current) {
        return require.stream().filter(current::contains).collect(Collectors.toSet());
    }
}
