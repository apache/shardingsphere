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

package org.apache.shardingsphere.infra.metadata.database;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.identifier.ShardingSphereIdentifier;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.datanode.MutableDataNodeRuleAttribute;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * ShardingSphere database.
 */
@Getter
public final class ShardingSphereDatabase {
    
    private final String name;
    
    private final DatabaseType protocolType;
    
    private final ResourceMetaData resourceMetaData;
    
    private final RuleMetaData ruleMetaData;
    
    @Getter(AccessLevel.NONE)
    private final Map<ShardingSphereIdentifier, ShardingSphereSchema> schemas;
    
    public ShardingSphereDatabase(final String name, final DatabaseType protocolType, final ResourceMetaData resourceMetaData,
                                  final RuleMetaData ruleMetaData, final Collection<ShardingSphereSchema> schemas) {
        this.name = name;
        this.protocolType = protocolType;
        this.resourceMetaData = resourceMetaData;
        this.ruleMetaData = ruleMetaData;
        this.schemas = new ConcurrentHashMap<>(schemas.stream().collect(Collectors.toMap(each -> new ShardingSphereIdentifier(each.getName()), each -> each)));
    }
    
    /**
     * Get all schemas.
     *
     * @return all schemas
     */
    public Collection<ShardingSphereSchema> getAllSchemas() {
        return schemas.values();
    }
    
    /**
     * Judge contains schema from database or not.
     *
     * @param schemaName schema name
     * @return contains schema from database or not
     */
    public boolean containsSchema(final String schemaName) {
        return schemas.containsKey(new ShardingSphereIdentifier(schemaName));
    }
    
    /**
     * Get schema.
     *
     * @param schemaName schema name
     * @return schema
     */
    public ShardingSphereSchema getSchema(final String schemaName) {
        return schemas.get(new ShardingSphereIdentifier(schemaName));
    }
    
    /**
     * Add schema.
     *
     * @param schema schema
     */
    public void addSchema(final ShardingSphereSchema schema) {
        schemas.put(new ShardingSphereIdentifier(schema.getName()), schema);
    }
    
    /**
     * Drop schema.
     *
     * @param schemaName schema name
     */
    public void dropSchema(final String schemaName) {
        schemas.remove(new ShardingSphereIdentifier(schemaName));
    }
    
    /**
     * Judge whether is completed.
     *
     * @return is completed or not
     */
    public boolean isComplete() {
        return !ruleMetaData.getRules().isEmpty() && !resourceMetaData.getStorageUnits().isEmpty();
    }
    
    /**
     * Judge whether contains data source.
     *
     * @return contains data source or not
     */
    public boolean containsDataSource() {
        return !resourceMetaData.getStorageUnits().isEmpty();
    }
    
    /**
     * Reload rules.
     */
    public synchronized void reloadRules() {
        Collection<ShardingSphereRule> toBeReloadedRules = ruleMetaData.getRules().stream()
                .filter(each -> each.getAttributes().findAttribute(MutableDataNodeRuleAttribute.class).isPresent()).collect(Collectors.toList());
        RuleConfiguration ruleConfig = toBeReloadedRules.stream().map(ShardingSphereRule::getConfiguration).findFirst().orElse(null);
        Collection<ShardingSphereRule> rules = new LinkedList<>(ruleMetaData.getRules());
        toBeReloadedRules.stream().findFirst().ifPresent(optional -> {
            rules.removeAll(toBeReloadedRules);
            Map<String, DataSource> dataSources = resourceMetaData.getStorageUnits().entrySet().stream()
                    .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getDataSource(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
            rules.add(optional.getAttributes().getAttribute(MutableDataNodeRuleAttribute.class).reloadRule(ruleConfig, name, dataSources, rules));
        });
        ruleMetaData.getRules().clear();
        ruleMetaData.getRules().addAll(rules);
    }
}
