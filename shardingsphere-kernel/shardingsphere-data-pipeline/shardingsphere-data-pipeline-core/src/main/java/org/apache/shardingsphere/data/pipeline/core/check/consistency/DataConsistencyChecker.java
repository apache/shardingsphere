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
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCalculateParameter;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyContentCheckResult;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCountCheckResult;
import org.apache.shardingsphere.data.pipeline.api.config.TableNameSchemaNameMapping;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.RuleAlteredJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfigurationFactory;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.yaml.YamlPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.job.JobOperationType;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceFactory;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineDataConsistencyCheckFailedException;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.PipelineSQLBuilderFactory;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredJobWorker;
import org.apache.shardingsphere.data.pipeline.spi.check.consistency.DataConsistencyCalculateAlgorithm;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.thread.ExecutorThreadFactoryBuilder;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Data consistency checker.
 */
public final class DataConsistencyChecker {
    
    // TODO remove jobConfig for common usage
    private final RuleAlteredJobConfiguration jobConfig;
    
    private final Collection<String> logicTableNames;
    
    private final TableNameSchemaNameMapping tableNameSchemaNameMapping;
    
    public DataConsistencyChecker(final RuleAlteredJobConfiguration jobConfig) {
        this.jobConfig = jobConfig;
        logicTableNames = jobConfig.splitLogicTableNames();
        ShardingSphereMetaData metaData = PipelineContext.getContextManager().getMetaDataContexts().getMetaData(jobConfig.getDatabaseName());
        tableNameSchemaNameMapping = new TableNameSchemaNameMapping(TableNameSchemaNameMapping.convert(metaData.getSchemas()));
    }
    
    /**
     * Check data consistency.
     *
     * @param calculator data consistency calculate algorithm
     * @return checked result. key is logic table name, value is check result.
     */
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
        Map<String, DataConsistencyCountCheckResult> result = new LinkedHashMap<>(logicTableNames.size(), 1);
        try (
                PipelineDataSourceWrapper sourceDataSource = PipelineDataSourceFactory.newInstance(sourceDataSourceConfig);
                PipelineDataSourceWrapper targetDataSource = PipelineDataSourceFactory.newInstance(targetDataSourceConfig)) {
            for (String each : logicTableNames) {
                result.put(each, checkCount(each, sourceDataSource, targetDataSource, executor));
            }
            return result;
        } catch (final SQLException ex) {
            throw new PipelineDataConsistencyCheckFailedException("Count check failed", ex);
        } finally {
            executor.shutdown();
            executor.shutdownNow();
        }
    }
    
    private DataConsistencyCountCheckResult checkCount(final String table, final PipelineDataSourceWrapper sourceDataSource, final PipelineDataSourceWrapper targetDataSource,
                                                       final ThreadPoolExecutor executor) {
        try {
            Future<Long> sourceFuture = executor.submit(() -> count(sourceDataSource, table, sourceDataSource.getDatabaseType()));
            Future<Long> targetFuture = executor.submit(() -> count(targetDataSource, table, targetDataSource.getDatabaseType()));
            long sourceCount = sourceFuture.get();
            long targetCount = targetFuture.get();
            return new DataConsistencyCountCheckResult(sourceCount, targetCount);
        } catch (final InterruptedException | ExecutionException ex) {
            throw new PipelineDataConsistencyCheckFailedException(String.format("Count check failed for table '%s'", table), ex);
        }
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
            throw new PipelineDataConsistencyCheckFailedException(String.format("Count for table '%s' failed", tableName), ex);
        }
    }
    
    private Map<String, DataConsistencyContentCheckResult> checkData(final DataConsistencyCalculateAlgorithm calculator) {
        PipelineDataSourceConfiguration sourceDataSourceConfig = getPipelineDataSourceConfiguration(calculator, jobConfig.getSource());
        PipelineDataSourceConfiguration targetDataSourceConfig = getPipelineDataSourceConfiguration(calculator, jobConfig.getTarget());
        ThreadFactory threadFactory = ExecutorThreadFactoryBuilder.build("job-" + getJobIdDigest(jobConfig.getJobId()) + "-data-check-%d");
        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 2, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2), threadFactory);
        JobRateLimitAlgorithm inputRateLimitAlgorithm = RuleAlteredJobWorker.createRuleAlteredContext(jobConfig).getInputRateLimitAlgorithm();
        Map<String, DataConsistencyContentCheckResult> result = new HashMap<>(logicTableNames.size(), 1);
        try (
                PipelineDataSourceWrapper sourceDataSource = PipelineDataSourceFactory.newInstance(sourceDataSourceConfig);
                PipelineDataSourceWrapper targetDataSource = PipelineDataSourceFactory.newInstance(targetDataSourceConfig)) {
            String sourceDatabaseType = sourceDataSourceConfig.getDatabaseType().getType();
            String targetDatabaseType = targetDataSourceConfig.getDatabaseType().getType();
            for (String each : logicTableNames) {
                TableMetaData tableMetaData = getTableMetaData(jobConfig.getDatabaseName(), each);
                if (null == tableMetaData) {
                    throw new PipelineDataConsistencyCheckFailedException("Can not get metadata for table " + each);
                }
                Collection<String> columnNames = tableMetaData.getColumns().keySet();
                String uniqueKey = tableMetaData.getPrimaryKeyColumns().get(0);
                DataConsistencyCalculateParameter sourceParameter = buildParameter(sourceDataSource, tableNameSchemaNameMapping, each, columnNames, sourceDatabaseType, targetDatabaseType, uniqueKey);
                DataConsistencyCalculateParameter targetParameter = buildParameter(targetDataSource, tableNameSchemaNameMapping, each, columnNames, targetDatabaseType, sourceDatabaseType, uniqueKey);
                Iterator<Object> sourceCalculatedResults = calculator.calculate(sourceParameter).iterator();
                Iterator<Object> targetCalculatedResults = calculator.calculate(targetParameter).iterator();
                boolean contentMatched = true;
                while (sourceCalculatedResults.hasNext() && targetCalculatedResults.hasNext()) {
                    if (null != inputRateLimitAlgorithm) {
                        inputRateLimitAlgorithm.intercept(JobOperationType.SELECT, 1);
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
        } catch (final ExecutionException | InterruptedException | SQLException ex) {
            throw new PipelineDataConsistencyCheckFailedException("Data check failed", ex);
        } finally {
            executor.shutdown();
            executor.shutdownNow();
        }
        return result;
    }
    
    private PipelineDataSourceConfiguration getPipelineDataSourceConfiguration(final DataConsistencyCalculateAlgorithm calculator, final YamlPipelineDataSourceConfiguration dataSourceConfig) {
        PipelineDataSourceConfiguration result = PipelineDataSourceConfigurationFactory.newInstance(dataSourceConfig.getType(), dataSourceConfig.getParameter());
        checkDatabaseTypeSupported(calculator.getSupportedDatabaseTypes(), result.getDatabaseType().getType());
        addMySQLDataSourceConfig(result);
        return result;
    }
    
    private void checkDatabaseTypeSupported(final Collection<String> supportedDatabaseTypes, final String databaseType) {
        if (!supportedDatabaseTypes.contains(databaseType)) {
            throw new PipelineDataConsistencyCheckFailedException("Database type " + databaseType + " is not supported in " + supportedDatabaseTypes);
        }
    }
    
    private void addMySQLDataSourceConfig(final PipelineDataSourceConfiguration dataSourceConfig) {
        if (dataSourceConfig.getDatabaseType().getType().equalsIgnoreCase(new MySQLDatabaseType().getType())) {
            Properties queryProps = new Properties();
            queryProps.setProperty("yearIsDateType", Boolean.FALSE.toString());
            dataSourceConfig.appendJDBCQueryProperties(queryProps);
        }
    }
    
    private TableMetaData getTableMetaData(final String databaseName, final String logicTableName) {
        ContextManager contextManager = PipelineContext.getContextManager();
        Preconditions.checkNotNull(contextManager, "ContextManager null");
        ShardingSphereMetaData metaData = contextManager.getMetaDataContexts().getMetaData(databaseName);
        if (null == metaData) {
            throw new RuntimeException("Can not get meta data by database name " + databaseName);
        }
        String schemaName = tableNameSchemaNameMapping.getSchemaName(logicTableName);
        ShardingSphereSchema schema = metaData.getSchemaByName(schemaName);
        if (null == schema) {
            throw new RuntimeException("Can not get schema by schema name " + schemaName + ", logicTableName=" + logicTableName);
        }
        return schema.get(logicTableName);
    }
    
    private DataConsistencyCalculateParameter buildParameter(final PipelineDataSourceWrapper sourceDataSource, final TableNameSchemaNameMapping tableNameSchemaNameMapping, final String tableName,
                                                             final Collection<String> columnNames, final String sourceDatabaseType, final String targetDatabaseType, final String uniqueKey) {
        return new DataConsistencyCalculateParameter(sourceDataSource, tableNameSchemaNameMapping, tableName, columnNames, sourceDatabaseType, targetDatabaseType, uniqueKey);
    }
}
