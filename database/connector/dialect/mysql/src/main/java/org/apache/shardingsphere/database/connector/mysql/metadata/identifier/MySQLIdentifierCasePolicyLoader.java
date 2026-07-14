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

import org.apache.shardingsphere.database.connector.core.metadata.identifier.DialectIdentifierCasePolicyLoader;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicy;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicySet;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;

/**
 * MySQL loader of identifier case policies.
 */
public final class MySQLIdentifierCasePolicyLoader implements DialectIdentifierCasePolicyLoader {
    
    private static final String QUERY_LOWER_CASE_TABLE_NAMES = "SELECT @@lower_case_table_names";
    
    @Override
    public IdentifierCasePolicySet load(final Connection connection) throws SQLException {
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(QUERY_LOWER_CASE_TABLE_NAMES);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            ShardingSpherePreconditions.checkState(resultSet.next(), () -> new SQLException("No lower_case_table_names value was returned."));
            return createPolicySet(resultSet.getInt(1));
        }
    }
    
    private IdentifierCasePolicySet createPolicySet(final int lowerCaseTableNames) throws SQLException {
        if (0 == lowerCaseTableNames) {
            Map<IdentifierScope, IdentifierCasePolicy> scopedPolicies = new EnumMap<>(IdentifierScope.class);
            scopedPolicies.put(IdentifierScope.SCHEMA, IdentifierCasePolicyFactory.newMySQLInsensitivePolicySet().getPolicy(IdentifierScope.SCHEMA));
            scopedPolicies.put(IdentifierScope.TABLE, IdentifierCasePolicyFactory.newSensitivePolicySet().getPolicy(IdentifierScope.TABLE));
            scopedPolicies.put(IdentifierScope.VIEW, IdentifierCasePolicyFactory.newSensitivePolicySet().getPolicy(IdentifierScope.VIEW));
            return new IdentifierCasePolicySet(IdentifierCasePolicyFactory.newInsensitivePolicySet().getPolicy(IdentifierScope.TABLE), scopedPolicies);
        }
        ShardingSpherePreconditions.checkState(1 == lowerCaseTableNames || 2 == lowerCaseTableNames,
                () -> new SQLException(String.format("Unsupported lower_case_table_names value `%s`.", lowerCaseTableNames)));
        return IdentifierCasePolicyFactory.newMySQLInsensitivePolicySet();
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
