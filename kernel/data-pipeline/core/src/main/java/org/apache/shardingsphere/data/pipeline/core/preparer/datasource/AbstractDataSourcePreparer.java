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

package org.apache.shardingsphere.data.pipeline.core.preparer.datasource;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.preparer.CreateTableConfiguration;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.metadata.generator.PipelineDDLGenerator;
import org.apache.shardingsphere.data.pipeline.core.preparer.datasource.param.PrepareTargetSchemasParameter;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.PipelineCommonSQLBuilder;
import org.apache.shardingsphere.infra.database.core.metadata.database.DialectDatabaseMetaData;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.parser.SQLParserEngine;

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
    
    @Override
    public void prepareTargetSchemas(final PrepareTargetSchemasParameter param) throws SQLException {
        DatabaseType targetDatabaseType = param.getTargetDatabaseType();
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(targetDatabaseType).getDialectDatabaseMetaData();
        if (!dialectDatabaseMetaData.isSchemaAvailable()) {
            return;
        }
        String defaultSchema = dialectDatabaseMetaData.getDefaultSchema().orElse(null);
        PipelineCommonSQLBuilder pipelineSQLBuilder = new PipelineCommonSQLBuilder(targetDatabaseType);
        Collection<String> createdSchemaNames = new HashSet<>();
        for (CreateTableConfiguration each : param.getCreateTableConfigurations()) {
            String targetSchemaName = each.getTargetName().getSchemaName().toString();
            if (null == targetSchemaName || targetSchemaName.equalsIgnoreCase(defaultSchema) || createdSchemaNames.contains(targetSchemaName)) {
                continue;
            }
            Optional<String> sql = pipelineSQLBuilder.buildCreateSchemaSQL(targetSchemaName);
            if (sql.isPresent()) {
                executeCreateSchema(param.getDataSourceManager(), each.getTargetDataSourceConfig(), sql.get());
                createdSchemaNames.add(targetSchemaName);
            }
        }
    }
    
    private void executeCreateSchema(final PipelineDataSourceManager dataSourceManager, final PipelineDataSourceConfiguration targetDataSourceConfig, final String sql) throws SQLException {
        log.info("Prepare target schemas SQL: {}", sql);
        try (
                Connection connection = dataSourceManager.getDataSource(targetDataSourceConfig).getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
    
    protected void executeTargetTableSQL(final Connection targetConnection, final String sql) throws SQLException {
        log.info("Execute target table SQL: {}", sql);
        try (Statement statement = targetConnection.createStatement()) {
            statement.execute(sql);
        }
    }
    
    protected final String addIfNotExistsForCreateTableSQL(final String createTableSQL) {
        if (PATTERN_CREATE_TABLE_IF_NOT_EXISTS.matcher(createTableSQL).find()) {
            return createTableSQL;
        }
        return PATTERN_CREATE_TABLE.matcher(createTableSQL).replaceFirst("CREATE TABLE IF NOT EXISTS ");
    }
    
    protected final String getCreateTargetTableSQL(final CreateTableConfiguration createTableConfig, final PipelineDataSourceManager dataSourceManager,
                                                   final SQLParserEngine sqlParserEngine) throws SQLException {
        DatabaseType databaseType = createTableConfig.getSourceDataSourceConfig().getDatabaseType();
        DataSource sourceDataSource = dataSourceManager.getDataSource(createTableConfig.getSourceDataSourceConfig());
        String schemaName = createTableConfig.getSourceName().getSchemaName().toString();
        String sourceTableName = createTableConfig.getSourceName().getTableName().toString();
        String targetTableName = createTableConfig.getTargetName().getTableName().toString();
        PipelineDDLGenerator generator = new PipelineDDLGenerator();
        return generator.generateLogicDDL(databaseType, sourceDataSource, schemaName, sourceTableName, targetTableName, sqlParserEngine);
    }
}
