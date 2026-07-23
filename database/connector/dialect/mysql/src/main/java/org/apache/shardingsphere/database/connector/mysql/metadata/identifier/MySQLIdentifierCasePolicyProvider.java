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

package org.apache.shardingsphere.database.connector.mysql.metadata.identifier;

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicy;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyProvider;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyProviderContext;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicySet;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;

/**
 * MySQL provider of identifier case policies.
 */
public final class MySQLIdentifierCasePolicyProvider implements IdentifierCasePolicyProvider {
    
    private static final String QUERY_LOWER_CASE_TABLE_NAMES = "SELECT @@lower_case_table_names";
    
    @Override
    public IdentifierCasePolicySet provide(final IdentifierCasePolicyProviderContext context) {
        if (null == context.getDataSource()) {
            return createStorageObjectSensitivePolicySet(IdentifierCasePolicyFactory.newQuotedInsensitivePolicySet());
        }
        try (Connection connection = context.getDataSource().getConnection()) {
            if (null == connection) {
                return createStorageObjectSensitivePolicySet(IdentifierCasePolicyFactory.newInsensitivePolicySet());
            }
            try (
                    PreparedStatement preparedStatement = connection.prepareStatement(QUERY_LOWER_CASE_TABLE_NAMES);
                    ResultSet resultSet = preparedStatement.executeQuery()) {
                return createStorageObjectSensitivePolicySet(resultSet.next() ? createPolicySet(resultSet.getInt(1)) : IdentifierCasePolicyFactory.newInsensitivePolicySet());
            }
        } catch (final SQLException ignored) {
            return createStorageObjectSensitivePolicySet(IdentifierCasePolicyFactory.newInsensitivePolicySet());
        }
    }
    
    private IdentifierCasePolicySet createStorageObjectSensitivePolicySet(final IdentifierCasePolicySet policySet) {
        Map<IdentifierScope, IdentifierCasePolicy> scopedPolicies = new EnumMap<>(IdentifierScope.class);
        for (IdentifierScope each : IdentifierScope.values()) {
            scopedPolicies.put(each, policySet.getPolicy(each));
        }
        IdentifierCasePolicy sensitivePolicy = IdentifierCasePolicyFactory.newSensitivePolicySet().getPolicy(IdentifierScope.COLUMN);
        scopedPolicies.put(IdentifierScope.COLUMN, sensitivePolicy);
        scopedPolicies.put(IdentifierScope.INDEX, sensitivePolicy);
        scopedPolicies.put(IdentifierScope.CONSTRAINT, sensitivePolicy);
        return new IdentifierCasePolicySet(policySet.getPolicy(IdentifierScope.TABLE), scopedPolicies);
    }
    
    private IdentifierCasePolicySet createPolicySet(final int lowerCaseTableNames) {
        if (1 == lowerCaseTableNames || 2 == lowerCaseTableNames) {
            return IdentifierCasePolicyFactory.newQuotedInsensitivePolicySet();
        }
        if (0 == lowerCaseTableNames) {
            Map<IdentifierScope, IdentifierCasePolicy> scopedPolicies = new EnumMap<>(IdentifierScope.class);
            scopedPolicies.put(IdentifierScope.SCHEMA, IdentifierCasePolicyFactory.newQuotedInsensitivePolicySet().getPolicy(IdentifierScope.SCHEMA));
            scopedPolicies.put(IdentifierScope.TABLE, IdentifierCasePolicyFactory.newSensitivePolicySet().getPolicy(IdentifierScope.TABLE));
            scopedPolicies.put(IdentifierScope.VIEW, IdentifierCasePolicyFactory.newSensitivePolicySet().getPolicy(IdentifierScope.VIEW));
            return new IdentifierCasePolicySet(IdentifierCasePolicyFactory.newInsensitivePolicySet().getPolicy(IdentifierScope.TABLE), scopedPolicies);
        }
        return IdentifierCasePolicyFactory.newInsensitivePolicySet();
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
