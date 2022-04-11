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

package org.apache.shardingsphere.dbdiscovery.opengauss;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryType;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.metadata.schema.QualifiedSchema;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceDisabledEvent;
import org.apache.shardingsphere.infra.rule.event.impl.PrimaryDataSourceChangedEvent;
import org.apache.shardingsphere.infra.storage.StorageNodeDataSource;
import org.apache.shardingsphere.infra.storage.StorageNodeRole;
import org.apache.shardingsphere.infra.storage.StorageNodeStatus;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * OpenGauss database discovery type.
 */
@Slf4j
public final class OpenGaussDatabaseDiscoveryType implements DatabaseDiscoveryType {
    
    private static final String DB_ROLE = "SELECT local_role,db_state FROM pg_stat_get_stream_replications()";
    
    private String oldPrimaryDataSource;
    
    @Getter
    @Setter
    private Properties props = new Properties();
    
    @Override
    public void checkDatabaseDiscoveryConfiguration(final String schemaName, final Map<String, DataSource> dataSourceMap) {
        //TODO Check openGauss configuration
    }
    
    @Override
    public void updatePrimaryDataSource(final String schemaName, final Map<String, DataSource> dataSourceMap, final Collection<String> disabledDataSourceNames, final String groupName) {
        String newPrimaryDataSource = determinePrimaryDataSource(dataSourceMap);
        if (newPrimaryDataSource.isEmpty()) {
            oldPrimaryDataSource = "";
            return;
        }
        if (!newPrimaryDataSource.equals(oldPrimaryDataSource)) {
            oldPrimaryDataSource = newPrimaryDataSource;
            ShardingSphereEventBus.getInstance().post(new PrimaryDataSourceChangedEvent(new QualifiedSchema(schemaName, groupName, newPrimaryDataSource)));
        }
    }
    
    private String determinePrimaryDataSource(final Map<String, DataSource> dataSourceMap) {
        String result = "";
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            try (Connection connection = entry.getValue().getConnection();
                 Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(DB_ROLE)) {
                if (resultSet.next()) {
                    if (resultSet.getString("local_role").equals("Primary") && resultSet.getString("db_state").equals("Normal")) {
                        return entry.getKey();
                    }
                }
            } catch (final SQLException ex) {
                log.error("An exception occurred while find primary data source url", ex);
            }
        }
        return result;
    }
    
    @Override
    public void updateMemberState(final String schemaName, final Map<String, DataSource> dataSourceMap, final String groupName) {
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            boolean disable = true;
            try (Connection connection = entry.getValue().getConnection();
                 Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(DB_ROLE)) {
                if (resultSet.next()) {
                    if ((resultSet.getString("local_role").equals("Standby") && resultSet.getString("db_state").equals("Normal"))
                            || entry.getKey().equals(oldPrimaryDataSource)) {
                        disable = false;
                    }
                }
            } catch (final SQLException ex) {
                log.error("An exception occurred while find data source urls", ex);
            }
            ShardingSphereEventBus.getInstance().post(new DataSourceDisabledEvent(schemaName, groupName, entry.getKey(),
                    new StorageNodeDataSource(StorageNodeRole.MEMBER, disable ? StorageNodeStatus.DISABLED : StorageNodeStatus.ENABLED)));
        }
    }
    
    @Override
    public String getPrimaryDataSource() {
        return oldPrimaryDataSource;
    }
    
    @Override
    public String getType() {
        return "openGauss";
    }
}
