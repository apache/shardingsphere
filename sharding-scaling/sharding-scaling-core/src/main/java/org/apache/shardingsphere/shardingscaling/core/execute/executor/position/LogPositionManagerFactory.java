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

package org.apache.shardingsphere.shardingscaling.core.execute.executor.position;

import lombok.SneakyThrows;
import org.apache.shardingsphere.shardingscaling.core.config.RdbmsConfiguration;
import org.apache.shardingsphere.shardingscaling.core.spi.ScalingEntry;
import org.apache.shardingsphere.shardingscaling.core.spi.ScalingEntryLoader;
import org.apache.shardingsphere.shardingscaling.core.util.DataSourceFactory;

/**
 * Log manager factory.
 *
 * @author avalon566
 */
public class LogPositionManagerFactory {

    /**
     * New instance of log manager.
     *
     * @param rdbmsConfiguration rdbms configuration
     * @param dataSourceFactory data source factory
     * @return log manager
     */
    @SneakyThrows
    public static LogPositionManager newInstanceLogManager(final RdbmsConfiguration rdbmsConfiguration, final DataSourceFactory dataSourceFactory) {
        return newInstanceLogManager(rdbmsConfiguration.getDataSourceConfiguration().getDatabaseType().getName(), rdbmsConfiguration, dataSourceFactory);
    }

    /**
     * New instance of log manager.
     *
     * @param databaseType database type
     * @param rdbmsConfiguration rdbms configuration
     * @param dataSourceFactory data source factory
     * @return log manager
     */
    @SneakyThrows
    public static LogPositionManager newInstanceLogManager(final String databaseType, final RdbmsConfiguration rdbmsConfiguration, final DataSourceFactory dataSourceFactory) {
        ScalingEntry scalingEntry = ScalingEntryLoader.getScalingEntryByDatabaseType(databaseType);
        return scalingEntry.getLogPositionManager().getConstructor(RdbmsConfiguration.class, DataSourceFactory.class).newInstance(rdbmsConfiguration, dataSourceFactory);
    }
}
