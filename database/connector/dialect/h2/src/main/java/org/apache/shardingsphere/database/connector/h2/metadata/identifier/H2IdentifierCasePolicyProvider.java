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

package org.apache.shardingsphere.database.connector.h2.metadata.identifier;

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicy;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyProvider;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyProviderContext;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicySet;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;

/**
 * H2 provider of identifier case policies.
 */
public final class H2IdentifierCasePolicyProvider implements IdentifierCasePolicyProvider {
    
    private static final IdentifierCasePolicySet UPPER_CASE_POLICY_SET = IdentifierCasePolicyFactory.newUpperCasePolicySet();
    
    private static final IdentifierCasePolicySet LOWER_CASE_POLICY_SET = IdentifierCasePolicyFactory.newLowerCasePolicySet();
    
    private static final IdentifierCasePolicySet SENSITIVE_POLICY_SET = IdentifierCasePolicyFactory.newSensitivePolicySet();
    
    private static final IdentifierCasePolicySet CASE_PRESERVING_INSENSITIVE_POLICY_SET = IdentifierCasePolicyFactory.newCasePreservingInsensitivePolicySet();
    
    private static final IdentifierCasePolicySet DEFAULT_POLICY_SET = IdentifierCasePolicyFactory.newInsensitivePolicySet();
    
    @Override
    public IdentifierCasePolicySet provide(final IdentifierCasePolicyProviderContext context) {
        if (null == context.getDataSource()) {
            return DEFAULT_POLICY_SET;
        }
        try (Connection connection = context.getDataSource().getConnection()) {
            return null == connection ? DEFAULT_POLICY_SET : createPolicySet(connection.getMetaData());
        } catch (final SQLException ignored) {
            return DEFAULT_POLICY_SET;
        }
    }
    
    private IdentifierCasePolicySet createPolicySet(final DatabaseMetaData metaData) throws SQLException {
        IdentifierCasePolicySet schemaPolicySet = getSchemaPolicySet(metaData);
        Map<IdentifierScope, IdentifierCasePolicy> scopedPolicies = new EnumMap<>(IdentifierScope.class);
        scopedPolicies.put(IdentifierScope.SCHEMA, schemaPolicySet.getPolicy(IdentifierScope.SCHEMA));
        return new IdentifierCasePolicySet(DEFAULT_POLICY_SET.getPolicy(IdentifierScope.TABLE), scopedPolicies);
    }
    
    private IdentifierCasePolicySet getSchemaPolicySet(final DatabaseMetaData metaData) throws SQLException {
        if (metaData.storesUpperCaseIdentifiers()) {
            return UPPER_CASE_POLICY_SET;
        }
        if (metaData.storesLowerCaseIdentifiers()) {
            return LOWER_CASE_POLICY_SET;
        }
        if (metaData.supportsMixedCaseIdentifiers()) {
            return SENSITIVE_POLICY_SET;
        }
        return metaData.storesMixedCaseIdentifiers() ? CASE_PRESERVING_INSENSITIVE_POLICY_SET : DEFAULT_POLICY_SET;
    }
    
    @Override
    public String getDatabaseType() {
        return "H2";
    }
}
