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
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.builder.TableMetaDataBuilder;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Schema loader.
 */
public final class SchemaLoader {
    
    private final Map<String, Map<String, DataSource>> dataSources;
    
    private final Map<String, Collection<RuleConfiguration>> schemaRuleConfigs;
    
    private final Map<String, Collection<ShardingSphereRule>> rules;
    
    private final ConfigurationProperties props;
    
    public SchemaLoader(final Map<String, Map<String, DataSource>> dataSources,
                        final Map<String, Collection<RuleConfiguration>> schemaRuleConfigs, final Map<String, Collection<ShardingSphereRule>> rules, final Properties props) {
        this.dataSources = dataSources;
        this.schemaRuleConfigs = schemaRuleConfigs;
        this.rules = rules;
        this.props = new ConfigurationProperties(null == props ? new Properties() : props);
    }
    
    /**
     * Load schema.
     *
     * @return schema
     * @throws SQLException SQL exception
     */
    public Map<String, ShardingSphereSchema> load() throws SQLException {
        Map<String, ShardingSphereSchema> result = new HashMap<>(schemaRuleConfigs.size(), 1);
        for (String each : schemaRuleConfigs.keySet()) {
            Map<String, DataSource> dataSourceMap = dataSources.get(each);
            DatabaseType databaseType = DatabaseTypeRecognizer.getDatabaseType(dataSources.get(each).values());
            Map<String, TableMetaData> tableMetaDataMap = TableMetaDataBuilder.load(getAllTableNames(rules.get(each)), new SchemaBuilderMaterials(databaseType, dataSourceMap, rules.get(each), props));
            result.put(each, new ShardingSphereSchema(tableMetaDataMap));
        }
        return result;
    }
    
    private Collection<String> getAllTableNames(final Collection<ShardingSphereRule> rules) {
        return rules.stream().filter(rule -> rule instanceof TableContainedRule)
                .flatMap(shardingSphereRule -> ((TableContainedRule) shardingSphereRule).getTables().stream()).collect(Collectors.toSet());
    }
}
