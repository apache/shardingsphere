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

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCaseRuleProvider;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCaseRuleProviderContext;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCaseRuleSet;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCaseRuleSets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

/**
 * MySQL provider of identifier case rules.
 */
public final class MySQLIdentifierCaseRuleProvider implements IdentifierCaseRuleProvider {
    
    private static final String QUERY_LOWER_CASE_TABLE_NAMES = "SELECT @@lower_case_table_names";
    
    @Override
    public Optional<IdentifierCaseRuleSet> provide(final IdentifierCaseRuleProviderContext context) {
        Objects.requireNonNull(context, "context cannot be null.");
        if (null == context.getDataSource()) {
            return Optional.empty();
        }
        try (
                Connection connection = context.getDataSource().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(QUERY_LOWER_CASE_TABLE_NAMES);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            return resultSet.next() ? createRuleSet(resultSet.getInt(1)) : Optional.empty();
        } catch (final SQLException ignored) {
            return Optional.empty();
        }
    }
    
    private Optional<IdentifierCaseRuleSet> createRuleSet(final int lowerCaseTableNames) {
        return 1 == lowerCaseTableNames || 2 == lowerCaseTableNames ? Optional.of(IdentifierCaseRuleSets.newMySQLInsensitiveRuleSet()) : Optional.empty();
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
