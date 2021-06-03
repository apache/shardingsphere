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

package org.apache.shardingsphere.scaling.core.executor.dumper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.scaling.core.common.datasource.DataSourceManager;
import org.apache.shardingsphere.scaling.core.config.DumperConfiguration;
import org.apache.shardingsphere.scaling.core.config.InventoryDumperConfiguration;
import org.apache.shardingsphere.scaling.core.job.position.ScalingPosition;
import org.apache.shardingsphere.scaling.core.spi.ScalingEntry;
import org.apache.shardingsphere.scaling.core.spi.ScalingEntryLoader;

/**
 * Dumper factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DumperFactory {
    
    /**
     * New instance of inventory dumper.
     *
     * @param inventoryDumperConfig inventory dumper configuration
     * @param dataSourceManager data source factory
     * @return JDBC dumper
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public static InventoryDumper newInstanceJdbcDumper(final InventoryDumperConfiguration inventoryDumperConfig, final DataSourceManager dataSourceManager) {
        ScalingEntry scalingEntry = ScalingEntryLoader.getInstance(inventoryDumperConfig.getDataSourceConfig().getDatabaseType().getName());
        return scalingEntry.getInventoryDumperClass().getConstructor(InventoryDumperConfiguration.class, DataSourceManager.class).newInstance(inventoryDumperConfig, dataSourceManager);
    }
    
    /**
     * New instance of incremental dumper.
     *
     * @param dumperConfig dumper configuration
     * @param position position
     * @return log dumper
     */
    public static IncrementalDumper newInstanceLogDumper(final DumperConfiguration dumperConfig, final ScalingPosition<?> position) {
        return newInstanceLogDumper(dumperConfig.getDataSourceConfig().getDatabaseType().getName(), dumperConfig, position);
    }
    
    /**
     * New instance of incremental dumper.
     *
     * @param databaseType database type
     * @param dumperConfig dumper configuration
     * @param position position
     * @return log dumper
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public static IncrementalDumper newInstanceLogDumper(final String databaseType, final DumperConfiguration dumperConfig, final ScalingPosition<?> position) {
        ScalingEntry scalingEntry = ScalingEntryLoader.getInstance(databaseType);
        return scalingEntry.getIncrementalDumperClass().getConstructor(DumperConfiguration.class, ScalingPosition.class).newInstance(dumperConfig, position);
    }
}
