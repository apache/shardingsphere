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
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.SingleTableInventoryCalculatedResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.TableDataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.TableDataConsistencyContentCheckResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.TableDataConsistencyCountCheckResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.calculator.SingleTableInventoryCalculateParameter;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.calculator.SingleTableInventoryCalculator;
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
    public TableDataConsistencyCheckResult checkSingleTableInventoryData(final TableDataConsistencyCheckParameter param) {
        ThreadFactory threadFactory = ExecutorThreadFactoryBuilder.build("job-" + getJobIdDigest(param.getJobId()) + "-check-%d");
        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 2, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2), threadFactory);
        try {
            return checkSingleTableInventoryData(param, executor);
        } finally {
            executor.shutdown();
            executor.shutdownNow();
        }
    }
    
    private TableDataConsistencyCheckResult checkSingleTableInventoryData(final TableDataConsistencyCheckParameter param, final ThreadPoolExecutor executor) {
        Map<String, Object> tableCheckPositions = param.getProgressContext().getTableCheckPositions();
        SingleTableInventoryCalculateParameter sourceParam = new SingleTableInventoryCalculateParameter(param.getSourceDataSource(), param.getSourceTable(),
                param.getColumnNames(), param.getUniqueKeys(), tableCheckPositions.get(param.getSourceTable().getTableName().getOriginal()));
        SingleTableInventoryCalculateParameter targetParam = new SingleTableInventoryCalculateParameter(param.getTargetDataSource(), param.getTargetTable(),
                param.getColumnNames(), param.getUniqueKeys(), tableCheckPositions.get(param.getTargetTable().getTableName().getOriginal()));
        SingleTableInventoryCalculator calculator = getSingleTableInventoryCalculator();
        Iterator<SingleTableInventoryCalculatedResult> sourceCalculatedResults = waitFuture(executor.submit(() -> calculator.calculate(sourceParam))).iterator();
        Iterator<SingleTableInventoryCalculatedResult> targetCalculatedResults = waitFuture(executor.submit(() -> calculator.calculate(targetParam))).iterator();
        try {
            return checkSingleTableInventoryData(sourceCalculatedResults, targetCalculatedResults, param, executor);
        } finally {
            QuietlyCloser.close(sourceParam.getCalculationContext());
            QuietlyCloser.close(targetParam.getCalculationContext());
        }
    }
    
    private TableDataConsistencyCheckResult checkSingleTableInventoryData(final Iterator<SingleTableInventoryCalculatedResult> sourceCalculatedResults,
                                                                          final Iterator<SingleTableInventoryCalculatedResult> targetCalculatedResults,
                                                                          final TableDataConsistencyCheckParameter param, final ThreadPoolExecutor executor) {
        long sourceRecordsCount = 0;
        long targetRecordsCount = 0;
        boolean contentMatched = true;
        while (sourceCalculatedResults.hasNext() && targetCalculatedResults.hasNext()) {
            if (null != param.getReadRateLimitAlgorithm()) {
                param.getReadRateLimitAlgorithm().intercept(JobOperationType.SELECT, 1);
            }
            SingleTableInventoryCalculatedResult sourceCalculatedResult = waitFuture(executor.submit(sourceCalculatedResults::next));
            SingleTableInventoryCalculatedResult targetCalculatedResult = waitFuture(executor.submit(targetCalculatedResults::next));
            sourceRecordsCount += sourceCalculatedResult.getRecordsCount();
            targetRecordsCount += targetCalculatedResult.getRecordsCount();
            contentMatched = Objects.equals(sourceCalculatedResult, targetCalculatedResult);
            if (!contentMatched) {
                log.info("content matched false, jobId={}, sourceTable={}, targetTable={}, uniqueKeys={}", param.getJobId(), param.getSourceTable(), param.getTargetTable(), param.getUniqueKeys());
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
            // TODO Refactor SingleTableInventoryCalculatedResult to represent inaccurate number
            return new TableDataConsistencyCheckResult(new TableDataConsistencyCountCheckResult(sourceRecordsCount + 1, targetRecordsCount), new TableDataConsistencyContentCheckResult(false));
        }
        if (targetCalculatedResults.hasNext()) {
            return new TableDataConsistencyCheckResult(new TableDataConsistencyCountCheckResult(sourceRecordsCount, targetRecordsCount + 1), new TableDataConsistencyContentCheckResult(false));
        }
        return new TableDataConsistencyCheckResult(new TableDataConsistencyCountCheckResult(sourceRecordsCount, targetRecordsCount), new TableDataConsistencyContentCheckResult(contentMatched));
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
    
    protected abstract SingleTableInventoryCalculator getSingleTableInventoryCalculator();
    
    @Override
    public void cancel() {
        getSingleTableInventoryCalculator().cancel();
    }
    
    @Override
    public boolean isCanceling() {
        return getSingleTableInventoryCalculator().isCanceling();
    }
}
