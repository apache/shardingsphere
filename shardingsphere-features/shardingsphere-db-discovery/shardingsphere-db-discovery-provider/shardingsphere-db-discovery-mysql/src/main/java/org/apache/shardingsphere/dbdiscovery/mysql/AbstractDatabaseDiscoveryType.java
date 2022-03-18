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

package org.apache.shardingsphere.dbdiscovery.mysql;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryType;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.rule.event.impl.PrimaryDataSourceChangedEvent;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract database discovery type.
 */
@Slf4j
public abstract class AbstractDatabaseDiscoveryType implements DatabaseDiscoveryType {
    
    @Getter
    private String oldPrimaryDataSource;
    
    protected abstract String getPrimaryDataSourceURL(Statement statement) throws SQLException;
    
    protected abstract void determineMemberDataSourceState(String schemaName, Map<String, DataSource> dataSourceMap);
    
    @Override
    public void updatePrimaryDataSource(final String schemaName, final Map<String, DataSource> dataSourceMap, final Collection<String> disabledDataSourceNames, final String groupName) {
        Map<String, DataSource> activeDataSourceMap = new HashMap<>(dataSourceMap);
        if (!disabledDataSourceNames.isEmpty()) {
            activeDataSourceMap.entrySet().removeIf(each -> disabledDataSourceNames.contains(each.getKey()));
        }
        String newPrimaryDataSource = determinePrimaryDataSource(activeDataSourceMap);
        if (newPrimaryDataSource.isEmpty()) {
            return;
        }
        if (!newPrimaryDataSource.equals(oldPrimaryDataSource)) {
            oldPrimaryDataSource = newPrimaryDataSource;
            ShardingSphereEventBus.getInstance().post(new PrimaryDataSourceChangedEvent(schemaName, groupName, newPrimaryDataSource));
        }
    }
    
    @Override
    public void updateMemberState(final String schemaName, final Map<String, DataSource> dataSourceMap, final Collection<String> disabledDataSourceNames) {
        Map<String, DataSource> activeDataSourceMap = new HashMap<>(dataSourceMap);
        if (!disabledDataSourceNames.isEmpty()) {
            activeDataSourceMap.entrySet().removeIf(each -> disabledDataSourceNames.contains(each.getKey()));
        }
        determineMemberDataSourceState(schemaName, activeDataSourceMap);
    }
    
    private String determinePrimaryDataSource(final Map<String, DataSource> dataSourceMap) {
        String primaryDataSourceURL = findPrimaryDataSourceURL(dataSourceMap);
        return findPrimaryDataSourceName(primaryDataSourceURL, dataSourceMap);
    }
    
    private String findPrimaryDataSourceURL(final Map<String, DataSource> dataSourceMap) {
        for (DataSource each : dataSourceMap.values()) {
            try (Connection connection = each.getConnection();
                 Statement statement = connection.createStatement()) {
                String primaryDataSourceURL = getPrimaryDataSourceURL(statement);
                if (!primaryDataSourceURL.isEmpty()) {
                    return primaryDataSourceURL;
                }
            } catch (final SQLException ex) {
                log.error("An exception occurred while find primary data source url", ex);
            }
        }
        return "";
    }
    
    private String findPrimaryDataSourceName(final String primaryDataSourceURL, final Map<String, DataSource> dataSourceMap) {
        String result = "";
        for (Map.Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            String url;
            try (Connection connection = entry.getValue().getConnection()) {
                url = connection.getMetaData().getURL();
                if (null != url && url.contains(primaryDataSourceURL)) {
                    return entry.getKey();
                }
            } catch (final SQLException ex) {
                log.error("An exception occurred while find primary data source name", ex);
            }
        }
        return result;
    }
    
    @Override
    public String getPrimaryDataSource() {
        return oldPrimaryDataSource;
    }
}
