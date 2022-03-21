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

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.dbdiscovery.mysql.AbstractDatabaseDiscoveryType;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceDisabledEvent;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
    public void checkDatabaseDiscoveryConfiguration(final String schemaName, final Map<String, DataSource> dataSourceMap) {
        //TODO Check master-slave mode
    }
    
    @Override
    protected String getPrimaryDataSourceURL(final Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(SHOW_SLAVE_STATUS)) {
            if (resultSet.next()) {
                return String.format("%s:%s", resultSet.getString("Master_Host"), resultSet.getString("Master_Port"));
            }
            return "";
        }
    }
    
    @Override
    protected void determineMemberDataSourceState(final String schemaName, final Map<String, DataSource> dataSourceMap) {
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            if (getOldPrimaryDataSource().equals(entry.getKey())) {
                continue;
            }
            determineDatasourceState(schemaName, entry.getKey(), entry.getValue());
        }
    }
    
    private void determineDatasourceState(final String schemaName, final String datasourceName, final DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            long replicationDelayTime = getSecondsBehindMaster(statement);
            if (replicationDelayTime * 1000 < Long.parseLong(props.getProperty("delay-milliseconds-threshold"))) {
                ShardingSphereEventBus.getInstance().post(new DataSourceDisabledEvent(schemaName, datasourceName, false));
            } else {
                ShardingSphereEventBus.getInstance().post(new DataSourceDisabledEvent(schemaName, datasourceName, true));
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
