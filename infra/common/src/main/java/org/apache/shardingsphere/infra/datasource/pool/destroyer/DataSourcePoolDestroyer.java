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

package org.apache.shardingsphere.infra.datasource.pool.destroyer;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.datasource.pool.destroyer.detector.DataSourcePoolActiveDetector;
import org.apache.shardingsphere.infra.datasource.pool.destroyer.detector.DataSourcePoolActiveDetectorFactory;

import javax.sql.DataSource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Data source pool destroyer.
 */
@RequiredArgsConstructor
public final class DataSourcePoolDestroyer {
    
    private final DataSource dataSource;
    
    /**
     * Asynchronous destroy data source pool gracefully.
     */
    public void asyncDestroy() {
        if (!(dataSource instanceof AutoCloseable)) {
            return;
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(this::graceDestroy);
        executor.shutdown();
    }
    
    @SneakyThrows
    private void graceDestroy() {
        waitUntilActiveConnectionComplete();
        ((AutoCloseable) dataSource).close();
    }
    
    private void waitUntilActiveConnectionComplete() {
        DataSourcePoolActiveDetector dataSourcePoolActiveDetector = DataSourcePoolActiveDetectorFactory.getInstance(dataSource.getClass().getName());
        while (dataSourcePoolActiveDetector.containsActiveConnection(dataSource)) {
            try {
                Thread.sleep(10L);
            } catch (final InterruptedException ignore) {
            }
        }
    }
}
