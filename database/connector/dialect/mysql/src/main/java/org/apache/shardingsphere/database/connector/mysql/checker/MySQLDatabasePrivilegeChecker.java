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

package org.apache.shardingsphere.database.connector.mysql.checker;

import org.apache.shardingsphere.database.connector.core.checker.DialectDatabasePrivilegeChecker;
import org.apache.shardingsphere.database.connector.core.checker.PrivilegeCheckType;
import org.apache.shardingsphere.database.connector.core.exception.CheckDatabaseEnvironmentFailedException;
import org.apache.shardingsphere.database.connector.core.exception.MissingRequiredPrivilegeException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Database environment checker for MySQL.
 */
public final class MySQLDatabasePrivilegeChecker implements DialectDatabasePrivilegeChecker {
    
    private static final String SHOW_GRANTS_SQL = "SHOW GRANTS";
    
    private static final int MYSQL_MAJOR_VERSION_8 = 8;
    
    // BINLOG MONITOR is a synonym for REPLICATION CLIENT for MariaDB
    private static final String[][] PIPELINE_REQUIRED_PRIVILEGES =
            {{"ALL PRIVILEGES", "ON *.*"}, {"REPLICATION SLAVE", "REPLICATION CLIENT", "ON *.*"}, {"REPLICATION SLAVE", "BINLOG MONITOR", "ON *.*"}};
    
    private static final String[][] XA_REQUIRED_PRIVILEGES = {{"ALL PRIVILEGES", "ON *.*"}, {"XA_RECOVER_ADMIN", "ON *.*"}};
    
    private static final Map<PrivilegeCheckType, Collection<String>> REQUIRED_PRIVILEGES_FOR_MESSAGE = new EnumMap<>(PrivilegeCheckType.class);
    
    static {
        REQUIRED_PRIVILEGES_FOR_MESSAGE.put(PrivilegeCheckType.PIPELINE, Arrays.asList("REPLICATION SLAVE", "REPLICATION CLIENT"));
        REQUIRED_PRIVILEGES_FOR_MESSAGE.put(PrivilegeCheckType.SELECT, Collections.singleton("SELECT ON DATABASE"));
        REQUIRED_PRIVILEGES_FOR_MESSAGE.put(PrivilegeCheckType.XA, Collections.singleton("XA_RECOVER_ADMIN"));
    }
    
    @Override
    public void check(final DataSource dataSource, final PrivilegeCheckType privilegeCheckType) {
        try (Connection connection = dataSource.getConnection()) {
            if (PrivilegeCheckType.XA == privilegeCheckType && MYSQL_MAJOR_VERSION_8 != connection.getMetaData().getDatabaseMajorVersion()) {
                return;
            }
            checkPrivilege(connection, privilegeCheckType);
        } catch (final SQLException ex) {
            throw new CheckDatabaseEnvironmentFailedException(ex);
        }
    }
    
    private void checkPrivilege(final Connection connection, final PrivilegeCheckType privilegeCheckType) {
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(SHOW_GRANTS_SQL);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String privilege = resultSet.getString(1).toUpperCase();
                if (matchPrivileges(privilege, getRequiredPrivileges(connection, privilegeCheckType))) {
                    return;
                }
            }
        } catch (final SQLException ex) {
            throw new CheckDatabaseEnvironmentFailedException(ex);
        }
        throw new MissingRequiredPrivilegeException(REQUIRED_PRIVILEGES_FOR_MESSAGE.get(privilegeCheckType));
    }
    
    private String[][] getRequiredPrivileges(final Connection connection, final PrivilegeCheckType privilegeCheckType) throws SQLException {
        switch (privilegeCheckType) {
            case PIPELINE:
                return PIPELINE_REQUIRED_PRIVILEGES;
            case SELECT:
                return getSelectRequiredPrivilege(connection);
            case XA:
                return XA_REQUIRED_PRIVILEGES;
            default:
                return new String[0][0];
        }
    }
    
    private String[][] getSelectRequiredPrivilege(final Connection connection) throws SQLException {
        String onCatalog = String.format("ON `%s`.*", connection.getCatalog().toUpperCase());
        return new String[][]{{"ALL PRIVILEGES", "ON *.*"}, {"SELECT", "ON *.*"}, {"ALL PRIVILEGES", onCatalog}, {"SELECT", onCatalog}};
    }
    
    private boolean matchPrivileges(final String grantedPrivileges, final String[][] requiredPrivileges) {
        return Arrays.stream(requiredPrivileges).anyMatch(each -> Arrays.stream(each).allMatch(grantedPrivileges::contains));
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
