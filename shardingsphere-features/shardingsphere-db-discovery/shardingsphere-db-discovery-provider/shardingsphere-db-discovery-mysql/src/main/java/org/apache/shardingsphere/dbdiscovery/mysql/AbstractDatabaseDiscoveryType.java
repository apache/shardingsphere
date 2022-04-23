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
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Abstract database discovery type.
 */
@Getter
@Setter
@Slf4j
public abstract class AbstractDatabaseDiscoveryType implements DatabaseDiscoveryType {
    
    private String oldPrimaryDataSource;
    
    @Override
    public final Optional<String> determinePrimaryDataSource(final Map<String, DataSource> dataSourceMap) {
        return findPrimaryDataSourceName(loadPrimaryDataSourceURL(dataSourceMap), dataSourceMap);
    }
    
    private String loadPrimaryDataSourceURL(final Map<String, DataSource> dataSourceMap) {
        for (DataSource each : dataSourceMap.values()) {
            try (
                    Connection connection = each.getConnection();
                    Statement statement = connection.createStatement()) {
                Optional<String> primaryDataSourceURL = loadPrimaryDataSourceURL(statement);
                if (primaryDataSourceURL.isPresent()) {
                    return primaryDataSourceURL.get();
                }
            } catch (final SQLException ex) {
                log.error("An exception occurred while find primary data source url", ex);
            }
        }
        return "";
    }
    
    protected abstract Optional<String> loadPrimaryDataSourceURL(Statement statement) throws SQLException;
    
    private Optional<String> findPrimaryDataSourceName(final String primaryDataSourceURL, final Map<String, DataSource> dataSourceMap) {
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            try (Connection connection = entry.getValue().getConnection()) {
                String url = connection.getMetaData().getURL();
                if (null != url && url.contains(primaryDataSourceURL)) {
                    return Optional.of(entry.getKey());
                }
            } catch (final SQLException ex) {
                log.error("An exception occurred while find primary data source name", ex);
            }
        }
        return Optional.empty();
    }
    
    @Override
    public final String getPrimaryDataSource() {
        return oldPrimaryDataSource;
    }
}
