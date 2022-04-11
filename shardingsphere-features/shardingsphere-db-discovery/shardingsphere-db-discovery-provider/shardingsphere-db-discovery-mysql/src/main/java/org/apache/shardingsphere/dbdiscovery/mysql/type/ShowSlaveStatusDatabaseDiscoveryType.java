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

package org.apache.shardingsphere.dbdiscovery.mysql.type;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.dbdiscovery.mysql.AbstractDatabaseDiscoveryType;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceDisabledEvent;
import org.apache.shardingsphere.infra.storage.StorageNodeDataSource;
import org.apache.shardingsphere.infra.storage.StorageNodeRole;
import org.apache.shardingsphere.infra.storage.StorageNodeStatus;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Show slave status database discovery type.
 */
@Slf4j
public final class ShowSlaveStatusDatabaseDiscoveryType extends AbstractDatabaseDiscoveryType {
    
    private static final String SHOW_SLAVE_STATUS = "SHOW SLAVE STATUS";
    
    @Getter
    @Setter
    private Properties props = new Properties();
    
    @Override
    public void checkDatabaseDiscoveryConfiguration(final String schemaName, final Map<String, DataSource> dataSourceMap) throws SQLException {
        Collection<String> result = getPrimaryDataSourceURLS(dataSourceMap);
        Preconditions.checkState(!result.isEmpty(), "Not found primary data source for schemaName `%s`", schemaName);
        Preconditions.checkState(1 == result.size(), "More than one primary data source for schemaName `%s`", schemaName);
    }
    
    private Collection<String> getPrimaryDataSourceURLS(final Map<String, DataSource> dataSourceMap) throws SQLException {
        Collection<String> result = new ArrayList<>();
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            try (Connection connection = entry.getValue().getConnection();
                 Statement statement = connection.createStatement()) {
                String url = getPrimaryDataSourceURL(statement);
                if (!url.isEmpty() && !result.contains(url)) {
                    result.add(url);
                }
            }
        }
        return result;
    }
    
    @Override
    protected String getPrimaryDataSourceURL(final Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(SHOW_SLAVE_STATUS)) {
            if (resultSet.next()) {
                String masterHost = resultSet.getString("Master_Host");
                String masterPort = resultSet.getString("Master_Port");
                if (null != masterHost && null != masterPort) {
                    return String.format("%s:%s", masterHost, masterPort);
                }
            }
            return "";
        }
    }
    
    @Override
    public void updateMemberState(final String schemaName, final Map<String, DataSource> dataSourceMap, final String groupName) {
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            if (entry.getKey().equals(getPrimaryDataSource())) {
                continue;
            }
            determineDatasourceState(schemaName, entry.getKey(), entry.getValue(), groupName);
        }
    }
    
    private void determineDatasourceState(final String schemaName, final String datasourceName, final DataSource dataSource, final String groupName) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            long replicationDelayMilliseconds = getSecondsBehindMaster(statement) * 1000L;
            if (replicationDelayMilliseconds < Long.parseLong(props.getProperty("delay-milliseconds-threshold"))) {
                ShardingSphereEventBus.getInstance().post(new DataSourceDisabledEvent(schemaName, groupName, datasourceName,
                        new StorageNodeDataSource(StorageNodeRole.MEMBER, StorageNodeStatus.ENABLED, replicationDelayMilliseconds)));
            } else {
                ShardingSphereEventBus.getInstance().post(new DataSourceDisabledEvent(schemaName, groupName, datasourceName,
                        new StorageNodeDataSource(StorageNodeRole.MEMBER, StorageNodeStatus.DISABLED, replicationDelayMilliseconds)));
            }
        } catch (SQLException ex) {
            log.error("An exception occurred while find member data source `Seconds_Behind_Master`", ex);
        }
    }
    
    private long getSecondsBehindMaster(final Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(SHOW_SLAVE_STATUS)) {
            if (resultSet.next()) {
                return resultSet.getLong("Seconds_Behind_Master");
            }
            return 0L;
        }
    }
    
    @Override
    public String getType() {
        return "SHOW_SLAVE_STATUS";
    }
}
