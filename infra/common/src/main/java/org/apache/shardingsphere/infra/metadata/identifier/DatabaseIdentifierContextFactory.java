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

package org.apache.shardingsphere.infra.metadata.identifier;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCaseRule;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCaseRuleSet;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCaseRuleSets;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Database identifier context factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseIdentifierContextFactory {
    
    /**
     * Create default identifier context.
     *
     * @return default identifier context
     */
    public static DatabaseIdentifierContext createDefault() {
        return new DatabaseIdentifierContext(IdentifierCaseRuleSets.newInsensitiveRuleSet());
    }
    
    /**
     * Create identifier context with protocol-aware identifier rules.
     *
     * @param protocolType protocol type
     * @param props configuration properties
     * @return identifier context
     */
    public static DatabaseIdentifierContext create(final DatabaseType protocolType, final ConfigurationProperties props) {
        ConfigurationProperties actualProps = getProps(props);
        IdentifierCaseRuleResolver resolver = new IdentifierCaseRuleResolver();
        IdentifierCaseRuleSet protocolRuleSet = resolver.resolve(protocolType, actualProps, null);
        return new DatabaseIdentifierContext(createScopeAwareRuleSet(protocolRuleSet, protocolRuleSet));
    }
    
    /**
     * Create identifier context with protocol-aware identifier rules.
     *
     * @param protocolType protocol type
     * @param resourceMetaData resource meta data
     * @param props configuration properties
     * @return identifier context
     */
    public static DatabaseIdentifierContext create(final DatabaseType protocolType, final ResourceMetaData resourceMetaData, final ConfigurationProperties props) {
        return new DatabaseIdentifierContext(createRuleSet(protocolType, resourceMetaData, getProps(props)));
    }
    
    /**
     * Refresh identifier context with protocol-aware identifier rules.
     *
     * @param identifierContext identifier context
     * @param protocolType protocol type
     * @param props configuration properties
     */
    public static void refresh(final DatabaseIdentifierContext identifierContext, final DatabaseType protocolType, final ConfigurationProperties props) {
        ConfigurationProperties actualProps = getProps(props);
        IdentifierCaseRuleResolver resolver = new IdentifierCaseRuleResolver();
        IdentifierCaseRuleSet protocolRuleSet = resolver.resolve(protocolType, actualProps, null);
        identifierContext.refresh(createScopeAwareRuleSet(protocolRuleSet, protocolRuleSet));
    }
    
    /**
     * Refresh identifier context with protocol-aware identifier rules.
     *
     * @param identifierContext identifier context
     * @param protocolType protocol type
     * @param resourceMetaData resource meta data
     * @param props configuration properties
     */
    public static void refresh(final DatabaseIdentifierContext identifierContext, final DatabaseType protocolType, final ResourceMetaData resourceMetaData, final ConfigurationProperties props) {
        identifierContext.refresh(createRuleSet(protocolType, resourceMetaData, getProps(props)));
    }
    
    private static ConfigurationProperties getProps(final ConfigurationProperties props) {
        return null == props ? new ConfigurationProperties(new Properties()) : props;
    }
    
    private static DataSource getFirstDataSource(final ResourceMetaData resourceMetaData) {
        if (null == resourceMetaData || null == resourceMetaData.getStorageUnits() || resourceMetaData.getStorageUnits().isEmpty()) {
            return null;
        }
        return resourceMetaData.getStorageUnits().values().iterator().next().getDataSource();
    }
    
    private static IdentifierCaseRuleSet createRuleSet(final DatabaseType protocolType, final ResourceMetaData resourceMetaData, final ConfigurationProperties props) {
        IdentifierCaseRuleResolver resolver = new IdentifierCaseRuleResolver();
        DatabaseType resolvedDatabaseType = getIdentifierRuleDatabaseType(resourceMetaData).orElse(protocolType);
        return createScopeAwareRuleSet(resolver.resolve(protocolType, props, null), resolver.resolve(resolvedDatabaseType, props, getFirstDataSource(resourceMetaData)));
    }
    
    private static IdentifierCaseRuleSet createScopeAwareRuleSet(final IdentifierCaseRuleSet protocolRuleSet, final IdentifierCaseRuleSet storageRuleSet) {
        IdentifierCaseRuleSet databaseRuleSet = IdentifierCaseRuleSets.newInsensitiveRuleSet();
        Map<IdentifierScope, IdentifierCaseRule> scopedRules = new EnumMap<>(IdentifierScope.class);
        for (IdentifierScope each : IdentifierScope.values()) {
            if (IdentifierScope.DATABASE == each) {
                scopedRules.put(each, databaseRuleSet.getRule(each));
                continue;
            }
            scopedRules.put(each, IdentifierScope.SCHEMA == each ? protocolRuleSet.getRule(each) : storageRuleSet.getRule(each));
        }
        return new IdentifierCaseRuleSet(storageRuleSet.getRule(IdentifierScope.TABLE), scopedRules);
    }
    
    private static Optional<DatabaseType> getIdentifierRuleDatabaseType(final ResourceMetaData resourceMetaData) {
        if (null == resourceMetaData || null == resourceMetaData.getStorageUnits() || resourceMetaData.getStorageUnits().isEmpty()) {
            return Optional.empty();
        }
        Collection<DatabaseType> storageDatabaseTypes = new LinkedHashSet<>(resourceMetaData.getStorageUnits().size(), 1F);
        for (StorageUnit each : resourceMetaData.getStorageUnits().values()) {
            storageDatabaseTypes.add(each.getStorageType());
        }
        return storageDatabaseTypes.stream().findFirst();
    }
}
