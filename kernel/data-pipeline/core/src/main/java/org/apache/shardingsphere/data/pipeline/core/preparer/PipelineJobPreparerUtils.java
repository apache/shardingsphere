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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.common.config.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.common.job.progress.JobItemIncrementalTasksProgress;
import org.apache.shardingsphere.data.pipeline.core.preparer.datasource.DataSourcePreparer;
import org.apache.shardingsphere.data.pipeline.core.preparer.datasource.PrepareTargetSchemasParameter;
import org.apache.shardingsphere.data.pipeline.core.preparer.datasource.PrepareTargetTablesParameter;
import org.apache.shardingsphere.data.pipeline.core.preparer.datasource.checker.BasicDataSourceChecker;
import org.apache.shardingsphere.data.pipeline.spi.check.datasource.DataSourceChecker;
import org.apache.shardingsphere.data.pipeline.spi.ingest.dumper.IncrementalDumperCreator;
import org.apache.shardingsphere.data.pipeline.spi.ingest.position.PositionInitializer;
import org.apache.shardingsphere.data.pipeline.util.spi.PipelineTypedSPILoader;
import org.apache.shardingsphere.infra.database.type.BranchDatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.parser.SQLParserEngine;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.parser.rule.SQLParserRule;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

/**
 * Pipeline job preparer utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class PipelineJobPreparerUtils {
    
    /**
     * Is incremental supported.
     *
     * @param databaseType database type
     * @return true if supported, otherwise false
     */
    public static boolean isIncrementalSupported(final String databaseType) {
        // TODO H2 doesn't support incremental, but H2DatabaseType.getTrunkDatabaseType() is MySQL. Ignore trunk database type for H2 for now.
        if ("H2".equalsIgnoreCase(databaseType)) {
            return TypedSPILoader.findService(IncrementalDumperCreator.class, databaseType).isPresent();
        }
        return PipelineTypedSPILoader.findDatabaseTypedService(IncrementalDumperCreator.class, databaseType).isPresent();
    }
    
    /**
     * Prepare target schema.
     *
     * @param databaseType database type
     * @param prepareTargetSchemasParam prepare target schemas parameter
     * @throws SQLException if prepare target schema fail
     */
    public static void prepareTargetSchema(final String databaseType, final PrepareTargetSchemasParameter prepareTargetSchemasParam) throws SQLException {
        Optional<DataSourcePreparer> dataSourcePreparer = PipelineTypedSPILoader.findDatabaseTypedService(DataSourcePreparer.class, databaseType);
        if (!dataSourcePreparer.isPresent()) {
            log.info("dataSourcePreparer null, ignore prepare target");
            return;
        }
        dataSourcePreparer.get().prepareTargetSchemas(prepareTargetSchemasParam);
    }
    
    /**
     * Get SQL parser engine.
     *
     * @param metaData meta data
     * @param targetDatabaseName target database name
     * @return SQL parser engine
     */
    public static SQLParserEngine getSQLParserEngine(final ShardingSphereMetaData metaData, final String targetDatabaseName) {
        ShardingSphereDatabase database = metaData.getDatabase(targetDatabaseName);
        DatabaseType databaseType = database.getProtocolType();
        if (databaseType instanceof BranchDatabaseType) {
            databaseType = ((BranchDatabaseType) databaseType).getTrunkDatabaseType();
        }
        return metaData.getGlobalRuleMetaData().getSingleRule(SQLParserRule.class).getSQLParserEngine(databaseType.getType());
    }
    
    /**
     * Prepare target tables.
     *
     * @param databaseType database type
     * @param prepareTargetTablesParam prepare target tables parameter
     * @throws SQLException SQL exception
     */
    public static void prepareTargetTables(final String databaseType, final PrepareTargetTablesParameter prepareTargetTablesParam) throws SQLException {
        Optional<DataSourcePreparer> dataSourcePreparer = PipelineTypedSPILoader.findDatabaseTypedService(DataSourcePreparer.class, databaseType);
        if (!dataSourcePreparer.isPresent()) {
            log.info("dataSourcePreparer null, ignore prepare target");
            return;
        }
        long startTimeMillis = System.currentTimeMillis();
        dataSourcePreparer.get().prepareTargetTables(prepareTargetTablesParam);
        log.info("prepareTargetTables cost {} ms", System.currentTimeMillis() - startTimeMillis);
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
    public static IngestPosition getIncrementalPosition(final JobItemIncrementalTasksProgress initIncremental, final DumperConfiguration dumperConfig,
                                                        final PipelineDataSourceManager dataSourceManager) throws SQLException {
        if (null != initIncremental) {
            Optional<IngestPosition> position = initIncremental.getIncrementalPosition();
            if (position.isPresent()) {
                return position.get();
            }
        }
        String databaseType = dumperConfig.getDataSourceConfig().getDatabaseType().getType();
        DataSource dataSource = dataSourceManager.getDataSource(dumperConfig.getDataSourceConfig());
        return PipelineTypedSPILoader.getDatabaseTypedService(PositionInitializer.class, databaseType).init(dataSource, dumperConfig.getJobId());
    }
    
    /**
     * Check data source.
     *
     * @param databaseType database type
     * @param dataSources data source
     */
    public static void checkSourceDataSource(final String databaseType, final Collection<? extends DataSource> dataSources) {
        if (dataSources.isEmpty()) {
            return;
        }
        DataSourceChecker dataSourceChecker = PipelineTypedSPILoader.findDatabaseTypedService(DataSourceChecker.class, databaseType).orElseGet(() -> new BasicDataSourceChecker(databaseType));
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
        if (null == targetDataSources || targetDataSources.isEmpty()) {
            log.info("target data source is empty, skip check");
            return;
        }
        DataSourceChecker dataSourceChecker = PipelineTypedSPILoader.findDatabaseTypedService(DataSourceChecker.class, databaseType).orElseGet(() -> new BasicDataSourceChecker(databaseType));
        dataSourceChecker.checkConnection(targetDataSources);
        dataSourceChecker.checkTargetTable(targetDataSources, importerConfig.getTableNameSchemaNameMapping(), importerConfig.getLogicTableNames());
    }
    
    /**
     * Cleanup job preparer.
     *
     * @param jobId job id
     * @param pipelineDataSourceConfig pipeline data source config
     * @throws SQLException sql exception
     */
    public static void destroyPosition(final String jobId, final PipelineDataSourceConfiguration pipelineDataSourceConfig) throws SQLException {
        DatabaseType databaseType = pipelineDataSourceConfig.getDatabaseType();
        PositionInitializer positionInitializer = PipelineTypedSPILoader.getDatabaseTypedService(PositionInitializer.class, databaseType.getType());
        final long startTimeMillis = System.currentTimeMillis();
        log.info("Cleanup database type:{}, data source type:{}", databaseType.getType(), pipelineDataSourceConfig.getType());
        if (pipelineDataSourceConfig instanceof ShardingSpherePipelineDataSourceConfiguration) {
            ShardingSpherePipelineDataSourceConfiguration dataSourceConfig = (ShardingSpherePipelineDataSourceConfiguration) pipelineDataSourceConfig;
            for (DataSourceProperties each : new YamlDataSourceConfigurationSwapper().getDataSourcePropertiesMap(dataSourceConfig.getRootConfig()).values()) {
                try (PipelineDataSourceWrapper dataSource = new PipelineDataSourceWrapper(DataSourcePoolCreator.create(each), databaseType)) {
                    positionInitializer.destroy(dataSource, jobId);
                }
            }
        }
        if (pipelineDataSourceConfig instanceof StandardPipelineDataSourceConfiguration) {
            StandardPipelineDataSourceConfiguration dataSourceConfig = (StandardPipelineDataSourceConfiguration) pipelineDataSourceConfig;
            try (
                    PipelineDataSourceWrapper dataSource = new PipelineDataSourceWrapper(
                            DataSourcePoolCreator.create((DataSourceProperties) dataSourceConfig.getDataSourceConfiguration()), databaseType)) {
                positionInitializer.destroy(dataSource, jobId);
            }
        }
        log.info("destroyPosition cost {} ms", System.currentTimeMillis() - startTimeMillis);
    }
}
