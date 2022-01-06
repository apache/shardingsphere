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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataCalculateParameter;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfigurationFactory;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceFactory;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineDataConsistencyCheckFailedException;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredJobContext;
import org.apache.shardingsphere.data.pipeline.spi.check.consistency.DataConsistencyCheckAlgorithm;
import org.apache.shardingsphere.data.pipeline.spi.check.consistency.SingleTableDataCalculator;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.thread.ExecutorThreadFactoryBuilder;
import org.apache.shardingsphere.scaling.core.job.sqlbuilder.ScalingSQLBuilderFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Data consistency checker implementation.
 */
@RequiredArgsConstructor
@Getter
@Slf4j
public final class DataConsistencyCheckerImpl implements DataConsistencyChecker {
    
    private static final Map<PipelineDataSourceConfiguration, PipelineTableMetaDataLoader> TABLE_META_DATA_LOADER_MAP = new ConcurrentHashMap<>();
    
    private final PipelineDataSourceFactory dataSourceFactory = new PipelineDataSourceFactory();
    
    // TODO replace to JobConfiguration
    private final RuleAlteredJobContext jobContext;
    
    @Override
    public Map<String, DataConsistencyCheckResult> checkRecordsCount() {
        ThreadFactory threadFactory = ExecutorThreadFactoryBuilder.build("job" + getJobIdPrefix(jobContext.getJobId()) + "-countCheck-%d");
        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 2, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2), threadFactory);
        PipelineDataSourceConfiguration sourceDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(
            jobContext.getJobConfig().getPipelineConfig().getSource().getType(), jobContext.getJobConfig().getPipelineConfig().getSource().getParameter());
        PipelineDataSourceConfiguration targetDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(
            jobContext.getJobConfig().getPipelineConfig().getTarget().getType(), jobContext.getJobConfig().getPipelineConfig().getTarget().getParameter());
        try (PipelineDataSourceWrapper sourceDataSource = dataSourceFactory.newInstance(sourceDataSourceConfig);
             PipelineDataSourceWrapper targetDataSource = dataSourceFactory.newInstance(targetDataSourceConfig)) {
            return jobContext.getTaskConfigs()
                .stream().flatMap(each -> each.getDumperConfig().getTableNameMap().values().stream()).collect(Collectors.toSet())
                .stream().collect(Collectors.toMap(Function.identity(), table -> countCheck(table, sourceDataSource, targetDataSource, executor),
                    (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
        } catch (final SQLException ex) {
            throw new PipelineDataConsistencyCheckFailedException("count check failed", ex);
        } finally {
            executor.shutdown();
            executor.shutdownNow();
        }
    }
    
    private String getJobIdPrefix(final String jobId) {
        if (jobId.length() <= 6) {
            return jobId;
        }
        return jobId.substring(0, 6);
    }
    
    private DataConsistencyCheckResult countCheck(
            final String table, final PipelineDataSourceWrapper sourceDataSource, final PipelineDataSourceWrapper targetDataSource, final ThreadPoolExecutor executor) {
        try {
            Future<Long> sourceFuture = executor.submit(() -> count(sourceDataSource, table, sourceDataSource.getDatabaseType()));
            Future<Long> targetFuture = executor.submit(() -> count(targetDataSource, table, targetDataSource.getDatabaseType()));
            long sourceCount = sourceFuture.get();
            long targetCount = targetFuture.get();
            return new DataConsistencyCheckResult(sourceCount, targetCount);
        } catch (final InterruptedException | ExecutionException ex) {
            throw new PipelineDataConsistencyCheckFailedException(String.format("count check failed for table '%s'", table), ex);
        }
    }
    
    private long count(final DataSource dataSource, final String table, final DatabaseType databaseType) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(ScalingSQLBuilderFactory.newInstance(databaseType.getName()).buildCountSQL(table));
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            return resultSet.getLong(1);
        } catch (final SQLException ex) {
            throw new PipelineDataConsistencyCheckFailedException(String.format("count for table '%s' failed", table), ex);
        }
    }
    
    @Override
    public Map<String, Boolean> checkRecordsContent(final DataConsistencyCheckAlgorithm checkAlgorithm) {
        Collection<String> supportedDatabaseTypes = checkAlgorithm.getSupportedDatabaseTypes();
        PipelineDataSourceConfiguration sourceDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(
                jobContext.getJobConfig().getPipelineConfig().getSource().getType(), jobContext.getJobConfig().getPipelineConfig().getSource().getParameter());
        checkDatabaseTypeSupportedOrNot(supportedDatabaseTypes, sourceDataSourceConfig.getDatabaseType().getName());
        PipelineDataSourceConfiguration targetDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(
                jobContext.getJobConfig().getPipelineConfig().getTarget().getType(), jobContext.getJobConfig().getPipelineConfig().getTarget().getParameter());
        checkDatabaseTypeSupportedOrNot(supportedDatabaseTypes, targetDataSourceConfig.getDatabaseType().getName());
        Collection<String> logicTableNames = jobContext.getTaskConfigs().stream().flatMap(each -> each.getDumperConfig().getTableNameMap().values().stream()).distinct().collect(Collectors.toList());
        String sourceDatabaseType = sourceDataSourceConfig.getDatabaseType().getName();
        String targetDatabaseType = targetDataSourceConfig.getDatabaseType().getName();
        SingleTableDataCalculator sourceCalculator = checkAlgorithm.getSingleTableDataCalculator(sourceDatabaseType);
        SingleTableDataCalculator targetCalculator = checkAlgorithm.getSingleTableDataCalculator(targetDatabaseType);
        Map<String, Boolean> result = new HashMap<>();
        ThreadFactory threadFactory = ExecutorThreadFactoryBuilder.build("job" + getJobIdPrefix(jobContext.getJobId()) + "-dataCheck-%d");
        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 2, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2), threadFactory);
        JobRateLimitAlgorithm rateLimitAlgorithm = jobContext.getRuleAlteredContext().getRateLimitAlgorithm();
        try (PipelineDataSourceWrapper sourceDataSource = dataSourceFactory.newInstance(sourceDataSourceConfig);
             PipelineDataSourceWrapper targetDataSource = dataSourceFactory.newInstance(targetDataSourceConfig)) {
            PipelineTableMetaDataLoader tableMetaDataLoader = getTableMetaDataLoader(sourceDataSourceConfig, sourceDataSource);
            logicTableNames.forEach(each -> {
                //TODO put to preparer
                if (null == tableMetaDataLoader.getTableMetaData(each)) {
                    throw new PipelineDataConsistencyCheckFailedException(String.format("could not get metadata for table '%s'", each));
                }
            });
            for (String each : logicTableNames) {
                PipelineTableMetaData tableMetaData = tableMetaDataLoader.getTableMetaData(each);
                Collection<String> columnNames = tableMetaData.getColumnNames();
                String uniqueKey = tableMetaData.getPrimaryKeys().get(0);
                DataCalculateParameter sourceCalculateParameter = DataCalculateParameter.builder().dataSource(sourceDataSource).databaseType(sourceDatabaseType).peerDatabaseType(targetDatabaseType)
                    .logicTableName(each).columnNames(columnNames).uniqueKey(uniqueKey).build();
                DataCalculateParameter targetCalculateParameter = DataCalculateParameter.builder().dataSource(targetDataSource).databaseType(targetDatabaseType).peerDatabaseType(sourceDatabaseType)
                    .logicTableName(each).columnNames(columnNames).uniqueKey(uniqueKey).build();
                Iterator<Object> sourceCalculatedResultIterator = sourceCalculator.calculate(sourceCalculateParameter).iterator();
                Iterator<Object> targetCalculatedResultIterator = targetCalculator.calculate(targetCalculateParameter).iterator();
                boolean calculateResultsEquals = true;
                while (sourceCalculatedResultIterator.hasNext() && targetCalculatedResultIterator.hasNext()) {
                    if (null != rateLimitAlgorithm) {
                        rateLimitAlgorithm.onQuery();
                    }
                    Future<Object> sourceFuture = executor.submit(sourceCalculatedResultIterator::next);
                    Future<Object> targetFuture = executor.submit(targetCalculatedResultIterator::next);
                    Object sourceCalculatedResult = sourceFuture.get();
                    Object targetCalculatedResult = targetFuture.get();
                    calculateResultsEquals = Objects.equals(sourceCalculatedResult, targetCalculatedResult);
                    if (!calculateResultsEquals) {
                        break;
                    }
                }
                result.put(each, calculateResultsEquals);
            }
        } catch (final ExecutionException | InterruptedException | SQLException ex) {
            throw new PipelineDataConsistencyCheckFailedException("data check failed", ex);
        } finally {
            executor.shutdown();
            executor.shutdownNow();
        }
        return result;
    }
    
    private void checkDatabaseTypeSupportedOrNot(final Collection<String> supportedDatabaseTypes, final String databaseType) {
        if (!supportedDatabaseTypes.contains(databaseType)) {
            throw new PipelineDataConsistencyCheckFailedException("database type " + databaseType + " is not supported in " + supportedDatabaseTypes);
        }
    }
    
    private PipelineTableMetaDataLoader getTableMetaDataLoader(final PipelineDataSourceConfiguration sourceDataSourceConfig, final PipelineDataSourceWrapper sourceDataSource) throws SQLException {
        PipelineTableMetaDataLoader result = TABLE_META_DATA_LOADER_MAP.get(sourceDataSourceConfig);
        if (null != result) {
            return result;
        }
        synchronized (TABLE_META_DATA_LOADER_MAP) {
            result = TABLE_META_DATA_LOADER_MAP.get(sourceDataSourceConfig);
            if (null != result) {
                return result;
            }
            try (Connection connection = sourceDataSource.getConnection()) {
                result = new PipelineTableMetaDataLoader(connection, "%");
                TABLE_META_DATA_LOADER_MAP.put(sourceDataSourceConfig, result);
            }
            return result;
        }
    }
}
