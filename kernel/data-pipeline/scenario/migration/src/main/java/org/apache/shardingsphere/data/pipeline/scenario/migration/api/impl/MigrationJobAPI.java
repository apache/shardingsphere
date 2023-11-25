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

package org.apache.shardingsphere.data.pipeline.scenario.migration.api.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextManager;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceFactory;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.common.sqlbuilder.PipelineCommonSQLBuilder;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.context.mapper.TableAndSchemaNameMapper;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.option.PipelineJobOption;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobConfigurationManager;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobManager;
import org.apache.shardingsphere.data.pipeline.core.job.service.TransmissionJobAPI;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

/**
 * Migration job API.
 */
@Slf4j
public final class MigrationJobAPI implements TransmissionJobAPI {
    
    @Override
    public void commit(final String jobId) {
        log.info("Commit job {}", jobId);
        final long startTimeMillis = System.currentTimeMillis();
        PipelineJobOption jobOption = new MigrationJobOption();
        PipelineJobManager jobManager = new PipelineJobManager(jobOption);
        jobManager.stop(jobId);
        dropCheckJobs(jobId);
        MigrationJobConfiguration jobConfig = new PipelineJobConfigurationManager(jobOption).getJobConfiguration(jobId);
        refreshTableMetadata(jobId, jobConfig.getTargetDatabaseName());
        jobManager.drop(jobId);
        log.info("Commit cost {} ms", System.currentTimeMillis() - startTimeMillis);
    }
    
    private void refreshTableMetadata(final String jobId, final String databaseName) {
        // TODO use origin database name now, wait reloadDatabaseMetaData fix case-sensitive probelm
        ContextManager contextManager = PipelineContextManager.getContext(PipelineJobIdUtils.parseContextKey(jobId)).getContextManager();
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName);
        contextManager.reloadDatabaseMetaData(database.getName());
    }
    
    @Override
    public void rollback(final String jobId) throws SQLException {
        final long startTimeMillis = System.currentTimeMillis();
        dropCheckJobs(jobId);
        cleanTempTableOnRollback(jobId);
        new PipelineJobManager(TypedSPILoader.getService(PipelineJobOption.class, getType())).drop(jobId);
        log.info("Rollback job {} cost {} ms", jobId, System.currentTimeMillis() - startTimeMillis);
    }
    
    private void dropCheckJobs(final String jobId) {
        Collection<String> checkJobIds = PipelineAPIFactory.getPipelineGovernanceFacade(PipelineJobIdUtils.parseContextKey(jobId)).getJobFacade().getCheck().listCheckJobIds(jobId);
        if (checkJobIds.isEmpty()) {
            return;
        }
        for (String each : checkJobIds) {
            try {
                new PipelineJobManager(TypedSPILoader.getService(PipelineJobOption.class, getType())).drop(each);
                // CHECKSTYLE:OFF
            } catch (final RuntimeException ex) {
                // CHECKSTYLE:ON
                log.info("drop check job failed, check job id: {}, error: {}", each, ex.getMessage());
            }
        }
    }
    
    private void cleanTempTableOnRollback(final String jobId) throws SQLException {
        MigrationJobConfiguration jobConfig = new PipelineJobConfigurationManager(TypedSPILoader.getService(PipelineJobOption.class, getType())).getJobConfiguration(jobId);
        PipelineCommonSQLBuilder pipelineSQLBuilder = new PipelineCommonSQLBuilder(jobConfig.getTargetDatabaseType());
        TableAndSchemaNameMapper mapping = new TableAndSchemaNameMapper(jobConfig.getTargetTableSchemaMap());
        try (
                PipelineDataSourceWrapper dataSource = PipelineDataSourceFactory.newInstance(jobConfig.getTarget());
                Connection connection = dataSource.getConnection()) {
            for (String each : jobConfig.getTargetTableNames()) {
                String targetSchemaName = mapping.getSchemaName(each);
                String sql = pipelineSQLBuilder.buildDropSQL(targetSchemaName, each);
                log.info("cleanTempTableOnRollback, targetSchemaName={}, targetTableName={}, sql={}", targetSchemaName, each, sql);
                try (Statement statement = connection.createStatement()) {
                    statement.execute(sql);
                }
            }
        }
    }
    
    @Override
    public String getType() {
        return "MIGRATION";
    }
}
