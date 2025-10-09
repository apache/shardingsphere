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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.position.TableCheckRangePosition;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.SingleTableInventoryCalculatedResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.TableDataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.yaml.YamlTableDataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.yaml.YamlTableDataConsistencyCheckResultSwapper;
import org.apache.shardingsphere.data.pipeline.core.constant.PipelineSQLOperationType;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.QueryRange;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.QueryType;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.calculator.SingleTableInventoryCalculateParameter;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.calculator.SingleTableInventoryCalculator;
import org.apache.shardingsphere.data.pipeline.core.job.progress.listener.PipelineJobUpdateProgress;
import org.apache.shardingsphere.data.pipeline.core.task.PipelineTaskUtils;
import org.apache.shardingsphere.infra.executor.kernel.thread.ExecutorThreadFactoryBuilder;
import org.apache.shardingsphere.infra.util.close.QuietlyCloser;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Matching table inventory checker.
 */
@RequiredArgsConstructor
@Slf4j
public abstract class MatchingTableInventoryChecker implements TableInventoryChecker {
    
    @Getter(AccessLevel.PROTECTED)
    private final TableInventoryCheckParameter param;
    
    private final AtomicBoolean canceling = new AtomicBoolean(false);
    
    private volatile SingleTableInventoryCalculator<SingleTableInventoryCalculatedResult> sourceCalculator;
    
    private volatile SingleTableInventoryCalculator<SingleTableInventoryCalculatedResult> targetCalculator;
    
    @Override
    public TableDataConsistencyCheckResult checkSingleTableInventoryData() {
        ThreadFactory threadFactory = ExecutorThreadFactoryBuilder.build(param.getJobId() + "-matching-check-%d");
        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 2, 60L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2), threadFactory);
        try {
            return checkSingleTableInventoryData(param, executor);
        } finally {
            executor.shutdown();
            executor.shutdownNow();
        }
    }
    
    private TableDataConsistencyCheckResult checkSingleTableInventoryData(final TableInventoryCheckParameter param, final ThreadPoolExecutor executor) {
        SingleTableInventoryCalculateParameter sourceParam = new SingleTableInventoryCalculateParameter(param.getSourceDataSource(), param.getSourceTable(),
                param.getColumnNames(), param.getUniqueKeys(), QueryType.RANGE_QUERY, param.getQueryCondition());
        TableCheckRangePosition checkRangePosition = param.getProgressContext().getTableCheckRangePositions().get(param.getSplittingItem());
        sourceParam.setQueryRange(new QueryRange(null != checkRangePosition.getSourcePosition() ? checkRangePosition.getSourcePosition() : checkRangePosition.getSourceRange().getBeginValue(),
                true, checkRangePosition.getSourceRange().getEndValue()));
        SingleTableInventoryCalculateParameter targetParam = new SingleTableInventoryCalculateParameter(param.getTargetDataSource(), param.getTargetTable(),
                param.getColumnNames(), param.getUniqueKeys(), QueryType.RANGE_QUERY, param.getQueryCondition());
        targetParam.setQueryRange(new QueryRange(null != checkRangePosition.getTargetPosition() ? checkRangePosition.getTargetPosition() : checkRangePosition.getTargetRange().getBeginValue(),
                true, checkRangePosition.getTargetRange().getEndValue()));
        SingleTableInventoryCalculator<SingleTableInventoryCalculatedResult> sourceCalculator = buildSingleTableInventoryCalculator();
        this.sourceCalculator = sourceCalculator;
        SingleTableInventoryCalculator<SingleTableInventoryCalculatedResult> targetCalculator = buildSingleTableInventoryCalculator();
        this.targetCalculator = targetCalculator;
        try {
            Iterator<SingleTableInventoryCalculatedResult> sourceCalculatedResults = PipelineTaskUtils.waitFuture(executor.submit(() -> sourceCalculator.calculate(sourceParam))).iterator();
            Iterator<SingleTableInventoryCalculatedResult> targetCalculatedResults = PipelineTaskUtils.waitFuture(executor.submit(() -> targetCalculator.calculate(targetParam))).iterator();
            return checkSingleTableInventoryData(sourceCalculatedResults, targetCalculatedResults, param, executor);
        } finally {
            QuietlyCloser.close(sourceParam.getCalculationContext());
            QuietlyCloser.close(targetParam.getCalculationContext());
            this.sourceCalculator = null;
            this.targetCalculator = null;
        }
    }
    
    private TableDataConsistencyCheckResult checkSingleTableInventoryData(final Iterator<SingleTableInventoryCalculatedResult> sourceCalculatedResults,
                                                                          final Iterator<SingleTableInventoryCalculatedResult> targetCalculatedResults,
                                                                          final TableInventoryCheckParameter param, final ThreadPoolExecutor executor) {
        YamlTableDataConsistencyCheckResult checkResult = new YamlTableDataConsistencyCheckResult(true);
        while (sourceCalculatedResults.hasNext() && targetCalculatedResults.hasNext()) {
            if (null != param.getReadRateLimitAlgorithm()) {
                param.getReadRateLimitAlgorithm().intercept(PipelineSQLOperationType.SELECT, 1);
            }
            SingleTableInventoryCalculatedResult sourceCalculatedResult = PipelineTaskUtils.waitFuture(executor.submit(sourceCalculatedResults::next));
            SingleTableInventoryCalculatedResult targetCalculatedResult = PipelineTaskUtils.waitFuture(executor.submit(targetCalculatedResults::next));
            if (!Objects.equals(sourceCalculatedResult, targetCalculatedResult)) {
                checkResult.setMatched(false);
                log.info("content matched false, jobId={}, sourceTable={}, targetTable={}, uniqueKeys={}", param.getJobId(), param.getSourceTable(), param.getTargetTable(), param.getUniqueKeys());
                break;
            }
            TableCheckRangePosition checkRangePosition = param.getProgressContext().getTableCheckRangePositions().get(param.getSplittingItem());
            if (sourceCalculatedResult.getMaxUniqueKeyValue().isPresent()) {
                checkRangePosition.setSourcePosition(sourceCalculatedResult.getMaxUniqueKeyValue().get());
            }
            if (targetCalculatedResult.getMaxUniqueKeyValue().isPresent()) {
                checkRangePosition.setTargetPosition(targetCalculatedResult.getMaxUniqueKeyValue().get());
            }
            param.getProgressContext().onProgressUpdated(new PipelineJobUpdateProgress(sourceCalculatedResult.getRecordsCount()));
        }
        TableCheckRangePosition checkRangePosition = param.getProgressContext().getTableCheckRangePositions().get(param.getSplittingItem());
        checkRangePosition.setFinished(true);
        if (sourceCalculatedResults.hasNext() || targetCalculatedResults.hasNext()) {
            checkResult.setMatched(false);
        }
        checkRangePosition.setMatched(checkResult.isMatched());
        return new YamlTableDataConsistencyCheckResultSwapper().swapToObject(checkResult);
    }
    
    protected abstract SingleTableInventoryCalculator<SingleTableInventoryCalculatedResult> buildSingleTableInventoryCalculator();
    
    @Override
    public void cancel() {
        canceling.set(true);
        Optional.ofNullable(sourceCalculator).ifPresent(SingleTableInventoryCalculator::cancel);
        Optional.ofNullable(targetCalculator).ifPresent(SingleTableInventoryCalculator::cancel);
    }
    
    @Override
    public boolean isCanceling() {
        return canceling.get();
    }
}
