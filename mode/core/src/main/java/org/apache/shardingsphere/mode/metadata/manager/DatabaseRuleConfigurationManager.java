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

package org.apache.shardingsphere.mode.metadata.manager;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.scope.DatabaseRuleConfiguration;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.PartialRuleUpdateSupported;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.database.DatabaseRulesBuilder;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.MetaDataContextsFactory;
import org.apache.shardingsphere.mode.spi.PersistRepository;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Database rule configuration manager.
 */
@Slf4j
public final class DatabaseRuleConfigurationManager {
    
    private final AtomicReference<MetaDataContexts> metaDataContexts;
    
    private final ComputeNodeInstanceContext computeNodeInstanceContext;
    
    private final MetaDataPersistService metaDataPersistService;
    
    public DatabaseRuleConfigurationManager(final AtomicReference<MetaDataContexts> metaDataContexts, final ComputeNodeInstanceContext computeNodeInstanceContext,
                                            final PersistRepository repository) {
        this.metaDataContexts = metaDataContexts;
        this.computeNodeInstanceContext = computeNodeInstanceContext;
        metaDataPersistService = new MetaDataPersistService(repository);
    }
    
    /**
     * Alter rule configuration.
     *
     * @param databaseName database name
     * @param ruleConfig rule configurations
     * @throws SQLException SQL Exception
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public synchronized void alterRuleConfiguration(final String databaseName, final RuleConfiguration ruleConfig) throws SQLException {
        ShardingSphereDatabase database = metaDataContexts.get().getMetaData().getDatabase(databaseName);
        Collection<ShardingSphereRule> rules = new LinkedList<>(database.getRuleMetaData().getRules());
        Optional<ShardingSphereRule> toBeChangedRule = rules.stream().filter(each -> each.getConfiguration().getClass().equals(ruleConfig.getClass())).findFirst();
        if (toBeChangedRule.isPresent() && toBeChangedRule.get() instanceof PartialRuleUpdateSupported && ((PartialRuleUpdateSupported) toBeChangedRule.get()).partialUpdate(ruleConfig)) {
            ((PartialRuleUpdateSupported) toBeChangedRule.get()).updateConfiguration(ruleConfig);
            return;
        }
        rules.removeIf(each -> each.getConfiguration().getClass().isAssignableFrom(ruleConfig.getClass()));
        rules.addAll(DatabaseRulesBuilder.build(databaseName, database.getProtocolType(), database.getRuleMetaData().getRules(),
                ruleConfig, computeNodeInstanceContext, database.getResourceMetaData()));
        refreshMetadata(databaseName, database, rules);
    }
    
    /**
     * Drop rule configuration.
     *
     * @param databaseName database name
     * @param ruleConfig rule configurations
     * @throws SQLException SQL Exception
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public synchronized void dropRuleConfiguration(final String databaseName, final RuleConfiguration ruleConfig) throws SQLException {
        ShardingSphereDatabase database = metaDataContexts.get().getMetaData().getDatabase(databaseName);
        Collection<ShardingSphereRule> rules = new LinkedList<>(database.getRuleMetaData().getRules());
        Optional<ShardingSphereRule> toBeChangedRule = rules.stream().filter(each -> each.getConfiguration().getClass().equals(ruleConfig.getClass())).findFirst();
        if (toBeChangedRule.isPresent() && toBeChangedRule.get() instanceof PartialRuleUpdateSupported && ((PartialRuleUpdateSupported) toBeChangedRule.get()).partialUpdate(ruleConfig)) {
            ((PartialRuleUpdateSupported) toBeChangedRule.get()).updateConfiguration(ruleConfig);
            return;
        }
        rules.removeIf(each -> each.getConfiguration().getClass().isAssignableFrom(ruleConfig.getClass()));
        if (!((DatabaseRuleConfiguration) ruleConfig).isEmpty()) {
            rules.addAll(DatabaseRulesBuilder.build(databaseName, database.getProtocolType(), database.getRuleMetaData().getRules(),
                    ruleConfig, computeNodeInstanceContext, database.getResourceMetaData()));
        }
        refreshMetadata(databaseName, database, rules);
    }
    
    private void refreshMetadata(final String databaseName, final ShardingSphereDatabase database, final Collection<ShardingSphereRule> rules) throws SQLException {
        database.getRuleMetaData().getRules().clear();
        database.getRuleMetaData().getRules().addAll(rules);
        MetaDataContexts reloadMetaDataContexts = MetaDataContextsFactory.createByAlterRule(databaseName, false,
                database.getRuleMetaData().getConfigurations(), metaDataContexts.get(), metaDataPersistService, computeNodeInstanceContext);
        metaDataContexts.set(reloadMetaDataContexts);
        metaDataContexts.get().getMetaData().getDatabase(databaseName).getSchemas().putAll(buildShardingSphereSchemas(metaDataContexts.get().getMetaData().getDatabase(databaseName)));
    }
    
    private Map<String, ShardingSphereSchema> buildShardingSphereSchemas(final ShardingSphereDatabase database) {
        Map<String, ShardingSphereSchema> result = new LinkedHashMap<>(database.getSchemas().size(), 1F);
        database.getSchemas().forEach((key, value) -> result.put(key, new ShardingSphereSchema(value.getTables(), value.getViews())));
        return result;
    }
}
