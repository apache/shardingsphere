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

package org.apache.shardingsphere.scaling.core.job.position;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.scaling.core.spi.ScalingEntry;
import org.apache.shardingsphere.scaling.core.spi.ScalingEntryLoader;

import javax.sql.DataSource;

/**
 * Position manager factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PositionManagerFactory {
    
    /**
     * New instance of position manager.
     *
     * @param databaseType database type
     * @param dataSource data source
     * @return position manager
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public static PositionManager newInstance(final String databaseType, final DataSource dataSource) {
        ScalingEntry scalingEntry = ScalingEntryLoader.getScalingEntryByDatabaseType(databaseType);
        return scalingEntry.getPositionManager().getConstructor(DataSource.class).newInstance(dataSource);
    }
    
    /**
     * New instance of position manager.
     *
     * @param databaseType database type
     * @param position position
     * @return position manager
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public static PositionManager newInstance(final String databaseType, final String position) {
        ScalingEntry scalingEntry = ScalingEntryLoader.getScalingEntryByDatabaseType(databaseType);
        return scalingEntry.getPositionManager().getConstructor(String.class).newInstance(position);
    }
}
