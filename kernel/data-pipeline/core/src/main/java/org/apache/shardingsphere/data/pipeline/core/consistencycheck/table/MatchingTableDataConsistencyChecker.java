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

package org.apache.shardingsphere.data.pipeline.core.consistencycheck.table;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.job.JobOperationType;
import org.apache.shardingsphere.data.pipeline.common.job.progress.listener.PipelineJobProgressUpdatedParameter;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.DataConsistencyCalculateParameter;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.algorithm.DataConsistencyCalculateAlgorithm;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.DataConsistencyCalculatedResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.DataConsistencyContentCheckResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.DataConsistencyCountCheckResult;
import org.apache.shardingsphere.infra.exception.core.external.sql.type.kernel.category.PipelineSQLException;
import org.apache.shardingsphere.infra.exception.core.external.sql.type.wrapper.SQLWrapperException;
import org.apache.shardingsphere.infra.executor.kernel.thread.ExecutorThreadFactoryBuilder;
import org.apache.shardingsphere.infra.util.close.QuietlyCloser;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Matching table data consistency checker.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class MatchingTableDataConsistencyChecker implements TableDataConsistencyChecker {
    
    @Override
    public DataConsistencyCheckResult checkSingleTableInventoryData(final TableDataConsistencyCheckParameter param) {
        ThreadFactory threadFactory = ExecutorThreadFactoryBuilder.build("job-" + getJobIdDigest(param.getJobId()) + "-check-%d");
        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 2, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2), threadFactory);
        try {
            return checkSingleTableInventoryData(param, executor);
        } finally {
            executor.shutdown();
            executor.shutdownNow();
        }
    }
    
    private DataConsistencyCheckResult checkSingleTableInventoryData(final TableDataConsistencyCheckParameter param, final ThreadPoolExecutor executor) {
        Map<String, Object> tableCheckPositions = param.getProgressContext().getTableCheckPositions();
        DataConsistencyCalculateParameter sourceParam = new DataConsistencyCalculateParameter(param.getSourceDataSource(), param.getSourceTable(),
                param.getColumnNames(), param.getUniqueKey(), tableCheckPositions.get(param.getSourceTable().getTableName().getOriginal()));
        DataConsistencyCalculateParameter targetParam = new DataConsistencyCalculateParameter(param.getTargetDataSource(), param.getTargetTable(),
                param.getColumnNames(), param.getUniqueKey(), tableCheckPositions.get(param.getTargetTable().getTableName().getOriginal()));
        DataConsistencyCalculateAlgorithm calculateAlgorithm = getDataConsistencyCalculateAlgorithm();
        Iterator<DataConsistencyCalculatedResult> sourceCalculatedResults = waitFuture(executor.submit(() -> calculateAlgorithm.calculate(sourceParam))).iterator();
        Iterator<DataConsistencyCalculatedResult> targetCalculatedResults = waitFuture(executor.submit(() -> calculateAlgorithm.calculate(targetParam))).iterator();
        try {
            return checkSingleTableInventoryData(sourceCalculatedResults, targetCalculatedResults, param, executor);
        } finally {
            QuietlyCloser.close(sourceParam.getCalculationContext());
            QuietlyCloser.close(targetParam.getCalculationContext());
        }
    }
    
    private DataConsistencyCheckResult checkSingleTableInventoryData(final Iterator<DataConsistencyCalculatedResult> sourceCalculatedResults,
                                                                     final Iterator<DataConsistencyCalculatedResult> targetCalculatedResults,
                                                                     final TableDataConsistencyCheckParameter param, final ThreadPoolExecutor executor) {
        long sourceRecordsCount = 0;
        long targetRecordsCount = 0;
        boolean contentMatched = true;
        while (sourceCalculatedResults.hasNext() && targetCalculatedResults.hasNext()) {
            if (null != param.getReadRateLimitAlgorithm()) {
                param.getReadRateLimitAlgorithm().intercept(JobOperationType.SELECT, 1);
            }
            DataConsistencyCalculatedResult sourceCalculatedResult = waitFuture(executor.submit(sourceCalculatedResults::next));
            DataConsistencyCalculatedResult targetCalculatedResult = waitFuture(executor.submit(targetCalculatedResults::next));
            sourceRecordsCount += sourceCalculatedResult.getRecordsCount();
            targetRecordsCount += targetCalculatedResult.getRecordsCount();
            contentMatched = Objects.equals(sourceCalculatedResult, targetCalculatedResult);
            if (!contentMatched) {
                log.info("content matched false, jobId={}, sourceTable={}, targetTable={}, uniqueKey={}", param.getJobId(), param.getSourceTable(), param.getTargetTable(), param.getUniqueKey());
                break;
            }
            if (sourceCalculatedResult.getMaxUniqueKeyValue().isPresent()) {
                param.getProgressContext().getTableCheckPositions().put(param.getSourceTable().getTableName().getOriginal(), sourceCalculatedResult.getMaxUniqueKeyValue().get());
            }
            if (targetCalculatedResult.getMaxUniqueKeyValue().isPresent()) {
                param.getProgressContext().getTableCheckPositions().put(param.getTargetTable().getTableName().getOriginal(), targetCalculatedResult.getMaxUniqueKeyValue().get());
            }
            param.getProgressContext().onProgressUpdated(new PipelineJobProgressUpdatedParameter(sourceCalculatedResult.getRecordsCount()));
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
    
    protected abstract DataConsistencyCalculateAlgorithm getDataConsistencyCalculateAlgorithm();
    
    @Override
    public void cancel() {
        getDataConsistencyCalculateAlgorithm().cancel();
    }
    
    @Override
    public boolean isCanceling() {
        return getDataConsistencyCalculateAlgorithm().isCanceling();
    }
}
