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

package org.apache.shardingsphere.agent.plugin.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.driver.ShardingSphereDriver;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.driver.jdbc.core.driver.DriverDataSourceCache;

import javax.sql.DataSource;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * ShardingSphere driver utils.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingSphereDriverUtils {
    
    /**
     * Find ShardingSphere data sources.
     *
     * @return found data source
     */
    public static Optional<Map<String, ShardingSphereDataSource>> findShardingSphereDataSources() {
        return findShardingSphereDriver().flatMap(ShardingSphereDriverUtils::findShardingSphereDataSources);
    }
    
    private static Optional<Map<String, ShardingSphereDataSource>> findShardingSphereDataSources(final Driver driver) {
        DriverDataSourceCache dataSourceCache = AgentReflectionUtils.getFieldValue(driver, "dataSourceCache");
        Map<String, DataSource> dataSourceMap = AgentReflectionUtils.getFieldValue(dataSourceCache, "dataSourceMap");
        Map<String, ShardingSphereDataSource> result = new LinkedHashMap<>();
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            if (entry.getValue() instanceof ShardingSphereDataSource) {
                result.put(entry.getKey(), (ShardingSphereDataSource) entry.getValue());
            }
        }
        return Optional.of(result);
    }
    
    private static Optional<ShardingSphereDriver> findShardingSphereDriver() {
        Enumeration<Driver> driverEnumeration = DriverManager.getDrivers();
        while (driverEnumeration.hasMoreElements()) {
            Driver driver = driverEnumeration.nextElement();
            if (driver instanceof ShardingSphereDriver) {
                return Optional.of((ShardingSphereDriver) driver);
            }
        }
        return Optional.empty();
    }
}
