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
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCaseRule;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.decorator.RuleConfigurationDecorator;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContext;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContextFactory;
import org.apache.shardingsphere.infra.metadata.identifier.IdentifierIndex;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.datanode.MutableDataNodeRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
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
    
    private final DatabaseIdentifierContext identifierContext;
    
    @Getter(AccessLevel.NONE)
    private final IdentifierIndex<ShardingSphereSchema> schemaIndex;
    
    /**
     * Construct database with protocol-aware identifier rules.
     *
     * @param name database name
     * @param protocolType protocol type
     * @param resourceMetaData resource meta data
     * @param ruleMetaData rule meta data
     * @param schemas schemas
     * @param props configuration properties
     */
    public ShardingSphereDatabase(final String name, final DatabaseType protocolType, final ResourceMetaData resourceMetaData,
                                  final RuleMetaData ruleMetaData, final Collection<ShardingSphereSchema> schemas, final ConfigurationProperties props) {
        this(name, protocolType, resourceMetaData, ruleMetaData, schemas, DatabaseIdentifierContextFactory.create(protocolType, resourceMetaData, props));
    }
    
    private ShardingSphereDatabase(final String name, final DatabaseType protocolType, final ResourceMetaData resourceMetaData,
                                   final RuleMetaData ruleMetaData, final Collection<ShardingSphereSchema> schemas,
                                   final DatabaseIdentifierContext identifierContext) {
        this.name = name;
        this.protocolType = protocolType;
        this.resourceMetaData = resourceMetaData;
        this.ruleMetaData = ruleMetaData;
        this.identifierContext = identifierContext;
        schemaIndex = new IdentifierIndex<>(identifierContext, IdentifierScope.SCHEMA);
        Map<String, ShardingSphereSchema> schemaMap = createSchemaMap(schemas);
        schemaMap.values().forEach(this::refreshSchemaIdentifierContext);
        schemaIndex.rebuild(schemaMap);
    }
    
    /**
     * Get all schemas.
     *
     * @return all schemas
     */
    public Collection<ShardingSphereSchema> getAllSchemas() {
        return schemaIndex.getAll();
    }
    
    /**
     * Find schema.
     *
     * @param schemaName schema name
     * @return schema
     */
    private Optional<ShardingSphereSchema> findSchema(final IdentifierValue schemaName) {
        return schemaIndex.find(schemaName);
    }
    
    /**
     * Judge contains schema from database or not.
     *
     * @param schemaName schema name
     * @return contains schema from database or not
     */
    public boolean containsSchema(final String schemaName) {
        return containsSchema(new IdentifierValue(schemaName, QuoteCharacter.NONE));
    }
    
    /**
     * Judge contains schema from database or not.
     *
     * @param schemaName schema name
     * @return contains schema from database or not
     */
    public boolean containsSchema(final IdentifierValue schemaName) {
        return findSchema(schemaName).isPresent();
    }
    
    /**
     * Get schema.
     *
     * @param schemaName schema name
     * @return schema
     */
    public ShardingSphereSchema getSchema(final String schemaName) {
        return getSchema(new IdentifierValue(schemaName, QuoteCharacter.NONE));
    }
    
    /**
     * Get schema.
     *
     * @param schemaName schema name
     * @return schema
     */
    public ShardingSphereSchema getSchema(final IdentifierValue schemaName) {
        return findSchema(schemaName).orElse(null);
    }
    
    /**
     * Get identifier case rule by scope.
     *
     * @param identifierScope identifier scope
     * @return identifier case rule
     */
    public IdentifierCaseRule getIdentifierCaseRule(final IdentifierScope identifierScope) {
        return identifierContext.getRule(identifierScope);
    }
    
    /**
     * Add schema.
     *
     * @param schema schema
     */
    public void addSchema(final ShardingSphereSchema schema) {
        refreshSchemaIdentifierContext(schema);
        schemaIndex.put(schema.getName(), schema);
    }
    
    /**
     * Drop schema.
     *
     * @param schemaName schema name
     */
    public void dropSchema(final String schemaName) {
        ShardingSphereSchema schema = getSchema(schemaName);
        if (null == schema) {
            return;
        }
        schemaIndex.remove(schema.getName());
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
    
    /**
     * Check storage units existed.
     *
     * @param storageUnitNames storage unit names
     */
    public void checkStorageUnitsExisted(final Collection<String> storageUnitNames) {
        Collection<String> notExistedDataSources = resourceMetaData.getNotExistedDataSources(storageUnitNames);
        Collection<String> logicDataSources = ruleMetaData.getAttributes(DataSourceMapperRuleAttribute.class).stream()
                .flatMap(each -> each.getDataSourceMapper().keySet().stream()).collect(Collectors.toSet());
        notExistedDataSources.removeIf(logicDataSources::contains);
        ShardingSpherePreconditions.checkMustEmpty(notExistedDataSources, () -> new MissingRequiredStorageUnitsException(name, notExistedDataSources));
    }
    
    /**
     * Refresh identifier context.
     *
     * @param props configuration properties
     */
    public synchronized void refreshIdentifierContext(final ConfigurationProperties props) {
        DatabaseIdentifierContextFactory.refresh(identifierContext, protocolType, resourceMetaData, props);
        Map<String, ShardingSphereSchema> schemaMap = new LinkedHashMap<>(schemaIndex.size(), 1F);
        schemaIndex.getAll().forEach(each -> {
            refreshSchemaIdentifierContext(each);
            schemaMap.put(each.getName(), each);
        });
        schemaIndex.rebuild(schemaMap);
    }
    
    /**
     * Decorate rule configuration.
     *
     * @param ruleConfig rule configuration
     * @return decorated rule configuration
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public RuleConfiguration decorateRuleConfiguration(final RuleConfiguration ruleConfig) {
        Optional<RuleConfigurationDecorator> decorator = TypedSPILoader.findService(RuleConfigurationDecorator.class, ruleConfig.getClass());
        if (!decorator.isPresent()) {
            return ruleConfig;
        }
        Map<String, DataSource> dataSources = resourceMetaData.getStorageUnits().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getDataSource(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
        return decorator.get().decorate(name, dataSources, ruleMetaData.getRules(), ruleConfig);
    }
    
    private void refreshSchemaIdentifierContext(final ShardingSphereSchema schema) {
        schema.refreshIdentifierContext(identifierContext);
    }
    
    private Map<String, ShardingSphereSchema> createSchemaMap(final Collection<ShardingSphereSchema> schemas) {
        return schemas.stream().collect(Collectors.toMap(ShardingSphereSchema::getName, each -> each, (oldValue, currentValue) -> currentValue,
                () -> new LinkedHashMap<>(schemas.size(), 1F)));
    }
    
    /**
     * Get default schema name.
     *
     * @return default schema name
     */
    public String getDefaultSchemaName() {
        return new DatabaseTypeRegistry(protocolType).getDefaultSchemaName(name);
    }
}
