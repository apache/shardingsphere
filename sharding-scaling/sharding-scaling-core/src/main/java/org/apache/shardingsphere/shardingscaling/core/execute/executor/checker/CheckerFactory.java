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

package org.apache.shardingsphere.shardingscaling.core.execute.executor.checker;

import lombok.SneakyThrows;
import org.apache.shardingsphere.shardingscaling.core.spi.ScalingEntry;
import org.apache.shardingsphere.shardingscaling.core.spi.ScalingEntryLoader;
import org.apache.shardingsphere.shardingscaling.core.util.DataSourceFactory;

/**
 * Checker factory.
 *
 * @author ssxlulu
 */
public class CheckerFactory {

    /**
     * New instance of checker.
     *
     * @param databaseType database type
     * @param dataSourceFactory data source
     * @return checker
     */
    @SneakyThrows
    public static Checker newInstanceChecker(final String databaseType, final DataSourceFactory dataSourceFactory) {
        ScalingEntry scalingEntry = ScalingEntryLoader.getScalingEntryByDatabaseType(databaseType);
        return scalingEntry.getCheckerClass().getConstructor(DataSourceFactory.class).newInstance(dataSourceFactory);
    }
}
