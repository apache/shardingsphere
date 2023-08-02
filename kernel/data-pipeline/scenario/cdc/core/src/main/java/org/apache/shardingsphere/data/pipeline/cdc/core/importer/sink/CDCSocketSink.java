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

package org.apache.shardingsphere.data.pipeline.cdc.core.importer.sink;

import io.netty.channel.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.cdc.generator.CDCResponseUtils;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse.ResponseCase;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult;
import org.apache.shardingsphere.data.pipeline.cdc.util.DataRecordResultConvertUtils;
import org.apache.shardingsphere.data.pipeline.common.job.progress.listener.PipelineJobProgressUpdatedParameter;
import org.apache.shardingsphere.data.pipeline.core.importer.sink.PipelineSink;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * CDC socket sink.
 */
@Slf4j
public final class CDCSocketSink implements PipelineSink {
    
    private static final long DEFAULT_TIMEOUT_MILLISECONDS = 200L;
    
    private final Lock lock = new ReentrantLock();
    
    private final Condition condition = lock.newCondition();
    
    private final ShardingSphereDatabase database;
    
    private final Channel channel;
    
    private final Map<String, String> tableNameSchemaMap = new HashMap<>();
    
    public CDCSocketSink(final Channel channel, final ShardingSphereDatabase database, final Collection<String> schemaTableNames) {
        this.channel = channel;
        this.database = database;
        schemaTableNames.stream().filter(each -> each.contains(".")).forEach(each -> {
            String[] split = each.split("\\.");
            tableNameSchemaMap.put(split[1], split[0]);
        });
    }
    
    @Override
    public boolean identifierMatched(final Object identifier) {
        return channel.id().equals(identifier);
    }
    
    @Override
    public PipelineJobProgressUpdatedParameter write(final String ackId, final List<Record> records) {
        if (records.isEmpty()) {
            return new PipelineJobProgressUpdatedParameter(0);
        }
        while (!channel.isWritable() && channel.isActive()) {
            doAwait();
        }
        if (!channel.isActive()) {
            return new PipelineJobProgressUpdatedParameter(0);
        }
        List<DataRecordResult.Record> resultRecords = new LinkedList<>();
        for (Record each : records) {
            if (!(each instanceof DataRecord)) {
                continue;
            }
            DataRecord dataRecord = (DataRecord) each;
            resultRecords.add(DataRecordResultConvertUtils.convertDataRecordToRecord(database.getName(), tableNameSchemaMap.get(dataRecord.getTableName()), dataRecord));
        }
        DataRecordResult dataRecordResult = DataRecordResult.newBuilder().addAllRecord(resultRecords).setAckId(ackId).build();
        channel.writeAndFlush(CDCResponseUtils.succeed("", ResponseCase.DATA_RECORD_RESULT, dataRecordResult));
        return new PipelineJobProgressUpdatedParameter(resultRecords.size());
    }
    
    @SneakyThrows(InterruptedException.class)
    private void doAwait() {
        lock.lock();
        long startMillis = System.currentTimeMillis();
        long endMillis = startMillis;
        boolean awaitResult;
        try {
            do {
                awaitResult = condition.await(DEFAULT_TIMEOUT_MILLISECONDS - (endMillis - startMillis), TimeUnit.MILLISECONDS);
                endMillis = System.currentTimeMillis();
            } while (!awaitResult && DEFAULT_TIMEOUT_MILLISECONDS > endMillis - startMillis);
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public void close() throws IOException {
    }
}
