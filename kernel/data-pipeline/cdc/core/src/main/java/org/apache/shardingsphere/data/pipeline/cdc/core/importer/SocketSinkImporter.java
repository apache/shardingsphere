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

package org.apache.shardingsphere.data.pipeline.cdc.core.importer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.api.executor.AbstractLifecycleExecutor;
import org.apache.shardingsphere.data.pipeline.api.importer.Importer;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.FinishedRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.PlaceholderRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.api.job.JobOperationType;
import org.apache.shardingsphere.data.pipeline.api.job.progress.listener.PipelineJobProgressListener;
import org.apache.shardingsphere.data.pipeline.api.job.progress.listener.PipelineJobProgressUpdatedParameter;
import org.apache.shardingsphere.data.pipeline.cdc.core.ack.CDCAckHolder;
import org.apache.shardingsphere.data.pipeline.cdc.core.ack.CDCAckPosition;
import org.apache.shardingsphere.data.pipeline.cdc.core.connector.SocketSinkImporterConnector;
import org.apache.shardingsphere.data.pipeline.spi.importer.ImporterType;
import org.apache.shardingsphere.data.pipeline.spi.importer.connector.ImporterConnector;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Socket sink importer.
 */
@Slf4j
public final class SocketSinkImporter extends AbstractLifecycleExecutor implements Importer {
    
    @Getter(AccessLevel.PROTECTED)
    private final ImporterConfiguration importerConfig;
    
    private final PipelineChannel channel;
    
    private final SocketSinkImporterConnector importerConnector;
    
    private final PipelineJobProgressListener jobProgressListener;
    
    @Getter
    private final ImporterType importerType;
    
    private final JobRateLimitAlgorithm rateLimitAlgorithm;
    
    public SocketSinkImporter(final ImporterConfiguration importerConfig, final ImporterConnector importerConnector, final PipelineChannel channel,
                              final PipelineJobProgressListener jobProgressListener, final ImporterType importerType) {
        this.importerConfig = importerConfig;
        rateLimitAlgorithm = null == importerConfig ? null : importerConfig.getRateLimitAlgorithm();
        this.channel = channel;
        this.importerConnector = (SocketSinkImporterConnector) importerConnector;
        this.jobProgressListener = jobProgressListener;
        this.importerType = importerType;
    }
    
    @Override
    protected void runBlocking() {
        int batchSize = importerConfig.getBatchSize();
        if (ImporterType.INCREMENTAL == importerType) {
            importerConnector.sendIncrementalStartEvent(this, batchSize);
        }
        while (isRunning()) {
            List<Record> records = channel.fetchRecords(batchSize, 3);
            if (null != records && !records.isEmpty()) {
                List<Record> recordList = records.stream().filter(each -> !(each instanceof PlaceholderRecord)).collect(Collectors.toList());
                try {
                    processDataRecords(recordList);
                } catch (final SQLException ex) {
                    log.error("process data records failed", ex);
                    throw new RuntimeException(ex);
                }
                if (FinishedRecord.class.equals(records.get(records.size() - 1).getClass())) {
                    break;
                }
            }
        }
    }
    
    private void processDataRecords(final List<Record> recordList) throws SQLException {
        if (null == recordList || recordList.isEmpty()) {
            return;
        }
        if (null != rateLimitAlgorithm) {
            rateLimitAlgorithm.intercept(JobOperationType.INSERT, 1);
        }
        importerConnector.write(recordList, this, importerType);
    }
    
    /**
     * Ack with last data record.
     *
     * @param cdcAckPosition cdc ack position
     */
    public void ackWithLastDataRecord(final CDCAckPosition cdcAckPosition) {
        channel.ack(Collections.singletonList(cdcAckPosition.getLastRecord()));
        jobProgressListener.onProgressUpdated(new PipelineJobProgressUpdatedParameter(cdcAckPosition.getDataRecordCount()));
    }
    
    @Override
    protected void doStop() {
        if (ImporterType.INCREMENTAL == importerType) {
            importerConnector.clean(this);
            CDCAckHolder.getInstance().cleanUp(this);
        }
    }
}
