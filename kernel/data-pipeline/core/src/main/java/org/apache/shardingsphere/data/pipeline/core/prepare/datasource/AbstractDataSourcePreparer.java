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

package org.apache.shardingsphere.data.pipeline.core.prepare.datasource;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.CreateTableConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.CreateTableConfiguration.CreateTableEntry;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.metadata.generator.PipelineDDLGenerator;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.PipelineSQLBuilderFactory;
import org.apache.shardingsphere.data.pipeline.spi.sqlbuilder.PipelineSQLBuilder;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Abstract data source preparer.
 */
@Slf4j
public abstract class AbstractDataSourcePreparer implements DataSourcePreparer {
    
    private static final Pattern PATTERN_CREATE_TABLE_IF_NOT_EXISTS = Pattern.compile("CREATE\\s+TABLE\\s+IF\\s+NOT\\s+EXISTS\\s+", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern PATTERN_CREATE_TABLE = Pattern.compile("CREATE\\s+TABLE\\s+", Pattern.CASE_INSENSITIVE);
    
    // TODO it's just used for openGauss
    private static final String[] IGNORE_EXCEPTION_MESSAGE = {"multiple primary keys for table", "already exists"};
    
    @Override
    public void prepareTargetSchemas(final PrepareTargetSchemasParameter parameter) {
        DatabaseType targetDatabaseType = parameter.getTargetDatabaseType();
        if (!targetDatabaseType.isSchemaAvailable()) {
            log.info("prepareTargetSchemas, target database does not support schema, ignore, targetDatabaseType={}", targetDatabaseType);
            return;
        }
        CreateTableConfiguration createTableConfig = parameter.getCreateTableConfig();
        String defaultSchema = DatabaseTypeEngine.getDefaultSchemaName(targetDatabaseType).orElse(null);
        PipelineSQLBuilder sqlBuilder = PipelineSQLBuilderFactory.getInstance(targetDatabaseType.getType());
        Collection<String> createdSchemaNames = new HashSet<>();
        for (CreateTableEntry each : createTableConfig.getCreateTableEntries()) {
            String targetSchemaName = each.getTargetName().getSchemaName().getOriginal();
            if (null == targetSchemaName || targetSchemaName.equalsIgnoreCase(defaultSchema) || createdSchemaNames.contains(targetSchemaName)) {
                continue;
            }
            Optional<String> sql = sqlBuilder.buildCreateSchemaSQL(targetSchemaName);
            if (sql.isPresent()) {
                executeCreateSchema(parameter.getDataSourceManager(), each.getTargetDataSourceConfig(), sql.get());
                createdSchemaNames.add(targetSchemaName);
            }
        }
        log.info("prepareTargetSchemas, createdSchemaNames={}, defaultSchema={}", createdSchemaNames, defaultSchema);
    }
    
    private void executeCreateSchema(final PipelineDataSourceManager dataSourceManager, final PipelineDataSourceConfiguration targetDataSourceConfig, final String sql) {
        log.info("prepareTargetSchemas, sql={}", sql);
        try (Connection connection = getCachedDataSource(dataSourceManager, targetDataSourceConfig).getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(sql);
            }
        } catch (final SQLException ex) {
            log.warn("create schema failed, error: {}", ex.getMessage());
        }
    }
    
    protected final PipelineDataSourceWrapper getCachedDataSource(final PipelineDataSourceManager dataSourceManager, final PipelineDataSourceConfiguration dataSourceConfig) {
        return dataSourceManager.getDataSource(dataSourceConfig);
    }
    
    protected final void executeTargetTableSQL(final Connection targetConnection, final String sql) throws SQLException {
        log.info("execute target table sql: {}", sql);
        try (Statement statement = targetConnection.createStatement()) {
            statement.execute(sql);
        } catch (final SQLException ex) {
            log.warn("execute target table sql failed", ex);
            for (String ignoreMessage : IGNORE_EXCEPTION_MESSAGE) {
                if (ex.getMessage().contains(ignoreMessage)) {
                    return;
                }
            }
            throw ex;
        }
    }
    
    protected final String addIfNotExistsForCreateTableSQL(final String createTableSQL) {
        if (PATTERN_CREATE_TABLE_IF_NOT_EXISTS.matcher(createTableSQL).find()) {
            return createTableSQL;
        }
        return PATTERN_CREATE_TABLE.matcher(createTableSQL).replaceFirst("CREATE TABLE IF NOT EXISTS ");
    }
    
    protected final String getCreateTargetTableSQL(final CreateTableEntry createTableEntry, final PipelineDataSourceManager dataSourceManager,
                                                   final ShardingSphereSQLParserEngine sqlParserEngine) throws SQLException {
        DatabaseType databaseType = createTableEntry.getSourceDataSourceConfig().getDatabaseType();
        DataSource sourceDataSource = dataSourceManager.getDataSource(createTableEntry.getSourceDataSourceConfig());
        String schemaName = createTableEntry.getSourceName().getSchemaName().getOriginal();
        String sourceTableName = createTableEntry.getSourceName().getTableName().getOriginal();
        String targetTableName = createTableEntry.getTargetName().getTableName().getOriginal();
        PipelineDDLGenerator generator = new PipelineDDLGenerator();
        return generator.generateLogicDDL(databaseType, sourceDataSource, schemaName, sourceTableName, targetTableName, sqlParserEngine);
    }
}
