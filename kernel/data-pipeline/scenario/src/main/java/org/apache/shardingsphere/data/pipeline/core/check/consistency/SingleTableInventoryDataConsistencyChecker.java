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
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCalculateParameter;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCalculatedResult;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyContentCheckResult;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCountCheckResult;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.job.JobOperationType;
import org.apache.shardingsphere.data.pipeline.api.job.progress.listener.PipelineJobProgressUpdatedParameter;
import org.apache.shardingsphere.data.pipeline.api.metadata.SchemaTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineSQLException;
import org.apache.shardingsphere.data.pipeline.core.exception.data.PipelineTableDataConsistencyCheckLoadingFailedException;
import org.apache.shardingsphere.data.pipeline.spi.check.consistency.DataConsistencyCalculateAlgorithm;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.infra.executor.kernel.thread.ExecutorThreadFactoryBuilder;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.wrapper.SQLWrapperException;

import java.sql.SQLException;
import java.util.Collection;
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
        String sourceDatabaseType = sourceDataSource.getDatabaseType().getType();
        String targetDatabaseType = targetDataSource.getDatabaseType().getType();
        String schemaName = sourceTable.getSchemaName().getOriginal();
        String sourceTableName = sourceTable.getTableName().getOriginal();
        PipelineTableMetaData tableMetaData = metaDataLoader.getTableMetaData(schemaName, sourceTableName);
        ShardingSpherePreconditions.checkNotNull(tableMetaData, () -> new PipelineTableDataConsistencyCheckLoadingFailedException(schemaName, sourceTableName));
        Collection<String> columnNames = tableMetaData.getColumnNames();
        Map<String, Object> tableCheckPositions = progressContext.getTableCheckPositions();
        DataConsistencyCalculateParameter sourceParam = buildParameter(
                sourceDataSource, schemaName, sourceTableName, columnNames, sourceDatabaseType, targetDatabaseType, uniqueKey, tableCheckPositions.get(sourceTableName));
        String targetTableName = targetTable.getTableName().getOriginal();
        DataConsistencyCalculateParameter targetParam = buildParameter(targetDataSource, targetTable.getSchemaName().getOriginal(), targetTableName,
                columnNames, targetDatabaseType, sourceDatabaseType, uniqueKey, tableCheckPositions.get(targetTableName));
        Iterator<DataConsistencyCalculatedResult> sourceCalculatedResults = calculateAlgorithm.calculate(sourceParam).iterator();
        Iterator<DataConsistencyCalculatedResult> targetCalculatedResults = calculateAlgorithm.calculate(targetParam).iterator();
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
            if (sourceCalculatedResult.getMaxUniqueKeyValue().isPresent()) {
                tableCheckPositions.put(sourceTableName, sourceCalculatedResult.getMaxUniqueKeyValue().get());
            }
            if (targetCalculatedResult.getMaxUniqueKeyValue().isPresent()) {
                tableCheckPositions.put(targetTableName, targetCalculatedResult.getMaxUniqueKeyValue().get());
            }
            progressContext.onProgressUpdated(new PipelineJobProgressUpdatedParameter(sourceCalculatedResult.getRecordsCount()));
        }
        return new DataConsistencyCheckResult(new DataConsistencyCountCheckResult(sourceRecordsCount, targetRecordsCount), new DataConsistencyContentCheckResult(contentMatched));
    }
    
    // TODO use digest (crc32, murmurhash)
    private String getJobIdDigest(final String jobId) {
        return jobId.length() <= 6 ? jobId : jobId.substring(0, 6);
    }
    
    private DataConsistencyCalculateParameter buildParameter(final PipelineDataSourceWrapper sourceDataSource,
                                                             final String schemaName, final String tableName, final Collection<String> columnNames,
                                                             final String sourceDatabaseType, final String targetDatabaseType, final PipelineColumnMetaData uniqueKey,
                                                             final Object tableCheckPositionValue) {
        return new DataConsistencyCalculateParameter(sourceDataSource, schemaName, tableName, columnNames, sourceDatabaseType, targetDatabaseType, uniqueKey, tableCheckPositionValue);
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
