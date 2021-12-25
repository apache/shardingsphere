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
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.PipelineConfiguration;
import org.apache.shardingsphere.data.pipeline.api.prepare.datasource.ActualTableDefinition;
import org.apache.shardingsphere.data.pipeline.api.prepare.datasource.TableDefinitionSQLType;
import org.apache.shardingsphere.data.pipeline.core.datasource.DataSourceFactory;
import org.apache.shardingsphere.data.pipeline.core.datasource.DataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.spi.rulealtered.DataSourcePreparer;
import org.apache.shardingsphere.data.pipeline.core.datasource.config.JDBCDataSourceConfigurationFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Abstract data source preparer.
 */
@Slf4j
public abstract class AbstractDataSourcePreparer implements DataSourcePreparer {
    
    private static final Pattern PATTERN_CREATE_TABLE_IF_NOT_EXISTS = Pattern.compile("CREATE\\s+TABLE\\s+IF\\s+NOT\\s+EXISTS\\s+", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern PATTERN_CREATE_TABLE = Pattern.compile("CREATE\\s+TABLE\\s+", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern PATTERN_ALTER_TABLE = Pattern.compile("ALTER\\s+TABLE\\s+", Pattern.CASE_INSENSITIVE);
    
    private static final String[] IGNORE_EXCEPTION_MESSAGE = {"multiple primary keys for table", "already exists"};
    
    private final DataSourceFactory dataSourceFactory = new DataSourceFactory();
    
    protected final DataSourceWrapper getSourceDataSource(final PipelineConfiguration pipelineConfig) {
        return dataSourceFactory.newInstance(JDBCDataSourceConfigurationFactory.newInstance(pipelineConfig.getSource().getType(), pipelineConfig.getSource().getParameter()));
    }
    
    protected final DataSourceWrapper getTargetDataSource(final PipelineConfiguration pipelineConfig) {
        return dataSourceFactory.newInstance(JDBCDataSourceConfigurationFactory.newInstance(pipelineConfig.getTarget().getType(), pipelineConfig.getTarget().getParameter()));
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
    
    protected final Collection<String> splitTableDefinitionToSQLs(final ActualTableDefinition actualTableDefinition) {
        return Arrays.stream(actualTableDefinition.getTableDefinition().split(";")).collect(Collectors.toList());
    }
    
    //TODO simple lexer
    protected final TableDefinitionSQLType getTableDefinitionSQLType(final String sql) {
        if (PATTERN_CREATE_TABLE.matcher(sql).find()) {
            return TableDefinitionSQLType.CREATE_TABLE;
        }
        if (PATTERN_ALTER_TABLE.matcher(sql).find()) {
            return TableDefinitionSQLType.ALTER_TABLE;
        }
        return TableDefinitionSQLType.UNKNOWN;
    }
    
    protected final String addIfNotExistsForCreateTableSQL(final String createTableSQL) {
        if (PATTERN_CREATE_TABLE_IF_NOT_EXISTS.matcher(createTableSQL).find()) {
            return createTableSQL;
        }
        return PATTERN_CREATE_TABLE.matcher(createTableSQL).replaceFirst("CREATE TABLE IF NOT EXISTS ");
    }
    
    protected String replaceActualTableNameToLogicTableName(final String createOrAlterTableSQL, final String actualTableName, final String logicTableName) {
        int start = createOrAlterTableSQL.indexOf(actualTableName);
        if (start <= 0) {
            return createOrAlterTableSQL;
        }
        int end = start + actualTableName.length();
        return new StringBuilder(createOrAlterTableSQL).replace(start, end, logicTableName).toString();
    }
}
