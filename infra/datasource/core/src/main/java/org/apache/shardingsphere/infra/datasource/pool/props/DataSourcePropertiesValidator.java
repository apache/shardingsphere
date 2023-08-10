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

package org.apache.shardingsphere.infra.datasource.pool.props;

import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.pool.destroyer.DataSourcePoolDestroyer;
import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Data source properties validator.
 */
public final class DataSourcePropertiesValidator {
    
    /**
     * Validate data source properties map.
     * 
     * @param dataSourcePropsMap data source properties map
     * @return error messages
     */
    public Collection<String> validate(final Map<String, DataSourceProperties> dataSourcePropsMap) {
        Collection<String> result = new LinkedList<>();
        for (Entry<String, DataSourceProperties> entry : dataSourcePropsMap.entrySet()) {
            try {
                validateProperties(entry.getKey(), entry.getValue());
                validateConnection(entry.getKey(), entry.getValue());
            } catch (final InvalidDataSourcePropertiesException ex) {
                result.add(ex.getMessage());
            }
        }
        return result;
    }
    
    private void validateProperties(final String dataSourceName, final DataSourceProperties dataSourceProps) throws InvalidDataSourcePropertiesException {
        Optional<DataSourcePoolMetaData> metaData = TypedSPILoader.findService(DataSourcePoolMetaData.class, dataSourceProps.getDataSourceClassName());
        if (!metaData.isPresent()) {
            return;
        }
        try {
            metaData.get().getDataSourcePoolPropertiesValidator().ifPresent(optional -> optional.validate(dataSourceProps));
        } catch (final IllegalArgumentException ex) {
            throw new InvalidDataSourcePropertiesException(dataSourceName, ex.getMessage());
        }
    }
    
    private void validateConnection(final String dataSourceName, final DataSourceProperties dataSourceProps) throws InvalidDataSourcePropertiesException {
        DataSource dataSource = null;
        try {
            dataSource = DataSourcePoolCreator.create(dataSourceProps);
            checkFailFast(dataSource);
            // CHECKSTYLE:OFF
        } catch (final SQLException | RuntimeException ex) {
            // CHECKSTYLE:ON
            throw new InvalidDataSourcePropertiesException(dataSourceName, ex.getMessage());
        } finally {
            if (null != dataSource) {
                new DataSourcePoolDestroyer(dataSource).asyncDestroy();
            }
        }
    }
    
    private void checkFailFast(final DataSource dataSource) throws SQLException {
        // CHECKSTYLE:OFF
        try (Connection ignored = dataSource.getConnection()) {
            // CHECKSTYLE:ON
        }
    }
}
