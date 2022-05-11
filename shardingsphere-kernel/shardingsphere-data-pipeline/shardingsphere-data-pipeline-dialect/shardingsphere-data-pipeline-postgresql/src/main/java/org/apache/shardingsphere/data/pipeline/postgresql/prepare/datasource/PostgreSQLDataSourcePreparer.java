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

package org.apache.shardingsphere.data.pipeline.postgresql.prepare.datasource;

import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.data.pipeline.api.datanode.JobDataNodeEntry;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobPrepareFailedException;
import org.apache.shardingsphere.data.pipeline.core.metadata.generator.PipelineDDLGenerator;
import org.apache.shardingsphere.data.pipeline.core.prepare.datasource.AbstractDataSourcePreparer;
import org.apache.shardingsphere.data.pipeline.core.prepare.datasource.PrepareTargetTablesParameter;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Data source preparer for PostgresSQL.
 */
@Slf4j
public final class PostgreSQLDataSourcePreparer extends AbstractDataSourcePreparer {
    
    private static final DatabaseType DATABASE_TYPE = new PostgreSQLDatabaseType();
    
    @Override
    public void prepareTargetTables(final PrepareTargetTablesParameter parameter) {
        List<String> createLogicalTableSQLs = listCreateLogicalTableSQL(parameter);
        try (Connection targetConnection = getTargetCachedDataSource(parameter.getTaskConfig(), parameter.getDataSourceManager()).getConnection()) {
            for (String createLogicalTableSQL : createLogicalTableSQLs) {
                for (String each : Splitter.on(";").splitToList(createLogicalTableSQL).stream().filter(StringUtils::isNotBlank).collect(Collectors.toList())) {
                    executeTargetTableSQL(targetConnection, each);
                }
            }
        } catch (final SQLException ex) {
            throw new PipelineJobPrepareFailedException("prepare target tables failed.", ex);
        }
    }
    
    private List<String> listCreateLogicalTableSQL(final PrepareTargetTablesParameter parameter) {
        PipelineDDLGenerator generator = new PipelineDDLGenerator(PipelineContext.getContextManager());
        List<String> result = new LinkedList<>();
        for (JobDataNodeEntry each : parameter.getTablesFirstDataNodes().getEntries()) {
            String schemaName = parameter.getTableNameSchemaNameMapping().getSchemaName(each.getLogicTableName());
            result.add(generator.generateLogicDDLSQL(DATABASE_TYPE, parameter.getJobConfig().getDatabaseName(), schemaName, each.getLogicTableName()));
        }
        return result;
    }
}
