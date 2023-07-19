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

package org.apache.shardingsphere.data.pipeline.core.consistencycheck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.job.JobOperationType;
import org.apache.shardingsphere.data.pipeline.api.metadata.SchemaTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.common.job.progress.listener.PipelineJobProgressUpdatedParameter;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.algorithm.DataConsistencyCalculateAlgorithm;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.DataConsistencyCalculatedResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.DataConsistencyContentCheckResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.DataConsistencyCountCheckResult;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineSQLException;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.infra.executor.kernel.thread.ExecutorThreadFactoryBuilder;
import org.apache.shardingsphere.infra.util.close.QuietlyCloser;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.wrapper.SQLWrapperException;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Single table inventory data consistency checker.
 */
@Slf4j
@RequiredArgsConstructor
public final class SingleTableInventoryDataConsistencyChecker {
    
    private final String jobId;
    
    private final PipelineDataSourceWrapper sourceDataSource;
    
    private final PipelineDataSourceWrapper targetDataSource;
    
    private final SchemaTableName sourceTable;
    
    private final SchemaTableName targetTable;
    
    private final List<String> columnNames;
    
    private final PipelineColumnMetaData uniqueKey;
    
    private final JobRateLimitAlgorithm readRateLimitAlgorithm;
    
    private final ConsistencyCheckJobItemProgressContext progressContext;
    
    /**
     * Data consistency check.
     *
     * @param calculateAlgorithm calculate algorithm
     * @return data consistency check result
     */
    public DataConsistencyCheckResult check(final DataConsistencyCalculateAlgorithm calculateAlgorithm) {
        ThreadFactory threadFactory = ExecutorThreadFactoryBuilder.build("job-" + getJobIdDigest(jobId) + "-check-%d");
        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 2, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2), threadFactory);
        try {
            return check(calculateAlgorithm, executor);
        } finally {
            executor.shutdown();
            executor.shutdownNow();
        }
    }
    
    private DataConsistencyCheckResult check(final DataConsistencyCalculateAlgorithm calculateAlgorithm, final ThreadPoolExecutor executor) {
        String schemaName = sourceTable.getSchemaName().getOriginal();
        String sourceTableName = sourceTable.getTableName().getOriginal();
        Map<String, Object> tableCheckPositions = progressContext.getTableCheckPositions();
        DataConsistencyCalculateParameter sourceParam = new DataConsistencyCalculateParameter(sourceDataSource,
                schemaName, sourceTableName, columnNames, sourceDataSource.getDatabaseType(), uniqueKey, tableCheckPositions.get(sourceTableName));
        String targetTableName = targetTable.getTableName().getOriginal();
        DataConsistencyCalculateParameter targetParam = new DataConsistencyCalculateParameter(targetDataSource,
                targetTable.getSchemaName().getOriginal(), targetTableName, columnNames, targetDataSource.getDatabaseType(), uniqueKey, tableCheckPositions.get(targetTableName));
        Iterator<DataConsistencyCalculatedResult> sourceCalculatedResults = waitFuture(executor.submit(() -> calculateAlgorithm.calculate(sourceParam))).iterator();
        Iterator<DataConsistencyCalculatedResult> targetCalculatedResults = waitFuture(executor.submit(() -> calculateAlgorithm.calculate(targetParam))).iterator();
        try {
            return check(sourceCalculatedResults, targetCalculatedResults, executor);
        } finally {
            QuietlyCloser.close(sourceParam.getCalculationContext());
            QuietlyCloser.close(targetParam.getCalculationContext());
        }
    }
    
    private DataConsistencyCheckResult check(final Iterator<DataConsistencyCalculatedResult> sourceCalculatedResults,
                                             final Iterator<DataConsistencyCalculatedResult> targetCalculatedResults, final ThreadPoolExecutor executor) {
        long sourceRecordsCount = 0;
        long targetRecordsCount = 0;
        boolean contentMatched = true;
        while (sourceCalculatedResults.hasNext() && targetCalculatedResults.hasNext()) {
            if (null != readRateLimitAlgorithm) {
                readRateLimitAlgorithm.intercept(JobOperationType.SELECT, 1);
            }
            DataConsistencyCalculatedResult sourceCalculatedResult = waitFuture(executor.submit(sourceCalculatedResults::next));
            DataConsistencyCalculatedResult targetCalculatedResult = waitFuture(executor.submit(targetCalculatedResults::next));
            sourceRecordsCount += sourceCalculatedResult.getRecordsCount();
            targetRecordsCount += targetCalculatedResult.getRecordsCount();
            contentMatched = Objects.equals(sourceCalculatedResult, targetCalculatedResult);
            if (!contentMatched) {
                log.info("content matched false, jobId={}, sourceTable={}, targetTable={}, uniqueKey={}", jobId, sourceTable, targetTable, uniqueKey);
                break;
            }
            if (sourceCalculatedResult.getMaxUniqueKeyValue().isPresent()) {
                progressContext.getTableCheckPositions().put(sourceTable.getTableName().getOriginal(), sourceCalculatedResult.getMaxUniqueKeyValue().get());
            }
            if (targetCalculatedResult.getMaxUniqueKeyValue().isPresent()) {
                progressContext.getTableCheckPositions().put(targetTable.getTableName().getOriginal(), targetCalculatedResult.getMaxUniqueKeyValue().get());
            }
            progressContext.onProgressUpdated(new PipelineJobProgressUpdatedParameter(sourceCalculatedResult.getRecordsCount()));
        }
        if (sourceCalculatedResults.hasNext()) {
            // TODO Refactor DataConsistencyCalculatedResult to represent inaccurate number
            return new DataConsistencyCheckResult(new DataConsistencyCountCheckResult(sourceRecordsCount + 1, targetRecordsCount), new DataConsistencyContentCheckResult(false));
        }
        if (targetCalculatedResults.hasNext()) {
            return new DataConsistencyCheckResult(new DataConsistencyCountCheckResult(sourceRecordsCount, targetRecordsCount + 1), new DataConsistencyContentCheckResult(false));
        }
        return new DataConsistencyCheckResult(new DataConsistencyCountCheckResult(sourceRecordsCount, targetRecordsCount), new DataConsistencyContentCheckResult(contentMatched));
    }
    
    // TODO use digest (crc32, murmurhash)
    private String getJobIdDigest(final String jobId) {
        return jobId.length() <= 6 ? jobId : jobId.substring(0, 6);
    }
    
    private <T> T waitFuture(final Future<T> future) {
        try {
            return future.get();
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new SQLWrapperException(new SQLException(ex));
        } catch (final ExecutionException ex) {
            if (ex.getCause() instanceof PipelineSQLException) {
                throw (PipelineSQLException) ex.getCause();
            }
            throw new SQLWrapperException(new SQLException(ex));
        }
    }
}
