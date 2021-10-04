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

package org.apache.shardingsphere.infra.config.datasource;

import org.apache.shardingsphere.infra.distsql.exception.resource.InvalidResourcesException;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Data source configuration validator.
 */
public final class DataSourceConfigurationValidator {
    
    /**
     * Validate data source configurations.
     * 
     * @param dataSourceConfigs data source configurations
     * @throws InvalidResourcesException invalid resources exception
     */
    public void validate(final Map<String, DataSourceConfiguration> dataSourceConfigs) throws InvalidResourcesException {
        Collection<String> errorMessages = new LinkedList<>();
        for (Entry<String, DataSourceConfiguration> entry : dataSourceConfigs.entrySet()) {
            try {
                validate(entry.getKey(), entry.getValue());
            } catch (final InvalidDataSourceConfigurationException ex) {
                errorMessages.add(ex.getMessage());
            }
        }
        if (!errorMessages.isEmpty()) {
            throw new InvalidResourcesException(errorMessages);
        }
    }
    
    private void validate(final String dataSourceConfigName, final DataSourceConfiguration dataSourceConfig) throws InvalidDataSourceConfigurationException {
        DataSource dataSource = null;
        try {
            dataSource = DataSourceConverter.getDataSource(dataSourceConfig);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            throw new InvalidDataSourceConfigurationException(dataSourceConfigName, ex.getMessage());
        } finally {
            if (null != dataSource) {
                close(dataSource);
            }
        }
    }
    
    private void close(final DataSource dataSource) {
        if (dataSource instanceof AutoCloseable) {
            try {
                ((AutoCloseable) dataSource).close();
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
            }
        }
    }
}
