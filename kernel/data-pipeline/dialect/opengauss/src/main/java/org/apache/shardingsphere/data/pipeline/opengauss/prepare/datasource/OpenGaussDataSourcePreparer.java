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

package org.apache.shardingsphere.data.pipeline.opengauss.prepare.datasource;

import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.shaded.com.google.common.base.Strings;
import org.apache.shardingsphere.data.pipeline.common.config.CreateTableConfiguration.CreateTableEntry;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.preparer.datasource.AbstractDataSourcePreparer;
import org.apache.shardingsphere.data.pipeline.core.preparer.datasource.PrepareTargetSchemasParameter;
import org.apache.shardingsphere.data.pipeline.core.preparer.datasource.PrepareTargetTablesParameter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.stream.Collectors;

/**
 * Data source preparer for openGauss.
 */
@Slf4j
public final class OpenGaussDataSourcePreparer extends AbstractDataSourcePreparer {
    
    private static final String[] IGNORE_EXCEPTION_MESSAGE = {"multiple primary keys for table", "already exists"};
    
    @Override
    public void prepareTargetSchemas(final PrepareTargetSchemasParameter param) {
        try {
            super.prepareTargetSchemas(param);
        } catch (final SQLException ex) {
            // openGauss CREATE SCHEMA doesn't support IF NOT EXISTS
            // TODO Use actual data source to create schema, check whether schema exists or not
            log.warn("create schema failed", ex);
        }
    }
    
    @Override
    public void prepareTargetTables(final PrepareTargetTablesParameter param) throws SQLException {
        PipelineDataSourceManager dataSourceManager = param.getDataSourceManager();
        for (CreateTableEntry each : param.getCreateTableConfig().getCreateTableEntries()) {
            String createTargetTableSQL = getCreateTargetTableSQL(each, dataSourceManager, param.getSqlParserEngine());
            try (Connection targetConnection = getCachedDataSource(dataSourceManager, each.getTargetDataSourceConfig()).getConnection()) {
                for (String sql : Splitter.on(";").trimResults().splitToList(createTargetTableSQL).stream().filter(cs -> !Strings.isNullOrEmpty(cs)).collect(Collectors.toList())) {
                    executeTargetTableSQL(targetConnection, addIfNotExistsForCreateTableSQL(sql));
                }
            }
        }
    }
    
    @Override
    protected void executeTargetTableSQL(final Connection targetConnection, final String sql) throws SQLException {
        try {
            super.executeTargetTableSQL(targetConnection, sql);
        } catch (final SQLException ex) {
            for (String ignoreMessage : IGNORE_EXCEPTION_MESSAGE) {
                if (ex.getMessage().contains(ignoreMessage)) {
                    return;
                }
            }
            throw ex;
        }
    }
    
    @Override
    public String getDatabaseType() {
        return "openGauss";
    }
}
