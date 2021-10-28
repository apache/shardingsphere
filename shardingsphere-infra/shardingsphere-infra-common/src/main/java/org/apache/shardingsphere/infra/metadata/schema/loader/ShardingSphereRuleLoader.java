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

package org.apache.shardingsphere.infra.metadata.schema.loader;

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRecognizer;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.schema.SchemaRulesBuilder;
import org.apache.shardingsphere.infra.rule.builder.schema.SchemaRulesBuilderMaterials;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * ShardingSphere Rule loader.
 */
public final class ShardingSphereRuleLoader {
    
    private final Map<String, Map<String, DataSource>> dataSources;
    
    private final Map<String, Collection<RuleConfiguration>> schemaRuleConfigs;
    
    private final ConfigurationProperties props;
    
    public ShardingSphereRuleLoader(final Map<String, Map<String, DataSource>> dataSources,
                        final Map<String, Collection<RuleConfiguration>> schemaRuleConfigs, final Properties props) {
        this.dataSources = dataSources;
        this.schemaRuleConfigs = schemaRuleConfigs;
        this.props = new ConfigurationProperties(null == props ? new Properties() : props);
    }
    
    /**
     * Load rules.
     *
     * @return rules
     */
    public Map<String, Collection<ShardingSphereRule>> load() {
        Map<String, Collection<ShardingSphereRule>> result = new HashMap<>(schemaRuleConfigs.size(), 1);
        for (String each : schemaRuleConfigs.keySet()) {
            Map<String, DataSource> dataSourceMap = dataSources.get(each);
            Collection<RuleConfiguration> ruleConfigs = schemaRuleConfigs.get(each);
            DatabaseType databaseType = DatabaseTypeRecognizer.getDatabaseType(dataSources.get(each).values());
            result.put(each, SchemaRulesBuilder.buildRules(new SchemaRulesBuilderMaterials(each, ruleConfigs, databaseType, dataSourceMap, props)));
        }
        return result;
    }
}
