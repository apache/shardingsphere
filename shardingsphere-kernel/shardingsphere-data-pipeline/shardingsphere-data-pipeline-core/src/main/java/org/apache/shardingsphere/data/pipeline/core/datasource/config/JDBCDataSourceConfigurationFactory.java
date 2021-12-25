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

package org.apache.shardingsphere.data.pipeline.core.datasource.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.core.datasource.config.impl.ShardingSphereJDBCDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.datasource.config.impl.StandardJDBCDataSourceConfiguration;

/**
 * JDBC data source configuration factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JDBCDataSourceConfigurationFactory {
    
    /**
     * Get new instance of JDBC data source configuration.
     *
     * @param type type of JDBC data source configuration
     * @param parameter parameter of JDBC data source configuration
     * @return new instance of JDBC data source configuration
     */
    public static JDBCDataSourceConfiguration newInstance(final String type, final String parameter) {
        switch (type) {
            case StandardJDBCDataSourceConfiguration.TYPE:
                return new StandardJDBCDataSourceConfiguration(parameter);
            case ShardingSphereJDBCDataSourceConfiguration.TYPE:
                return new ShardingSphereJDBCDataSourceConfiguration(parameter);
            default:
                throw new UnsupportedOperationException(String.format("Unsupported data source type '%s'", type));
        }
    }
}
