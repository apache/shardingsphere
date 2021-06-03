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

package org.apache.shardingsphere.infra.rule.builder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.level.DefaultKernelRuleConfigurationBuilder;
import org.apache.shardingsphere.infra.rule.builder.level.KernelRuleBuilder;
import org.apache.shardingsphere.infra.rule.builder.scope.GlobalRuleBuilder;
import org.apache.shardingsphere.infra.rule.builder.scope.SchemaRuleBuilder;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPIRegistry;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * ShardingSphere rule builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingSphereRulesBuilder {
    
    static {
        ShardingSphereServiceLoader.register(SchemaRuleBuilder.class);
        ShardingSphereServiceLoader.register(GlobalRuleBuilder.class);
        ShardingSphereServiceLoader.register(DefaultKernelRuleConfigurationBuilder.class);
    }
    
    /**
     * Build schema rules.
     *
     * @param schemaName schema name
     * @param schemaRuleConfigurations schema rule configurations
     * @param databaseType database type
     * @param dataSourceMap data source map
     * @return built schema rules
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Collection<ShardingSphereRule> buildSchemaRules(final String schemaName, final Collection<RuleConfiguration> schemaRuleConfigurations,
                                                                  final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap) {
        Map<RuleConfiguration, SchemaRuleBuilder> builders = OrderedSPIRegistry.getRegisteredServices(schemaRuleConfigurations, SchemaRuleBuilder.class);
        appendDefaultKernelSchemaRuleConfigurationBuilder(builders);
        return builders.entrySet().stream().map(entry -> entry.getValue().build(schemaName, dataSourceMap, databaseType, entry.getKey())).collect(Collectors.toList());
    }
    
    @SuppressWarnings("rawtypes")
    private static void appendDefaultKernelSchemaRuleConfigurationBuilder(final Map<RuleConfiguration, SchemaRuleBuilder> builders) {
        Map<SchemaRuleBuilder, DefaultKernelRuleConfigurationBuilder> defaultBuilders = 
                OrderedSPIRegistry.getRegisteredServices(getMissedKernelSchemaRuleBuilders(builders.values()), DefaultKernelRuleConfigurationBuilder.class);
        // TODO consider about order for new put items
        for (Entry<SchemaRuleBuilder, DefaultKernelRuleConfigurationBuilder> entry : defaultBuilders.entrySet()) {
            builders.put(entry.getValue().build(), entry.getKey());
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Collection<SchemaRuleBuilder> getMissedKernelSchemaRuleBuilders(final Collection<SchemaRuleBuilder> configuredBuilders) {
        Collection<Class<SchemaRuleBuilder>> configuredBuilderClasses = configuredBuilders.stream().map(each -> (Class<SchemaRuleBuilder>) each.getClass()).collect(Collectors.toSet());
        return OrderedSPIRegistry.getRegisteredServices(SchemaRuleBuilder.class).stream().filter(
            each -> each instanceof KernelRuleBuilder && !configuredBuilderClasses.contains(each.getClass())).collect(Collectors.toList());
    }
    
    /**
     * Build global rules.
     *
     * @param globalRuleConfigurations global rule configurations
     * @param mataDataMap mata data map
     * @return built global rules
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Collection<ShardingSphereRule> buildGlobalRules(final Collection<RuleConfiguration> globalRuleConfigurations, 
                                                                  final Map<String, ShardingSphereMetaData> mataDataMap) {
        Map<RuleConfiguration, GlobalRuleBuilder> builders = OrderedSPIRegistry.getRegisteredServices(globalRuleConfigurations, GlobalRuleBuilder.class);
        appendDefaultKernelGlobalRuleConfigurationBuilder(builders);
        Collection<ShardingSphereRule> result = new LinkedList<>();
        for (Entry<RuleConfiguration, GlobalRuleBuilder> entry : builders.entrySet()) {
            result.add(entry.getValue().build(entry.getKey(), mataDataMap));
        }
        return result;
    }
    
    @SuppressWarnings("rawtypes")
    private static void appendDefaultKernelGlobalRuleConfigurationBuilder(final Map<RuleConfiguration, GlobalRuleBuilder> builders) {
        Map<GlobalRuleBuilder, DefaultKernelRuleConfigurationBuilder> defaultBuilders =
                OrderedSPIRegistry.getRegisteredServices(getMissedKernelGlobalRuleBuilders(builders.values()), DefaultKernelRuleConfigurationBuilder.class);
        // TODO consider about order for new put items
        for (Entry<GlobalRuleBuilder, DefaultKernelRuleConfigurationBuilder> entry : defaultBuilders.entrySet()) {
            builders.put(entry.getValue().build(), entry.getKey());
        }
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Collection<GlobalRuleBuilder> getMissedKernelGlobalRuleBuilders(final Collection<GlobalRuleBuilder> configuredBuilders) {
        Collection<Class<GlobalRuleBuilder>> configuredBuilderClasses = configuredBuilders.stream().map(each -> (Class<GlobalRuleBuilder>) each.getClass()).collect(Collectors.toSet());
        return OrderedSPIRegistry.getRegisteredServices(GlobalRuleBuilder.class).stream().filter(
            each -> each instanceof KernelRuleBuilder && !configuredBuilderClasses.contains(each.getClass())).collect(Collectors.toList());
    }
}
