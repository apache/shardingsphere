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
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicy;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicySet;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.MetadataIdentifierCaseSensitivity;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

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
        ResolvedIdentifierContext resolvedContext = resolve(protocolType, null, props);
        return new DatabaseIdentifierContext(resolvedContext.protocolPolicySet, resolvedContext.storagePolicySet,
                resolvedContext.metaDataPolicySet, resolvedContext.heterogeneousTableLookupEnabled);
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
        ResolvedIdentifierContext resolvedContext = resolve(protocolType, resourceMetaData, props);
        return new DatabaseIdentifierContext(resolvedContext.protocolPolicySet, resolvedContext.storagePolicySet,
                resolvedContext.metaDataPolicySet, resolvedContext.heterogeneousTableLookupEnabled);
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
        ResolvedIdentifierContext resolvedContext = resolve(protocolType, resourceMetaData, props);
        identifierContext.refresh(resolvedContext.protocolPolicySet, resolvedContext.storagePolicySet,
                resolvedContext.metaDataPolicySet, resolvedContext.heterogeneousTableLookupEnabled);
    }
    
    private static ResolvedIdentifierContext resolve(final DatabaseType protocolType, final ResourceMetaData resourceMetaData, final ConfigurationProperties props) {
        Collection<StorageUnit> storageUnits = getStorageUnits(resourceMetaData);
        StorageUnit storageUnit = storageUnits.stream().findFirst().orElse(null);
        IdentifierCasePolicySet protocolPolicySet = IdentifierCasePolicyResolver.resolveProtocol(protocolType);
        IdentifierCasePolicySet storagePolicySet = null == storageUnit
                ? protocolPolicySet
                : IdentifierCasePolicyResolver.resolveStorage(storageUnit.getStorageType(), storageUnit.getDataSource());
        return new ResolvedIdentifierContext(protocolPolicySet, storagePolicySet,
                createMetaDataPolicySet(protocolPolicySet, storagePolicySet, props), isHeterogeneous(protocolType, storageUnits));
    }
    
    private static IdentifierCasePolicySet createMetaDataPolicySet(final IdentifierCasePolicySet protocolPolicySet, final IdentifierCasePolicySet storagePolicySet,
                                                                   final ConfigurationProperties props) {
        MetadataIdentifierCaseSensitivity configuredCaseSensitivity = new TemporaryConfigurationProperties(props.getProps())
                .getValue(TemporaryConfigurationPropertyKey.METADATA_IDENTIFIER_CASE_SENSITIVITY);
        if (MetadataIdentifierCaseSensitivity.INSENSITIVE != configuredCaseSensitivity) {
            return createScopeAwarePolicySet(protocolPolicySet, storagePolicySet);
        }
        IdentifierCasePolicySet insensitivePolicySet = IdentifierCasePolicyFactory.newInsensitivePolicySet();
        return createScopeAwarePolicySet(insensitivePolicySet, insensitivePolicySet);
    }
    
    private static Collection<StorageUnit> getStorageUnits(final ResourceMetaData resourceMetaData) {
        if (null == resourceMetaData || null == resourceMetaData.getStorageUnits() || resourceMetaData.getStorageUnits().isEmpty()) {
            return Collections.emptyList();
        }
        return resourceMetaData.getStorageUnits().values();
    }
    
    private static IdentifierCasePolicySet createScopeAwarePolicySet(final IdentifierCasePolicySet protocolPolicySet, final IdentifierCasePolicySet storagePolicySet) {
        Map<IdentifierScope, IdentifierCasePolicy> scopedPolicies = new EnumMap<>(IdentifierScope.class);
        for (IdentifierScope each : IdentifierScope.values()) {
            scopedPolicies.put(each, storagePolicySet.getPolicy(each));
        }
        IdentifierCasePolicy databasePolicy = IdentifierCasePolicyFactory.newInsensitivePolicySet().getPolicy(IdentifierScope.DATABASE);
        IdentifierCasePolicy storageObjectPolicy = IdentifierCasePolicyFactory.newQuotedInsensitivePolicySet().getPolicy(IdentifierScope.COLUMN);
        scopedPolicies.put(IdentifierScope.DATABASE, databasePolicy);
        scopedPolicies.put(IdentifierScope.SCHEMA, protocolPolicySet.getPolicy(IdentifierScope.SCHEMA));
        scopedPolicies.put(IdentifierScope.LOGICAL_TABLE, protocolPolicySet.getPolicy(IdentifierScope.LOGICAL_TABLE));
        scopedPolicies.put(IdentifierScope.COLUMN, storageObjectPolicy);
        scopedPolicies.put(IdentifierScope.INDEX, storageObjectPolicy);
        scopedPolicies.put(IdentifierScope.CONSTRAINT, storageObjectPolicy);
        return new IdentifierCasePolicySet(storagePolicySet.getPolicy(IdentifierScope.TABLE), scopedPolicies);
    }
    
    private static boolean isHeterogeneous(final DatabaseType protocolType, final Collection<StorageUnit> storageUnits) {
        return null != protocolType && null != protocolType.getType() && storageUnits.stream()
                .map(StorageUnit::getStorageType).anyMatch(each -> null != each && null != each.getType() && !isSameProtocolType(protocolType, each));
    }
    
    private static boolean isSameProtocolType(final DatabaseType protocolType, final DatabaseType storageType) {
        return protocolType.getType().equalsIgnoreCase(storageType.getType());
    }
    
    @RequiredArgsConstructor
    private static final class ResolvedIdentifierContext {
        
        private final IdentifierCasePolicySet protocolPolicySet;
        
        private final IdentifierCasePolicySet storagePolicySet;
        
        private final IdentifierCasePolicySet metaDataPolicySet;
        
        private final boolean heterogeneousTableLookupEnabled;
    }
}
