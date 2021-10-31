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

package org.apache.shardingsphere.scaling.core.job.check.consistency;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.thread.ExecutorThreadFactoryBuilder;
import org.apache.shardingsphere.scaling.core.api.DataCalculateParameter;
import org.apache.shardingsphere.scaling.core.api.ScalingDataConsistencyCheckAlgorithm;
import org.apache.shardingsphere.scaling.core.api.SingleTableDataCalculator;
import org.apache.shardingsphere.scaling.core.common.datasource.DataSourceFactory;
import org.apache.shardingsphere.scaling.core.common.datasource.DataSourceWrapper;
import org.apache.shardingsphere.scaling.core.common.exception.DataCheckFailException;
import org.apache.shardingsphere.scaling.core.common.sqlbuilder.ScalingSQLBuilderFactory;
import org.apache.shardingsphere.scaling.core.config.datasource.ScalingDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.job.JobContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
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
    
    private final JobContext jobContext;
    
    @Override
    public Map<String, DataConsistencyCheckResult> countCheck() {
        ThreadFactory threadFactory = ExecutorThreadFactoryBuilder.build("job" + jobContext.getJobId() % 10_000 + "-countCheck-%d");
        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 2, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2), threadFactory);
        try {
            return jobContext.getTaskConfigs()
                    .stream().flatMap(each -> each.getDumperConfig().getTableNameMap().values().stream()).collect(Collectors.toSet())
                    .stream().collect(Collectors.toMap(Function.identity(), table -> countCheck(table, executor), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
        } finally {
            executor.shutdown();
            executor.shutdownNow();
        }
    }
    
    private DataConsistencyCheckResult countCheck(final String table, final ThreadPoolExecutor executor) {
        ScalingDataSourceConfiguration sourceConfig = jobContext.getJobConfig().getRuleConfig().getSource().unwrap();
        ScalingDataSourceConfiguration targetConfig = jobContext.getJobConfig().getRuleConfig().getTarget().unwrap();
        try (DataSourceWrapper sourceDataSource = dataSourceFactory.newInstance(sourceConfig);
             DataSourceWrapper targetDataSource = dataSourceFactory.newInstance(targetConfig)) {
            Future<Long> sourceFuture = executor.submit(() -> count(sourceDataSource, table, sourceConfig.getDatabaseType()));
            Future<Long> targetFuture = executor.submit(() -> count(targetDataSource, table, targetConfig.getDatabaseType()));
            long sourceCount = sourceFuture.get();
            long targetCount = targetFuture.get();
            return new DataConsistencyCheckResult(sourceCount, targetCount);
        } catch (final SQLException | InterruptedException | ExecutionException ex) {
            throw new DataCheckFailException(String.format("table %s count check failed.", table), ex);
        }
    }
    
    private long count(final DataSource dataSource, final String table, final DatabaseType databaseType) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(ScalingSQLBuilderFactory.newInstance(databaseType.getName()).buildCountSQL(table));
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            return resultSet.getLong(1);
        } catch (final SQLException ex) {
            throw new DataCheckFailException(String.format("table %s count failed.", table), ex);
        }
    }
    
    @Override
    public Map<String, Boolean> dataCheck(final ScalingDataConsistencyCheckAlgorithm checkAlgorithm) {
        Collection<String> supportedDatabaseTypes = checkAlgorithm.getSupportedDatabaseTypes();
        ScalingDataSourceConfiguration sourceConfig = jobContext.getJobConfig().getRuleConfig().getSource().unwrap();
        checkDatabaseTypeSupportedOrNot(supportedDatabaseTypes, sourceConfig.getDatabaseType().getName());
        ScalingDataSourceConfiguration targetConfig = jobContext.getJobConfig().getRuleConfig().getTarget().unwrap();
        checkDatabaseTypeSupportedOrNot(supportedDatabaseTypes, targetConfig.getDatabaseType().getName());
        Collection<String> logicTableNames = jobContext.getTaskConfigs().stream().flatMap(each -> each.getDumperConfig().getTableNameMap().values().stream())
                .distinct().collect(Collectors.toList());
        Map<String, Collection<String>> tablesColumnNamesMap = getTablesColumnNamesMap(sourceConfig);
        logicTableNames.forEach(each -> {
            //TODO put to preparer
            if (!tablesColumnNamesMap.containsKey(each)) {
                throw new DataCheckFailException(String.format("could not get table columns for '%s'", each));
            }
        });
        SingleTableDataCalculator sourceCalculator = checkAlgorithm.getSingleTableDataCalculator(sourceConfig.getDatabaseType().getName());
        SingleTableDataCalculator targetCalculator = checkAlgorithm.getSingleTableDataCalculator(targetConfig.getDatabaseType().getName());
        Map<String, Boolean> result = new HashMap<>();
        ThreadFactory threadFactory = ExecutorThreadFactoryBuilder.build("job" + jobContext.getJobId() % 10_000 + "-dataCheck-%d");
        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 2, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2), threadFactory);
        try {
            for (String each : logicTableNames) {
                Collection<String> columnNames = tablesColumnNamesMap.get(each);
                DataCalculateParameter sourceCalculateParameter = DataCalculateParameter.builder().dataSourceConfig(sourceConfig).logicTableName(each).columnNames(columnNames).build();
                Future<Object> sourceFuture = executor.submit(() -> sourceCalculator.dataCalculate(sourceCalculateParameter));
                DataCalculateParameter targetCalculateParameter = DataCalculateParameter.builder().dataSourceConfig(targetConfig).logicTableName(each).columnNames(columnNames).build();
                Future<Object> targetFuture = executor.submit(() -> targetCalculator.dataCalculate(targetCalculateParameter));
                Object sourceCalculateResult = sourceFuture.get();
                Object targetCalculateResult = targetFuture.get();
                boolean calculateResultsEquals = Objects.equals(sourceCalculateResult, targetCalculateResult);
                result.put(each, calculateResultsEquals);
            }
        } catch (final ExecutionException | InterruptedException ex) {
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
    
    private Map<String, Collection<String>> getTablesColumnNamesMap(final ScalingDataSourceConfiguration dataSourceConfig) {
        try (DataSourceWrapper dataSource = dataSourceFactory.newInstance(dataSourceConfig);
             Connection connection = dataSource.getConnection()) {
            Map<String, Collection<String>> result = new LinkedHashMap<>();
            try (ResultSet resultSet = connection.getMetaData().getColumns(connection.getCatalog(), null, "%", "%")) {
                while (resultSet.next()) {
                    result.computeIfAbsent(resultSet.getString("TABLE_NAME"), tableName -> new ArrayList<>()).add(resultSet.getString("COLUMN_NAME"));
                }
            }
            return result;
        } catch (final SQLException ex) {
            throw new DataCheckFailException("get table columns failed", ex);
        }
    }
}
