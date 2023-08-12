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

package org.apache.shardingsphere.infra.datasource.pool.props.validator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.pool.destroyer.DataSourcePoolDestroyer;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Data source pool properties validator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourcePoolPropertiesValidator {
    
    /**
     * Validate data source pool properties map.
     * 
     * @param propsMap data source pool properties map
     * @return error messages
     */
    public static Collection<String> validate(final Map<String, DataSourcePoolProperties> propsMap) {
        Collection<String> result = new LinkedList<>();
        for (Entry<String, DataSourcePoolProperties> entry : propsMap.entrySet()) {
            try {
                validateProperties(entry.getKey(), entry.getValue());
                validateConnection(entry.getKey(), entry.getValue());
            } catch (final InvalidDataSourcePoolPropertiesException ex) {
                result.add(ex.getMessage());
            }
        }
        return result;
    }
    
    private static void validateProperties(final String dataSourceName, final DataSourcePoolProperties props) throws InvalidDataSourcePoolPropertiesException {
        try {
            TypedSPILoader.findService(DataSourcePoolPropertiesContentValidator.class, props.getPoolClassName()).ifPresent(optional -> optional.validate(props));
        } catch (final IllegalArgumentException ex) {
            throw new InvalidDataSourcePoolPropertiesException(dataSourceName, ex.getMessage());
        }
    }
    
    private static void validateConnection(final String dataSourceName, final DataSourcePoolProperties props) throws InvalidDataSourcePoolPropertiesException {
        DataSource dataSource = null;
        try {
            dataSource = DataSourcePoolCreator.create(props);
            checkFailFast(dataSource);
            // CHECKSTYLE:OFF
        } catch (final SQLException | RuntimeException ex) {
            // CHECKSTYLE:ON
            throw new InvalidDataSourcePoolPropertiesException(dataSourceName, ex.getMessage());
        } finally {
            if (null != dataSource) {
                new DataSourcePoolDestroyer(dataSource).asyncDestroy();
            }
        }
    }
    
    @SuppressWarnings("EmptyTryBlock")
    private static void checkFailFast(final DataSource dataSource) throws SQLException {
        // CHECKSTYLE:OFF
        try (Connection ignored = dataSource.getConnection()) {
            // CHECKSTYLE:ON
        }
    }
}
