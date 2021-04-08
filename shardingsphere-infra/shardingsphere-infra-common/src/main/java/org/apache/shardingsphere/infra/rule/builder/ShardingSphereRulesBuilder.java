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

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPIRegistry;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

/**
 * ShardingSphere rule builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingSphereRulesBuilder {
    
    static {
        ShardingSphereServiceLoader.register(SchemaRuleBuilder.class);
        ShardingSphereServiceLoader.register(GlobalRuleBuilder.class);
    }
    
    /**
     * Build schema rules.
     *
     * @param schemaName schema name
     * @param schemaRuleConfigurations schema rule configurations
     * @param databaseType database type
     * @param dataSourceMap data source map
     * @param users users
     * @return built schema rules
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Collection<ShardingSphereRule> buildSchemaRules(final String schemaName, final Collection<RuleConfiguration> schemaRuleConfigurations,
                                                                  final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereUser> users) {
        Map<RuleConfiguration, SchemaRuleBuilder> builders = OrderedSPIRegistry.getRegisteredServices(schemaRuleConfigurations, SchemaRuleBuilder.class);
        Collection<ShardingSphereRule> result = new LinkedList<>();
        for (Entry<RuleConfiguration, SchemaRuleBuilder> entry : builders.entrySet()) {
            result.add(entry.getValue().build(schemaName, dataSourceMap, databaseType, entry.getKey(), users, result));
        }
        return result;
    }
    
    /**
     * Build global rules.
     *
     * @param globalRuleConfigurations global rule configurations
     * @param mataDataMap mata data map
     * @param users users
     * @param schemaRules built schema rules
     * @return built global rules
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Collection<ShardingSphereRule> buildGlobalRules(final Collection<RuleConfiguration> globalRuleConfigurations, final Map<String, ShardingSphereMetaData> mataDataMap, 
                                                                  final Collection<ShardingSphereUser> users, final Collection<ShardingSphereRule> schemaRules) {
        Map<RuleConfiguration, GlobalRuleBuilder> builders = OrderedSPIRegistry.getRegisteredServices(globalRuleConfigurations, GlobalRuleBuilder.class);
        Collection<ShardingSphereRule> result = new LinkedList<>(schemaRules);
        for (Entry<RuleConfiguration, GlobalRuleBuilder> entry : builders.entrySet()) {
            DatabaseType databaseType = mataDataMap.isEmpty() ? new MySQLDatabaseType() : getDatabaseType(mataDataMap.values().iterator().next().getResource().getDataSources());
            result.add(entry.getValue().build(mataDataMap, databaseType, entry.getKey(), users, result));
        }
        return result;
    }
    
    private static DatabaseType getDatabaseType(final Map<String, DataSource> dataSourceMap) {
        DatabaseType result = null;
        for (DataSource each : dataSourceMap.values()) {
            DatabaseType databaseType = getDatabaseType(each);
            Preconditions.checkState(null == result || result == databaseType, String.format("Database type inconsistent with '%s' and '%s'", result, databaseType));
            result = databaseType;
        }
        return null == result ? DatabaseTypeRegistry.getDefaultDatabaseType() : result;
    }
    
    private static DatabaseType getDatabaseType(final DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            return DatabaseTypeRegistry.getDatabaseTypeByURL(connection.getMetaData().getURL());
        } catch (final SQLException ex) {
            return null;
        }
    }
}
