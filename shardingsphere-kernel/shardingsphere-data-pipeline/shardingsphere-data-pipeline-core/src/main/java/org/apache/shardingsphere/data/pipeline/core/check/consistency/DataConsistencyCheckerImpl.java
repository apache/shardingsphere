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
import org.apache.shardingsphere.data.pipeline.core.datasource.DataSourceFactory;
import org.apache.shardingsphere.data.pipeline.core.datasource.DataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.core.exception.DataCheckFailException;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredJobContext;
import org.apache.shardingsphere.data.pipeline.spi.check.consistency.DataConsistencyCheckAlgorithm;
import org.apache.shardingsphere.data.pipeline.spi.check.consistency.SingleTableDataCalculator;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.infra.config.datasource.jdbc.config.JDBCDataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.jdbc.config.JDBCDataSourceConfigurationFactory;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.thread.ExecutorThreadFactoryBuilder;
import org.apache.shardingsphere.infra.metadata.schema.builder.loader.common.TableMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
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
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
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
    
    private final DataSourceFactory dataSourceFactory = new DataSourceFactory();
    
    // TODO replace to JobConfiguration
    private final RuleAlteredJobContext jobContext;
    
    @Override
    public Map<String, DataConsistencyCheckResult> checkRecordsCount() {
        ThreadFactory threadFactory = ExecutorThreadFactoryBuilder.build("job" + jobContext.getJobId() % 10_000 + "-countCheck-%d");
        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 2, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2), threadFactory);
        JDBCDataSourceConfiguration sourceConfig = JDBCDataSourceConfigurationFactory.newInstance(
            jobContext.getJobConfig().getPipelineConfig().getSource().getType(), jobContext.getJobConfig().getPipelineConfig().getSource().getParameter());
        JDBCDataSourceConfiguration targetConfig = JDBCDataSourceConfigurationFactory.newInstance(
            jobContext.getJobConfig().getPipelineConfig().getTarget().getType(), jobContext.getJobConfig().getPipelineConfig().getTarget().getParameter());
        try (DataSourceWrapper sourceDataSource = dataSourceFactory.newInstance(sourceConfig);
             DataSourceWrapper targetDataSource = dataSourceFactory.newInstance(targetConfig)) {
            return jobContext.getTaskConfigs()
                .stream().flatMap(each -> each.getDumperConfig().getTableNameMap().values().stream()).collect(Collectors.toSet())
                .stream().collect(Collectors.toMap(Function.identity(), table -> countCheck(table, sourceDataSource, targetDataSource, executor),
                    (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
        } catch (final SQLException ex) {
            throw new DataCheckFailException("count check failed", ex);
        } finally {
            executor.shutdown();
            executor.shutdownNow();
        }
    }
    
    private DataConsistencyCheckResult countCheck(final String table, final DataSourceWrapper sourceDataSource, final DataSourceWrapper targetDataSource, final ThreadPoolExecutor executor) {
        try {
            Future<Long> sourceFuture = executor.submit(() -> count(sourceDataSource, table, sourceDataSource.getDatabaseType()));
            Future<Long> targetFuture = executor.submit(() -> count(targetDataSource, table, targetDataSource.getDatabaseType()));
            long sourceCount = sourceFuture.get();
            long targetCount = targetFuture.get();
            return new DataConsistencyCheckResult(sourceCount, targetCount);
        } catch (final InterruptedException | ExecutionException ex) {
            throw new DataCheckFailException(String.format("count check failed for table '%s'", table), ex);
        }
    }
    
    private long count(final DataSource dataSource, final String table, final DatabaseType databaseType) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(ScalingSQLBuilderFactory.newInstance(databaseType.getName()).buildCountSQL(table));
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            return resultSet.getLong(1);
        } catch (final SQLException ex) {
            throw new DataCheckFailException(String.format("count for table '%s' failed", table), ex);
        }
    }
    
    @Override
    public Map<String, Boolean> checkRecordsContent(final DataConsistencyCheckAlgorithm checkAlgorithm) {
        Collection<String> supportedDatabaseTypes = checkAlgorithm.getSupportedDatabaseTypes();
        JDBCDataSourceConfiguration sourceConfig = JDBCDataSourceConfigurationFactory.newInstance(
                jobContext.getJobConfig().getPipelineConfig().getSource().getType(), jobContext.getJobConfig().getPipelineConfig().getSource().getParameter());
        checkDatabaseTypeSupportedOrNot(supportedDatabaseTypes, sourceConfig.getDatabaseType().getName());
        JDBCDataSourceConfiguration targetConfig = JDBCDataSourceConfigurationFactory.newInstance(
                jobContext.getJobConfig().getPipelineConfig().getTarget().getType(), jobContext.getJobConfig().getPipelineConfig().getTarget().getParameter());
        checkDatabaseTypeSupportedOrNot(supportedDatabaseTypes, targetConfig.getDatabaseType().getName());
        Collection<String> logicTableNames = jobContext.getTaskConfigs().stream().flatMap(each -> each.getDumperConfig().getTableNameMap().values().stream()).distinct().collect(Collectors.toList());
        Map<String, TableMetaData> tableMetaDataMap = getTablesColumnsMap(sourceConfig, logicTableNames);
        logicTableNames.forEach(each -> {
            //TODO put to preparer
            if (!tableMetaDataMap.containsKey(each)) {
                throw new DataCheckFailException(String.format("could not get columns for table '%s'", each));
            }
        });
        String sourceDatabaseType = sourceConfig.getDatabaseType().getName();
        String targetDatabaseType = targetConfig.getDatabaseType().getName();
        SingleTableDataCalculator sourceCalculator = checkAlgorithm.getSingleTableDataCalculator(sourceDatabaseType);
        SingleTableDataCalculator targetCalculator = checkAlgorithm.getSingleTableDataCalculator(targetDatabaseType);
        Map<String, Boolean> result = new HashMap<>();
        ThreadFactory threadFactory = ExecutorThreadFactoryBuilder.build("job" + jobContext.getJobId() % 10_000 + "-dataCheck-%d");
        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 2, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2), threadFactory);
        JobRateLimitAlgorithm rateLimitAlgorithm = jobContext.getRuleAlteredContext().getRateLimitAlgorithm();
        try (DataSourceWrapper sourceDataSource = dataSourceFactory.newInstance(sourceConfig);
             DataSourceWrapper targetDataSource = dataSourceFactory.newInstance(targetConfig)) {
            for (String each : logicTableNames) {
                Collection<String> columnNames = tableMetaDataMap.get(each).getColumns().keySet();
                String uniqueKey = tableMetaDataMap.get(each).getPrimaryKeyColumns().get(0);
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
            throw new DataCheckFailException("data check failed");
        } finally {
            executor.shutdown();
            executor.shutdownNow();
        }
        return result;
    }
    
    private void checkDatabaseTypeSupportedOrNot(final Collection<String> supportedDatabaseTypes, final String databaseType) {
        if (!supportedDatabaseTypes.contains(databaseType)) {
            throw new DataCheckFailException("database type " + databaseType + " is not supported in " + supportedDatabaseTypes);
        }
    }
    
    // TODO reuse metadata
    private Map<String, TableMetaData> getTablesColumnsMap(final JDBCDataSourceConfiguration dataSourceConfig, final Collection<String> tableNames) {
        try (DataSourceWrapper dataSource = dataSourceFactory.newInstance(dataSourceConfig)) {
            Map<String, TableMetaData> result = new LinkedHashMap<>();
            for (String each : tableNames) {
                Optional<TableMetaData> tableMetaDataOptional = TableMetaDataLoader.load(dataSource, each, dataSourceConfig.getDatabaseType());
                TableMetaData tableMetaData = tableMetaDataOptional.orElseThrow(() -> new DataCheckFailException(String.format("get metadata failed for table '%s'", each)));
                result.put(each, tableMetaData);
            }
            return result;
        } catch (final SQLException ex) {
            throw new DataCheckFailException("get table columns failed", ex);
        }
    }
}
