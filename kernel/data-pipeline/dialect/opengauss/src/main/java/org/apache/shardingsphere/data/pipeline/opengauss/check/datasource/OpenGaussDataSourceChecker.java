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

package org.apache.shardingsphere.data.pipeline.opengauss.check.datasource;

import org.apache.shardingsphere.data.pipeline.core.exception.job.PrepareJobWithCheckPrivilegeFailedException;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PrepareJobWithoutEnoughPrivilegeException;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PrepareJobWithoutUserException;
import org.apache.shardingsphere.data.pipeline.core.preparer.datasource.checker.AbstractDataSourceChecker;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

/**
 * Data source checker of openGauss.
 */
public final class OpenGaussDataSourceChecker extends AbstractDataSourceChecker {
    
    private static final String SHOW_GRANTS_SQL = "SELECT * FROM pg_roles WHERE rolname = ?";
    
    @Override
    public void checkPrivilege(final Collection<? extends DataSource> dataSources) {
        for (DataSource each : dataSources) {
            checkPrivilege(each);
        }
    }
    
    private void checkPrivilege(final DataSource dataSource) {
        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SHOW_GRANTS_SQL)) {
            DatabaseMetaData metaData = connection.getMetaData();
            preparedStatement.setString(1, metaData.getUserName());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                String username = metaData.getUserName();
                ShardingSpherePreconditions.checkState(resultSet.next(), () -> new PrepareJobWithoutUserException(username));
                String isSuperRole = resultSet.getString("rolsuper");
                String isReplicationRole = resultSet.getString("rolreplication");
                String isSystemAdminRole = resultSet.getString("rolsystemadmin");
                ShardingSpherePreconditions.checkState("t".equalsIgnoreCase(isSuperRole) || "t".equalsIgnoreCase(isReplicationRole) || "t".equalsIgnoreCase(isSystemAdminRole),
                        () -> new PrepareJobWithoutEnoughPrivilegeException(Collections.singleton("REPLICATION")));
            }
        } catch (final SQLException ex) {
            throw new PrepareJobWithCheckPrivilegeFailedException(ex);
        }
    }
    
    @Override
    public void checkVariable(final Collection<? extends DataSource> dataSources) {
    }
    
    @Override
    public String getDatabaseType() {
        return "openGauss";
    }
}
