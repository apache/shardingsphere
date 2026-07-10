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
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicy;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicySet;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
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
        return new DatabaseIdentifierContext(IdentifierCasePolicyFactory.newInsensitivePolicySet());
    }
    
    /**
     * Create identifier context with protocol-aware identifier policies.
     *
     * @param protocolType protocol type
     * @param props configuration properties
     * @return identifier context
     */
    public static DatabaseIdentifierContext create(final DatabaseType protocolType, final ConfigurationProperties props) {
        ConfigurationProperties actualProps = getProps(props);
        IdentifierCasePolicyResolver resolver = new IdentifierCasePolicyResolver();
        IdentifierCasePolicySet protocolPolicySet = resolver.resolve(protocolType, actualProps, null);
        IdentifierCasePolicySet scopeAwarePolicySet = createScopeAwarePolicySet(protocolPolicySet, protocolPolicySet);
        return new DatabaseIdentifierContext(scopeAwarePolicySet, false);
    }
    
    /**
     * Create identifier context with protocol-aware identifier policies.
     *
     * @param protocolType protocol type
     * @param resourceMetaData resource meta data
     * @param props configuration properties
     * @return identifier context
     */
    public static DatabaseIdentifierContext create(final DatabaseType protocolType, final ResourceMetaData resourceMetaData, final ConfigurationProperties props) {
        ConfigurationProperties actualProps = getProps(props);
        IdentifierCasePolicyResolver resolver = new IdentifierCasePolicyResolver();
        IdentifierCasePolicySet protocolPolicySet = resolver.resolve(protocolType, actualProps, getFirstDataSource(resourceMetaData));
        Optional<DatabaseType> storageDatabaseType = getIdentifierPolicyDatabaseType(resourceMetaData);
        IdentifierCasePolicySet storagePolicySet = resolver.resolve(storageDatabaseType.orElse(protocolType), actualProps, getFirstDataSource(resourceMetaData));
        IdentifierCasePolicySet scopeAwarePolicySet = createScopeAwarePolicySet(protocolPolicySet, storagePolicySet);
        return new DatabaseIdentifierContext(scopeAwarePolicySet, isHeterogeneous(protocolType, getStorageDatabaseTypes(resourceMetaData)));
    }
    
    /**
     * Refresh identifier context with protocol-aware identifier policies.
     *
     * @param identifierContext identifier context
     * @param protocolType protocol type
     * @param props configuration properties
     */
    public static void refresh(final DatabaseIdentifierContext identifierContext, final DatabaseType protocolType, final ConfigurationProperties props) {
        ConfigurationProperties actualProps = getProps(props);
        IdentifierCasePolicyResolver resolver = new IdentifierCasePolicyResolver();
        IdentifierCasePolicySet protocolPolicySet = resolver.resolve(protocolType, actualProps, null);
        identifierContext.refresh(createScopeAwarePolicySet(protocolPolicySet, protocolPolicySet), false);
    }
    
    /**
     * Refresh identifier context with protocol-aware identifier policies.
     *
     * @param identifierContext identifier context
     * @param protocolType protocol type
     * @param resourceMetaData resource meta data
     * @param props configuration properties
     */
    public static void refresh(final DatabaseIdentifierContext identifierContext, final DatabaseType protocolType, final ResourceMetaData resourceMetaData, final ConfigurationProperties props) {
        ConfigurationProperties actualProps = getProps(props);
        IdentifierCasePolicyResolver resolver = new IdentifierCasePolicyResolver();
        IdentifierCasePolicySet protocolPolicySet = resolver.resolve(protocolType, actualProps, getFirstDataSource(resourceMetaData));
        Optional<DatabaseType> storageDatabaseType = getIdentifierPolicyDatabaseType(resourceMetaData);
        IdentifierCasePolicySet storagePolicySet = resolver.resolve(storageDatabaseType.orElse(protocolType), actualProps, getFirstDataSource(resourceMetaData));
        identifierContext.refresh(createScopeAwarePolicySet(protocolPolicySet, storagePolicySet), isHeterogeneous(protocolType, getStorageDatabaseTypes(resourceMetaData)));
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
    
    private static IdentifierCasePolicySet createScopeAwarePolicySet(final IdentifierCasePolicySet protocolPolicySet, final IdentifierCasePolicySet storagePolicySet) {
        IdentifierCasePolicySet databasePolicySet = IdentifierCasePolicyFactory.newInsensitivePolicySet();
        IdentifierCasePolicySet storageObjectPolicySet = IdentifierCasePolicyFactory.newQuotedInsensitivePolicySet();
        Map<IdentifierScope, IdentifierCasePolicy> scopedPolicies = new EnumMap<>(IdentifierScope.class);
        for (IdentifierScope each : IdentifierScope.values()) {
            if (IdentifierScope.DATABASE == each) {
                scopedPolicies.put(each, databasePolicySet.getPolicy(each));
                continue;
            }
            if (isStorageObjectScope(each)) {
                scopedPolicies.put(each, storageObjectPolicySet.getPolicy(each));
                continue;
            }
            scopedPolicies.put(each, IdentifierScope.SCHEMA == each ? protocolPolicySet.getPolicy(each) : storagePolicySet.getPolicy(each));
        }
        scopedPolicies.put(IdentifierScope.LOGICAL_TABLE, protocolPolicySet.getPolicy(IdentifierScope.LOGICAL_TABLE));
        return new IdentifierCasePolicySet(storagePolicySet.getPolicy(IdentifierScope.TABLE), scopedPolicies);
    }
    
    private static boolean isStorageObjectScope(final IdentifierScope identifierScope) {
        return IdentifierScope.COLUMN == identifierScope || IdentifierScope.INDEX == identifierScope || IdentifierScope.CONSTRAINT == identifierScope;
    }
    
    private static Optional<DatabaseType> getIdentifierPolicyDatabaseType(final ResourceMetaData resourceMetaData) {
        Collection<DatabaseType> storageDatabaseTypes = getStorageDatabaseTypes(resourceMetaData);
        return storageDatabaseTypes.stream().findFirst();
    }
    
    private static Collection<DatabaseType> getStorageDatabaseTypes(final ResourceMetaData resourceMetaData) {
        if (null == resourceMetaData || null == resourceMetaData.getStorageUnits() || resourceMetaData.getStorageUnits().isEmpty()) {
            return Collections.emptyList();
        }
        Collection<DatabaseType> storageDatabaseTypes = new LinkedHashSet<>(resourceMetaData.getStorageUnits().size(), 1F);
        for (StorageUnit each : resourceMetaData.getStorageUnits().values()) {
            storageDatabaseTypes.add(each.getStorageType());
        }
        return storageDatabaseTypes;
    }
    
    private static boolean isHeterogeneous(final DatabaseType protocolType, final Collection<DatabaseType> storageDatabaseTypes) {
        return null != protocolType && null != protocolType.getType() && storageDatabaseTypes.stream()
                .anyMatch(each -> null != each && null != each.getType() && !isSameProtocolType(protocolType, each));
    }
    
    private static boolean isSameProtocolType(final DatabaseType protocolType, final DatabaseType storageType) {
        return protocolType.getType().equalsIgnoreCase(storageType.getType());
    }
}
