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

package org.apache.shardingsphere.transaction.xa.jta.datasource.checker.dialect;

import org.apache.shardingsphere.transaction.xa.jta.datasource.checker.XATransactionPrivilegeChecker;
import org.apache.shardingsphere.transaction.xa.jta.exception.XATransactionCheckPrivilegeFailedException;
import org.apache.shardingsphere.transaction.xa.jta.exception.XATransactionPrivilegeException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * XA transaction privilege checker of MySQL.
 */
public final class MySQLXATransactionPrivilegeChecker implements XATransactionPrivilegeChecker {
    
    private static final String SHOW_GRANTS_SQL = "SHOW GRANTS";
    
    private static final String[][] REQUIRED_PRIVILEGES = {{"ALL PRIVILEGES", "ON *.*"}, {"XA_RECOVER_ADMIN", "ON *.*"}};
    
    private static final int MYSQL_MAJOR_VERSION_8 = 8;
    
    @Override
    public void check(final DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            if (MYSQL_MAJOR_VERSION_8 == connection.getMetaData().getDatabaseMajorVersion()) {
                checkPrivilege(connection);
            }
        } catch (final SQLException ex) {
            throw new XATransactionCheckPrivilegeFailedException(ex);
        }
    }
    
    private void checkPrivilege(final Connection connection) {
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(SHOW_GRANTS_SQL);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String privilege = resultSet.getString(1).toUpperCase();
                if (matchPrivileges(privilege)) {
                    return;
                }
            }
        } catch (final SQLException ex) {
            throw new XATransactionCheckPrivilegeFailedException(ex);
        }
        throw new XATransactionPrivilegeException("XA_RECOVER_ADMIN");
    }
    
    private boolean matchPrivileges(final String privilege) {
        return Arrays.stream(REQUIRED_PRIVILEGES).anyMatch(each -> Arrays.stream(each).allMatch(privilege::contains));
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
