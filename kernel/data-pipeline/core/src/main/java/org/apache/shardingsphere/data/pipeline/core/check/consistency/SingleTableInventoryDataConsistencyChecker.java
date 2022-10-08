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
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyContentCheckResult;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCountCheckResult;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.job.JobOperationType;
import org.apache.shardingsphere.data.pipeline.api.metadata.SchemaTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineSQLException;
import org.apache.shardingsphere.data.pipeline.core.exception.data.PipelineTableDataConsistencyCheckLoadingFailedException;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.StandardPipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.PipelineSQLBuilderFactory;
import org.apache.shardingsphere.data.pipeline.spi.check.consistency.DataConsistencyCalculateAlgorithm;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.infra.executor.kernel.thread.ExecutorThreadFactoryBuilder;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.wrapper.SQLWrapperException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
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
    
    private final JobRateLimitAlgorithm readRateLimitAlgorithm;
    
    /**
     * Data consistency check.
     *
     * @param calculateAlgorithm calculate algorithm
     * @return data consistency check result
     */
    public DataConsistencyCheckResult check(final DataConsistencyCalculateAlgorithm calculateAlgorithm) {
        DataConsistencyCountCheckResult countCheckResult = checkCount();
        DataConsistencyContentCheckResult contentCheckResult = countCheckResult.isMatched() ? checkData(calculateAlgorithm) : new DataConsistencyContentCheckResult(false);
        return new DataConsistencyCheckResult(countCheckResult, contentCheckResult);
    }
    
    private DataConsistencyCountCheckResult checkCount() {
        ThreadFactory threadFactory = ExecutorThreadFactoryBuilder.build("job-" + getJobIdDigest(jobId) + "-count-check-%d");
        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 2, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2), threadFactory);
        try {
            Future<Long> sourceFuture = executor.submit(() -> count(sourceDataSource, sourceTable));
            Future<Long> targetFuture = executor.submit(() -> count(targetDataSource, targetTable));
            long sourceCount = waitFuture(sourceFuture);
            long targetCount = waitFuture(targetFuture);
            return new DataConsistencyCountCheckResult(sourceCount, targetCount);
        } finally {
            executor.shutdown();
            executor.shutdownNow();
        }
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
    
    // TODO use digest (crc32, murmurhash)
    private String getJobIdDigest(final String jobId) {
        return jobId.length() <= 6 ? jobId : jobId.substring(0, 6);
    }
    
    private long count(final PipelineDataSourceWrapper dataSource, final SchemaTableName schemaTableName) {
        String tableName = schemaTableName.getTableName().getOriginal();
        String sql = PipelineSQLBuilderFactory.getInstance(dataSource.getDatabaseType().getType()).buildCountSQL(schemaTableName.getSchemaName().getOriginal(), tableName);
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            return resultSet.getLong(1);
        } catch (final SQLException ex) {
            throw new PipelineTableDataConsistencyCheckLoadingFailedException(tableName);
        }
    }
    
    private DataConsistencyContentCheckResult checkData(final DataConsistencyCalculateAlgorithm calculateAlgorithm) {
        ThreadFactory threadFactory = ExecutorThreadFactoryBuilder.build("job-" + getJobIdDigest(jobId) + "-data-check-%d");
        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 2, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2), threadFactory);
        try {
            return checkData(calculateAlgorithm, executor);
        } finally {
            executor.shutdown();
            executor.shutdownNow();
        }
    }
    
    private DataConsistencyContentCheckResult checkData(final DataConsistencyCalculateAlgorithm calculateAlgorithm, final ThreadPoolExecutor executor) {
        String sourceDatabaseType = sourceDataSource.getDatabaseType().getType();
        String targetDatabaseType = targetDataSource.getDatabaseType().getType();
        PipelineTableMetaDataLoader metaDataLoader = new StandardPipelineTableMetaDataLoader(sourceDataSource);
        String sourceTableName = sourceTable.getTableName().getOriginal();
        PipelineTableMetaData tableMetaData = metaDataLoader.getTableMetaData(sourceTable.getSchemaName().getOriginal(), sourceTableName);
        ShardingSpherePreconditions.checkNotNull(tableMetaData, () -> new PipelineTableDataConsistencyCheckLoadingFailedException(sourceTableName));
        Collection<String> columnNames = tableMetaData.getColumnNames();
        DataConsistencyCalculateParameter sourceParameter = buildParameter(
                sourceDataSource, sourceTable.getSchemaName().getOriginal(), sourceTableName, columnNames, sourceDatabaseType, targetDatabaseType, uniqueKey);
        DataConsistencyCalculateParameter targetParameter = buildParameter(
                targetDataSource, targetTable.getSchemaName().getOriginal(), targetTable.getTableName().getOriginal(), columnNames, targetDatabaseType, sourceDatabaseType, uniqueKey);
        Iterator<Object> sourceCalculatedResults = calculateAlgorithm.calculate(sourceParameter).iterator();
        Iterator<Object> targetCalculatedResults = calculateAlgorithm.calculate(targetParameter).iterator();
        boolean contentMatched = true;
        while (sourceCalculatedResults.hasNext() && targetCalculatedResults.hasNext()) {
            if (null != readRateLimitAlgorithm) {
                readRateLimitAlgorithm.intercept(JobOperationType.SELECT, 1);
            }
            Future<Object> sourceFuture = executor.submit(sourceCalculatedResults::next);
            Future<Object> targetFuture = executor.submit(targetCalculatedResults::next);
            Object sourceCalculatedResult = waitFuture(sourceFuture);
            Object targetCalculatedResult = waitFuture(targetFuture);
            contentMatched = Objects.equals(sourceCalculatedResult, targetCalculatedResult);
            if (!contentMatched) {
                log.info("content matched false, jobId={}, sourceTable={}, targetTable={}, uniqueKey={}", jobId, sourceTable, targetTable, uniqueKey);
                break;
            }
        }
        return new DataConsistencyContentCheckResult(contentMatched);
    }
    
    private DataConsistencyCalculateParameter buildParameter(final PipelineDataSourceWrapper sourceDataSource,
                                                             final String schemaName, final String tableName, final Collection<String> columnNames,
                                                             final String sourceDatabaseType, final String targetDatabaseType, final PipelineColumnMetaData uniqueKey) {
        return new DataConsistencyCalculateParameter(sourceDataSource, schemaName, tableName, columnNames, sourceDatabaseType, targetDatabaseType, uniqueKey);
    }
}
