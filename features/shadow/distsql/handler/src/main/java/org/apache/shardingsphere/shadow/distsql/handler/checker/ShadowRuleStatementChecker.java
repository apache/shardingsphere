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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.external.sql.type.kernel.KernelSQLException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Shadow rule statement checker.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShadowRuleStatementChecker {
    
    /**
     * Check if storage units exist in meta data.
     *
     * @param requiredStorageUnits required storage units
     * @param database database
     */
    public static void checkStorageUnitsExist(final Collection<String> requiredStorageUnits, final ShardingSphereDatabase database) {
        Collection<String> notExistedStorageUnits = database.getResourceMetaData().getNotExistedDataSources(requiredStorageUnits);
        ShardingSpherePreconditions.checkMustEmpty(notExistedStorageUnits, () -> new MissingRequiredStorageUnitsException(database.getName(), notExistedStorageUnits));
    }
    
    /**
     * Check if there are duplicated rules.
     *
     * @param rules rules to be checked
     * @param thrower exception thrower
     */
    public static void checkDuplicated(final Collection<String> rules, final Function<Collection<String>, KernelSQLException> thrower) {
        Collection<String> duplicated = getDuplicated(rules);
        ShardingSpherePreconditions.checkMustEmpty(duplicated, () -> thrower.apply(duplicated));
    }
    
    /**
     * Check if there are duplicated rules.
     *
     * @param requiredRules required rules
     * @param currentRules current rules
     * @param thrower exception thrower
     */
    public static void checkDuplicated(final Collection<String> requiredRules, final Collection<String> currentRules, final Function<Collection<String>, KernelSQLException> thrower) {
        Collection<String> duplicated = getDuplicated(requiredRules, currentRules);
        ShardingSpherePreconditions.checkMustEmpty(duplicated, () -> thrower.apply(duplicated));
    }
    
    /**
     * Check the required rules existed.
     *
     * @param requiredRules required rules
     * @param currentRules current rules
     * @param thrower exception thrower
     */
    public static void checkExisted(final Collection<String> requiredRules, final Collection<String> currentRules, final Function<Collection<String>, KernelSQLException> thrower) {
        Collection<String> notExisted = getNotExisted(requiredRules, currentRules);
        ShardingSpherePreconditions.checkMustEmpty(notExisted, () -> thrower.apply(notExisted));
    }
    
    private static Collection<String> getDuplicated(final Collection<String> names) {
        return names.stream().collect(Collectors.groupingBy(each -> each, Collectors.counting())).entrySet().stream()
                .filter(each -> each.getValue() > 1L).map(Entry::getKey).collect(Collectors.toSet());
    }
    
    private static Collection<String> getDuplicated(final Collection<String> required, final Collection<String> current) {
        return required.stream().filter(current::contains).collect(Collectors.toSet());
    }
    
    private static Collection<String> getNotExisted(final Collection<String> required, final Collection<String> current) {
        return required.stream().filter(each -> !current.contains(each)).collect(Collectors.toSet());
    }
    
    /**
     * Check if there are duplicated names with logical data sources.
     *
     * @param toBeCreatedRuleNames rule names
     * @param database ShardingSphere database
     */
    public static void checkDuplicatedWithLogicDataSource(final Collection<String> toBeCreatedRuleNames, final ShardingSphereDatabase database) {
        Collection<String> logicDataSources = getLogicDataSources(database);
        if (!logicDataSources.isEmpty()) {
            Collection<String> duplicatedNames = toBeCreatedRuleNames.stream().filter(logicDataSources::contains).collect(Collectors.toList());
            ShardingSpherePreconditions.checkMustEmpty(duplicatedNames, () -> new InvalidRuleConfigurationException("shadow", duplicatedNames,
                    Collections.singleton(String.format("%s already exists in storage unit", duplicatedNames))));
        }
    }
    
    private static Collection<String> getLogicDataSources(final ShardingSphereDatabase database) {
        Collection<String> result = new LinkedHashSet<>();
        for (DataSourceMapperRuleAttribute each : database.getRuleMetaData().getAttributes(DataSourceMapperRuleAttribute.class)) {
            result.addAll(each.getDataSourceMapper().keySet());
        }
        return result;
    }
}
