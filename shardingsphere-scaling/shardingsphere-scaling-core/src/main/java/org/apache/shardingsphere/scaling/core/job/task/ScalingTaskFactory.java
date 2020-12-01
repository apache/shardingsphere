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

package org.apache.shardingsphere.scaling.core.job.task;

import org.apache.shardingsphere.scaling.core.config.DumperConfiguration;
import org.apache.shardingsphere.scaling.core.config.ImporterConfiguration;
import org.apache.shardingsphere.scaling.core.config.InventoryDumperConfiguration;
import org.apache.shardingsphere.scaling.core.job.task.incremental.IncrementalTask;
import org.apache.shardingsphere.scaling.core.job.task.inventory.InventoryTask;

/**
 * Sync task factory.
 */
public interface ScalingTaskFactory {
    
    /**
     * Create inventory data sync task.
     *
     * @param inventoryDumperConfig inventory dumper configuration
     * @param importerConfig importer configuration
     * @return inventory data sync task
     */
    InventoryTask createInventoryTask(InventoryDumperConfiguration inventoryDumperConfig, ImporterConfiguration importerConfig);
    
    /**
     * Create incremental data sync task.
     *
     * @param concurrency concurrency
     * @param dumperConfig dumper configuration
     * @param importerConfig importer configuration
     * @return incremental data sync task
     */
    IncrementalTask createIncrementalTask(int concurrency, DumperConfiguration dumperConfig, ImporterConfiguration importerConfig);
}
