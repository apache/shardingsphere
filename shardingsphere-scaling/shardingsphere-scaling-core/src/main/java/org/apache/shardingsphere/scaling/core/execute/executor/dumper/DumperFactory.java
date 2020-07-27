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

package org.apache.shardingsphere.scaling.core.execute.executor.dumper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.scaling.core.config.RdbmsConfiguration;
import org.apache.shardingsphere.scaling.core.job.position.Position;
import org.apache.shardingsphere.scaling.core.spi.ScalingEntry;
import org.apache.shardingsphere.scaling.core.spi.ScalingEntryLoader;
import org.apache.shardingsphere.scaling.core.datasource.DataSourceManager;

/**
 * Dumper factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DumperFactory {
    
    /**
     * New instance of JDBC dumper.
     *
     * @param rdbmsConfiguration rdbms configuration
     * @param dataSourceManager data source factory
     * @return JDBC dumper
     */
    @SneakyThrows
    public static JDBCDumper newInstanceJdbcDumper(final RdbmsConfiguration rdbmsConfiguration, final DataSourceManager dataSourceManager) {
        return newInstanceJdbcDumper(rdbmsConfiguration.getDataSourceConfiguration().getDatabaseType().getName(), rdbmsConfiguration, dataSourceManager);
    }
    
    /**
     * New instance of JDBC dumper.
     *
     * @param databaseType database type
     * @param rdbmsConfiguration rdbms configuration
     * @param dataSourceManager data source factory
     * @return JDBC dumper
     */
    @SneakyThrows
    public static JDBCDumper newInstanceJdbcDumper(final String databaseType, final RdbmsConfiguration rdbmsConfiguration, final DataSourceManager dataSourceManager) {
        ScalingEntry scalingEntry = ScalingEntryLoader.getScalingEntryByDatabaseType(databaseType);
        return scalingEntry.getJdbcDumperClass().getConstructor(RdbmsConfiguration.class, DataSourceManager.class).newInstance(rdbmsConfiguration, dataSourceManager);
    }
    
    /**
     * New instance of log dumper.
     *
     * @param rdbmsConfiguration rdbms configuration
     * @param position position
     * @return log dumper
     */
    @SneakyThrows
    public static LogDumper newInstanceLogDumper(final RdbmsConfiguration rdbmsConfiguration, final Position position) {
        return newInstanceLogDumper(rdbmsConfiguration.getDataSourceConfiguration().getDatabaseType().getName(), rdbmsConfiguration, position);
    }
    
    /**
     * New instance of log dumper.
     *
     * @param databaseType database type
     * @param rdbmsConfiguration rdbms configuration
     * @param position position
     * @return log dumper
     */
    @SneakyThrows
    public static LogDumper newInstanceLogDumper(final String databaseType, final RdbmsConfiguration rdbmsConfiguration, final Position position) {
        ScalingEntry scalingEntry = ScalingEntryLoader.getScalingEntryByDatabaseType(databaseType);
        return scalingEntry.getLogDumperClass().getConstructor(RdbmsConfiguration.class, Position.class).newInstance(rdbmsConfiguration, position);
    }
}
