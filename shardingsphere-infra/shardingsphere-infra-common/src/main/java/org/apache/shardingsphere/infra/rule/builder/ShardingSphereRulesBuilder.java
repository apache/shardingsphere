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
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.aware.ResourceAware;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPIRegistry;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

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
     * @param ruleConfigurations rule configurations
     * @param databaseType database type
     * @param dataSourceMap data source map
     * @return rules
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Collection<ShardingSphereRule> build(final Collection<RuleConfiguration> ruleConfigurations, final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap) {
        Map<RuleConfiguration, ShardingSphereRuleBuilder> builders = OrderedSPIRegistry.getRegisteredServices(ruleConfigurations, ShardingSphereRuleBuilder.class);
        setResources(builders.values(), databaseType, dataSourceMap);
        return builders.entrySet().stream().map(entry -> entry.getValue().build(entry.getKey())).collect(Collectors.toList());
    }
    
    @SuppressWarnings("rawtypes")
    private static void setResources(final Collection<ShardingSphereRuleBuilder> builders, final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap) {
        for (ShardingSphereRuleBuilder each : builders) {
            if (each instanceof ResourceAware) {
                ((ResourceAware) each).setDatabaseType(databaseType);
                ((ResourceAware) each).setDataSourceMap(dataSourceMap);
            }
        }
    }
}
