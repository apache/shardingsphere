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

import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
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
    public static Map<String, ShardingSphereDataSource> findShardingSphereDataSources() {
        return findShardingSphereDriver().map(ShardingSphereDriver::getShardingSphereDataSources).orElse(Collections.emptyMap());
    }
    
    @SuppressWarnings("UseOfJDBCDriverClass")
    private static Optional<ShardingSphereDriver> findShardingSphereDriver() {
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver instanceof ShardingSphereDriver) {
                return Optional.of((ShardingSphereDriver) driver);
            }
        }
        return Optional.empty();
    }
}
