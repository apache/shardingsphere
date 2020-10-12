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

package org.apache.shardingsphere.scaling.mysql;

import org.apache.shardingsphere.scaling.core.exception.PrepareFailedException;
import org.apache.shardingsphere.scaling.core.job.preparer.checker.AbstractDataSourceChecker;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Data source checker for MySQL.
 */
public final class MySQLDataSourceChecker extends AbstractDataSourceChecker {
    
    private static final String SHOW_GRANTS_SQL = "SHOW GRANTS";
    
    private static final String[][] REQUIRED_PRIVILEGES = {{"ALL PRIVILEGES", "ON *.*"}, {"REPLICATION SLAVE", "REPLICATION CLIENT", "ON *.*"}};
    
    private static final String SHOW_VARIABLES_SQL = "SHOW VARIABLES LIKE '%s'";
    
    private static final Map<String, String> REQUIRED_VARIABLES = new HashMap<>(2);
    
    static {
        REQUIRED_VARIABLES.put("LOG_BIN", "ON");
        REQUIRED_VARIABLES.put("BINLOG_FORMAT", "ROW");
        REQUIRED_VARIABLES.put("BINLOG_ROW_IMAGE", "FULL");
    }
    
    @Override
    public void checkPrivilege(final Collection<? extends DataSource> dataSources) {
        for (DataSource each : dataSources) {
            checkPrivilege(each);
        }
    }
    
    private void checkPrivilege(final DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SHOW_GRANTS_SQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String privilege = resultSet.getString(1).toUpperCase();
                if (matchPrivileges(privilege)) {
                    return;
                }
            }
        } catch (final SQLException ex) {
            throw new PrepareFailedException("Source data source check privileges failed.", ex);
        }
        throw new PrepareFailedException("Source data source is lack of REPLICATION SLAVE, REPLICATION CLIENT ON *.* privileges.");
    }
    
    private boolean matchPrivileges(final String privilege) {
        return Arrays.stream(REQUIRED_PRIVILEGES).anyMatch(each -> Arrays.stream(each).allMatch(privilege::contains));
    }
    
    @Override
    public void checkVariable(final Collection<? extends DataSource> dataSources) {
        for (DataSource each : dataSources) {
            checkVariable(each);
        }
    }
    
    private void checkVariable(final DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            for (Entry<String, String> entry : REQUIRED_VARIABLES.entrySet()) {
                checkVariable(connection, entry);
            }
        } catch (final SQLException ex) {
            throw new PrepareFailedException("Source data source check variables failed.", ex);
        }
    }
    
    private void checkVariable(final Connection connection, final Entry<String, String> entry) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(String.format(SHOW_VARIABLES_SQL, entry.getKey()));
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            String value = resultSet.getString(2);
            if (!entry.getValue().equalsIgnoreCase(value)) {
                throw new PrepareFailedException(String.format("Source data source required %s = %s, now is %s", entry.getKey(), entry.getValue(), value));
            }
        }
    }
}
