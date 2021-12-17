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

package org.apache.shardingsphere.data.pipeline.core.task;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.InventoryDumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.ImporterConfiguration;

/**
 * Pipeline task factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
// TODO remove?
public final class PipelineTaskFactory {
    
    /**
     * Create inventory task.
     *
     * @param inventoryDumperConfig inventory dumper configuration
     * @param importerConfig importer configuration
     * @return inventory task
     */
    public static InventoryTask createInventoryTask(final InventoryDumperConfiguration inventoryDumperConfig, final ImporterConfiguration importerConfig) {
        return new InventoryTask(inventoryDumperConfig, importerConfig);
    }
    
    /**
     * Create incremental task.
     *
     * @param concurrency concurrency
     * @param dumperConfig dumper configuration
     * @param importerConfig importer configuration
     * @return incremental task
     */
    public static IncrementalTask createIncrementalTask(final int concurrency, final DumperConfiguration dumperConfig, final ImporterConfiguration importerConfig) {
        return new IncrementalTask(concurrency, dumperConfig, importerConfig);
    }
}
