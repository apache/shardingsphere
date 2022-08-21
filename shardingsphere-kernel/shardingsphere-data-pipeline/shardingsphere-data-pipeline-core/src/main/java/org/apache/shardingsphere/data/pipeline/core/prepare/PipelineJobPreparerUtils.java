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

package org.apache.shardingsphere.data.pipeline.core.prepare;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobItemIncrementalTasksProgress;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.PositionInitializerFactory;
import org.apache.shardingsphere.data.pipeline.core.prepare.datasource.DataSourcePreparer;
import org.apache.shardingsphere.data.pipeline.core.prepare.datasource.DataSourcePreparerFactory;
import org.apache.shardingsphere.data.pipeline.core.prepare.datasource.PrepareTargetSchemasParameter;
import org.apache.shardingsphere.data.pipeline.core.prepare.datasource.PrepareTargetTablesParameter;
import org.apache.shardingsphere.data.pipeline.spi.check.datasource.DataSourceChecker;
import org.apache.shardingsphere.data.pipeline.spi.check.datasource.DataSourceCheckerFactory;
import org.apache.shardingsphere.data.pipeline.spi.ingest.position.PositionInitializer;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

/**
 * Pipeline job preparer utils.
 */
@Slf4j
public final class PipelineJobPreparerUtils {
    
    /**
     * Prepare target schema.
     *
     * @param databaseType database type
     * @param prepareTargetSchemasParameter prepare target schemas parameter
     */
    public static void prepareTargetSchema(final String databaseType, final PrepareTargetSchemasParameter prepareTargetSchemasParameter) {
        Optional<DataSourcePreparer> dataSourcePreparer = DataSourcePreparerFactory.getInstance(databaseType);
        if (!dataSourcePreparer.isPresent()) {
            log.info("dataSourcePreparer null, ignore prepare target");
            return;
        }
        dataSourcePreparer.get().prepareTargetSchemas(prepareTargetSchemasParameter);
    }
    
    /**
     * Prepare target tables.
     *
     * @param databaseType database type
     * @param prepareTargetTablesParameter prepare target tables parameter
     * @throws SQLException SQL exception
     */
    public static void prepareTargetTables(final String databaseType, final PrepareTargetTablesParameter prepareTargetTablesParameter) throws SQLException {
        Optional<DataSourcePreparer> dataSourcePreparer = DataSourcePreparerFactory.getInstance(databaseType);
        if (!dataSourcePreparer.isPresent()) {
            log.info("dataSourcePreparer null, ignore prepare target");
            return;
        }
        dataSourcePreparer.get().prepareTargetTables(prepareTargetTablesParameter);
    }
    
    /**
     * Get incremental position.
     *
     * @param initIncremental init incremental
     * @param dumperConfig dumper config
     * @param dataSourceManager data source manager
     * @return ingest position
     * @throws SQLException sql exception
     */
    public static IngestPosition<?> getIncrementalPosition(final JobItemIncrementalTasksProgress initIncremental, final DumperConfiguration dumperConfig,
                                                           final PipelineDataSourceManager dataSourceManager) throws SQLException {
        if (null != initIncremental) {
            Optional<IngestPosition<?>> position = initIncremental.getIncrementalPosition(dumperConfig.getDataSourceName());
            if (position.isPresent()) {
                return position.get();
            }
        }
        String databaseType = dumperConfig.getDataSourceConfig().getDatabaseType().getType();
        DataSource dataSource = dataSourceManager.getDataSource(dumperConfig.getDataSourceConfig());
        return PositionInitializerFactory.getInstance(databaseType).init(dataSource);
    }
    
    /**
     * Check data source.
     *
     * @param databaseType database type
     * @param dataSources data source
     */
    public static void checkSourceDataSource(final String databaseType, final Collection<? extends DataSource> dataSources) {
        if (null == dataSources || dataSources.isEmpty()) {
            log.info("source data source is empty, skip check");
            return;
        }
        DataSourceChecker dataSourceChecker = DataSourceCheckerFactory.getInstance(databaseType);
        dataSourceChecker.checkConnection(dataSources);
        dataSourceChecker.checkPrivilege(dataSources);
        dataSourceChecker.checkVariable(dataSources);
    }
    
    /**
     * Check target data source.
     *
     * @param databaseType database type
     * @param importerConfig importer config
     * @param targetDataSources target data sources
     */
    public static void checkTargetDataSource(final String databaseType, final ImporterConfiguration importerConfig, final Collection<? extends DataSource> targetDataSources) {
        DataSourceChecker dataSourceChecker = DataSourceCheckerFactory.getInstance(databaseType);
        if (null == targetDataSources || targetDataSources.isEmpty()) {
            log.info("target data source is empty, skip check");
            return;
        }
        dataSourceChecker.checkConnection(targetDataSources);
        dataSourceChecker.checkTargetTable(targetDataSources, importerConfig.getTableNameSchemaNameMapping(), importerConfig.getLogicTableNames());
    }
    
    /**
     * Cleanup job preparer.
     *
     * @param pipelineDataSourceConfig pipeline data source config
     * @throws SQLException sql exception
     */
    public static void destroyPosition(final PipelineDataSourceConfiguration pipelineDataSourceConfig) throws SQLException {
        DatabaseType databaseType = pipelineDataSourceConfig.getDatabaseType();
        PositionInitializer positionInitializer = PositionInitializerFactory.getInstance(databaseType.getType());
        log.info("Cleanup database type:{}, data source type:{}", databaseType.getType(), pipelineDataSourceConfig.getType());
        if (pipelineDataSourceConfig instanceof ShardingSpherePipelineDataSourceConfiguration) {
            ShardingSpherePipelineDataSourceConfiguration dataSourceConfig = (ShardingSpherePipelineDataSourceConfiguration) pipelineDataSourceConfig;
            for (DataSourceProperties each : new YamlDataSourceConfigurationSwapper().getDataSourcePropertiesMap(dataSourceConfig.getRootConfig()).values()) {
                try (PipelineDataSourceWrapper dataSource = new PipelineDataSourceWrapper(DataSourcePoolCreator.create(each), databaseType)) {
                    positionInitializer.destroy(dataSource);
                }
            }
        }
        if (pipelineDataSourceConfig instanceof StandardPipelineDataSourceConfiguration) {
            StandardPipelineDataSourceConfiguration dataSourceConfig = (StandardPipelineDataSourceConfiguration) pipelineDataSourceConfig;
            try (
                    PipelineDataSourceWrapper dataSource = new PipelineDataSourceWrapper(
                            DataSourcePoolCreator.create((DataSourceProperties) dataSourceConfig.getDataSourceConfiguration()), databaseType)) {
                positionInitializer.destroy(dataSource);
            }
        }
    }
}
