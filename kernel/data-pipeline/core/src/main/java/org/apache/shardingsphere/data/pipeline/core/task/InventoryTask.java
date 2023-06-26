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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.ingest.dumper.Dumper;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.common.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.common.task.progress.InventoryTaskProgress;
import org.apache.shardingsphere.data.pipeline.core.importer.Importer;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Inventory task.
 */
@RequiredArgsConstructor
@ToString(exclude = {"inventoryDumperExecuteEngine", "inventoryImporterExecuteEngine", "dumper", "importer"})
@Slf4j
public final class InventoryTask implements PipelineTask {
    
    @Getter
    private final String taskId;
    
    private final ExecuteEngine inventoryDumperExecuteEngine;
    
    private final ExecuteEngine inventoryImporterExecuteEngine;
    
    private final Dumper dumper;
    
    private final Importer importer;
    
    private final AtomicReference<IngestPosition> position;
    
    @Override
    public Collection<CompletableFuture<?>> start() {
        Collection<CompletableFuture<?>> result = new LinkedList<>();
        result.add(inventoryDumperExecuteEngine.submit(dumper, new TaskExecuteCallback(this)));
        result.add(inventoryImporterExecuteEngine.submit(importer, new TaskExecuteCallback(this)));
        return result;
    }
    
    @Override
    public void stop() {
        dumper.stop();
        importer.stop();
    }
    
    @Override
    public InventoryTaskProgress getTaskProgress() {
        return new InventoryTaskProgress(position.get());
    }
    
    @Override
    public void close() {
    }
}
