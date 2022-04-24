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
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryProviderAlgorithm;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;

/**
 * Abstract MySQL database discovery provider algorithm.
 */
@Getter
@Setter
@Slf4j
public abstract class AbstractMySQLDatabaseDiscoveryProviderAlgorithm implements DatabaseDiscoveryProviderAlgorithm {
    
    private Properties props = new Properties();
    
    private String primaryDataSource;
    
    @Override
    public final Optional<String> findPrimaryDataSourceName(final Map<String, DataSource> dataSourceMap) {
        String primaryDatabaseInstanceURL = loadPrimaryDatabaseInstanceURL(dataSourceMap);
        return findPrimaryDataSourceName(dataSourceMap, primaryDatabaseInstanceURL);
    }
    
    private Optional<String> findPrimaryDataSourceName(final Map<String, DataSource> dataSourceMap, final String primaryDatabaseInstanceURL) {
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            try (Connection connection = entry.getValue().getConnection()) {
                String url = connection.getMetaData().getURL();
                if (null != url && url.contains(primaryDatabaseInstanceURL)) {
                    return Optional.of(entry.getKey());
                }
            } catch (final SQLException ex) {
                log.error("An exception occurred while find primary data source name", ex);
            }
        }
        return Optional.empty();
    }
    
    private String loadPrimaryDatabaseInstanceURL(final Map<String, DataSource> dataSourceMap) {
        for (DataSource each : dataSourceMap.values()) {
            try (
                    Connection connection = each.getConnection();
                    Statement statement = connection.createStatement()) {
                Optional<String> primaryDatabaseInstanceURL = loadPrimaryDatabaseInstanceURL(statement);
                if (primaryDatabaseInstanceURL.isPresent()) {
                    return primaryDatabaseInstanceURL.get();
                }
            } catch (final SQLException ex) {
                log.error("An exception occurred while find primary data source url", ex);
            }
        }
        return "";
    }
    
    protected abstract Optional<String> loadPrimaryDatabaseInstanceURL(Statement statement) throws SQLException;
}
