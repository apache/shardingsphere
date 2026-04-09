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

package org.apache.shardingsphere.infra.metadata;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationProperties;
import org.apache.shardingsphere.infra.datasource.pool.destroyer.DataSourcePoolDestroyer;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabaseFactory;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContext;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContextFactory;
import org.apache.shardingsphere.infra.metadata.identifier.IdentifierIndex;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.datasource.StaticDataSourceRuleAttribute;
import org.apache.shardingsphere.infra.rule.scope.GlobalRule;
import org.apache.shardingsphere.infra.rule.scope.GlobalRule.GlobalRuleChangedType;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * ShardingSphere meta data.
 */
@Getter
public final class ShardingSphereMetaData implements AutoCloseable {
    
    @Getter(AccessLevel.NONE)
    private final IdentifierIndex<ShardingSphereDatabase> databaseIndex;
    
    private final ResourceMetaData globalResourceMetaData;
    
    private final RuleMetaData globalRuleMetaData;
    
    private final ConfigurationProperties props;
    
    private final TemporaryConfigurationProperties temporaryProps;
    
    private final DatabaseType protocolType;
    
    /**
     * Construct metadata through the legacy compatibility path.
     *
     * <p>This constructor keeps existing callers and tests working until all metadata creation paths pass an explicit protocol-aware identifier context.</p>
     *
     * <p>TODO(haoran): Remove this constructor after all metadata initialization paths migrate to the protocol-aware constructor.</p>
     *
     * @param databases databases
     * @param globalResourceMetaData global resource meta data
     * @param globalRuleMetaData global rule meta data
     * @param props configuration properties
     */
    public ShardingSphereMetaData(final Collection<ShardingSphereDatabase> databases, final ResourceMetaData globalResourceMetaData,
                                  final RuleMetaData globalRuleMetaData, final ConfigurationProperties props) {
        this(databases, globalResourceMetaData, globalRuleMetaData, props, resolveProtocolType(databases, props), DatabaseIdentifierContextFactory.createDefault());
    }
    
    /**
     * Construct metadata with protocol-aware identifier rules.
     *
     * @param databases databases
     * @param globalResourceMetaData global resource meta data
     * @param globalRuleMetaData global rule meta data
     * @param props configuration properties
     * @param protocolType protocol type
     */
    public ShardingSphereMetaData(final Collection<ShardingSphereDatabase> databases, final ResourceMetaData globalResourceMetaData,
                                  final RuleMetaData globalRuleMetaData, final ConfigurationProperties props, final DatabaseType protocolType) {
        this(databases, globalResourceMetaData, globalRuleMetaData, props, protocolType, DatabaseIdentifierContextFactory.create(protocolType, props));
    }
    
    private ShardingSphereMetaData(final Collection<ShardingSphereDatabase> databases, final ResourceMetaData globalResourceMetaData,
                                   final RuleMetaData globalRuleMetaData, final ConfigurationProperties props, final DatabaseType protocolType,
                                   final DatabaseIdentifierContext identifierContext) {
        this.globalResourceMetaData = globalResourceMetaData;
        this.globalRuleMetaData = globalRuleMetaData;
        this.props = props;
        temporaryProps = new TemporaryConfigurationProperties(props.getProps());
        this.protocolType = protocolType;
        databaseIndex = new IdentifierIndex<>(identifierContext, IdentifierScope.DATABASE);
        databaseIndex.rebuild(new LinkedHashMap<>(databases.stream().collect(Collectors.toMap(ShardingSphereDatabase::getName, each -> each))));
    }
    
    /**
     * Get all databases.
     *
     * @return all databases
     */
    public Collection<ShardingSphereDatabase> getAllDatabases() {
        return databaseIndex.getAll();
    }
    
    /**
     * Find database.
     *
     * @param databaseName database name
     * @return found database
     */
    private Optional<ShardingSphereDatabase> findDatabase(final IdentifierValue databaseName) {
        return databaseIndex.find(databaseName);
    }
    
    /**
     * Judge contains database from meta data or not.
     *
     * @param databaseName database name
     * @return contains database from meta data or not
     */
    public boolean containsDatabase(final String databaseName) {
        return containsDatabase(new IdentifierValue(databaseName, QuoteCharacter.NONE));
    }
    
    /**
     * Judge contains database from meta data or not.
     *
     * @param databaseName database name
     * @return contains database from meta data or not
     */
    public boolean containsDatabase(final IdentifierValue databaseName) {
        return findDatabase(databaseName).isPresent();
    }
    
    /**
     * Get database.
     *
     * @param databaseName database name
     * @return meta data database
     */
    public ShardingSphereDatabase getDatabase(final String databaseName) {
        return getDatabase(new IdentifierValue(databaseName, QuoteCharacter.NONE));
    }
    
    /**
     * Get database.
     *
     * @param databaseName database name
     * @return meta data database
     */
    public ShardingSphereDatabase getDatabase(final IdentifierValue databaseName) {
        return findDatabase(databaseName).orElse(null);
    }
    
    /**
     * Add database.
     *
     * @param databaseName database name
     * @param protocolType protocol database type
     * @param props configuration properties
     */
    public void addDatabase(final String databaseName, final DatabaseType protocolType, final ConfigurationProperties props) {
        ShardingSphereDatabase database = ShardingSphereDatabaseFactory.create(databaseName, protocolType, props);
        databaseIndex.put(database.getName(), database);
        globalRuleMetaData.getRules().forEach(each -> ((GlobalRule) each).refresh(getAllDatabases(), GlobalRuleChangedType.DATABASE_CHANGED));
    }
    
    /**
     * Put database.
     *
     * @param database database
     */
    public void putDatabase(final ShardingSphereDatabase database) {
        databaseIndex.put(database.getName(), database);
    }
    
    /**
     * Drop database.
     *
     * @param databaseName database name
     */
    public void dropDatabase(final String databaseName) {
        ShardingSphereDatabase database = getDatabase(databaseName);
        if (null == database) {
            return;
        }
        databaseIndex.remove(database.getName());
        cleanResources(database);
    }
    
    @SneakyThrows(Exception.class)
    private void cleanResources(final ShardingSphereDatabase database) {
        globalRuleMetaData.getRules().forEach(each -> ((GlobalRule) each).refresh(getAllDatabases(), GlobalRuleChangedType.DATABASE_CHANGED));
        for (ShardingSphereRule each : database.getRuleMetaData().getRules()) {
            if (each instanceof AutoCloseable) {
                ((AutoCloseable) each).close();
            }
        }
        database.getRuleMetaData().getAttributes(StaticDataSourceRuleAttribute.class).forEach(StaticDataSourceRuleAttribute::cleanStorageNodeDataSources);
        Optional.ofNullable(database.getResourceMetaData())
                .ifPresent(optional -> optional.getStorageUnits().values().forEach(each -> new DataSourcePoolDestroyer(each.getDataSource()).asyncDestroy()));
    }
    
    @SneakyThrows(Exception.class)
    @Override
    public void close() {
        for (ShardingSphereRule each : getAllRules()) {
            if (each instanceof AutoCloseable) {
                ((AutoCloseable) each).close();
            }
        }
    }
    
    private Collection<ShardingSphereRule> getAllRules() {
        Collection<ShardingSphereRule> result = new LinkedList<>(globalRuleMetaData.getRules());
        getAllDatabases().stream().map(each -> each.getRuleMetaData().getRules()).forEach(result::addAll);
        return result;
    }
    
    private static ConfigurationProperties getProps(final ConfigurationProperties props) {
        return null == props ? new ConfigurationProperties(new Properties()) : props;
    }
    
    private static DatabaseType resolveProtocolType(final Collection<ShardingSphereDatabase> databases, final ConfigurationProperties props) {
        return databases.isEmpty() ? DatabaseTypeEngine.getProtocolType(Collections.emptyMap(), getProps(props)) : databases.iterator().next().getProtocolType();
    }
}
