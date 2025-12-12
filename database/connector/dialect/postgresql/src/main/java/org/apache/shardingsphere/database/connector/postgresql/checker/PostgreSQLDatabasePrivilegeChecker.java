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

package org.apache.shardingsphere.database.connector.postgresql.checker;

import org.apache.shardingsphere.database.connector.core.checker.DialectDatabasePrivilegeChecker;
import org.apache.shardingsphere.database.connector.core.checker.PrivilegeCheckType;
import org.apache.shardingsphere.database.connector.core.exception.CheckDatabaseEnvironmentFailedException;
import org.apache.shardingsphere.database.connector.core.exception.MissingRequiredPrivilegeException;
import org.apache.shardingsphere.database.connector.core.exception.MissingRequiredUserException;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

/**
 * Database environment checker for PostgreSQL.
 */
public final class PostgreSQLDatabasePrivilegeChecker implements DialectDatabasePrivilegeChecker {
    
    private static final String SHOW_GRANTS_SQL = "SELECT * FROM pg_roles WHERE rolname = ?";
    
    @Override
    public void check(final DataSource dataSource, final PrivilegeCheckType privilegeCheckType) {
        if (PrivilegeCheckType.PIPELINE != privilegeCheckType) {
            return;
        }
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(SHOW_GRANTS_SQL)) {
            DatabaseMetaData metaData = connection.getMetaData();
            preparedStatement.setString(1, metaData.getUserName());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                String username = metaData.getUserName();
                ShardingSpherePreconditions.checkState(resultSet.next(), () -> new MissingRequiredUserException(username));
                String isSuperRole = resultSet.getString("rolsuper");
                String isReplicationRole = resultSet.getString("rolreplication");
                ShardingSpherePreconditions.checkState("t".equalsIgnoreCase(isSuperRole) || "t".equalsIgnoreCase(isReplicationRole),
                        () -> new MissingRequiredPrivilegeException(Collections.singleton("REPLICATION")));
            }
        } catch (final SQLException ex) {
            throw new CheckDatabaseEnvironmentFailedException(ex);
        }
    }
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }
}
