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

package org.apache.shardingsphere.infra.config.datasource.killer.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.apache.shardingsphere.infra.config.datasource.killer.DataSourceKiller;

import com.zaxxer.hikari.HikariDataSource;

/**
 * Hikari data source killer.
 */
public final class HikariDataSourceKiller implements DataSourceKiller {
    
    @Override
    public String getType() {
        return "com.zaxxer.hikari.HikariDataSource";
    }
    
    @Override
    public void closeDataSource(final DataSource dataSource) {
        HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            while (true) {
                if (hikariDataSource.getHikariPoolMXBean().getActiveConnections() == 0) {
                    hikariDataSource.close();
                    break;
                }
                try {
                    Thread.sleep(10);
                } catch (final InterruptedException ignore) {
                }
            }
        });
        executor.shutdown();
    }
}
