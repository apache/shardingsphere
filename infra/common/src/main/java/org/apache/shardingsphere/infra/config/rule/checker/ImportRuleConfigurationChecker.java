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
import org.apache.shardingsphere.infra.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Import rule configuration checker.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ImportRuleConfigurationChecker {
    
    /**
     * Check rule.
     * 
     * @param ruleConfig rule configuration
     * @param database database
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void checkRule(final RuleConfiguration ruleConfig, final ShardingSphereDatabase database) {
        Optional<ImportRuleConfigurationProvider> importProvider = TypedSPILoader.findService(ImportRuleConfigurationProvider.class, ruleConfig.getClass());
        if (importProvider.isPresent()) {
            Collection<String> requiredDataSourceNames = importProvider.get().getRequiredDataSourceNames(ruleConfig);
            if (!requiredDataSourceNames.isEmpty()) {
                checkDataSourcesExisted(database, requiredDataSourceNames);
            }
            importProvider.get().check(database.getName(), ruleConfig);
        }
    }
    
    private static void checkDataSourcesExisted(final ShardingSphereDatabase database, final Collection<String> requiredDataSourceNames) {
        Collection<String> notExistedDataSources = database.getResourceMetaData().getNotExistedDataSources(requiredDataSourceNames);
        Collection<String> logicDataSources = database.getRuleMetaData().getAttributes(DataSourceMapperRuleAttribute.class).stream()
                .flatMap(each -> each.getDataSourceMapper().keySet().stream()).collect(Collectors.toSet());
        notExistedDataSources.removeIf(logicDataSources::contains);
        ShardingSpherePreconditions.checkState(notExistedDataSources.isEmpty(), () -> new MissingRequiredStorageUnitsException(database.getName(), notExistedDataSources));
    }
}
