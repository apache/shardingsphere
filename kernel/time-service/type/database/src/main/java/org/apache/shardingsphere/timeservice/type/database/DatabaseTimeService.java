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

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.timeservice.spi.ShardingSphereTimeService;
import org.apache.shardingsphere.timeservice.type.database.exception.DatetimeLoadingException;
import org.apache.shardingsphere.timeservice.type.database.provider.DatetimeLoadingSQLProvider;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Database time service.
 */
public final class DatabaseTimeService implements ShardingSphereTimeService {
    
    private DataSource dataSource;
    
    private DatabaseType storageType;
    
    @Override
    public void init(final Properties props) {
        dataSource = DataSourcePoolCreator.create(new YamlDataSourceConfigurationSwapper().swapToDataSourceProperties(
                props.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().toString(), Entry::getValue, (key, value) -> value))));
        storageType = DatabaseTypeEngine.getStorageType(Collections.singleton(dataSource));
    }
    
    @Override
    public Date getDatetime() {
        try {
            return loadDatetime(dataSource, TypedSPILoader.getService(DatetimeLoadingSQLProvider.class, DatabaseTypeEngine.getTrunkDatabaseTypeName(storageType)).getDatetimeLoadingSQL());
        } catch (final SQLException ex) {
            throw new DatetimeLoadingException(ex);
        }
    }
    
    private Date loadDatetime(final DataSource dataSource, final String datetimeLoadingSQL) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(datetimeLoadingSQL)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                return (Date) resultSet.getObject(1);
            }
        }
    }
    
    @Override
    public String getType() {
        return "Database";
    }
}
