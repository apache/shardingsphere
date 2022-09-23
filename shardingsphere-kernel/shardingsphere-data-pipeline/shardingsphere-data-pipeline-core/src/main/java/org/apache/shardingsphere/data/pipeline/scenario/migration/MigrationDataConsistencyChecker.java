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

package org.apache.shardingsphere.data.pipeline.scenario.migration;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCalculateParameter;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyContentCheckResult;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCountCheckResult;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.PipelineDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.api.config.TableNameSchemaNameMapping;
import org.apache.shardingsphere.data.pipeline.api.config.job.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfigurationFactory;
import org.apache.shardingsphere.data.pipeline.api.job.JobOperationType;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.data.pipeline.core.context.InventoryIncrementalProcessContext;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceFactory;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineSQLException;
import org.apache.shardingsphere.data.pipeline.core.exception.data.PipelineTableDataConsistencyCheckLoadingFailedException;
import org.apache.shardingsphere.data.pipeline.core.exception.data.UnsupportedPipelineDatabaseTypeException;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.StandardPipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.PipelineSQLBuilderFactory;
import org.apache.shardingsphere.data.pipeline.spi.check.consistency.DataConsistencyCalculateAlgorithm;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.thread.ExecutorThreadFactoryBuilder;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.wrapper.SQLWrapperException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Data consistency checker for migration job.
 */
@Slf4j
public final class MigrationDataConsistencyChecker implements PipelineDataConsistencyChecker {
    
    private final MigrationJobConfiguration jobConfig;
    
    private final JobRateLimitAlgorithm readRateLimitAlgorithm;
    
    private final TableNameSchemaNameMapping tableNameSchemaNameMapping;
    
    public MigrationDataConsistencyChecker(final MigrationJobConfiguration jobConfig, final InventoryIncrementalProcessContext processContext) {
        this.jobConfig = jobConfig;
        this.readRateLimitAlgorithm = null != processContext ? processContext.getReadRateLimitAlgorithm() : null;
        tableNameSchemaNameMapping = new TableNameSchemaNameMapping(
                TableNameSchemaNameMapping.convert(jobConfig.getSourceSchemaName(), new HashSet<>(Arrays.asList(jobConfig.getSourceTableName(), jobConfig.getTargetTableName()))));
    }
    
    @Override
    public Map<String, DataConsistencyCheckResult> check(final DataConsistencyCalculateAlgorithm calculator) {
        Map<String, DataConsistencyCountCheckResult> countCheckResult = checkCount();
        Map<String, DataConsistencyContentCheckResult> contentCheckResult = countCheckResult.values().stream().allMatch(DataConsistencyCountCheckResult::isMatched)
                ? checkData(calculator)
                : Collections.emptyMap();
        Map<String, DataConsistencyCheckResult> result = new LinkedHashMap<>(countCheckResult.size());
        for (Entry<String, DataConsistencyCountCheckResult> entry : countCheckResult.entrySet()) {
            result.put(entry.getKey(), new DataConsistencyCheckResult(entry.getValue(), contentCheckResult.getOrDefault(entry.getKey(), new DataConsistencyContentCheckResult(false))));
        }
        return result;
    }
    
    private Map<String, DataConsistencyCountCheckResult> checkCount() {
        ThreadFactory threadFactory = ExecutorThreadFactoryBuilder.build("job-" + getJobIdDigest(jobConfig.getJobId()) + "-count-check-%d");
        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 2, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2), threadFactory);
        PipelineDataSourceConfiguration sourceDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(jobConfig.getSource().getType(), jobConfig.getSource().getParameter());
        PipelineDataSourceConfiguration targetDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(jobConfig.getTarget().getType(), jobConfig.getTarget().getParameter());
        Map<String, DataConsistencyCountCheckResult> result = new LinkedHashMap<>(1, 1);
        try (
                PipelineDataSourceWrapper sourceDataSource = PipelineDataSourceFactory.newInstance(sourceDataSourceConfig);
                PipelineDataSourceWrapper targetDataSource = PipelineDataSourceFactory.newInstance(targetDataSourceConfig)) {
            result.put(jobConfig.getSourceTableName(), checkCount(sourceDataSource, targetDataSource, executor));
            return result;
        } catch (final SQLException ex) {
            throw new SQLWrapperException(ex);
        } finally {
            executor.shutdown();
            executor.shutdownNow();
        }
    }
    
    private DataConsistencyCountCheckResult checkCount(final PipelineDataSourceWrapper sourceDataSource, final PipelineDataSourceWrapper targetDataSource, final ThreadPoolExecutor executor) {
        Future<Long> sourceFuture = executor.submit(() -> count(sourceDataSource, jobConfig.getSourceTableName(), sourceDataSource.getDatabaseType()));
        Future<Long> targetFuture = executor.submit(() -> count(targetDataSource, jobConfig.getTargetTableName(), targetDataSource.getDatabaseType()));
        long sourceCount;
        long targetCount;
        try {
            sourceCount = sourceFuture.get();
        } catch (final InterruptedException | ExecutionException ex) {
            if (ex.getCause() instanceof PipelineSQLException) {
                throw (PipelineSQLException) ex.getCause();
            }
            throw new SQLWrapperException(new SQLException(ex));
        }
        try {
            targetCount = targetFuture.get();
        } catch (final InterruptedException | ExecutionException ex) {
            if (ex.getCause() instanceof PipelineSQLException) {
                throw (PipelineSQLException) ex.getCause();
            }
            throw new SQLWrapperException(new SQLException(ex));
        }
        return new DataConsistencyCountCheckResult(sourceCount, targetCount);
    }
    
    // TODO use digest (crc32, murmurhash)
    private String getJobIdDigest(final String jobId) {
        return jobId.length() <= 6 ? jobId : jobId.substring(0, 6);
    }
    
    private long count(final DataSource dataSource, final String tableName, final DatabaseType databaseType) {
        String sql = PipelineSQLBuilderFactory.getInstance(databaseType.getType()).buildCountSQL(tableNameSchemaNameMapping.getSchemaName(tableName), tableName);
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
    
    private Map<String, DataConsistencyContentCheckResult> checkData(final DataConsistencyCalculateAlgorithm calculator) {
        checkPipelineDatabaseType(calculator, jobConfig.getSource());
        PipelineDataSourceConfiguration sourceDataSourceConfig = jobConfig.getSource();
        checkPipelineDatabaseType(calculator, jobConfig.getTarget());
        PipelineDataSourceConfiguration targetDataSourceConfig = jobConfig.getTarget();
        ThreadFactory threadFactory = ExecutorThreadFactoryBuilder.build("job-" + getJobIdDigest(jobConfig.getJobId()) + "-data-check-%d");
        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 2, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2), threadFactory);
        Map<String, DataConsistencyContentCheckResult> result = new HashMap<>(1, 1);
        try (
                PipelineDataSourceWrapper sourceDataSource = PipelineDataSourceFactory.newInstance(sourceDataSourceConfig);
                PipelineDataSourceWrapper targetDataSource = PipelineDataSourceFactory.newInstance(targetDataSourceConfig)) {
            String sourceDatabaseType = sourceDataSourceConfig.getDatabaseType().getType();
            String targetDatabaseType = targetDataSourceConfig.getDatabaseType().getType();
            StandardPipelineTableMetaDataLoader metaDataLoader = new StandardPipelineTableMetaDataLoader(sourceDataSource);
            for (String each : Collections.singleton(jobConfig.getSourceTableName())) {
                PipelineTableMetaData tableMetaData = metaDataLoader.getTableMetaData(tableNameSchemaNameMapping.getSchemaName(each), each);
                ShardingSpherePreconditions.checkNotNull(tableMetaData, () -> new PipelineTableDataConsistencyCheckLoadingFailedException(each));
                Collection<String> columnNames = tableMetaData.getColumnNames();
                PipelineColumnMetaData uniqueKey = jobConfig.getUniqueKeyColumn();
                DataConsistencyCalculateParameter sourceParameter = buildParameter(sourceDataSource, tableNameSchemaNameMapping, each, columnNames, sourceDatabaseType, targetDatabaseType, uniqueKey);
                DataConsistencyCalculateParameter targetParameter = buildParameter(
                        targetDataSource, tableNameSchemaNameMapping, jobConfig.getTargetTableName(), columnNames, targetDatabaseType, sourceDatabaseType, uniqueKey);
                Iterator<Object> sourceCalculatedResults = calculator.calculate(sourceParameter).iterator();
                Iterator<Object> targetCalculatedResults = calculator.calculate(targetParameter).iterator();
                boolean contentMatched = true;
                while (sourceCalculatedResults.hasNext() && targetCalculatedResults.hasNext()) {
                    if (null != readRateLimitAlgorithm) {
                        readRateLimitAlgorithm.intercept(JobOperationType.SELECT, 1);
                    }
                    Future<Object> sourceFuture = executor.submit(sourceCalculatedResults::next);
                    Future<Object> targetFuture = executor.submit(targetCalculatedResults::next);
                    Object sourceCalculatedResult = sourceFuture.get();
                    Object targetCalculatedResult = targetFuture.get();
                    contentMatched = Objects.equals(sourceCalculatedResult, targetCalculatedResult);
                    if (!contentMatched) {
                        break;
                    }
                }
                result.put(each, new DataConsistencyContentCheckResult(contentMatched));
            }
        } catch (final SQLException ex) {
            throw new SQLWrapperException(ex);
        } catch (final ExecutionException | InterruptedException ex) {
            throw new SQLWrapperException(new SQLException(ex.getCause()));
        } finally {
            executor.shutdown();
            executor.shutdownNow();
        }
        return result;
    }
    
    private void checkPipelineDatabaseType(final DataConsistencyCalculateAlgorithm calculator, final PipelineDataSourceConfiguration dataSourceConfig) {
        ShardingSpherePreconditions.checkState(calculator.getSupportedDatabaseTypes().contains(dataSourceConfig.getDatabaseType().getType()),
                () -> new UnsupportedPipelineDatabaseTypeException(dataSourceConfig.getDatabaseType()));
    }
    
    private DataConsistencyCalculateParameter buildParameter(final PipelineDataSourceWrapper sourceDataSource, final TableNameSchemaNameMapping tableNameSchemaNameMapping,
                                                             final String tableName, final Collection<String> columnNames,
                                                             final String sourceDatabaseType, final String targetDatabaseType, final PipelineColumnMetaData uniqueKey) {
        return new DataConsistencyCalculateParameter(sourceDataSource, tableNameSchemaNameMapping, tableName, columnNames, sourceDatabaseType, targetDatabaseType, uniqueKey);
    }
}
