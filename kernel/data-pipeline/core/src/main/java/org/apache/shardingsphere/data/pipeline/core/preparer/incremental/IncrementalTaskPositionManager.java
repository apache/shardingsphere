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

package org.apache.shardingsphere.data.pipeline.core.preparer.incremental;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.type.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.type.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.IncrementalDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.job.progress.JobItemIncrementalTasksProgress;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.DialectIngestPositionManager;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Incremental task position manager.
 */
@Slf4j
public final class IncrementalTaskPositionManager {
    
    private final DatabaseType databaseType;
    
    private final DialectIngestPositionManager positionInitializer;
    
    public IncrementalTaskPositionManager(final DatabaseType databaseType) {
        this.databaseType = databaseType;
        positionInitializer = DatabaseTypedSPILoader.getService(DialectIngestPositionManager.class, databaseType);
    }
    
    /**
     * Get ingest position.
     *
     * @param initialProgress initial job item incremental tasks progress
     * @param dumperContext incremental dumper context
     * @param dataSourceManager pipeline data source manager
     * @return ingest position
     * @throws SQLException SQL exception
     */
    public IngestPosition getPosition(final JobItemIncrementalTasksProgress initialProgress,
                                      final IncrementalDumperContext dumperContext, final PipelineDataSourceManager dataSourceManager) throws SQLException {
        if (null != initialProgress) {
            Optional<IngestPosition> position = initialProgress.getIncrementalPosition();
            if (position.isPresent()) {
                return position.get();
            }
        }
        return positionInitializer.init(dataSourceManager.getDataSource(dumperContext.getCommonContext().getDataSourceConfig()), dumperContext.getJobId());
    }
    
    /**
     * Destroy ingest position.
     *
     * @param jobId pipeline job id
     * @param pipelineDataSourceConfig pipeline data source configuration
     * @throws SQLException SQL exception
     */
    public void destroyPosition(final String jobId, final PipelineDataSourceConfiguration pipelineDataSourceConfig) throws SQLException {
        final long startTimeMillis = System.currentTimeMillis();
        log.info("Cleanup position, database type: {}, pipeline data source type: {}", databaseType.getType(), pipelineDataSourceConfig.getType());
        if (pipelineDataSourceConfig instanceof ShardingSpherePipelineDataSourceConfiguration) {
            destroyPosition(jobId, (ShardingSpherePipelineDataSourceConfiguration) pipelineDataSourceConfig, positionInitializer);
        } else if (pipelineDataSourceConfig instanceof StandardPipelineDataSourceConfiguration) {
            destroyPosition(jobId, (StandardPipelineDataSourceConfiguration) pipelineDataSourceConfig, positionInitializer);
        }
        log.info("destroyPosition cost {} ms", System.currentTimeMillis() - startTimeMillis);
    }
    
    private void destroyPosition(final String jobId,
                                 final ShardingSpherePipelineDataSourceConfiguration pipelineDataSourceConfig, final DialectIngestPositionManager positionInitializer) throws SQLException {
        for (DataSourcePoolProperties each : new YamlDataSourceConfigurationSwapper().getDataSourcePoolPropertiesMap(pipelineDataSourceConfig.getRootConfig()).values()) {
            try (PipelineDataSourceWrapper dataSource = new PipelineDataSourceWrapper(DataSourcePoolCreator.create(each), databaseType)) {
                positionInitializer.destroy(dataSource, jobId);
            }
        }
    }
    
    private void destroyPosition(final String jobId, final StandardPipelineDataSourceConfiguration pipelineDataSourceConfig,
                                 final DialectIngestPositionManager positionInitializer) throws SQLException {
        try (
                PipelineDataSourceWrapper dataSource = new PipelineDataSourceWrapper(
                        DataSourcePoolCreator.create((DataSourcePoolProperties) pipelineDataSourceConfig.getDataSourceConfiguration()), databaseType)) {
            positionInitializer.destroy(dataSource, jobId);
        }
    }
}
