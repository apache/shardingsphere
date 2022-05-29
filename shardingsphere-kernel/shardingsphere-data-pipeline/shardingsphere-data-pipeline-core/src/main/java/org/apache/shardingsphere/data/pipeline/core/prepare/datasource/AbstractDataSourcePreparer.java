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

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.RuleAlteredJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.TaskConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfigurationFactory;
import org.apache.shardingsphere.data.pipeline.api.prepare.datasource.TableDefinitionSQLType;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobPrepareFailedException;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.PipelineSQLBuilderFactory;
import org.apache.shardingsphere.data.pipeline.spi.sqlbuilder.PipelineSQLBuilder;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Abstract data source preparer.
 */
@Slf4j
public abstract class AbstractDataSourcePreparer implements DataSourcePreparer {
    
    private static final Pattern PATTERN_CREATE_TABLE_IF_NOT_EXISTS = Pattern.compile("CREATE\\s+TABLE\\s+IF\\s+NOT\\s+EXISTS\\s+", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern PATTERN_CREATE_TABLE = Pattern.compile("CREATE\\s+TABLE\\s+", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern PATTERN_ALTER_TABLE = Pattern.compile("ALTER\\s+TABLE\\s+", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern PATTERN_CREATE_INDEX = Pattern.compile("CREATE\\s+(UNIQUE\\s+)?INDEX+\\s", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern PATTERN_DROP_INDEX = Pattern.compile("DROP\\s+INDEX+\\s", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern PATTERN_COMMENT_ON = Pattern.compile("COMMENT\\s+ON\\s+(COLUMN\\s+|TABLE\\s+)", Pattern.CASE_INSENSITIVE);
    
    private static final String[] IGNORE_EXCEPTION_MESSAGE = {"multiple primary keys for table", "already exists"};
    
    @Override
    public void prepareTargetSchemas(final PrepareTargetSchemasParameter parameter) {
        DatabaseType sourceDatabaseType = DatabaseTypeFactory.getInstance(parameter.getTaskConfig().getJobConfig().getSourceDatabaseType());
        DatabaseType targetDatabaseType = DatabaseTypeFactory.getInstance(parameter.getTaskConfig().getJobConfig().getTargetDatabaseType());
        if (!sourceDatabaseType.isSchemaAvailable() || !targetDatabaseType.isSchemaAvailable()) {
            log.info("prepareTargetSchemas, one of source or target database type schema is not available, ignore");
            return;
        }
        Set<String> schemaNames = getSchemaNames(parameter);
        String defaultSchema = DatabaseTypeEngine.getDefaultSchemaName(targetDatabaseType, parameter.getTaskConfig().getJobConfig().getDatabaseName());
        log.info("prepareTargetSchemas, schemaNames={}, defaultSchema={}", schemaNames, defaultSchema);
        PipelineSQLBuilder pipelineSQLBuilder = PipelineSQLBuilderFactory.getInstance(targetDatabaseType.getType());
        try (Connection targetConnection = getTargetCachedDataSource(parameter.getTaskConfig(), parameter.getDataSourceManager()).getConnection()) {
            for (String each : schemaNames) {
                if (each.equalsIgnoreCase(defaultSchema)) {
                    continue;
                }
                String sql = pipelineSQLBuilder.buildCreateSchemaSQL(each);
                log.info("prepareTargetSchemas, sql={}", sql);
                try (Statement statement = targetConnection.createStatement()) {
                    statement.execute(sql);
                } catch (final SQLException ignored) {
                }
            }
        } catch (final SQLException ex) {
            throw new PipelineJobPrepareFailedException("Can not get connection.", ex);
        }
    }
    
    private Set<String> getSchemaNames(final PrepareTargetSchemasParameter parameter) {
        Set<String> result = new HashSet<>();
        for (String each : parameter.getTaskConfig().getJobConfig().splitLogicTableNames()) {
            String schemaName = parameter.getTableNameSchemaNameMapping().getSchemaName(each);
            if (null == schemaName) {
                throw new PipelineJobPrepareFailedException("Can not get schemaName by logic table name " + each);
            }
            result.add(schemaName);
        }
        return result;
    }
    
    // TODO the invocation is disabled for now, it might be used again for next new feature
    protected final PipelineDataSourceWrapper getSourceCachedDataSource(final RuleAlteredJobConfiguration jobConfig, final PipelineDataSourceManager dataSourceManager) {
        return dataSourceManager.getDataSource(PipelineDataSourceConfigurationFactory.newInstance(jobConfig.getSource().getType(), jobConfig.getSource().getParameter()));
    }
    
    protected final PipelineDataSourceWrapper getTargetCachedDataSource(final TaskConfiguration taskConfig, final PipelineDataSourceManager dataSourceManager) {
        return dataSourceManager.getDataSource(taskConfig.getImporterConfig().getDataSourceConfig());
    }
    
    protected final void executeTargetTableSQL(final Connection targetConnection, final String sql) throws SQLException {
        log.info("execute target table sql: {}", sql);
        try (Statement statement = targetConnection.createStatement()) {
            statement.execute(sql);
        } catch (final SQLException ex) {
            for (String ignoreMessage : IGNORE_EXCEPTION_MESSAGE) {
                if (ex.getMessage().contains(ignoreMessage)) {
                    return;
                }
            }
            throw ex;
        }
    }
    
    // TODO simple lexer
    protected final TableDefinitionSQLType getTableDefinitionSQLType(final String sql) {
        if (PATTERN_CREATE_TABLE.matcher(sql).find()) {
            return TableDefinitionSQLType.CREATE_TABLE;
        }
        if (PATTERN_ALTER_TABLE.matcher(sql).find()) {
            return TableDefinitionSQLType.ALTER_TABLE;
        }
        if (PATTERN_CREATE_INDEX.matcher(sql).find()) {
            return TableDefinitionSQLType.CREATE_INDEX;
        }
        if (PATTERN_DROP_INDEX.matcher(sql).find()) {
            return TableDefinitionSQLType.DROP_INDEX;
        }
        if (PATTERN_COMMENT_ON.matcher(sql).find()) {
            return TableDefinitionSQLType.COMMENT_ON;
        }
        return TableDefinitionSQLType.UNKNOWN;
    }
    
    protected final String addIfNotExistsForCreateTableSQL(final String createTableSQL) {
        if (PATTERN_CREATE_TABLE_IF_NOT_EXISTS.matcher(createTableSQL).find()) {
            return createTableSQL;
        }
        return PATTERN_CREATE_TABLE.matcher(createTableSQL).replaceFirst("CREATE TABLE IF NOT EXISTS ");
    }
    
    protected String replaceActualTableNameToLogicTableName(final String createOrAlterTableSQL, final @NonNull String actualTableName, final @NonNull String logicTableName) {
        if (actualTableName.equalsIgnoreCase(logicTableName)) {
            return createOrAlterTableSQL;
        }
        StringBuilder logicalTableSQL = new StringBuilder(createOrAlterTableSQL);
        for (int i = 0; i < 10_000; i++) {
            int start = logicalTableSQL.indexOf(actualTableName);
            if (start <= 0) {
                return logicalTableSQL.toString();
            }
            int end = start + actualTableName.length();
            logicalTableSQL.replace(start, end, logicTableName);
        }
        log.error("replaceActualTableNameToLogicTableName, too many times loop, createOrAlterTableSQL={}, actualTableName={}, logicTableName={}",
                createOrAlterTableSQL, actualTableName, logicalTableSQL);
        throw new RuntimeException("Too many times loop");
    }
}
