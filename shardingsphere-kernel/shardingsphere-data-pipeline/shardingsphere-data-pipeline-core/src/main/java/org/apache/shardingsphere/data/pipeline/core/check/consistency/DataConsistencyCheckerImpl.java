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

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataCalculateParameter;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.JobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfigurationFactory;
import org.apache.shardingsphere.data.pipeline.api.job.JobOperationType;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceFactory;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineDataConsistencyCheckFailedException;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.PipelineSQLBuilderFactory;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredContext;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredJobWorker;
import org.apache.shardingsphere.data.pipeline.spi.check.consistency.DataConsistencyCheckAlgorithm;
import org.apache.shardingsphere.data.pipeline.spi.check.consistency.SingleTableDataCalculator;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.thread.ExecutorThreadFactoryBuilder;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;

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
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Data consistency checker implementation.
 */
@Getter
@Slf4j
public final class DataConsistencyCheckerImpl implements DataConsistencyChecker {
    
    private final PipelineDataSourceFactory dataSourceFactory = new PipelineDataSourceFactory();
    
    private final JobConfiguration jobConfig;
    
    private final RuleAlteredContext ruleAlteredContext;
    
    private final String jobId;
    
    private final Collection<String> logicTableNames;
    
    public DataConsistencyCheckerImpl(final JobConfiguration jobConfig) {
        this.jobConfig = jobConfig;
        ruleAlteredContext = RuleAlteredJobWorker.createRuleAlteredContext(jobConfig);
        jobId = jobConfig.getHandleConfig().getJobId();
        logicTableNames = jobConfig.getHandleConfig().splitLogicTableNames();
    }
    
    @Override
    public Map<String, DataConsistencyCheckResult> checkRecordsCount() {
        ThreadFactory threadFactory = ExecutorThreadFactoryBuilder.build("job" + getJobIdPrefix(jobId) + "-countCheck-%d");
        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 2, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2), threadFactory);
        PipelineDataSourceConfiguration sourceDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(
            jobConfig.getPipelineConfig().getSource().getType(), jobConfig.getPipelineConfig().getSource().getParameter());
        PipelineDataSourceConfiguration targetDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(
            jobConfig.getPipelineConfig().getTarget().getType(), jobConfig.getPipelineConfig().getTarget().getParameter());
        try (PipelineDataSourceWrapper sourceDataSource = dataSourceFactory.newInstance(sourceDataSourceConfig);
             PipelineDataSourceWrapper targetDataSource = dataSourceFactory.newInstance(targetDataSourceConfig)) {
            Map<String, DataConsistencyCheckResult> result = new LinkedHashMap<>();
            for (String each : logicTableNames) {
                result.put(each, countCheck(each, sourceDataSource, targetDataSource, executor));
            }
            return result;
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
             PreparedStatement preparedStatement = connection.prepareStatement(PipelineSQLBuilderFactory.getSQLBuilder(databaseType.getName()).buildCountSQL(table));
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
                jobConfig.getPipelineConfig().getSource().getType(), jobConfig.getPipelineConfig().getSource().getParameter());
        checkDatabaseTypeSupportedOrNot(supportedDatabaseTypes, sourceDataSourceConfig.getDatabaseType().getName());
        PipelineDataSourceConfiguration targetDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(
                jobConfig.getPipelineConfig().getTarget().getType(), jobConfig.getPipelineConfig().getTarget().getParameter());
        checkDatabaseTypeSupportedOrNot(supportedDatabaseTypes, targetDataSourceConfig.getDatabaseType().getName());
        addDataSourceConfigToMySQL(sourceDataSourceConfig, targetDataSourceConfig);
        String sourceDatabaseType = sourceDataSourceConfig.getDatabaseType().getName();
        String targetDatabaseType = targetDataSourceConfig.getDatabaseType().getName();
        SingleTableDataCalculator sourceCalculator = checkAlgorithm.getSingleTableDataCalculator(sourceDatabaseType);
        SingleTableDataCalculator targetCalculator = checkAlgorithm.getSingleTableDataCalculator(targetDatabaseType);
        Map<String, Boolean> result = new HashMap<>();
        ThreadFactory threadFactory = ExecutorThreadFactoryBuilder.build("job" + getJobIdPrefix(jobId) + "-dataCheck-%d");
        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 2, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2), threadFactory);
        JobRateLimitAlgorithm inputRateLimitAlgorithm = ruleAlteredContext.getInputRateLimitAlgorithm();
        try (PipelineDataSourceWrapper sourceDataSource = dataSourceFactory.newInstance(sourceDataSourceConfig);
             PipelineDataSourceWrapper targetDataSource = dataSourceFactory.newInstance(targetDataSourceConfig)) {
            Map<String, TableMetaData> tableMetaDataMap = getTableMetaDataMap(jobConfig.getWorkflowConfig().getSchemaName());
            logicTableNames.forEach(each -> {
                //TODO put to preparer
                if (!tableMetaDataMap.containsKey(each)) {
                    throw new PipelineDataConsistencyCheckFailedException(String.format("could not get metadata for table '%s'", each));
                }
            });
            for (String each : logicTableNames) {
                TableMetaData tableMetaData = tableMetaDataMap.get(each);
                Collection<String> columnNames = tableMetaData.getColumns().keySet();
                String uniqueKey = tableMetaData.getPrimaryKeyColumns().get(0);
                DataCalculateParameter sourceCalculateParameter = DataCalculateParameter.builder().dataSource(sourceDataSource).databaseType(sourceDatabaseType).peerDatabaseType(targetDatabaseType)
                    .logicTableName(each).columnNames(columnNames).uniqueKey(uniqueKey).build();
                DataCalculateParameter targetCalculateParameter = DataCalculateParameter.builder().dataSource(targetDataSource).databaseType(targetDatabaseType).peerDatabaseType(sourceDatabaseType)
                    .logicTableName(each).columnNames(columnNames).uniqueKey(uniqueKey).build();
                Iterator<Object> sourceCalculatedResultIterator = sourceCalculator.calculate(sourceCalculateParameter).iterator();
                Iterator<Object> targetCalculatedResultIterator = targetCalculator.calculate(targetCalculateParameter).iterator();
                boolean calculateResultsEquals = true;
                while (sourceCalculatedResultIterator.hasNext() && targetCalculatedResultIterator.hasNext()) {
                    if (null != inputRateLimitAlgorithm) {
                        inputRateLimitAlgorithm.intercept(JobOperationType.SELECT, 1);
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
    
    private Map<String, TableMetaData> getTableMetaDataMap(final String schemaName) {
        ContextManager contextManager = PipelineContext.getContextManager();
        Preconditions.checkNotNull(contextManager, "contextManager null");
        ShardingSphereMetaData metaData = contextManager.getMetaDataContexts().getMetaData(schemaName);
        if (null == metaData) {
            throw new RuntimeException("Can not get metaData by schemaName " + schemaName);
        }
        return metaData.getDefaultSchema().getTables();
    }
    
    private void addDataSourceConfigToMySQL(final PipelineDataSourceConfiguration sourceDataSourceConfig, final PipelineDataSourceConfiguration targetDataSourceConfig) {
        if (sourceDataSourceConfig.getDatabaseType().getName().equalsIgnoreCase(new MySQLDatabaseType().getName())) {
            Properties queryProps = new Properties();
            queryProps.setProperty("yearIsDateType", Boolean.FALSE.toString());
            sourceDataSourceConfig.appendJDBCQueryProperties(queryProps);
            targetDataSourceConfig.appendJDBCQueryProperties(queryProps);
        }
    }
}
