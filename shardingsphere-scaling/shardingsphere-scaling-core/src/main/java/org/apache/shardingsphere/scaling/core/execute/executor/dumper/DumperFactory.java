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
import org.apache.shardingsphere.scaling.core.config.DumperConfiguration;
import org.apache.shardingsphere.scaling.core.config.InventoryDumperConfiguration;
import org.apache.shardingsphere.scaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.scaling.core.job.position.Position;
import org.apache.shardingsphere.scaling.core.spi.ScalingEntry;
import org.apache.shardingsphere.scaling.core.spi.ScalingEntryLoader;

/**
 * Dumper factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DumperFactory {
    
    /**
     * New instance of JDBC dumper.
     *
     * @param inventoryDumperConfig inventory dumper configuration
     * @param dataSourceManager data source factory
     * @return JDBC dumper
     */
    public static JDBCDumper newInstanceJdbcDumper(final InventoryDumperConfiguration inventoryDumperConfig, final DataSourceManager dataSourceManager) {
        return newInstanceJdbcDumper(inventoryDumperConfig.getDataSourceConfiguration().getDatabaseType().getName(), inventoryDumperConfig, dataSourceManager);
    }
    
    /**
     * New instance of JDBC dumper.
     *
     * @param databaseType database type
     * @param inventoryDumperConfig inventory dumper configuration
     * @param dataSourceManager data source factory
     * @return JDBC dumper
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public static JDBCDumper newInstanceJdbcDumper(final String databaseType, final InventoryDumperConfiguration inventoryDumperConfig, final DataSourceManager dataSourceManager) {
        ScalingEntry scalingEntry = ScalingEntryLoader.getScalingEntryByDatabaseType(databaseType);
        return scalingEntry.getJdbcDumperClass().getConstructor(InventoryDumperConfiguration.class, DataSourceManager.class).newInstance(inventoryDumperConfig, dataSourceManager);
    }
    
    /**
     * New instance of log dumper.
     *
     * @param dumperConfig rdbms configuration
     * @param position position
     * @return log dumper
     */
    public static LogDumper newInstanceLogDumper(final DumperConfiguration dumperConfig, final Position position) {
        return newInstanceLogDumper(dumperConfig.getDataSourceConfiguration().getDatabaseType().getName(), dumperConfig, position);
    }
    
    /**
     * New instance of log dumper.
     *
     * @param databaseType database type
     * @param dumperConfig rdbms configuration
     * @param position position
     * @return log dumper
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public static LogDumper newInstanceLogDumper(final String databaseType, final DumperConfiguration dumperConfig, final Position position) {
        ScalingEntry scalingEntry = ScalingEntryLoader.getScalingEntryByDatabaseType(databaseType);
        return scalingEntry.getLogDumperClass().getConstructor(DumperConfiguration.class, Position.class).newInstance(dumperConfig, position);
    }
}
