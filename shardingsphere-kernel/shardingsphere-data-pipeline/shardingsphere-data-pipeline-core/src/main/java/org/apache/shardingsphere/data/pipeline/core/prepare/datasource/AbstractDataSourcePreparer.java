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

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.job.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfigurationFactory;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobPrepareFailedException;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.PipelineSQLBuilderFactory;
import org.apache.shardingsphere.data.pipeline.spi.sqlbuilder.PipelineSQLBuilder;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datanode.DataNodes;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;

/**
 * Abstract data source preparer.
 */
@Slf4j
public abstract class AbstractDataSourcePreparer implements DataSourcePreparer {
    
    private static final Pattern PATTERN_CREATE_TABLE_IF_NOT_EXISTS = Pattern.compile("CREATE\\s+TABLE\\s+IF\\s+NOT\\s+EXISTS\\s+", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern PATTERN_CREATE_TABLE = Pattern.compile("CREATE\\s+TABLE\\s+", Pattern.CASE_INSENSITIVE);
    
    private static final String[] IGNORE_EXCEPTION_MESSAGE = {"multiple primary keys for table", "already exists"};
    
    @Override
    public void prepareTargetSchemas(final PrepareTargetSchemasParameter parameter) {
        Set<String> schemaNames = getSchemaNames(parameter);
        String defaultSchema = DatabaseTypeEngine.getDefaultSchemaName(parameter.getTargetDatabaseType(), parameter.getDatabaseName());
        log.info("prepareTargetSchemas, schemaNames={}, defaultSchema={}", schemaNames, defaultSchema);
        PipelineSQLBuilder pipelineSQLBuilder = PipelineSQLBuilderFactory.getInstance(parameter.getTargetDatabaseType().getType());
        try (Connection targetConnection = getTargetCachedDataSource(parameter.getDataSourceConfig(), parameter.getDataSourceManager()).getConnection()) {
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
        for (String each : parameter.getLogicTableNames()) {
            String schemaName = parameter.getTableNameSchemaNameMapping().getSchemaName(each);
            if (null == schemaName) {
                throw new PipelineJobPrepareFailedException("Can not get schemaName by logic table name " + each);
            }
            result.add(schemaName);
        }
        return result;
    }
    
    // TODO the invocation is disabled for now, it might be used again for next new feature
    protected final PipelineDataSourceWrapper getSourceCachedDataSource(final MigrationJobConfiguration jobConfig, final PipelineDataSourceManager dataSourceManager) {
        return dataSourceManager.getDataSource(PipelineDataSourceConfigurationFactory.newInstance(jobConfig.getSource().getType(), jobConfig.getSource().getParameter()));
    }
    
    protected final PipelineDataSourceWrapper getTargetCachedDataSource(final PipelineDataSourceConfiguration dataSourceConfig, final PipelineDataSourceManager dataSourceManager) {
        return dataSourceManager.getDataSource(dataSourceConfig);
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
    
    protected final String addIfNotExistsForCreateTableSQL(final String createTableSQL) {
        if (PATTERN_CREATE_TABLE_IF_NOT_EXISTS.matcher(createTableSQL).find()) {
            return createTableSQL;
        }
        return PATTERN_CREATE_TABLE.matcher(createTableSQL).replaceFirst("CREATE TABLE IF NOT EXISTS ");
    }
    
    protected String getActualTable(final ShardingSphereDatabase database, final String tableName) {
        DataNodes dataNodes = new DataNodes(database.getRuleMetaData().getRules());
        Optional<DataNode> filteredDataNode = dataNodes.getDataNodes(tableName).stream()
                .filter(each -> database.getResource().getDataSources().containsKey(each.getDataSourceName().contains(".") ? each.getDataSourceName().split("\\.")[0] : each.getDataSourceName()))
                .findFirst();
        return filteredDataNode.map(DataNode::getTableName).orElse(tableName);
    }
}
