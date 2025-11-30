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

package org.apache.shardingsphere.mode.metadata.manager.rule;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.PartialRuleUpdateSupported;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.database.DatabaseRulesBuilder;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.factory.MetaDataContextsFactory;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Database rule configuration manager.
 */
@RequiredArgsConstructor
public final class DatabaseRuleConfigurationManager {
    
    private final MetaDataContexts metaDataContexts;
    
    private final ComputeNodeInstanceContext computeNodeInstanceContext;
    
    private final MetaDataPersistFacade metaDataPersistFacade;
    
    /**
     * Refresh rule configuration.
     *
     * @param databaseName database name
     * @param ruleConfig rule configurations
     * @param reBuildRules is need to rebuild rules
     * @throws SQLException SQL Exception
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public synchronized void refresh(final String databaseName, final RuleConfiguration ruleConfig, final boolean reBuildRules) throws SQLException {
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(databaseName);
        Collection<ShardingSphereRule> rules = new LinkedList<>(database.getRuleMetaData().getRules());
        Optional<ShardingSphereRule> toBeChangedRule = rules.stream().filter(each -> each.getConfiguration().getClass().equals(ruleConfig.getClass())).findFirst();
        if (toBeChangedRule.isPresent() && toBeChangedRule.get() instanceof PartialRuleUpdateSupported) {
            boolean needRefreshSchemas = ((PartialRuleUpdateSupported) toBeChangedRule.get()).partialUpdate(ruleConfig);
            ((PartialRuleUpdateSupported) toBeChangedRule.get()).updateConfiguration(ruleConfig);
            if (needRefreshSchemas) {
                refreshMetadata(databaseName, ruleConfig, reBuildRules, database, rules);
            }
        } else {
            refreshMetadata(databaseName, ruleConfig, reBuildRules, database, rules);
        }
    }
    
    private void refreshMetadata(final String databaseName, final RuleConfiguration ruleConfig, final boolean reBuildRules, final ShardingSphereDatabase database,
                                 final Collection<ShardingSphereRule> rules) throws SQLException {
        Collection<ShardingSphereRule> toBeRemovedRules = rules.stream().filter(each -> each.getConfiguration().getClass().isAssignableFrom(ruleConfig.getClass())).collect(Collectors.toList());
        rules.removeAll(toBeRemovedRules);
        if (reBuildRules) {
            rules.add(DatabaseRulesBuilder.build(
                    databaseName, database.getProtocolType(), database.getRuleMetaData().getRules(), ruleConfig, computeNodeInstanceContext, database.getResourceMetaData()));
        }
        refreshMetadata(databaseName, rules, toBeRemovedRules);
    }
    
    private void refreshMetadata(final String databaseName, final Collection<ShardingSphereRule> rules, final Collection<ShardingSphereRule> toBeRemovedRules) throws SQLException {
        Collection<RuleConfiguration> ruleConfigs = rules.stream().map(ShardingSphereRule::getConfiguration).collect(Collectors.toList());
        metaDataContexts.update(new MetaDataContextsFactory(metaDataPersistFacade, computeNodeInstanceContext).createByAlterRule(databaseName, false, ruleConfigs, metaDataContexts));
        closeOriginalRules(toBeRemovedRules);
    }
    
    @SneakyThrows(Exception.class)
    private void closeOriginalRules(final Collection<ShardingSphereRule> toBeRemovedRules) {
        for (ShardingSphereRule each : toBeRemovedRules) {
            if (each instanceof AutoCloseable) {
                ((AutoCloseable) each).close();
            }
        }
    }
}
