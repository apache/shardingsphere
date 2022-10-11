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

package org.apache.shardingsphere.data.pipeline.core.check.consistency;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.InventoryIncrementalJobPublicAPI;
import org.apache.shardingsphere.data.pipeline.api.PipelineJobPublicAPIFactory;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCalculateParameter;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCalculatedResult;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyContentCheckResult;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCountCheckResult;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.job.JobOperationType;
import org.apache.shardingsphere.data.pipeline.api.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.api.job.progress.listener.PipelineJobProgressUpdatedParameter;
import org.apache.shardingsphere.data.pipeline.api.metadata.SchemaTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineSQLException;
import org.apache.shardingsphere.data.pipeline.core.exception.data.PipelineTableDataConsistencyCheckLoadingFailedException;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.ConsistencyCheckJobItemContext;
import org.apache.shardingsphere.data.pipeline.spi.check.consistency.DataConsistencyCalculateAlgorithm;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.infra.executor.kernel.thread.ExecutorThreadFactoryBuilder;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.wrapper.SQLWrapperException;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
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
    
    private final PipelineColumnMetaData uniqueKey;
    
    private final PipelineTableMetaDataLoader metaDataLoader;
    
    private final JobRateLimitAlgorithm readRateLimitAlgorithm;
    
    private final ConsistencyCheckJobItemContext consistencyCheckJobItemContext;
    
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
            return check(calculateAlgorithm, executor, consistencyCheckJobItemContext);
        } finally {
            executor.shutdown();
            executor.shutdownNow();
        }
    }
    
    private DataConsistencyCheckResult check(final DataConsistencyCalculateAlgorithm calculateAlgorithm, final ThreadPoolExecutor executor,
                                             final ConsistencyCheckJobItemContext consistencyCheckJobItemContext) {
        String sourceDatabaseType = sourceDataSource.getDatabaseType().getType();
        String targetDatabaseType = targetDataSource.getDatabaseType().getType();
        String sourceTableName = sourceTable.getTableName().getOriginal();
        String schemaName = sourceTable.getSchemaName().getOriginal();
        PipelineTableMetaData tableMetaData = metaDataLoader.getTableMetaData(schemaName, sourceTableName);
        ShardingSpherePreconditions.checkNotNull(tableMetaData, () -> new PipelineTableDataConsistencyCheckLoadingFailedException(sourceTableName));
        Collection<String> columnNames = tableMetaData.getColumnNames();
        DataConsistencyCalculateParameter sourceParameter = buildParameter(
                sourceDataSource, schemaName, sourceTableName, columnNames, sourceDatabaseType, targetDatabaseType, uniqueKey);
        DataConsistencyCalculateParameter targetParameter = buildParameter(
                targetDataSource, targetTable.getSchemaName().getOriginal(), targetTable.getTableName().getOriginal(), columnNames, targetDatabaseType, sourceDatabaseType, uniqueKey);
        Iterator<DataConsistencyCalculatedResult> sourceCalculatedResults = calculateAlgorithm.calculate(sourceParameter).iterator();
        Iterator<DataConsistencyCalculatedResult> targetCalculatedResults = calculateAlgorithm.calculate(targetParameter).iterator();
        if (null != consistencyCheckJobItemContext) {
            consistencyCheckJobItemContext.setTableNames(Collections.singletonList(sourceTableName));
            InventoryIncrementalJobPublicAPI inventoryIncrementalJobPublicAPI = PipelineJobPublicAPIFactory.getInventoryIncrementalJobPublicAPI(PipelineJobIdUtils.parseJobType(jobId).getTypeName());
            Map<Integer, InventoryIncrementalJobItemProgress> jobProgress = inventoryIncrementalJobPublicAPI.getJobProgress(jobId);
            long recordsCount = jobProgress.values().stream().filter(Objects::nonNull).mapToLong(InventoryIncrementalJobItemProgress::getProcessedRecordsCount).sum();
            consistencyCheckJobItemContext.setRecordsCount(recordsCount);
        }
        long sourceRecordsCount = 0;
        long targetRecordsCount = 0;
        boolean contentMatched = true;
        while (sourceCalculatedResults.hasNext() && targetCalculatedResults.hasNext()) {
            if (null != readRateLimitAlgorithm) {
                readRateLimitAlgorithm.intercept(JobOperationType.SELECT, 1);
            }
            Future<DataConsistencyCalculatedResult> sourceFuture = executor.submit(sourceCalculatedResults::next);
            Future<DataConsistencyCalculatedResult> targetFuture = executor.submit(targetCalculatedResults::next);
            DataConsistencyCalculatedResult sourceCalculatedResult = waitFuture(sourceFuture);
            DataConsistencyCalculatedResult targetCalculatedResult = waitFuture(targetFuture);
            sourceRecordsCount += sourceCalculatedResult.getRecordsCount();
            targetRecordsCount += targetCalculatedResult.getRecordsCount();
            contentMatched = Objects.equals(sourceCalculatedResult, targetCalculatedResult);
            if (!contentMatched) {
                log.info("content matched false, jobId={}, sourceTable={}, targetTable={}, uniqueKey={}", jobId, sourceTable, targetTable, uniqueKey);
                break;
            }
            if (null != consistencyCheckJobItemContext) {
                consistencyCheckJobItemContext.onProgressUpdated(new PipelineJobProgressUpdatedParameter(sourceCalculatedResult.getRecordsCount()));
            }
        }
        if (null != consistencyCheckJobItemContext) {
            consistencyCheckJobItemContext.setCheckEndTimeMillis(System.currentTimeMillis());
        }
        return new DataConsistencyCheckResult(new DataConsistencyCountCheckResult(sourceRecordsCount, targetRecordsCount), new DataConsistencyContentCheckResult(contentMatched));
    }
    
    // TODO use digest (crc32, murmurhash)
    private String getJobIdDigest(final String jobId) {
        return jobId.length() <= 6 ? jobId : jobId.substring(0, 6);
    }
    
    private DataConsistencyCalculateParameter buildParameter(final PipelineDataSourceWrapper sourceDataSource,
                                                             final String schemaName, final String tableName, final Collection<String> columnNames,
                                                             final String sourceDatabaseType, final String targetDatabaseType, final PipelineColumnMetaData uniqueKey) {
        return new DataConsistencyCalculateParameter(sourceDataSource, schemaName, tableName, columnNames, sourceDatabaseType, targetDatabaseType, uniqueKey);
    }
    
    private <T> T waitFuture(final Future<T> future) {
        try {
            return future.get();
        } catch (final InterruptedException | ExecutionException ex) {
            if (ex.getCause() instanceof PipelineSQLException) {
                throw (PipelineSQLException) ex.getCause();
            }
            throw new SQLWrapperException(new SQLException(ex));
        }
    }
}
