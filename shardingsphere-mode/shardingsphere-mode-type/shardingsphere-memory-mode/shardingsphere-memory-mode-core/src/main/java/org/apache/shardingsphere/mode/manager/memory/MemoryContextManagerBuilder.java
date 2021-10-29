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

package org.apache.shardingsphere.mode.manager.memory;

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.loader.SchemaLoader;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.schema.SchemaRulesBuilder;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilder;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.MetaDataContextsBuilder;
import org.apache.shardingsphere.transaction.ShardingSphereTransactionManagerEngine;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.apache.shardingsphere.transaction.rule.builder.DefaultTransactionRuleConfigurationBuilder;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Memory context manager builder.
 */
public final class MemoryContextManagerBuilder implements ContextManagerBuilder {
    
    @Override
    public ContextManager build(final ModeConfiguration modeConfig, final Map<String, Map<String, DataSource>> dataSourcesMap,
                                final Map<String, Collection<RuleConfiguration>> schemaRuleConfigs, final Collection<RuleConfiguration> globalRuleConfigs,
                                final Properties props, final boolean isOverwrite, final Integer port) throws SQLException {
        Map<String, Collection<ShardingSphereRule>> rules = SchemaRulesBuilder.buildRules(dataSourcesMap, schemaRuleConfigs, props);
        Map<String, ShardingSphereSchema> schemas = new SchemaLoader(dataSourcesMap, schemaRuleConfigs, rules, props).load();
        MetaDataContexts metaDataContexts = new MetaDataContextsBuilder(dataSourcesMap, schemaRuleConfigs, globalRuleConfigs, schemas, rules, props).build(null);
        TransactionContexts transactionContexts = createTransactionContexts(metaDataContexts);
        ContextManager result = new ContextManager();
        result.init(metaDataContexts, transactionContexts);
        return result;
    }
    
    private TransactionContexts createTransactionContexts(final MetaDataContexts metaDataContexts) {
        Map<String, ShardingSphereTransactionManagerEngine> engines = new HashMap<>(metaDataContexts.getAllSchemaNames().size(), 1);
        TransactionRule transactionRule = getTransactionRule(metaDataContexts);
        for (String each : metaDataContexts.getAllSchemaNames()) {
            ShardingSphereTransactionManagerEngine engine = new ShardingSphereTransactionManagerEngine();
            ShardingSphereResource resource = metaDataContexts.getMetaData(each).getResource();
            engine.init(resource.getDatabaseType(), resource.getDataSources(), transactionRule);
            engines.put(each, engine);
        }
        return new TransactionContexts(engines);
    }
    
    private TransactionRule getTransactionRule(final MetaDataContexts metaDataContexts) {
        Optional<TransactionRule> transactionRule = metaDataContexts.getGlobalRuleMetaData().getRules().stream().filter(
            each -> each instanceof TransactionRule).map(each -> (TransactionRule) each).findFirst();
        return transactionRule.orElseGet(() -> new TransactionRule(new DefaultTransactionRuleConfigurationBuilder().build()));
    }
    
    @Override
    public String getType() {
        return "Memory";
    }
    
    @Override
    public boolean isDefault() {
        return true;
    }
}
