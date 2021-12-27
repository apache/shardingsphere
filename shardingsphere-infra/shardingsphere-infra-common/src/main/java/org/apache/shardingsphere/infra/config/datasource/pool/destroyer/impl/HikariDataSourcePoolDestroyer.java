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

package org.apache.shardingsphere.infra.config.datasource.pool.destroyer.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.infra.config.datasource.pool.destroyer.DataSourcePoolDestroyer;

import javax.sql.DataSource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Hikari data source pool destroyer.
 */
public final class HikariDataSourcePoolDestroyer implements DataSourcePoolDestroyer {
    
    @Override
    public void destroy(final DataSource dataSource) {
        HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            while (true) {
                if (0 == hikariDataSource.getHikariPoolMXBean().getActiveConnections()) {
                    hikariDataSource.close();
                    break;
                }
                try {
                    Thread.sleep(10L);
                } catch (final InterruptedException ignore) {
                }
            }
        });
        executor.shutdown();
    }
    
    @Override
    public String getType() {
        return HikariDataSource.class.getCanonicalName();
    }
}
