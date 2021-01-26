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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * Data source validator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceValidator {
    
    /**
     * Validate.
     *
     * @param dataSources data sources.
     * @return is valid or not
     */
    public static boolean validate(final Map<String, DataSourceConfiguration> dataSources) {
        Collection<DataSource> result = new LinkedList<>();
        try {
            for (DataSourceConfiguration each : dataSources.values()) {
                result.add(each.createDataSource());
            }
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            return false;
        } finally {
            result.forEach(DataSourceValidator::close);
        }
        return true;
    }
    
    private static void close(final DataSource dataSource) {
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
