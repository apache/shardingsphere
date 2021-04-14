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

import javax.sql.DataSource;

/**
 * Data source validator.
 */
public final class DataSourceValidator {
    
    /**
     * Validate.
     *
     * @param dataSourceConfiguration data source configuration
     * @return is valid or not
     */
    public boolean validate(final DataSourceConfiguration dataSourceConfiguration) {
        DataSource dataSource = null;
        try {
            dataSource = dataSourceConfiguration.createDataSource();
            return true;
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            return false;
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
