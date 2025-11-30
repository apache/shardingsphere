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

package org.apache.shardingsphere.timeservice.type.database;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.timeservice.spi.TimestampService;
import org.apache.shardingsphere.timeservice.type.database.exception.DatetimeLoadingException;
import org.apache.shardingsphere.timeservice.type.database.provider.TimestampLoadingSQLProvider;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Database timestamp service.
 */
public final class DatabaseTimestampService implements TimestampService {
    
    private DataSource dataSource;
    
    private DatabaseType storageType;
    
    @Override
    public void init(final Properties props) {
        dataSource = DataSourcePoolCreator.create(new YamlDataSourceConfigurationSwapper().swapToDataSourcePoolProperties(
                props.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().toString(), Entry::getValue))));
        storageType = DatabaseTypeEngine.getStorageType(dataSource);
    }
    
    @Override
    public Timestamp getTimestamp() {
        try {
            return loadDatetime(dataSource, DatabaseTypedSPILoader.getService(TimestampLoadingSQLProvider.class, storageType).getTimestampLoadingSQL());
        } catch (final SQLException ex) {
            throw new DatetimeLoadingException(ex);
        }
    }
    
    private Timestamp loadDatetime(final DataSource dataSource, final String datetimeLoadingSQL) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(datetimeLoadingSQL)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                return (Timestamp) resultSet.getObject(1);
            }
        }
    }
    
    @Override
    public String getType() {
        return "Database";
    }
}
