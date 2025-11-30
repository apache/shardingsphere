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

package org.apache.shardingsphere.infra.config.rule.checker;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Database rule configuration check engine.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseRuleConfigurationCheckEngine {
    
    /**
     * Check rule configuration.
     *
     * @param ruleConfig rule configuration to be checked
     * @param database database
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void check(final RuleConfiguration ruleConfig, final ShardingSphereDatabase database) {
        DatabaseRuleConfigurationChecker checker = OrderedSPILoader.getServicesByClass(DatabaseRuleConfigurationChecker.class, Collections.singleton(ruleConfig.getClass())).get(ruleConfig.getClass());
        if (null == checker) {
            // TODO Remove after implementing the checker of BroadcastRuleConfiguration and SingleRuleConfiguration
            return;
        }
        Collection<String> requiredDataSourceNames = checker.getRequiredDataSourceNames(ruleConfig);
        if (!requiredDataSourceNames.isEmpty()) {
            database.checkStorageUnitsExisted(requiredDataSourceNames);
        }
        Collection<String> tableNames = checker.getTableNames(ruleConfig);
        if (!tableNames.isEmpty()) {
            checkTablesNotDuplicated(ruleConfig, database.getName(), tableNames);
        }
        Map<String, DataSource> dataSources = database.getResourceMetaData().getStorageUnits().entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getDataSource()));
        checker.check(database.getName(), ruleConfig, dataSources, database.getRuleMetaData().getRules());
    }
    
    private static void checkTablesNotDuplicated(final RuleConfiguration ruleConfig, final String databaseName, final Collection<String> tableNames) {
        Collection<String> duplicatedTables = tableNames.stream()
                .collect(Collectors.groupingBy(each -> each, Collectors.counting())).entrySet().stream().filter(each -> each.getValue() > 1L).map(Entry::getKey).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkMustEmpty(duplicatedTables, () -> new DuplicateRuleException(getRuleType(ruleConfig), databaseName, duplicatedTables));
    }
    
    private static String getRuleType(final RuleConfiguration ruleConfig) {
        String ruleConfigClassName = ruleConfig.getClass().getSimpleName();
        return ruleConfigClassName.substring(0, ruleConfigClassName.indexOf("RuleConfiguration"));
    }
}
