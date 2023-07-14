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

package org.apache.shardingsphere.data.pipeline.core.importer;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.data.pipeline.api.executor.AbstractLifecycleExecutor;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.FinishedRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.PlaceholderRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.common.job.progress.listener.PipelineJobProgressListener;
import org.apache.shardingsphere.data.pipeline.common.job.progress.listener.PipelineJobProgressUpdatedParameter;
import org.apache.shardingsphere.infra.util.close.QuietlyCloser;
import org.apache.shardingsphere.data.pipeline.core.importer.sink.PipelineSink;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Single channel consumer importer.
 */
@RequiredArgsConstructor
public final class SingleChannelConsumerImporter extends AbstractLifecycleExecutor implements Importer {
    
    private final PipelineChannel channel;
    
    private final int batchSize;
    
    private final int timeout;
    
    private final TimeUnit timeUnit;
    
    private final PipelineSink sink;
    
    private final PipelineJobProgressListener jobProgressListener;
    
    @Override
    protected void runBlocking() {
        while (isRunning()) {
            List<Record> records = channel.fetchRecords(batchSize, timeout, timeUnit).stream().filter(each -> !(each instanceof PlaceholderRecord)).collect(Collectors.toList());
            if (records.isEmpty()) {
                continue;
            }
            PipelineJobProgressUpdatedParameter updatedParam = sink.write("", records);
            channel.ack(records);
            jobProgressListener.onProgressUpdated(updatedParam);
            if (FinishedRecord.class.equals(records.get(records.size() - 1).getClass())) {
                break;
            }
        }
    }
    
    @Override
    protected void doStop() {
        QuietlyCloser.close(sink);
    }
}
