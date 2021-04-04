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
import org.apache.shardingsphere.infra.metadata.auth.model.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPIRegistry;

import javax.sql.DataSource;
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
        ShardingSphereServiceLoader.register(ShardingSphereRuleBuilder.class);
    }
    
    /**
     * Build rules.
     *
     * @param schemaName schema name
     * @param ruleConfigurations rule configurations
     * @param databaseType database type
     * @param dataSourceMap data source map
     * @param users users
     * @return built rules
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Collection<ShardingSphereRule> build(final String schemaName, final Collection<RuleConfiguration> ruleConfigurations, 
                                                       final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereUser> users) {
        Map<RuleConfiguration, ShardingSphereRuleBuilder> builders = OrderedSPIRegistry.getRegisteredServices(ruleConfigurations, ShardingSphereRuleBuilder.class);
        Collection<ShardingSphereRule> result = new LinkedList<>();
        for (Entry<RuleConfiguration, ShardingSphereRuleBuilder> entry : builders.entrySet()) {
            result.add(entry.getValue().build(schemaName, dataSourceMap, databaseType, entry.getKey(), users, result));
        }
        return result;
    }
}
