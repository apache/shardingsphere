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

package org.apache.shardingsphere.data.pipeline.core.preparer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.type.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.type.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.checker.DataSourceCheckEngine;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.core.importer.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.context.IncrementalDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.job.progress.JobItemIncrementalTasksProgress;
import org.apache.shardingsphere.data.pipeline.core.preparer.datasource.PipelineJobDataSourcePreparer;
import org.apache.shardingsphere.data.pipeline.core.preparer.datasource.option.DialectPipelineJobDataSourcePrepareOption;
import org.apache.shardingsphere.data.pipeline.core.preparer.datasource.param.PrepareTargetSchemasParameter;
import org.apache.shardingsphere.data.pipeline.core.preparer.datasource.param.PrepareTargetTablesParameter;
import org.apache.shardingsphere.data.pipeline.core.spi.ingest.dumper.IncrementalDumperCreator;
import org.apache.shardingsphere.data.pipeline.core.spi.ingest.position.PositionInitializer;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.parser.SQLParserEngine;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.parser.rule.SQLParserRule;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

/**
 * Pipeline job preparer.
 */
@RequiredArgsConstructor
@Slf4j
public final class PipelineJobPreparer {
    
    private final DatabaseType databaseType;
    
    /**
     * Is incremental supported.
     *
     * @return support incremental or not
     */
    public boolean isIncrementalSupported() {
        return DatabaseTypedSPILoader.findService(IncrementalDumperCreator.class, databaseType).map(IncrementalDumperCreator::isSupportIncrementalDump).orElse(false);
    }
    
    /**
     * Prepare target schema.
     *
     * @param param prepare target schemas parameter
     * @throws SQLException if prepare target schema fail
     */
    public void prepareTargetSchema(final PrepareTargetSchemasParameter param) throws SQLException {
        new PipelineJobDataSourcePreparer(DatabaseTypedSPILoader.getService(DialectPipelineJobDataSourcePrepareOption.class, databaseType)).prepareTargetSchemas(param);
    }
    
    /**
     * Get SQL parser engine.
     *
     * @param metaData meta data
     * @return SQL parser engine
     */
    public SQLParserEngine getSQLParserEngine(final ShardingSphereMetaData metaData) {
        return metaData.getGlobalRuleMetaData().getSingleRule(SQLParserRule.class).getSQLParserEngine(databaseType);
    }
    
    /**
     * Prepare target tables.
     *
     * @param prepareTargetTablesParam prepare target tables parameter
     * @throws SQLException SQL exception
     */
    public void prepareTargetTables(final PrepareTargetTablesParameter prepareTargetTablesParam) throws SQLException {
        long startTimeMillis = System.currentTimeMillis();
        new PipelineJobDataSourcePreparer(DatabaseTypedSPILoader.getService(DialectPipelineJobDataSourcePrepareOption.class, databaseType)).prepareTargetTables(prepareTargetTablesParam);
        log.info("prepareTargetTables cost {} ms", System.currentTimeMillis() - startTimeMillis);
    }
    
    /**
     * Get incremental position.
     *
     * @param initIncremental init incremental
     * @param dumperContext dumper config
     * @param dataSourceManager data source manager
     * @return ingest position
     * @throws SQLException sql exception
     */
    public IngestPosition getIncrementalPosition(final JobItemIncrementalTasksProgress initIncremental, final IncrementalDumperContext dumperContext,
                                                 final PipelineDataSourceManager dataSourceManager) throws SQLException {
        if (null != initIncremental) {
            Optional<IngestPosition> position = initIncremental.getIncrementalPosition();
            if (position.isPresent()) {
                return position.get();
            }
        }
        DataSource dataSource = dataSourceManager.getDataSource(dumperContext.getCommonContext().getDataSourceConfig());
        return DatabaseTypedSPILoader.getService(PositionInitializer.class, databaseType).init(dataSource, dumperContext.getJobId());
    }
    
    /**
     * Check data source.
     *
     * @param dataSources data source
     */
    public void checkSourceDataSource(final Collection<? extends DataSource> dataSources) {
        if (dataSources.isEmpty()) {
            return;
        }
        DataSourceCheckEngine dataSourceCheckEngine = new DataSourceCheckEngine(databaseType);
        dataSourceCheckEngine.checkConnection(dataSources);
        dataSourceCheckEngine.checkPrivilege(dataSources);
        dataSourceCheckEngine.checkVariable(dataSources);
    }
    
    /**
     * Check target data source.
     *
     * @param importerConfig importer config
     * @param targetDataSources target data sources
     */
    public void checkTargetDataSource(final ImporterConfiguration importerConfig, final Collection<? extends DataSource> targetDataSources) {
        if (null == targetDataSources || targetDataSources.isEmpty()) {
            log.info("target data source is empty, skip check");
            return;
        }
        DataSourceCheckEngine dataSourceCheckEngine = new DataSourceCheckEngine(databaseType);
        dataSourceCheckEngine.checkConnection(targetDataSources);
        dataSourceCheckEngine.checkTargetTable(targetDataSources, importerConfig.getTableAndSchemaNameMapper(), importerConfig.getLogicTableNames());
    }
    
    /**
     * Cleanup job preparer.
     *
     * @param jobId pipeline job id
     * @param pipelineDataSourceConfig pipeline data source config
     * @throws SQLException SQL exception
     */
    public void destroyPosition(final String jobId, final PipelineDataSourceConfiguration pipelineDataSourceConfig) throws SQLException {
        PositionInitializer positionInitializer = DatabaseTypedSPILoader.getService(PositionInitializer.class, databaseType);
        final long startTimeMillis = System.currentTimeMillis();
        log.info("Cleanup database type:{}, data source type:{}", databaseType.getType(), pipelineDataSourceConfig.getType());
        if (pipelineDataSourceConfig instanceof ShardingSpherePipelineDataSourceConfiguration) {
            ShardingSpherePipelineDataSourceConfiguration dataSourceConfig = (ShardingSpherePipelineDataSourceConfiguration) pipelineDataSourceConfig;
            for (DataSourcePoolProperties each : new YamlDataSourceConfigurationSwapper().getDataSourcePoolPropertiesMap(dataSourceConfig.getRootConfig()).values()) {
                try (PipelineDataSourceWrapper dataSource = new PipelineDataSourceWrapper(DataSourcePoolCreator.create(each), databaseType)) {
                    positionInitializer.destroy(dataSource, jobId);
                }
            }
        }
        if (pipelineDataSourceConfig instanceof StandardPipelineDataSourceConfiguration) {
            StandardPipelineDataSourceConfiguration dataSourceConfig = (StandardPipelineDataSourceConfiguration) pipelineDataSourceConfig;
            try (
                    PipelineDataSourceWrapper dataSource = new PipelineDataSourceWrapper(
                            DataSourcePoolCreator.create((DataSourcePoolProperties) dataSourceConfig.getDataSourceConfiguration()), databaseType)) {
                positionInitializer.destroy(dataSource, jobId);
            }
        }
        log.info("destroyPosition cost {} ms", System.currentTimeMillis() - startTimeMillis);
    }
}
