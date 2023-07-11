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

package org.apache.shardingsphere.data.pipeline.mysql.check.datasource;

import org.apache.shardingsphere.data.pipeline.core.exception.job.PrepareJobWithCheckPrivilegeFailedException;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PrepareJobWithInvalidSourceDataSourceException;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PrepareJobWithoutEnoughPrivilegeException;
import org.apache.shardingsphere.data.pipeline.core.preparer.datasource.checker.AbstractDataSourceChecker;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;

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
import java.util.stream.Collectors;

/**
 * Data source checker for MySQL.
 */
public final class MySQLDataSourceChecker extends AbstractDataSourceChecker {
    
    private static final String SHOW_GRANTS_SQL = "SHOW GRANTS";
    
    private static final String[][] REQUIRED_PRIVILEGES = {{"ALL PRIVILEGES", "ON *.*"}, {"REPLICATION SLAVE", "REPLICATION CLIENT", "ON *.*"}};
    
    private static final Map<String, String> REQUIRED_VARIABLES = new HashMap<>(3, 1F);
    
    private static final String SHOW_VARIABLES_SQL;
    
    static {
        REQUIRED_VARIABLES.put("LOG_BIN", "ON");
        REQUIRED_VARIABLES.put("BINLOG_FORMAT", "ROW");
        // It does not exist in all versions of MySQL
        REQUIRED_VARIABLES.put("BINLOG_ROW_IMAGE", "FULL");
        SHOW_VARIABLES_SQL = String.format("SHOW VARIABLES WHERE Variable_name IN (%s)", REQUIRED_VARIABLES.keySet().stream().map(each -> "?").collect(Collectors.joining(",")));
    }
    
    @Override
    public void checkPrivilege(final Collection<? extends DataSource> dataSources) {
        for (DataSource each : dataSources) {
            checkPrivilege(each);
        }
    }
    
    private void checkPrivilege(final DataSource dataSource) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(SHOW_GRANTS_SQL);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String privilege = resultSet.getString(1).toUpperCase();
                if (matchPrivileges(privilege)) {
                    return;
                }
            }
        } catch (final SQLException ex) {
            throw new PrepareJobWithCheckPrivilegeFailedException(ex);
        }
        throw new PrepareJobWithoutEnoughPrivilegeException(Arrays.asList("REPLICATION SLAVE", "REPLICATION CLIENT"));
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
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(SHOW_VARIABLES_SQL)) {
            int parameterIndex = 1;
            for (Entry<String, String> entry : REQUIRED_VARIABLES.entrySet()) {
                preparedStatement.setString(parameterIndex++, entry.getKey());
            }
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String key = resultSet.getString(1).toUpperCase();
                    String expectedValue = REQUIRED_VARIABLES.get(key);
                    String actualValue = resultSet.getString(2);
                    ShardingSpherePreconditions.checkState(expectedValue.equalsIgnoreCase(actualValue),
                            () -> new PrepareJobWithInvalidSourceDataSourceException(key, expectedValue, actualValue));
                }
            }
        } catch (final SQLException ex) {
            throw new PrepareJobWithCheckPrivilegeFailedException(ex);
        }
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
