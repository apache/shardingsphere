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

package org.apache.shardingsphere.data.pipeline.core.preparer.inventory.splitter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.channel.InventoryChannelCreator;
import org.apache.shardingsphere.data.pipeline.core.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionJobItemContext;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionProcessContext;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSource;
import org.apache.shardingsphere.data.pipeline.core.importer.Importer;
import org.apache.shardingsphere.data.pipeline.core.importer.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.core.importer.SingleChannelConsumerImporter;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.Dumper;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.InventoryDumper;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.InventoryDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.position.InventoryDataRecordPositionCreator;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.position.type.PlaceholderInventoryDataRecordPositionCreator;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.position.type.UniqueKeyInventoryDataRecordPositionCreator;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryTask;
import org.apache.shardingsphere.data.pipeline.core.task.PipelineTaskUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Inventory task splitter.
 */
@RequiredArgsConstructor
@Slf4j
public final class InventoryTaskSplitter {
    
    private final PipelineDataSource dataSource;
    
    private final InventoryDumperContext dumperContext;
    
    private final ImporterConfiguration importerConfig;
    
    /**
     * Split inventory data to multi-tasks.
     *
     * @param jobItemContext job item context
     * @return split inventory data task
     */
    public List<InventoryTask> split(final TransmissionJobItemContext jobItemContext) {
        List<InventoryTask> result = new LinkedList<>();
        long startTimeMillis = System.currentTimeMillis();
        TransmissionProcessContext processContext = jobItemContext.getJobProcessContext();
        InventoryDumperContextSplitter dumperContextSplitter = new InventoryDumperContextSplitter(dataSource, dumperContext);
        for (InventoryDumperContext each : dumperContextSplitter.split(jobItemContext)) {
            AtomicReference<IngestPosition> position = new AtomicReference<>(each.getCommonContext().getPosition());
            PipelineChannel channel = InventoryChannelCreator.create(processContext.getProcessConfiguration().getStreamChannel(), importerConfig.getBatchSize(), position);
            InventoryDataRecordPositionCreator positionCreator = each.hasUniqueKey() ? new UniqueKeyInventoryDataRecordPositionCreator() : new PlaceholderInventoryDataRecordPositionCreator();
            Dumper dumper = new InventoryDumper(each, channel, dataSource, positionCreator);
            Importer importer = new SingleChannelConsumerImporter(channel, importerConfig.getBatchSize(), 3000L, jobItemContext.getSink(), jobItemContext);
            result.add(new InventoryTask(PipelineTaskUtils.generateInventoryTaskId(each),
                    processContext.getInventoryDumperExecuteEngine(), processContext.getInventoryImporterExecuteEngine(), dumper, importer, position));
        }
        log.info("Split inventory tasks cost {} ms", System.currentTimeMillis() - startTimeMillis);
        return result;
    }
}
