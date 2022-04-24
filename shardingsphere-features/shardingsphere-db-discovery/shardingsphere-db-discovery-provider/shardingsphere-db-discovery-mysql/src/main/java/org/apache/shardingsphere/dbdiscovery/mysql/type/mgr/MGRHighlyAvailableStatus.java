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

package org.apache.shardingsphere.dbdiscovery.mysql.type.mgr;

import com.google.common.base.Preconditions;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.dbdiscovery.spi.instance.type.IPPortPrimaryDatabaseInstance;
import org.apache.shardingsphere.dbdiscovery.spi.status.type.GlobalHighlyAvailableStatus;
import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Highly available status of MySQL MGR cluster.
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public final class MGRHighlyAvailableStatus implements GlobalHighlyAvailableStatus {
    
    private final boolean pluginActive;
    
    private final boolean singlePrimaryMode;
    
    private final String groupName;
    
    private final Collection<IPPortPrimaryDatabaseInstance> databaseInstances;
    
    @Override
    public void validate(final String databaseName, final Map<String, DataSource> dataSourceMap, final Properties props) throws SQLException {
        Preconditions.checkState(pluginActive, "MGR plugin is not active in database `%s`.", databaseName);
        Preconditions.checkState(singlePrimaryMode, "MGR is not in single primary mode in database `%s`.", databaseName);
        Preconditions.checkState(props.getProperty("group-name", "").equals(groupName),
                "Group name `%s` in MGR is not same with configured one `%s` in database `%s`.", groupName, props.getProperty("group-name"), databaseName);
        Preconditions.checkState(!databaseInstances.isEmpty(), "MGR member is empty in database `%s`.", databaseName);
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            checkDataSourceInReplicationGroup(databaseName, entry.getKey(), entry.getValue());
        }
    }
    
    private void checkDataSourceInReplicationGroup(final String databaseName, final String dataSourceName, final DataSource dataSource) throws SQLException {
        for (IPPortPrimaryDatabaseInstance each : databaseInstances) {
            try (Connection connection = dataSource.getConnection()) {
                if (connection.getMetaData().getURL().contains(each.toString())) {
                    return;
                }
            }
        }
        throw new ShardingSphereConfigurationException("%s is not in MGR replication group member in database `%s`.", dataSourceName, databaseName);
    }
}
